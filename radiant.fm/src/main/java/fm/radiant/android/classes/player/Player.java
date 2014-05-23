package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.interfaces.AudioModel;
import fm.radiant.android.interfaces.DeckEventListener;
import fm.radiant.android.models.Ad;
import fm.radiant.android.models.Campaign;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.MediaReceiver;

public class Player {
    private static final String TAG = "Player";

    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;

    private int currentState = STATE_STOPPED;

    private final Context context;
    private final AlarmManager alarmManager;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture adsTask;

    private final Deck deckA;
    private final Deck deckB;
    private final Deck deckC;

    private final DeckEventListener deckEventListener;
    private Deck currentDeck;

    private TracksIndexer tracksIndexer; private AdsIndexer adsIndexer;

    private Period currentPeriod;
    private List<Period> periods;

    private List<Campaign> campaigns;

    private float currentVolume;

    public Player(final Context context) {
        this.context      = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        this.deckA = new Deck(context);
        this.deckB = new Deck(context);
        this.deckC = new Deck(context);

        deckEventListener = new DeckEventListener() {
            @Override
            public void onReady(Deck deck, MediaPlayer player, AudioModel audio) {
                deck.start();
            }

            @Override
            public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {
                // no implementation necessary...
            }

            @Override
            public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {
                deck.load(Track.selectRandom(tracksIndexer.getRemotedQueue(), currentPeriod.collectStyleIds()), this);
            }

            @Override
            public void onComplete(Deck deck, MediaPlayer player) {
                // no implementation necessary...
            }
        };

        deckA.setCue(new Cue(-30000) {
            @Override
            public Void call() {
                currentDeck = deckB;

                deckA.pause();
                deckB.load(Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds()), deckEventListener);

                return null;
            }
        });

        deckB.setCue(new Cue(-30000) {
            @Override
            public Void call() {
                currentDeck = deckA;

                deckB.pause();
                deckA.load(Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds()), deckEventListener);

                return null;
            }
        });


        this.currentDeck = deckA;
    }

    public void setTracksIndexer(TracksIndexer indexer) {
        this.tracksIndexer = indexer;
    }

    public void setAdsIndexer(AdsIndexer indexer) {
        this.adsIndexer = indexer;
    }

    public void setPeriods(List<Period> periods) {
        this.periods = periods;
    }

    public void play() {
        if (!currentPeriod.isNow()) {
            stop(STATE_WAITING);
            return;
        }

        Log.d(TAG, "now play");

        if (currentDeck.getPlayer().isPlaying()) {
            return;
        }

        if (currentDeck.getAudio() == null || currentPeriod.collectStyleIds().contains(((Track) currentDeck.getAudio()).getStyleId())) {
            Log.d(TAG, "1");
            currentDeck.load(Track.selectRandom(tracksIndexer.getQueue(), currentPeriod.collectStyleIds()), deckEventListener);
        } else {
            Log.d(TAG, "2");
            // deckA.start();
        }

        adsTask = worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!adsIndexer.getRemotedQueue().isEmpty()) {
                    return;
                }

                final Campaign campaign = Campaign.selectRandom(campaigns);

                deckC.load(Ad.selectRandom(campaign.getAds(), campaign.getSelectivity()), new DeckEventListener() {
                    @Override
                    public void onReady(Deck deck, MediaPlayer player, AudioModel audio) {
                        deck.start();

                        if (campaign.isDucked()) {
                            currentVolume = 0.5f;
                        } else {
                            currentVolume = 0.0f;
                        }
                    }

                    @Override
                    public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {

                    }

                    @Override
                    public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {

                    }

                    @Override
                    public void onComplete(Deck deck, MediaPlayer player) {
                        currentVolume = 1.0f;
                    }
                });
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    public void stop(int state) {
        setState(state);

        // adsTask.cancel(true);

        deckA.pause();
        deckB.pause();
        deckC.pause();
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public int getState() {
        return currentState;
    }

    private void setState(int state) {
        this.currentState = state;
        sendBroadcast();
    }

    public void enqueuePeriod() {
        this.currentPeriod = Period.findCurrent(periods);

        if (currentPeriod == null) return;

        alarmManager.set(AlarmManager.RTC_WAKEUP, currentPeriod.getDelay(), MediaReceiver.getBroadcast(context));
    }


    private void sendBroadcast() {

    }

}