package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
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
import fm.radiant.android.models.Campaign;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Style;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.MediaReceiver;
import fm.radiant.android.utils.NetworkUtils;

public class Player {
    private static final int   VALUE_FADE_DURATION       = 3000;
    private static final float VALUE_STREAM_TRACKS_RATIO = 0.6f;
    private static final float VALUE_ONLINE_TRACKS_RATIO = 0.3f;

    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_FAILED  = 4;

    private int currentState = STATE_STOPPED;

    private static final String PROPERTY_STATE  = "state";
    private static final String PROPERTY_VOLUME = "volume";

    private final Context context;

    private final AudioManager audioManager;
    private final AlarmManager alarmManager;

    private final DeckEventListener deckEventListener;
    private float deckVolumeLimit = 1.0f;

    private final Deck deckA;
    private final Deck deckB;
    private final Deck deckC;
    private Deck currentDeck;

    private List<Period> periods;
    private Period currentPeriod;

    private List<Campaign> campaigns;
    private Campaign currentCampaign;

    private TracksIndexer tracksIndexer;
    private AdsIndexer adsIndexer;

    private final Handler timer = new Handler();

    public Player(Context context) {
        this.context = context;

        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        this.deckA = new Deck(context);
        this.deckB = new Deck(context);
        this.deckC = new Deck(context);

        this.currentDeck = deckA;

        this.deckEventListener = new DeckEventListener() {
            @Override
            public void onReady(Deck nextDeck, MediaPlayer player, AudioModel audio) {
                nextDeck.start();

                if (nextDeck != currentDeck && currentDeck.getPlayer().isPlaying()) {
                    mix(currentDeck, nextDeck);
                } else {
                    nextDeck.setVolume(deckVolumeLimit);
                }

                Player.this.currentDeck = nextDeck;
            }

            @Override
            public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {
                // no implementation necessary...
            }

            @Override
            public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {
                deck.load(getRandomTrack(currentPeriod), this);
            }

            @Override
            public void onComplete(Deck deck, MediaPlayer player) {
                // no implementation necessary...
            }
        };

        deckA.setCue(new Cue(-15000) {
            @Override
            public void run() {
                deckB.load(getRandomTrack(currentPeriod), deckEventListener);
            }
        });

        deckB.setCue(new Cue(-15000) {
            @Override
            public void run() {
                deckA.load(getRandomTrack(currentPeriod), deckEventListener);
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

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public void play() {
        if (!currentPeriod.isNow()) {
            stop(STATE_WAITING);
        } else

        if (!NetworkUtils.isNetworkConnected() && getPersistedTracksRatio(currentPeriod) < VALUE_ONLINE_TRACKS_RATIO) {
            stop(STATE_FAILED);
        } else

        if (!currentDeck.getPlayer().isPlaying()) {
            stop(STATE_PLAYING);

            if (currentDeck.getAudio() == null || Style.collectIds(currentPeriod.getGenre().getStyles()).contains(((Track) currentDeck.getAudio()).getStyleId())) {
                currentDeck.load(getRandomTrack(currentPeriod), deckEventListener);
            } else {
                currentDeck.setVolume(deckVolumeLimit);
                currentDeck.start();
            }

            advertise();
        }
    }

    public void stop(int state) {
        setState(state);

        timer.removeCallbacksAndMessages(null);

        deckA.pause();
        deckB.pause();
        deckC.pause();

        this.deckVolumeLimit = 1.0f;
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void schedule() {
        this.currentPeriod = Period.findCurrent(periods);

        if (currentPeriod != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, currentPeriod.getDelay(), MediaReceiver.getBroadcast(context));
        } else {
            sendBroadcast();
        }
    }

    public void sendBroadcast() {
        Intent intent = new Intent(Radiant.INTENT_PLAYER_STATE_CHANGED);
        intent.putExtra(PROPERTY_STATE,  getState());
        intent.putExtra(PROPERTY_VOLUME, getVolume());

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public int getState() {
        return currentState;
    }

    public int getVolume() {
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMaxVolume() {
        return audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public Period getPeriod() {
        return currentPeriod;
    }

    public void setVolume(int value) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
        sendBroadcast();
    }

    public void adjustVolume(int direction) {
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
        sendBroadcast();
    }

    private void advertise() {
        Runnable advertiseIteration = new Runnable() {
            @Override
            public void run() {
                final Campaign campaign = Campaign.getRandom(campaigns);

                if (adsIndexer.getRemotedCount() != 0 || campaign == null || campaign.getAds().isEmpty()) {
                    return;
                }

                deckC.load(campaign.randomAds(), new DeckEventListener() {
                    boolean fade = true;

                    @Override
                    public void onReady(Deck deck, MediaPlayer player, AudioModel audio) {
                        if (fade == false) {
                            deckC.start();
                            return;
                        }

                        fade = false;

                        Player.this.deckVolumeLimit = campaign.isDucked() ? 0.5f : 0.0f;

                        currentDeck.fade(deckVolumeLimit, VALUE_FADE_DURATION, null);

                        timer.postDelayed(new Runnable() {
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
                        Player.this.deckVolumeLimit = 1.0f;
                        currentDeck.fade(deckVolumeLimit, VALUE_FADE_DURATION, null);
                        advertise();
                    }
                });
            }
        };

        timer.removeCallbacksAndMessages(null);
        timer.postDelayed(advertiseIteration, 600000);
    }

    private void mix(Deck deckA, Deck deckB) {
        deckA.fade(deckVolumeLimit, 0.0f, VALUE_FADE_DURATION, null);
        deckB.fade(0.0f, deckVolumeLimit, VALUE_FADE_DURATION, null);
    }

    private void setState(int state) {
        this.currentState = state;
        sendBroadcast();
    }

    private float getPersistedTracksRatio(Period period) {
        float persistedMinutes = 0;

        for (Integer styleId : Style.collectIds(period.getGenre().getStyles())) {
            persistedMinutes += tracksIndexer.getPersistedMinutes(styleId);
        }

        return persistedMinutes / period.getDuration();
    }

    private Track getRandomTrack(Period period) {
        List<Track> queue;

        if (getPersistedTracksRatio(period) > 0.7) {
            queue = tracksIndexer.getPersistedQueue();
        } else {
            queue = tracksIndexer.getQueue();
        }

        return Track.getRandom(queue, period.getGenre().getStyles());
    }
}
