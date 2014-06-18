package fm.radiant.android.lib.player;

import android.app.AlarmManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.indexer.AdsIndexer;
import fm.radiant.android.lib.indexer.TracksIndexer;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.models.Campaign;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Style;
import fm.radiant.android.models.Track;
import fm.radiant.android.receivers.ScheduleReceiver;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.NetworkUtils;

public class Player {
    public static final int   VALUE_FADE_DURATION       = 3000;
    public static final float VALUE_STREAM_TRACKS_RATIO = 0.5f;

    public static final byte STATE_PLAYING               = 0x1;
    public static final byte STATE_WAITING               = 0x2;
    public static final byte STATE_INDEXING              = 0x3;
    public static final byte STATE_IDLE_PERIODS_REQUIRED = 0x4;
    public static final byte STATE_STOPPED               = 0x5;

    private final Context      mContext;
    private final AudioManager mAudioManager;
    private final AlarmManager mAlarmManager;

    private Period mCurrentPeriod;
    private Deck   mCurrentDeck;
    private byte   mCurrentState = STATE_STOPPED;

    private final Deck.OnChangeListener mDeckEventListener;
    private float mDeckVolumeLimit = 1.0f;

    private final Deck mDeckA;
    private final Deck mDeckB;
    private final Deck mDeckC;

    private List<Period>   mPeriods;
    private List<Campaign> mCampaigns;

    private Syncer mSyncer;

    private TracksIndexer mTracksIndexer; private AdsIndexer mAdsIndexer;

    private final Handler mTimer = new Handler();
    private final Lock    mLock  = new ReentrantLock();

    public Player(Context context) {
        mContext      = context;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        mDeckA = new Deck(context); mCurrentDeck = mDeckA;
        mDeckB = new Deck(context);
        mDeckC = new Deck(context);

        mDeckEventListener = new Deck.OnChangeListener() {
            @Override
            public void onReady(Deck nextDeck, AudioModel audio) {
                nextDeck.start();

                if (nextDeck != mCurrentDeck && mCurrentDeck.getPlayer().isPlaying()) {
                    mix(mCurrentDeck, nextDeck);
                } else {
                    nextDeck.setVolume(mDeckVolumeLimit);
                }

                Player.this.mCurrentDeck = nextDeck;
            }

            @Override
            public void onQueue(Deck deck, AudioModel audio) {
                // no implementation necessary...
            }

            @Override
            public void onEmpty(Deck deck) {
                // no implementation necessary...
            }

            @Override
            public void onFailure(Deck deck, AudioModel audio, IOException exception) {
                deck.load(getRandomTrack(mCurrentPeriod), this);
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

    public void setIndexers(TracksIndexer tracksIndexer, AdsIndexer adsIndexer) {
        mTracksIndexer = tracksIndexer; mAdsIndexer = adsIndexer;
    }

    public void setPeriods(List<Period> periods) {
        mPeriods = periods;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        mCampaigns = campaigns;
    }

    public void play() {
        if (mCurrentPeriod == null) {
            stop(STATE_IDLE_PERIODS_REQUIRED);
        } else

        if (!mCurrentPeriod.isNow()) {
            stop(STATE_WAITING);
        } else

        if (!mCurrentDeck.getPlayer().isPlaying()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    stop(STATE_INDEXING);

                    mTracksIndexer.index(); mAdsIndexer.index();

                    if (mCurrentState != STATE_INDEXING) {
                        return;
                    }

                    stop(STATE_PLAYING);

                    if (mCurrentDeck.getAudio() == null || mCurrentDeck.isEnding() || !Style.collectIds(mCurrentPeriod.getGenre().getStyles()).contains(((Track) mCurrentDeck.getAudio()).getStyleId())) {
                        mCurrentDeck.load(getRandomTrack(mCurrentPeriod), mDeckEventListener);
                    } else {
                        mCurrentDeck.setVolume(mDeckVolumeLimit);
                        mCurrentDeck.start();
                    }

                    advertise();
                }
            }).start();
        }
    }

    public void stop(byte state) {
        setState(state);

        mTimer.removeCallbacksAndMessages(null);

        mDeckA.pause();
        mDeckB.pause();
        mDeckC.pause();

        mDeckVolumeLimit = 1.0f;
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void reset() {
        stop();

        mDeckA.eject(); mCurrentDeck = mDeckA;
        mDeckB.eject();
        mDeckC.eject();

        mCurrentPeriod = null;
        mCampaigns.clear();
        mTracksIndexer = null;
        mAdsIndexer = null;
    }

    public void schedule() {
        mCurrentPeriod = Period.findCurrent(mPeriods);

        if (mCurrentPeriod != null) {
            mAlarmManager.set(AlarmManager.RTC_WAKEUP, mCurrentPeriod.getDelay(), ScheduleReceiver.getBroadcast(mContext));
        }

        if (mCurrentState != STATE_STOPPED) {
            play();
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

    public boolean isMusicEnough() {
        double ratio = 0;

        for (Integer styleId : mTracksIndexer.getStyleIds()) {
            ratio += (double) mTracksIndexer.getPersistedMinutes(styleId) / mTracksIndexer.getTotalMinutes(styleId);
        }

        return ratio / mTracksIndexer.getStyleIds().size() > VALUE_STREAM_TRACKS_RATIO;
    }

    public void setSyncer(Syncer syncer) {
        mSyncer = syncer;
    }

    public void setVolume(int value) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value, 0);
    }

    public void adjustVolume(int direction) {
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, 0);
        EventBus.getDefault().post(new Events.PlayerVolumeChanged(getVolume()));
    }

    private void advertise() {
        Runnable advertiseIteration = new Runnable() {
            @Override
            public void run() {
                final Campaign campaign = Campaign.getRandom(mCampaigns);

                if (mAdsIndexer.getRemotedCount() != 0 || campaign == null) {
                    return;
                }

                mDeckC.load(campaign.randomAds(), new Deck.OnChangeListener() {
                    boolean faded = true;

                    @Override
                    public void onReady(Deck deck, AudioModel audio) {
                        if (faded == false) {
                            mDeckC.start();
                            return;
                        }

                        faded = false;

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
                    public void onQueue(Deck deck, AudioModel audio) {
                        // no implementation necessary...
                    }

                    @Override
                    public void onFailure(Deck deck, AudioModel audio, IOException exception) {
                        // no implementation necessary...
                    }

                    @Override
                    public void onEmpty(Deck deck) {
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

    private void setState(byte state) {
        mCurrentState = state;

        EventBus.getDefault().postSticky(new Events.PlayerStateChanged(mCurrentState));
    }

    private Track getRandomTrack(Period period) {
        List<Track> queue;

        if (isMusicEnough()) {
            queue = mTracksIndexer.getPersistedQueue();
        } else {
            queue = mTracksIndexer.getQueue();
        }

        return Track.getRandom(queue, period.getGenre().getStyles());
    }
}