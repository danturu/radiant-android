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
import fm.radiant.android.interfaces.Audioable;
import fm.radiant.android.interfaces.DownloadEventListener;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.NetworkUtils;
import fm.radiant.android.utils.StorageUtils;

public class Syncer implements DownloadEventListener{
    private static final String TAG = "Syncer";

    public static final int STATE_SYNCED   = 1;
    public static final int STATE_STOPPED  = 2;
    public static final int STATE_IDLE     = 3;
    public static final int STATE_FAILED   = 4;
    public static final int STATE_INDEXING = 5;
    public static final int STATE_SYNCING  = 6;

    private int currentState = STATE_IDLE;

    private static final String PROPERTY_STATE          = "state";
    private static final String PROPERTY_SYNCED_PERCENT = "synced_percent";
    private static final String PROPERTY_ESTIMATED_TIME = "estimated_time";
    private static final String PROPERTY_DOWNLOAD_SPEED = "download_speed";
    private static final String PROPERTY_ERROR_CODE     = "error_code";

    private static final int ERROR_NO_STORAGE  = 1;
    private static final int ERROR_NO_SPACE    = 2;
    private static final int ERROR_NO_INTERNET = 3;

    private final Context context;

    private List<AbstractIndexer> indexers;

    private List<Download> downloads;
    private Download currentDownload;

    private Integer syncedPercent = 0;
    private Integer estimatedTime = 0;
    private Integer downloadSpeed = 0;
    private Integer receivedBytes = 0;
    private Integer errorCode;

    private List<Integer> speedSamples = new ArrayList<Integer>(Arrays.asList(0, 0, 0, 0, 0));
    private RateLimiter   trackLimiter = RateLimiter.create(1.0);

    public Syncer(Context context) {
        this.context = context;
    }

    public void setIndexers(AbstractIndexer... indexers) {
        this.indexers = Arrays.asList(indexers);
    }

    public void sync() {
        try {
            index();
            start();

            stop(STATE_SYNCED);
        } catch (IOException e) {
            Log.e(TAG, "An exception has occurred: ", e);
        }
    }

    public void stop(int state, int errorCode) {
        this.errorCode = errorCode;

        stop(state);
    }

    public void stop(int state) {
        setState(state);

        if (currentDownload != null) currentDownload.abort();
    }

    public int getState() {
        return currentState;
    }

    @Override
    public void onSuccess(final Audioable model, File file) {
        AbstractIndexer indexer = Iterables.find(indexers, new Predicate<AbstractIndexer>() {
            @Override
            public boolean apply(AbstractIndexer abstractIndexer) {
                return model.getClass() == abstractIndexer.getModelClass();
            }
        });

        try {
            indexer.moveToPersisted(model);
        } catch (IOException e) {
            Log.d(TAG, indexer.getClass().getSimpleName() + " must be reindexed: ", e);
        }
    }

    @Override
    public void onFailure(Audioable model, final IOException exception) {
        Log.e(TAG, "Could not download audio(id=" + model.getStringId() + "): ", exception);
    }

    @Override
    public void onComplete(Audioable model) {
        this.receivedBytes = 0;
    }

    @Override
    public void onProgress(Audioable model, int receivedBytes, int totalBytes) {
        if (trackLimiter.tryAcquire()) {
            speedSamples.remove(0);
            speedSamples.add(receivedBytes - this.receivedBytes);

            this.receivedBytes = receivedBytes;
            this.syncedPercent = calculateSyncedPercent();
            this.downloadSpeed = calculateDownloadSpeed();
            this.estimatedTime = calculateEstimatedTime();

            sendBroadcast();
        }
    }

    private void index() throws IOException {
        setState(STATE_INDEXING);

        downloads  = new ArrayList<Download>();
        int frozen = 0;
        int offset = 0;

        for(AbstractIndexer indexer: indexers) {
            indexer.index();
            LibraryUtils.inspect(indexer);

            for (Audioable model : indexer.getRemotedQueue()) {
                Download download = new Download(context, model, this);

                if (indexer.isFrontQueue()) {
                    offset = -1; frozen++;
                } else {
                    offset = downloads.size() - frozen > 0 ? RandomUtils.nextInt(downloads.size() - frozen) : 0;
                }

                downloads.add(offset + frozen, download);
            }
        }
    }

    private void start() throws IOException {
        setState(STATE_SYNCING);

        for (Download download : downloads) {
            checkRequirements(); throwOnInterrupt();

            (currentDownload = download).start();
        }
    }

    private void setState(int state) {
        this.currentState = state;
        sendBroadcast();
    }

    private void checkRequirements() {
        if (!StorageUtils.isExternalStorageWritable()) {
            stop(STATE_FAILED, ERROR_NO_STORAGE);
        } else

        if (!StorageUtils.isFreeSpaceEnough()) {
            stop(STATE_FAILED, ERROR_NO_SPACE);
        } else

        if (!NetworkUtils.isNetworkConnected()) {
            stop(STATE_IDLE, ERROR_NO_INTERNET);
        }

        if (currentState != STATE_SYNCING) {
            Log.d(TAG, "must be stopped: " + Integer.valueOf(errorCode));
        }
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (currentState != STATE_SYNCING) throw new InterruptedIOException();
    }

    private void sendBroadcast() {
        Intent intent = new Intent(Radiant.INTENT_SYNCER_STATE_CHANGED);

        // base...

        intent.putExtra(PROPERTY_STATE, currentState);

        // advanced...

        intent.putExtra(PROPERTY_SYNCED_PERCENT, syncedPercent);
        intent.putExtra(PROPERTY_ESTIMATED_TIME, downloadSpeed);
        intent.putExtra(PROPERTY_DOWNLOAD_SPEED, estimatedTime);
        intent.putExtra(PROPERTY_ERROR_CODE,     errorCode);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private Integer calculateSyncedPercent() {
        double persistedCount = 0;
        double totalCount     = 0;

        for (AbstractIndexer indexer : indexers) {
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
        int receivedBytes = 0; for (int sample : speedSamples) {
            receivedBytes += sample;
        }

        return receivedBytes / speedSamples.size();
    }

    private Integer calculateEstimatedTime() {
        int remotedBytes = 0; for (AbstractIndexer indexer : indexers) {
            remotedBytes += indexer.getRemotedBytes();
        }

        if (downloadSpeed == 0) {
            return Integer.MAX_VALUE;
        } else {
            return (remotedBytes + receivedBytes) / downloadSpeed;
        }
    }
}