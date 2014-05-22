package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.classes.player.effects.Fader;
import fm.radiant.android.interfaces.DeckEventListener;
import fm.radiant.android.models.Ad;
import fm.radiant.android.models.Campaign;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.MediaReceiver;
import fm.radiant.android.utils.LibraryUtils;

public class Player {
    private static final String TAG = "Player";

    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_SYNCING = 4;

    private int currentState = STATE_STOPPED;

    private final Context context;
    private final AlarmManager alarmManager;
    private final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    private  ScheduledFuture adsTask;

    private final Deck deckA = new Deck();
    private final Deck deckB = new Deck();
    private final Deck deckC = new Deck();

    private Deck currentDeck;

    private final DeckEventListener changer;
    private TracksIndexer tracksIndexer; private AdsIndexer adsIndexer;

    private Period currentPeriod;
    private List<Period> periods;

    private List<Campaign> campaigns;

    private float currentVolume;

    public Player(final Context context) {
        this.context      = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // deckA.setOnPlayEffect(new Fader(0, 3000));
        // deckA.setOnPauseEffect(new Fader(1, 3000));

        changer = new DeckEventListener() {
            @Override
            public void onReady(Deck deck, MediaPlayer player) {
                deck.start();
            }

            @Override
            public void onFailure(Deck deck, MediaPlayer player) {
                deck.load(context, Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds()), this);
            }

            @Override
            public void onComplete(Deck deck, MediaPlayer player) {

            }
        };

        deckA.setCue(new Cue(-30000) {
            @Override
            public Void call() {
                deckA.pause();

                currentDeck = deckB;

                deckB.load(context, Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds()), changer);

                return null;
            }
        });

        deckB.setCue(new Cue(-30000) {
            @Override
            public Void call() {
                deckB.pause();

                currentDeck = deckA;

                deckA.load(context, Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds()), changer);

                return null;
            }
        });
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

        if (currentDeck.getPlayer().isPlaying()) {
            return;
        }

        if (currentDeck.getAudio() == null || currentPeriod.collectStyleIds().contains(((Track) currentDeck.getAudio()).getStyleId())) {
            currentDeck.load(context, Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds()), changer);
        } else {
            deckA.start();
        }

        adsTask = worker.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (!adsIndexer.getRemotedQueue().isEmpty()) {
                    return;
                }

                final Campaign campaign = Campaign.selectRandom(campaigns);

                // null ---> Ad.selectRandom(campaign.getAds(), campaign.getSelectivity());

                deckC.load(context, null, new DeckEventListener() {
                    @Override
                    public void onReady(Deck deck, MediaPlayer player) {
                        deck.start();

                        if (campaign.isDucked()) {
                            currentVolume = 0.5f;
                        } else {
                            currentVolume = 0.0f;
                        }

                        // fade out decks to current volume
                    }

                    @Override
                    public void onFailure(Deck deck, MediaPlayer player) {

                    }

                    @Override
                    public void onComplete(Deck deck, MediaPlayer player) {
                        currentVolume = 1.0f;

                        // fade in decks to current volume
                    }
                });
            }
        }, 5, 5, TimeUnit.MINUTES);
    }

    public void stop(int state) {
        setState(state);

        adsTask.cancel(true);

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

        if (currentPeriod != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, currentPeriod.getDelay(), MediaReceiver.getBroadcast(context));
        }
    }

    private void sendBroadcast() {

    }
}
/*


        Log.d(TAG, "player");
        Log.d(TAG, "Time now" + new DateTime().toString());
        Log.d(TAG, "Start" + currentPeriod.getInterval().getStart().toString());
        Log.d(TAG, "End" + currentPeriod.getInterval().getEnd().toString());
        Log.d(TAG, Long.toString((currentPeriod.getDelay() -  System.currentTimeMillis()) / 1000 / 60) );

 */