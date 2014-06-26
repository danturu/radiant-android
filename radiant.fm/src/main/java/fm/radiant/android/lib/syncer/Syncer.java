package fm.radiant.android.lib.syncer;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.Radiant;
import fm.radiant.android.lib.indexer.AdsIndexer;
import fm.radiant.android.lib.indexer.TracksIndexer;
import fm.radiant.android.models.Ad;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.models.Track;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.utils.NetworkUtils;
import fm.radiant.android.utils.StorageUtils;

public class Syncer implements AbstractDownload.OnProgressListener {
    public static final String TAG = Syncer.class.getSimpleName();

    public static final byte STATE_PREPARING         = 0x0;
    public static final byte STATE_SYNCED            = 0x1;
    public static final byte STATE_SYNCING           = 0x2;
    public static final byte STATE_INDEXING          = 0x3;
    public static final byte STATE_IDLE_NO_INTERNET  = 0x4;
    public static final byte STATE_FAILED_NO_SPACE   = 0x5;
    public static final byte STATE_FAILED_NO_STORAGE = 0x6;
    public static final byte STATE_STOPPED           = 0x7;

    private static volatile Syncer instance;
    private final Context mContext;

    private volatile TracksIndexer mTracksIndexer;
    private volatile AdsIndexer mAdsIndexer;

    private Stack<AbstractDownload> mDownloads = new Stack<AbstractDownload>();
    private AbstractDownload mCurrentDownload;

    private byte mCurrentState  = STATE_PREPARING;
    private byte mSyncedPercent = 0;
    private long mEstimatedTime = 0;
    private long mDownloadSpeed = 0;
    private long mReceivedBytes = 0;

    private List<Integer> mSpeedSamples = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
    private RateLimiter mTrackLimiter   = RateLimiter.create(1.0);

    private Handler mBackoff = new Handler();
    private boolean mResync = false;
    private final Lock mLock = new ReentrantLock();

    public static Syncer getInstance() {
        Syncer localInstance = instance;

        if (localInstance == null) {
            synchronized (Syncer.class) {
                localInstance = instance;

                if (localInstance == null) {
                    instance = localInstance = new Syncer(Radiant.getContext());
                }
            }
        }

        return localInstance;
    }

    private Syncer(Context context) {
        mContext = context;
    }

    public void startService() {
        stopService(STATE_PREPARING);

        Intent service = new Intent(mContext, DownloadService.class);
        mContext.startService(service);
    }

    public void stopService(byte state) {
        if (mCurrentState == state) return;

        Intent service = new Intent(mContext, DownloadService.class);
        mContext.stopService(service);

        stop(state);
    }

    public void stopService() {
        stopService(STATE_STOPPED);
    }

    public void start() {
        synchronized (mLock) {
            try {
                FileUtils.deleteQuietly(mContext.getExternalCacheDir());

                index();
                fetch();

                measureProgress(); measurePercent(); stop(STATE_SYNCED);

                if (mResync) resync();
            } catch (IOException exception) {
                Log.e(TAG, "An exception has occurred: ", exception);
            }
        }
    }

    public void stop(final byte state) {
        if (mLock.tryLock()) {
             try {
                 setState(state);

                 mBackoff.removeCallbacksAndMessages(null);

                 if (mCurrentDownload != null) mCurrentDownload.abort();
             } finally {
                 mLock.unlock();
             }
        }
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void reset() {
        stop(STATE_PREPARING);

        synchronized (mLock) {
            mTracksIndexer = null;
            mAdsIndexer = null;
            mCurrentDownload = null;

            mDownloads.clear();

            try {
                FileUtils.deleteDirectory(new Track().getDirectory(mContext));
            } catch (IOException e) {
                Log.e(TAG, "Could not clean tracks cache.", e);
            }

            try {
                FileUtils.deleteDirectory(new Ad().getDirectory(mContext));
            } catch (IOException e) {
                Log.e(TAG, "Could not clean ads cache.", e);
            }
        }
    }

    public Byte getState() {
        return mCurrentState;
    }

    public Byte getSyncedPercent() {
        return mSyncedPercent;
    }

    public Long getDownloadSpeed() {
        return mDownloadSpeed;
    }

    public Long getEstimatedTime() {
        return mEstimatedTime;
    }

    @Override
    public void onSuccess(AbstractDownload download, AudioModel model, File tempFile) {
        if (model instanceof Track) {
            mTracksIndexer.moveToPersisted(model);
        } else {
            mAdsIndexer.moveToPersisted(model);
        }

        measurePercent();
    }

    @Override
    public void onFailure(AbstractDownload download, AudioModel model, IOException exception) {
        mResync = true;
        Log.e(TAG, "Could not download " + model.getClass().getSimpleName() + " (id=" + model.getStringId() + "): ", exception);
    }

    @Override
    public void onComplete(AbstractDownload download, AudioModel model) {
        mReceivedBytes = 0;
    }

    @Override
    public void onProgress(AbstractDownload download, AudioModel model, int receivedBytes, int totalBytes) {
        if (mTrackLimiter.tryAcquire()) {
            mSpeedSamples.remove(0);
            mSpeedSamples.add(receivedBytes - (int) mReceivedBytes);
            mReceivedBytes = receivedBytes;

            measureProgress();
        }
    }

    private void index() throws IOException {
        stop(STATE_INDEXING);

        synchronized (mLock) {
            mDownloads.clear();
            mAdsIndexer.index();
            mTracksIndexer.index();

            for (Ad model : mAdsIndexer.getRemotedQueue()) {
                mDownloads.add(new AdDownload(mContext, model, this));
            }

            for (Track model : mTracksIndexer.getBalancedRemotedQueue()) {
                mDownloads.add(new TrackDownload(mContext, model, this));
            }

            Collections.reverse(mDownloads);

            measurePercent();
        }
    }

    private void fetch() throws InterruptedIOException {
        setState(STATE_SYNCING);

        synchronized (mLock) {
            check(); throwOnInterrupt();

            for (AbstractDownload download : mDownloads) {
                check(); throwOnInterrupt();

                (mCurrentDownload = download).start();
            }
        }
    }

    private void check() {
        if (!StorageUtils.isExternalStorageWritable()) {
            stop(STATE_FAILED_NO_STORAGE);
        } else if (!StorageUtils.isFreeSpaceEnough()) {
            stop(STATE_FAILED_NO_SPACE);
        } else if (!NetworkUtils.isNetworkConnected()) {
            stop(STATE_IDLE_NO_INTERNET);
        }
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (mCurrentState != STATE_SYNCING) throw new InterruptedIOException();
    }

    private void setState(byte state) {
        mCurrentState = state;

        EventBus.getDefault().postSticky(new Events.SyncerStateChanged(mCurrentState));
    }

    public void setIndexers(TracksIndexer tracksIndexer, AdsIndexer adsIndexer) {
        mTracksIndexer = tracksIndexer;
        mAdsIndexer    = adsIndexer;
    }

    private Byte calculateSyncedPercent() {
        long persistedBytes = mTracksIndexer.getPersistedBytes() + mAdsIndexer.getPersistedBytes();
        long totalBytes = mTracksIndexer.getTotalBytes() + mAdsIndexer.getTotalBytes();

        if (totalBytes == persistedBytes) {
            return (byte) 100;
        } else {
            return (byte) (persistedBytes * 100 / totalBytes);
        }
    }

    private Long calculateDownloadSpeed() {
        long receivedBytes = 0;
        for (int sample : mSpeedSamples) {
            receivedBytes += sample;
        }

        return receivedBytes / mSpeedSamples.size();
    }

    private Long calculateEstimatedTime() {
        long remotedBytes = mTracksIndexer.getRemotedBytes() + mAdsIndexer.getRemotedBytes();

        if (mDownloadSpeed < 1) {
            return Long.MAX_VALUE;
        } else {
            return (remotedBytes + mReceivedBytes) / mDownloadSpeed;
        }
    }

    private final Events.SyncerProgressChanged syncerProgressChanged = new Events.SyncerProgressChanged();

    private void measureProgress() {
        mDownloadSpeed = calculateDownloadSpeed();
        mEstimatedTime = calculateEstimatedTime();

        syncerProgressChanged.setDownloadSpeed(mDownloadSpeed);
        syncerProgressChanged.setEstimatedTime(mEstimatedTime);

        EventBus.getDefault().postSticky(syncerProgressChanged);
    }

    private void measurePercent() {
        mSyncedPercent = calculateSyncedPercent();

        EventBus.getDefault().postSticky(new Events.SyncerSyncedPercentChanged(mSyncedPercent));
    }

    private void resync() {
        Runnable resync = new Runnable() {
            public void run() {
                mContext.startService(new Intent(mContext, DownloadService.class));
            }
        };

        mBackoff.postDelayed(resync, 10000);
    }

}