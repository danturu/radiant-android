package fm.radiant.android.classes.indexer;

import android.util.Log;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableLong;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import fm.radiant.android.interfaces.Audioable;

public abstract class AbstractIndexer {
    private File directory;

    private Collection<? extends Audioable> queue;
    private Collection<? super Audioable> persistedQueue = new ArrayList();
    private Collection<? super Audioable> remotedQueue   = new ArrayList();

    private MutableLong persistedBytes = new MutableLong();
    private MutableLong remotedBytes   = new MutableLong();
    private MutableLong totalBytes     = new MutableLong();

    private boolean indexed;

    public AbstractIndexer(Collection<? extends Audioable> queue) {
        this.queue = queue;
    }

    public abstract File getDirectory();

    public abstract Class getModelClass();

    public abstract String getIndexerName();

    public abstract boolean isBalancedQueue();

    public void index() throws IOException {
        this.directory = getDirectory();

        for (Audioable model : queue) {
            File file = model.getFile(directory);

            try {
                if (isPersisted(model, file)) {
                    addToQueue(persistedQueue, persistedBytes, model);
                } else {
                    addToQueue(remotedQueue, remotedBytes, model);
                }
            } catch (IOException e) {
                addToQueue(remotedQueue, remotedBytes, model);
            }
        }

        indexed = true;
    }

    public void moveToPersisted(Audioable model, File file) throws IOException {
        int filesize = model.getAudio().getSize();

        remotedQueue.remove(model);
        remotedBytes.subtract(filesize);

        persistedQueue.add(model);
        persistedBytes.add(filesize);
    }

    public boolean isIndexed() {
        return indexed;
    }

    public Collection<? super Audioable> getPersistedQueue() {
        return persistedQueue;
    }

    public int getPersistedCount() {
        return persistedQueue.size();
    }

    public long getPersistedBytes() {
        return persistedBytes.longValue();
    }

    public Collection<? super Audioable> getRemotedQueue() {
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

    protected void addToQueue(Collection<? super Audioable> queue, MutableLong queueBytes, Audioable model) {
        int filesize = model.getAudio().getSize();

        queue.add(model);

        queueBytes.add(filesize);
        totalBytes.add(filesize);
    }

    protected boolean isPersisted(Audioable audio, File file) throws IOException {
        return file.exists();
    }
}

