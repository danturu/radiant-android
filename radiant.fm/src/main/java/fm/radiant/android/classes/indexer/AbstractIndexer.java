package fm.radiant.android.classes.indexer;

import android.content.SharedPreferences;
import android.util.Log;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.mutable.MutableLong;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import fm.radiant.android.interfaces.Audioable;

public abstract class AbstractIndexer {
    private File directory;
    private SharedPreferences checksums;

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

    public abstract Class getModelClass();

    public abstract File getDirectory();

    public abstract boolean shouldBeShuffled();

    public void index() throws IOException {
        this.checksums = getChecksums();
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
        inspect(getClass().getName());
    }

    public void inspect(String tag) {
        String[] counts = new String[] {
                StringUtils.leftPad(Integer.toString(persistedQueue.size()), 8),
                StringUtils.leftPad(Integer.toString(remotedQueue.size()),   8),
                StringUtils.leftPad(Integer.toString(queue.size()),          8),
        };

        String[] sizes = new String[] {
                StringUtils.leftPad(persistedBytes.toString(), 12),
                StringUtils.leftPad(remotedBytes.toString(),   12),
                StringUtils.leftPad(totalBytes.toString(),     12),
        };

        Log.i(tag, "+===+=========+==============+");
        Log.i(tag, "|   |    Count |        Size |");
        Log.i(tag, "+===+=========+==============+");
        Log.i(tag, "| P | " + counts[0]  + " | " + sizes[0] + "|");
        Log.i(tag, "| R | " + counts[1]  + " | " + sizes[1] + "|");
        Log.i(tag, "| T | " + counts[2]  + " | " + sizes[2] + "|");
        Log.i(tag, "+===+========================+");
    }

    public void moveToPersisted(Audioable model, File file) throws IOException {
        storeChecksum(file.getName(), FileUtils.checksumCRC32(file));

        //new String(Hex.encodeHex(DigestUtils.md5(FileUtils.readFileToString())));
        // remove from remoted...

        remotedQueue.remove(model);
        remotedBytes.subtract(model.getFilesize());

        // add to stored...

        persistedQueue.add(model);
        persistedBytes.add(model.getFilesize());
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
        return persistedBytes.intValue();
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

    protected abstract SharedPreferences getChecksums();

    protected void addToQueue(Collection<? super Audioable> queue, MutableLong queueBytes, Audioable model) {
        queue.add(model);

        queueBytes.add(model.getFilesize());
        totalBytes.add(model.getFilesize());
    }

    protected boolean isPersisted(Audioable audio, File file) throws IOException {
        return file.exists() && FileUtils.checksumCRC32(file) == getChecksum(file.getName());
    }

    protected void storeChecksum(String filename, long checksum) {
        checksums.edit().putLong(filename, checksum).commit();
    }

    protected long getChecksum(String filename) {
        return checksums.getLong(filename, 0);
    }
}

