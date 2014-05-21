package fm.radiant.android.classes.indexer;

import android.content.Context;

import org.apache.commons.lang.mutable.MutableLong;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fm.radiant.android.interfaces.Audioable;

public abstract class AbstractIndexer {
    private Context context;

    private List<? extends Audioable> queue;
    private List<Audioable> persistedQueue = new ArrayList<Audioable>();
    private List<Audioable> remotedQueue   = new ArrayList<Audioable>();

    private MutableLong persistedBytes = new MutableLong();
    private MutableLong remotedBytes   = new MutableLong();
    private MutableLong totalBytes     = new MutableLong();

    private boolean indexed;

    public AbstractIndexer(Context context, List<? extends Audioable> queue) {
        this.context = context;
        this.queue   = queue;
    }

    public abstract Class getModelClass();

    public abstract boolean isFrontQueue();

    public void index() throws IOException {
        for (Audioable model : queue) {
            try {
                if (isAudioExists(model)) {
                    addToQueue(persistedQueue, persistedBytes, model);
                } else {
                    addToQueue(remotedQueue, remotedBytes, model);
                }
            } catch (IOException ignored) {
                addToQueue(remotedQueue, remotedBytes, model);
            }
        }

        indexed = true;
    }

    public void moveToPersisted(Audioable model) throws IOException {
        int filesize = model.getAudio().getSize();

        remotedQueue.remove(model);
        remotedBytes.subtract(filesize);

        persistedQueue.add(model);
        persistedBytes.add(filesize);
    }

    public boolean isIndexed() {
        return indexed;
    }

    public List<? extends Audioable> getPersistedQueue() {
        return persistedQueue;
    }

    public int getPersistedCount() {
        return persistedQueue.size();
    }

    public long getPersistedBytes() {
        return persistedBytes.longValue();
    }

    public List<? extends Audioable> getRemotedQueue() {
        return remotedQueue;
    }

    public int getRemotedCount() {
        return remotedQueue.size();
    }

    public long getRemotedBytes() {
        return remotedBytes.longValue();
    }

    public int getTotalCount() {
        return queue.size();
    }

    public long getTotalBytes() {
        return totalBytes.longValue();
    }

    protected void addToQueue(List<Audioable> queue, MutableLong queueBytes, Audioable model) {
        int filesize = model.getAudio().getSize();

        queue.add(model);

        queueBytes.add(filesize);
        totalBytes.add(filesize);
    }

    protected boolean isAudioExists(Audioable model) throws IOException {
        return model.getFile(context).exists();
    }
}

