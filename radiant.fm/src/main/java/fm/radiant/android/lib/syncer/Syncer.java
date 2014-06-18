package fm.radiant.android.lib.syncer;

import android.content.Context;
import android.util.Log;

import com.google.common.util.concurrent.RateLimiter;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.indexer.AdsIndexer;
import fm.radiant.android.lib.indexer.TracksIndexer;
import fm.radiant.android.models.Ad;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.models.Track;
import fm.radiant.android.utils.NetworkUtils;
import fm.radiant.android.utils.StorageUtils;

public class Syncer implements Download.OnProgressListener {
    public static final String TAG = Syncer.class.getSimpleName();

    public static final byte STATE_NULL              = 0x0;
    public static final byte STATE_SYNCED            = 0x1;
    public static final byte STATE_SYNCING           = 0x2;
    public static final byte STATE_INDEXING          = 0x3;
    public static final byte STATE_IDLE_NO_INTERNET  = 0x4;
    public static final byte STATE_FAILED_NO_SPACE   = 0x5;
    public static final byte STATE_FAILED_NO_STORAGE = 0x6;
    public static final byte STATE_STOPPED           = 0x7;

    private final Context mContext;

    private TracksIndexer mTracksIndexer; private AdsIndexer mAdsIndexer;

    private List<Download> mDownloads = new CopyOnWriteArrayList<Download>();
    private Download mCurrentDownload;

    private byte mCurrentState  = STATE_NULL;
    private byte mSyncedPercent = 0;
    private long mEstimatedTime = 0;
    private long mDownloadSpeed = 0;
    private long mReceivedBytes = 0;

    private List<Integer> mSpeedSamples = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
    private RateLimiter   mTrackLimiter = RateLimiter.create(1.0);

    public Syncer(Context context) {
        mContext = context;
    }

    public void start() {
        try {
            index();
            fetch();

            measureProgress(); stop(STATE_SYNCED);
        } catch (IOException exception) {
            Log.e(TAG, "An exception has occurred: ", exception);
        }
    }

    public void touch() {
        try {
            index();
            check();

            measureProgress(); stop(STATE_STOPPED);
        } catch (IOException exception) {
            Log.e(TAG, "An exception has occurred: ", exception);
        }
    }

    public void stop(byte state) {
        setState(state);

        if (mCurrentDownload != null) mCurrentDownload.abort();
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void reset() {
        stop(STATE_NULL);

        mTracksIndexer   = null;
        mAdsIndexer      = null;
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
    public void onSuccess(Download download, final AudioModel model, File file) {
        if (model instanceof Track) { mTracksIndexer.moveToPersisted(model); } else { mAdsIndexer.moveToPersisted(model); }

        EventBus.getDefault().postSticky(new Events.SyncerSyncedPercentChanged(mSyncedPercent = calculateSyncedPercent()));
    }

    @Override
    public void onFailure(Download download, AudioModel model, final IOException e) {
        Log.e(TAG, "Could not download audio(id=" + model.getStringId() + "): ", e);
    }

    @Override
    public void onComplete(Download download, AudioModel model) {
        mReceivedBytes = 0;
    }

    private final Events.SyncerProgressChanged syncerProgressChanged = new Events.SyncerProgressChanged();

    @Override
    public void onProgress(Download download, AudioModel model, int receivedBytes, int totalBytes) {
        if (mTrackLimiter.tryAcquire()) {
            mSpeedSamples.remove(0); mSpeedSamples.add(receivedBytes - (int) mReceivedBytes);

            mDownloadSpeed = calculateDownloadSpeed();
            mEstimatedTime = calculateEstimatedTime();
            mReceivedBytes = receivedBytes;

            syncerProgressChanged.setDownloadSpeed(mDownloadSpeed);
            syncerProgressChanged.setEstimatedTime(mEstimatedTime);

            EventBus.getDefault().postSticky(syncerProgressChanged);
        }
    }

    private void index() throws IOException {
        stop(STATE_INDEXING);

        mDownloads.clear(); mAdsIndexer.index(); mTracksIndexer.index();

        for (AudioModel model : mAdsIndexer.getRemotedQueue()) {
            mDownloads.add(new Download(mContext, model, this));
        }

        for (AudioModel model : mTracksIndexer.getBalancedRemotedQueue()) {
            mDownloads.add(new Download(mContext, model, this));
        }
    }

    private void fetch() throws IOException {
        stop(STATE_SYNCING);

        for (Download download : mDownloads) {
            check(); throwOnInterrupt();

            (mCurrentDownload = download).start();
        }
    }

    private void check() {
        if (!StorageUtils.isExternalStorageWritable()) {
            stop(STATE_FAILED_NO_STORAGE);
        } else

        if (!StorageUtils.isFreeSpaceEnough()) {
            stop(STATE_FAILED_NO_SPACE);
        } else

        if (!NetworkUtils.isNetworkConnected()) {
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
        mTracksIndexer = tracksIndexer; mAdsIndexer = adsIndexer;
    }

    private Byte calculateSyncedPercent() {
        double persistedBytes = mTracksIndexer.getPersistedBytes() + mAdsIndexer.getPersistedBytes();
        double totalBytes     = mTracksIndexer.getTotalBytes()     + mAdsIndexer.getTotalBytes();

        if (totalBytes == 0) {
            return (byte) 100;
        } else {
            return (byte) (persistedBytes / totalBytes * 100);
        }
    }

    private Long calculateDownloadSpeed() {
        long receivedBytes = 0; for (int sample : mSpeedSamples) {
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

    private void measureProgress() {
        mDownloadSpeed = calculateDownloadSpeed();
        mEstimatedTime = calculateEstimatedTime();
        mSyncedPercent = calculateSyncedPercent();

        syncerProgressChanged.setDownloadSpeed(mDownloadSpeed);
        syncerProgressChanged.setEstimatedTime(mEstimatedTime);

        EventBus.getDefault().postSticky(syncerProgressChanged);
        EventBus.getDefault().postSticky(new Events.SyncerSyncedPercentChanged(mSyncedPercent));
    }
}