package fm.radiant.android.lib.syncer;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.RateLimiter;

import org.apache.commons.lang.math.RandomUtils;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.indexer.AbstractIndexer;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.NetworkUtils;
import fm.radiant.android.utils.StorageUtils;

public class Syncer implements Download.OnProgressListener {
    private static final String TAG = Syncer.class.getSimpleName();

    public static final int STATE_SYNCED   = 1;
    public static final int STATE_STOPPED  = 2;
    public static final int STATE_IDLE     = 3;
    public static final int STATE_FAILED   = 4;
    public static final int STATE_INDEXING = 5;
    public static final int STATE_SYNCING  = 6;

    private static final String PROPERTY_STATE          = "state";
    private static final String PROPERTY_ERROR_CODE     = "error_code";
    private static final String PROPERTY_SYNCED_PERCENT = "synced_percent";
    private static final String PROPERTY_ESTIMATED_TIME = "estimated_time";
    private static final String PROPERTY_DOWNLOAD_SPEED = "download_speed";

    private static final int ERROR_NO_STORAGE  = 1;
    private static final int ERROR_NO_SPACE    = 2;
    private static final int ERROR_NO_INTERNET = 3;

    private final Context               mContext;
    private final LocalBroadcastManager mEventManager;

    private List<AbstractIndexer> mIndexers;

    private List<Download> mDownloads;
    private Download mCurrentDownload;

    private int mCurrentState = STATE_IDLE;
    private int mErrorCode;

    private byte mSyncedPercent = 0;
    private long mEstimatedTime = 0;
    private long mDownloadSpeed = 0;
    private long mReceivedBytes = 0;

    private List<Integer> mSpeedSamples = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
    private RateLimiter   mTrackLimiter = RateLimiter.create(1.0);

    private Object mLock = new Object();

    public Syncer(Context context) {
        mContext      = context;
        mEventManager = LocalBroadcastManager.getInstance(context);
    }

    public void setIndexers(AbstractIndexer... indexers) {
        mIndexers = Arrays.asList(indexers);
    }

    public void start() {
        try {
            index();
            fetch();

            stop(STATE_SYNCED);
        } catch (IOException exception) {
            Log.e(TAG, "An exception has occurred: ", exception);
        }
    }

    public void touch() {
        try {
            index();
            check();

            stop(STATE_STOPPED);
        } catch (IOException exception) {
            Log.e(TAG, "An exception has occurred: ", exception);
        }
    }

    public void stop() {
        stop(STATE_STOPPED);
    }

    public void stop(int state) {
        setState(state);

        if (mCurrentDownload != null) mCurrentDownload.abort();
    }

    public void stop(int state, int errorCode) {
        mErrorCode = errorCode;

        stop(state);
    }

    public Integer getState() {
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
        AbstractIndexer indexer = Iterables.find(mIndexers, new Predicate<AbstractIndexer>() {
            @Override
            public boolean apply(AbstractIndexer abstractIndexer) {
                return model.getClass() == abstractIndexer.getModelClass();
            }
        });

        indexer.moveToPersisted(model);
    }

    @Override
    public void onFailure(Download download, AudioModel model, final IOException exception) {
        Log.e(TAG, "Could not download audio(id=" + model.getStringId() + "): ", exception);
    }

    @Override
    public void onComplete(Download download, AudioModel model) {
        mReceivedBytes = 0;
    }

    @Override
    public void onProgress(Download download, AudioModel model, int receivedBytes, int totalBytes) {
        if (mTrackLimiter.tryAcquire()) {
            mSpeedSamples.remove(0); mSpeedSamples.add(receivedBytes - (int) mReceivedBytes);

            mReceivedBytes = receivedBytes;
            mSyncedPercent = calculateSyncedPercent();
            mDownloadSpeed = calculateDownloadSpeed();
            mEstimatedTime = calculateEstimatedTime();

            EventBus.getDefault().postSticky(new Events.SyncerProgressChanged(mSyncedPercent, mDownloadSpeed, mEstimatedTime));
        }
    }

    private void index() throws IOException {
        stop(STATE_INDEXING);
        Log.d("sdsdsd", "sdcsdcs");

        mDownloads = new ArrayList<Download>();
        int frozen = 0;
        int offset = 0;

        for(AbstractIndexer indexer: mIndexers) {
            indexer.index(); LibraryUtils.inspect(indexer);

            for (AudioModel model : indexer.getRemotedQueue()) {
                Download download = new Download(mContext, model, this);

                if (indexer.isFrontQueue()) {
                    offset = -1; frozen++;
                } else {
                    offset = mDownloads.size() - frozen > 0 ? RandomUtils.nextInt(mDownloads.size() - frozen) : 0;
                }

                mDownloads.add(offset + frozen, download);
            }
        }

        mSyncedPercent = calculateSyncedPercent();
        mDownloadSpeed = calculateDownloadSpeed();
        mEstimatedTime = calculateEstimatedTime();

        EventBus.getDefault().postSticky(new Events.SyncerProgressChanged(mSyncedPercent, mDownloadSpeed, mEstimatedTime));
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
            stop(STATE_FAILED, ERROR_NO_STORAGE);
        } else

        if (!StorageUtils.isFreeSpaceEnough()) {
            stop(STATE_FAILED, ERROR_NO_SPACE);
        } else

        if (!NetworkUtils.isNetworkConnected()) {
            stop(STATE_IDLE, ERROR_NO_INTERNET);
        }
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (mCurrentState != STATE_SYNCING) throw new InterruptedIOException();
    }

    private void setState(int state) {
        mCurrentState = state;

        EventBus.getDefault().postSticky(new Events.SyncerStateChanged(mCurrentState, mErrorCode));
        mSyncedPercent = calculateSyncedPercent();
        EventBus.getDefault().postSticky(new Events.SyncerProgressChanged(mSyncedPercent, mDownloadSpeed, mEstimatedTime));
    }

    private Byte calculateSyncedPercent() {
        double persistedCount = 0;
        double totalCount     = 0;

        for (AbstractIndexer indexer : mIndexers) {
            persistedCount += indexer.getPersistedCount();
            totalCount     += indexer.getTotalCount();
        }

        if (totalCount == 0) {
            return 100;
        } else {
            return (byte) (persistedCount / totalCount * 100);
        }
    }

    private Long calculateDownloadSpeed() {
        int receivedBytes = 0; for (int sample : mSpeedSamples) {
            receivedBytes += sample;
        }

        return (long) receivedBytes / mSpeedSamples.size();
    }

    private Long calculateEstimatedTime() {
        int remotedBytes = 0; for (AbstractIndexer indexer : mIndexers) {
            remotedBytes += indexer.getRemotedBytes();
        }

        if (mDownloadSpeed <= 1) {
            return (long) -1;
        } else {
            return (remotedBytes + mReceivedBytes) / mDownloadSpeed;
        }
    }
}