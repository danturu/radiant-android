package fm.radiant.android.lib.indexer;

import android.content.Context;

import org.apache.commons.lang.mutable.MutableLong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fm.radiant.android.models.AudioModel;

public abstract class AbstractIndexer {
    private Context mContext;

    private List<? extends AudioModel> mQueue;
    private List<AudioModel> mPersistedQueue = new ArrayList<AudioModel>();
    private List<AudioModel> mRemotedQueue   = new ArrayList<AudioModel>();

    private MutableLong mPersistedBytes = new MutableLong();
    private MutableLong mRemotedBytes   = new MutableLong();
    private MutableLong mTotalBytes     = new MutableLong();

    private boolean mIndexed = false;

    public AbstractIndexer(Context context, List<? extends AudioModel> queue) {
        mContext = context;
        mQueue   = queue;
    }

    public abstract Class getModelClass();

    public abstract boolean isFrontQueue();

    public synchronized void index() {
        if (mIndexed) return;

        for (AudioModel model : mQueue) {
            try {
                if (isAudioExists(model)) {
                    addToQueue(mPersistedQueue, mPersistedBytes, model);
                    onPersistentModel(model);
                } else {
                    addToQueue(mRemotedQueue, mRemotedBytes, model);
                }
            } catch (IOException ignored) {
                addToQueue(mRemotedQueue, mRemotedBytes, model);
            }
        }

        mIndexed = true;
    }

    public synchronized void moveToPersisted(AudioModel model) {
        int filesize = model.getAudio().getSize();

        mRemotedQueue.remove(model);
        mRemotedBytes.subtract(filesize);

        mPersistedQueue.add(model);
        mPersistedBytes.add(filesize);

        onPersistentModel(model);
    }

    public List<? extends AudioModel> getPersistedQueue() {
        return mPersistedQueue;
    }

    public int getPersistedCount() {
        return mPersistedQueue.size();
    }

    public long getPersistedBytes() {
        return mPersistedBytes.longValue();
    }

    public List<? extends AudioModel> getRemotedQueue() {
        return mRemotedQueue;
    }

    public int getRemotedCount() {
        return mRemotedQueue.size();
    }

    public long getRemotedBytes() {
        return mRemotedBytes.longValue();
    }

    public List<? extends AudioModel> getQueue() {
        return mQueue;
    }

    public int getTotalCount() {
        return mQueue.size();
    }

    public long getTotalBytes() {
        return mTotalBytes.longValue();
    }

    protected abstract void onPersistentModel(AudioModel model);

    protected void addToQueue(List<AudioModel> queue, MutableLong queueBytes, AudioModel model) {
        int filesize = model.getAudio().getSize();

        queue.add(model);

        queueBytes.add(filesize);
        mTotalBytes.add(filesize);
    }

    protected boolean isAudioExists(AudioModel model) throws IOException {
        return model.getFile(mContext).exists();
    }
}

