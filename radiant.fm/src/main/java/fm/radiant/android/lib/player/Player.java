package fm.radiant.android.lib.player;

import android.app.AlarmManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.indexer.AdsIndexer;
import fm.radiant.android.lib.indexer.TracksIndexer;
import fm.radiant.android.models.AudioModel;
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

    public static final int STATE_INDEXING = 1;
    public static final int STATE_PLAYING  = 2;
    public static final int STATE_STOPPED  = 3;
    public static final int STATE_WAITING  = 4;
    public static final int STATE_FAILED   = 5;

    private int mCurrentState = STATE_STOPPED;

    private static final String PROPERTY_STATE  = "state";
    private static final String PROPERTY_VOLUME = "volume";

    private final Context mContext;

    private final AudioManager mAudioManager;
    private final AlarmManager mAlarmManager;

    private final Deck.OnProgressListener mDeckEventListener;
    private float mDeckVolumeLimit = 1.0f;

    private final Deck mDeckA;
    private final Deck mDeckB;
    private final Deck mDeckC;
    private Deck mCurrentDeck;

    private List<Period> mPeriods;
    private Period mCurrentPeriod;

    private List<Campaign> mCampaigns;
    private Campaign mCurrentCampaign;

    private TracksIndexer mTracksIndexer;
    private AdsIndexer    mAdsIndexer;

    private final Handler mTimer = new Handler();
    private final Lock    mLock  = new ReentrantLock();

    public Player(Context context) {
        mContext      = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        this.mDeckA = new Deck(context);
        this.mDeckB = new Deck(context);
        this.mDeckC = new Deck(context);

        this.mCurrentDeck = mDeckA;

        this.mDeckEventListener = new Deck.OnProgressListener() {
            @Override
            public void onReady(Deck nextDeck, MediaPlayer player, AudioModel audio) {
                nextDeck.start();

                if (nextDeck != mCurrentDeck && mCurrentDeck.getPlayer().isPlaying()) {
                    mix(mCurrentDeck, nextDeck);
                } else {
                    nextDeck.setVolume(mDeckVolumeLimit);
                }

                Player.this.mCurrentDeck = nextDeck;
            }

            @Override
            public void onNext(Deck deck, MediaPlayer player, AudioModel audio) {
                // no implementation necessary...
            }

            @Override
            public void onFailure(Deck deck, MediaPlayer player, AudioModel audio, IOException exception) {
                deck.load(getRandomTrack(mCurrentPeriod), this);
            }

            @Override
            public void onComplete(Deck deck, MediaPlayer player) {
                // no implementation necessary...
            }
        };

        mDeckA.setCue(new Cue(-15000) {
            @Override
            public void run() {
                mDeckB.load(getRandomTrack(mCurrentPeriod), mDeckEventListener);
            }
        });

        mDeckB.setCue(new Cue(-15000) {
            @Override
            public void run() {
                mDeckA.load(getRandomTrack(mCurrentPeriod), mDeckEventListener);
            }
        });
    }

    public void setTracksIndexer(TracksIndexer indexer) {
        this.mTracksIndexer = indexer;
    }

    public void setAdsIndexer(AdsIndexer indexer) {
        this.mAdsIndexer = indexer;
    }

    public void setPeriods(List<Period> periods) {
        this.mPeriods = periods;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.mCampaigns = campaigns;
    }

    public void play() {
        mLock.lock();

        if (!mCurrentPeriod.isNow()) {
            stop(STATE_WAITING);
            mLock.unlock();
        } else

        if (!NetworkUtils.isNetworkConnected() && getPersistedTracksRatio(mCurrentPeriod) < VALUE_ONLINE_TRACKS_RATIO) {
            stop(STATE_FAILED);
            mLock.unlock();
        } else

        if (!mCurrentDeck.getPlayer().isPlaying()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        stop(STATE_INDEXING);

                        mTracksIndexer.index();
                        mAdsIndexer.index();

                        if (mCurrentState != STATE_INDEXING) {
                            return;
                        }

                        stop(STATE_PLAYING);

                        if (mCurrentDeck.getAudio() == null || Style.collectIds(mCurrentPeriod.getGenre().getStyles()).contains(((Track) mCurrentDeck.getAudio()).getStyleId())) {
                            mCurrentDeck.load(getRandomTrack(mCurrentPeriod), mDeckEventListener);
                        } else {
                            mCurrentDeck.setVolume(mDeckVolumeLimit);
                            mCurrentDeck.start();
                        }

                        advertise();
                    } finally {
                        mLock.unlock();
                    }
                }
            }).start();
        }
    }

    public void stop(int state) {
        setState(state);

        mTimer.removeCallbacksAndMessages(null);

        mDeckA.pause();
        mDeckB.pause();
        mDeckC.pause();

        this.mDeckVolumeLimit = 1.0f;
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void schedule() {
        mCurrentPeriod = Period.findCurrent(mPeriods);

        if (mCurrentPeriod != null) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCurrentPeriod.getDelay(), MediaReceiver.getBroadcast(mContext));
        }

        EventBus.getDefault().postSticky(new Events.PlayerPeriodChanged(mCurrentPeriod));
    }

    public int getState() {
        return mCurrentState;
    }

    public int getVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    public int getMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    public Period getPeriod() {
        return mCurrentPeriod;
    }

    public void setVolume(int value) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
    }

    public void adjustVolume(int direction) {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
        EventBus.getDefault().postSticky(new Events.PlayerVolumeChanged(getVolume()));
    }

    private void advertise() {
        Runnable advertiseIteration = new Runnable() {
            @Override
            public void run() {
                final Campaign campaign = Campaign.getRandom(mCampaigns);

                if (mAdsIndexer.getRemotedCount() != 0 || campaign == null || campaign.getAds().isEmpty()) {
                    return;
                }

                mDeckC.load(campaign.randomAds(), new Deck.OnProgressListener() {
                    boolean fade = true;

                    @Override
                    public void onReady(Deck deck, MediaPlayer player, AudioModel audio) {
                        if (fade == false) {
                            mDeckC.start();
                            return;
                        }

                        fade = false;

                        Player.this.mDeckVolumeLimit = campaign.isDucked() ? 0.5f : 0.0f;

                        mCurrentDeck.fade(mDeckVolumeLimit, VALUE_FADE_DURATION, null);

                        mTimer.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDeckC.start();
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
                        Player.this.mDeckVolumeLimit = 1.0f;
                        mCurrentDeck.fade(mDeckVolumeLimit, VALUE_FADE_DURATION, null);
                        advertise();
                    }
                });
            }
        };

        mTimer.removeCallbacksAndMessages(null);
        mTimer.postDelayed(advertiseIteration, 600000);
    }

    private void mix(Deck deckA, Deck deckB) {
        deckA.fade(mDeckVolumeLimit, 0.0f, VALUE_FADE_DURATION, null);
        deckB.fade(0.0f, mDeckVolumeLimit, VALUE_FADE_DURATION, null);
    }

    private void setState(int state) {
        this.mCurrentState = state;

        EventBus.getDefault().postSticky(new Events.PlayerStateChanged(mCurrentState));
    }

    private float getPersistedTracksRatio(Period period) {
        float persistedMinutes = 0;

        for (Integer styleId : Style.collectIds(period.getGenre().getStyles())) {
            persistedMinutes += mTracksIndexer.getPersistedMinutes(styleId);
        }

        return persistedMinutes / period.getDuration();
    }

    private Track getRandomTrack(Period period) {
        List<Track> queue;

        if (getPersistedTracksRatio(period) > 0.7) {
            queue = mTracksIndexer.getPersistedQueue();
        } else {
            queue = mTracksIndexer.getQueue();
        }

        return Track.getRandom(queue, period.getGenre().getStyles());
    }
}