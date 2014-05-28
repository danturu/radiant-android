package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.List;

import fm.radiant.android.Radiant;
import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.interfaces.DeckEventListener;
import fm.radiant.android.lib.AudioModel;
import fm.radiant.android.models.Ad;
import fm.radiant.android.models.Campaign;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.MediaReceiver;
import fm.radiant.android.utils.NetworkUtils;

public class Player {
    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_FAILED  = 4;

    private int currentState = STATE_STOPPED;

    private static final String PROPERTY_STATE = "state";

    private static final int VALUE_FADE_DURATION = 2000;

    private final Context context;

    private final AlarmManager periodsTimer;
    private final Handler campaignsTimer;

    private final Deck deckA;
    private final Deck deckB;
    private final Deck deckC;

    private List<Period> periods;
    private List<Campaign> campaigns;

    private TracksIndexer tracksIndexer; private AdsIndexer adsIndexer;

    private final DeckEventListener deckEventListener;

    private Deck currentDeck;
    private Period currentPeriod;
    private float currentVolume;

    public Player(final Context context) {
        this.context = context;

        this.periodsTimer   = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.campaignsTimer = new Handler();

        this.deckA = new Deck(context);
        this.deckB = new Deck(context);
        this.deckC = new Deck(context);

        this.currentDeck   = deckA;
        this.currentVolume = 1.0f;

        deckEventListener = new DeckEventListener() {
            @Override
            public void onReady(final Deck nextDeck, MediaPlayer player, AudioModel audio) {
                if (currentDeck.getPlayer().isPlaying()) {

                }
                mix(nextDeck, currentDeck);
            }

            @Override
            public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {
                // no implementation necessary...
            }

            @Override
            public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {
                deck.load(selectTrackForCurrentPeriod(), this);
            }

            @Override
            public void onComplete(Deck deck, MediaPlayer player) {
                // no implementation necessary...
            }
        };

        deckA.setCue(new Cue(-15000) {
            @Override
            public void run() {
                deckB.load(selectTrackForCurrentPeriod(), deckEventListener);
            }
        });

        deckB.setCue(new Cue(-15000) {
            @Override
            public void run() {
                deckA.load(selectTrackForCurrentPeriod(), deckEventListener);
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

    public void setCampaigns(List<Campaign> periods) {
        this.campaigns = campaigns;
    }

    public void play() {
        if (!currentPeriod.isNow()) {
            stop(STATE_WAITING);
            return;
        }

        // if (!NetworkUtils.isNetworkConnected() && tracksIndexer.getRatio() < 0.5) {
        //     return;
        // }

        //if (currentDeck.getPlayer().isPlaying()) {
        //    return;
        //}
//
        //if (currentDeck.getAudio() == null || currentPeriod.collectStyleIds().contains(((Track) currentDeck.getAudio()).getStyleId())) {
        //    currentDeck.load(selectTrackForCurrentPeriod(), deckEventListener);
        //} else {
        //    currentDeck.start(0.0f, currentVolume, 3000);
        //}
//
        //deckEventListener.onReady(null, null, null);

        advertise();

        setState(STATE_PLAYING);
    }

    public void stop(int state) {
        setState(state);

        campaignsTimer.removeCallbacksAndMessages(null);

        deckA.pause();
        deckB.pause();
        deckC.pause();
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void schedule() {
        if ((this.currentPeriod = Period.findCurrent(periods)) == null) {
            return;
        }

        periodsTimer.set(AlarmManager.RTC_WAKEUP, currentPeriod.getDelay(), MediaReceiver.getBroadcast(context));
    }

    public int getState() {
        return currentState;
    }

    private void mix(Deck... decks) {
        decks[0].start();

        decks[0].fade(0.0f, currentVolume, VALUE_FADE_DURATION, null);
        decks[1].fade(currentVolume, 0.0f, VALUE_FADE_DURATION, null);

        this.currentDeck = decks[0];
    }

    // if (adsIndexer.getRemotedCount() != 0) {
    //     campaignsTimer.postDelayed(this, 300000);
    //     return;
    // }

    private void advertise() {
        Runnable advertiseIteration = new Runnable() {
            @Override
            public void run() {
                final Campaign campaign = Campaign.sample(campaigns);

                deckC.load(campaign.randomAds(), new DeckEventListener() {
                    @Override
                    public void onReady(Deck deck, MediaPlayer player, AudioModel audio) {
                        currentVolume = campaign.isDucked() ? 0.3f : 0.0f;

                        deckA.fade(currentVolume, VALUE_FADE_DURATION, null);
                        deckB.fade(currentVolume, VALUE_FADE_DURATION, null);

                        campaignsTimer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deckC.start();
                            }
                        }, VALUE_FADE_DURATION);
                    }

                    @Override
                    public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {
                        // no implementation necessary...
                    }

                    @Override
                    public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {
                        // no implementation necessary...
                    }

                    @Override
                    public void onComplete(Deck deck, MediaPlayer player) {
                        currentVolume = 1.0f;

                        deckA.fade(currentVolume, VALUE_FADE_DURATION, null);
                        deckB.fade(currentVolume, VALUE_FADE_DURATION, null);

                        advertise();
                    }
                });
            }
        };

        campaignsTimer.postDelayed(advertiseIteration, 300000);
    }

    private void setState(int state) {
        this.currentState = state;
        sendBroadcast();
    }

    private void checkRequirements() {

    }

    private Track selectTrackForCurrentPeriod() {
        return Track.selectRandom(tracksIndexer.getQueue(), currentPeriod.collectStyleIds());
    }

    private List<Ad> selectAdsForCurrentPeriod() {
        final Campaign campaign = Campaign.sample(campaigns);
        final List<Ad> ads      = Ad.sample(campaign.getAds(), campaign.getAdsCountPerBlock());

        return null;
    }

    private void sendBroadcast() {
        Intent intent = new Intent(Radiant.INTENT_PLAYER_STATE_CHANGED);
        intent.putExtra(PROPERTY_STATE, currentState);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}