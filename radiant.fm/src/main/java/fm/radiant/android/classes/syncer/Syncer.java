package fm.radiant.android.classes.syncer;

import android.content.Context;
import android.content.Intent;
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

import fm.radiant.android.Radiant;
import fm.radiant.android.classes.indexer.AbstractIndexer;
import fm.radiant.android.interfaces.DownloadEventListener;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.NetworkUtils;
import fm.radiant.android.utils.StorageUtils;

public class Syncer implements DownloadEventListener{
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

    private int mSyncedPercent = 0;
    private int mEstimatedTime = 0;
    private int mDownloadSpeed = 0;
    private int mReceivedBytes = 0;

    private List<Integer> mSpeedSamples = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
    private RateLimiter   mTrackLimiter = RateLimiter.create(1.0);

    public Syncer(Context context) {
        mContext      = context;
        mEventManager = LocalBroadcastManager.getInstance(context);
    }

    public void setIndexers(AbstractIndexer... indexers) {
        mIndexers = Arrays.asList(indexers);
    }

    public synchronized void start() {
        try {
            index();
            fetch();

            stop(STATE_SYNCED);
        } catch (IOException exception) {
            Log.e(TAG, "An exception has occurred: ", exception);
        }
    }

    public synchronized void touch() {
        try {
            index();
            check();

            stop(STATE_STOPPED);
        } catch (IOException exception) {
            Log.e(TAG, "An exception has occurred: ", exception);
        }
    }

    public synchronized void stop() {
        stop(STATE_STOPPED);
    }

    public synchronized void stop(int state) {
        setState(state);

        if (mCurrentDownload != null) mCurrentDownload.abort();
    }

    public synchronized void stop(int state, int errorCode) {
        mErrorCode = errorCode;

        stop(state);
    }

    public Integer getState() {
        return mCurrentState;
    }

    public Integer getSyncedPercent() {
        return mSyncedPercent;
    }

    public Integer getDownloadSpeed() {
        return mDownloadSpeed;
    }

    public Integer getEstimatedTime() {
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
            mSpeedSamples.remove(0); mSpeedSamples.add(receivedBytes - mReceivedBytes);

            mReceivedBytes = receivedBytes;
            mSyncedPercent = calculateSyncedPercent();
            mDownloadSpeed = calculateDownloadSpeed();
            mEstimatedTime = calculateEstimatedTime();

            sendProgressBroadcast();
        }
    }

    private void index() throws IOException {
        setState(STATE_INDEXING);

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
    }

    private void fetch() throws IOException {
        setState(STATE_SYNCING);

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

    private void sendStateBroadcast() {
        Intent intent = new Intent(Radiant.INTENT_SYNCER_STATE_CHANGED);

        intent.putExtra(PROPERTY_STATE,      mCurrentState);
        intent.putExtra(PROPERTY_ERROR_CODE, mErrorCode);

        mEventManager.sendBroadcast(intent);
    }

    private void sendProgressBroadcast() {
        Intent intent = new Intent(Radiant.INTENT_SYNCER_PROGRESS_CHANGED);

        intent.putExtra(PROPERTY_SYNCED_PERCENT, mSyncedPercent);
        intent.putExtra(PROPERTY_ESTIMATED_TIME, mDownloadSpeed);
        intent.putExtra(PROPERTY_DOWNLOAD_SPEED, mEstimatedTime);

        mEventManager.sendBroadcast(intent);
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (mCurrentState != STATE_SYNCING) throw new InterruptedIOException();
    }

    private void setState(int state) {
        mCurrentState = state; sendStateBroadcast();
    }

    private Integer calculateSyncedPercent() {
        double persistedCount = 0;
        double totalCount     = 0;

        for (AbstractIndexer indexer : mIndexers) {
            persistedCount += indexer.getPersistedCount();
            totalCount     += indexer.getTotalCount();
        }

        if (totalCount == 0) {
            return 100;
        } else {
            return (int) (persistedCount / totalCount * 100);
        }
    }

    private Integer calculateDownloadSpeed() {
        int receivedBytes = 0; for (int sample : mSpeedSamples) {
            receivedBytes += sample;
        }

        return receivedBytes / mSpeedSamples.size();
    }

    private Integer calculateEstimatedTime() {
        int remotedBytes = 0; for (AbstractIndexer indexer : mIndexers) {
            remotedBytes += indexer.getRemotedBytes();
        }

        if (mDownloadSpeed == 0) {
            return Integer.MAX_VALUE;
        } else {
            return (remotedBytes + mReceivedBytes) / mDownloadSpeed;
        }
    }
}