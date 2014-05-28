package fm.radiant.android.classes.player;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.SparseIntArray;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Date;
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
    private static final String TAG = "Place";

    public static final int STATE_PLAYING = 1;
    public static final int STATE_STOPPED = 2;
    public static final int STATE_WAITING = 3;
    public static final int STATE_FAILED  = 4;

    private int currentState = STATE_STOPPED;

    private static final String PROPERTY_STATE = "state";

    private static final int VALUE_FADE_DURATION = 3000;

    private final Context context;
    private final SharedPreferences preferences;

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

    private  AudioManager audioManager;

    private SparseIntArray lastPlays = new SparseIntArray();

    public Player(final Context context) {
        this.context     = context;
        this.preferences = context.getSharedPreferences(TAG + "plays", Context.MODE_PRIVATE );

        this.periodsTimer   = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        this.campaignsTimer = new Handler();

        this.deckA = new Deck(context);
        this.deckB = new Deck(context);
        this.deckC = new Deck(context);

        this.currentDeck   = deckA;
        this.currentVolume = 1.0f;

        // try {
        //     lastPlays = ParseUtils.fromJSON(preferences.getString("lastplays", ""), SparseIntArray.class);
        // } catch (IOException e) {
        //     lastPlays = new SparseIntArray();
        //     Log.e("Player", "sparsead", e);
        // }
//
        deckEventListener = new DeckEventListener() {
            @Override
            public void onReady(final Deck nextDeck, MediaPlayer player, AudioModel audio) {
                nextDeck.start();

                lastPlays.put(audio.getId(), (int) (new Date().getTime() / 1000));

                Log.d("player", "s " + lastPlays.size());
                //try {
                //    preferences.edit().putString("lastplays", ParseUtils.toJSON(lastPlays)).commit();
                //} catch (IOException e) {
                //    e.printStackTrace();
                //}

                if (currentDeck != nextDeck && currentDeck.getPlayer().isPlaying()) {
                     currentDeck.fade(currentVolume, 0.0f, VALUE_FADE_DURATION, null);
                     nextDeck.fade(0.0f, currentVolume, VALUE_FADE_DURATION, null);
                } else {
                     nextDeck.setVolume(Player.this.currentVolume);
                }

                currentDeck = nextDeck;
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

        deckA.setCue(new Cue(15000) {
            @Override
            public void run() {
                Log.d("player", "deckB.load");
                deckB.load(selectTrackForCurrentPeriod(), deckEventListener);
            }
        });

        deckB.setCue(new Cue(15000) {
            @Override
            public void run() {
                Log.d("player", "deckA.load");
                deckA.load(selectTrackForCurrentPeriod(), deckEventListener);
            }
        });

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
            return;
        }

        // failed

        double persistedMinutes = 0;

        for (Integer styleId : currentPeriod.collectStyleIds()) {
            persistedMinutes += tracksIndexer.getPersistedMinutes(styleId);
        }

        if (!NetworkUtils.isNetworkConnected() && (persistedMinutes / currentPeriod.getDuration() < 0.3)) {
            stop(STATE_FAILED);
            return;
        }

        // stream and play

        if (currentDeck.getPlayer().isPlaying()) {
            return;
        }

        if (currentDeck.getAudio() == null || !currentPeriod.collectStyleIds().contains(((Track) currentDeck.getAudio()).getStyleId())) {
            currentDeck.load(selectTrackForCurrentPeriod(), deckEventListener);
        } else {
            currentDeck.setVolume(currentVolume);
            currentDeck.start();
        }

        advertise();

        setState(STATE_PLAYING);
    }

    public void stop(int state) {
        setState(state);

        campaignsTimer.removeCallbacksAndMessages(null);

        deckA.pause();
        deckB.pause();
        deckC.pause();

        this.currentVolume = 1.0f;
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

    public void setVolume(int level) {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume    = Math.round(level * maxVolume / 100);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    public int getVolume() {
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume    = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        return Math.round(volume / maxVolume * 100);
    }

    private void advertise() {
        // if (adsIndexer.getRemotedCount() != 0) {
        //     campaignsTimer.postDelayed(this, 300000);
        //     return;
        // }

        Runnable advertiseIteration = new Runnable() {
            @Override
            public void run() {
                Log.d("player", "player now playing ads. time: " + new DateTime().toString());

                final Campaign campaign = campaigns.get(0);//  Campaign.sample(campaigns);

                if (campaign == null) {
                    // advertise
                    return;
                }
                Log.d("player", "com" + campaign.randomAds().size());

                boolean first = true;

                deckC.load(campaign.randomAds(), new DeckEventListener() {
                    boolean fade = true;

                    @Override
                    public void onReady(Deck deck, MediaPlayer player, AudioModel audio) {
                        if (fade == false) {
                            deckC.start();
                            return;
                        }
                        fade = false;

                        Log.d("sdc", "onReady");

                        Player.this.currentVolume = campaign.isDucked() ? 0.2f : 0.0f;

                        if (campaign.isDucked()) Log.d("ducked", "ducked");

                        currentDeck.fade(currentVolume, VALUE_FADE_DURATION, null);

                        campaignsTimer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                deckC.start();
                                deckC.getPlayer().setVolume(0, 0);
                            }
                        }, VALUE_FADE_DURATION);
                    }

                    @Override
                    public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {
                        // no implementation necessary...
                    }

                    @Override
                    public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {
                        Log.e("sd", "sd", exception);
                        // no implementation necessary...
                    }

                    @Override
                    public void onComplete(Deck deck, MediaPlayer player) {
                        currentDeck.fade(currentVolume = 1.0f, VALUE_FADE_DURATION, null);

                        // advertise();
                    }
                });
            }
        };

        Log.d("adverd", "asad");

        campaignsTimer.removeCallbacksAndMessages(null);
        campaignsTimer.postDelayed(advertiseIteration, 5000);
    }

    private void setState(int state) {
        this.currentState = state;
        sendBroadcast();
    }

    private void checkRequirements() {

    }

    private Track selectTrackForCurrentPeriod() {
        double persistedMinutes = 0;

        for (Integer styleId : currentPeriod.collectStyleIds()) {
            persistedMinutes += tracksIndexer.getPersistedMinutes(styleId);
        }

        if (persistedMinutes / currentPeriod.getDuration() > 0.7) {
            return Track.selectRandom(tracksIndexer.getPersistedQueue(), currentPeriod.collectStyleIds(), lastPlays);
        } else {
            return Track.selectRandom(tracksIndexer.getQueue(), currentPeriod.collectStyleIds(), lastPlays);
        }
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
