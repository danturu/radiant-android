package fm.radiant.android.lib.syncer;

import android.content.Context;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import fm.radiant.android.lib.TimeoutInputStream;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.utils.StorageUtils;

public class Download {
    private final Context mContext;

    private final AudioModel         mModel;
    private final OnProgressListener mEvent;

    private boolean mAborted = false;

    public Download(Context context, AudioModel model, OnProgressListener event) {
        mContext = context;
        mModel   = model;
        mEvent   = event;
    }

    public void start() throws InterruptedIOException {
        File tempFile = null; URL url = null; URLConnection connection = null; BufferedInputStream inputStream = null; OutputStream outputStream = null;

        try {
            throwOnInterrupt();

            tempFile = File.createTempFile(mModel.getStringId(), ".mp3", mContext.getExternalCacheDir());

            url = new URL(mModel.getAudio().getURL());
            connection = url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();

            inputStream  = new BufferedInputStream(url.openStream(), 4000);
            outputStream = new FileOutputStream(tempFile);

            byte data[] = new byte[1024]; int bytesRead = 0; int bytesWritten = 0; int contentLength = connection.getContentLength();

            while ((bytesRead = inputStream.read(data)) != -1) {
                throwOnInterrupt(); outputStream.write(data, 0, bytesRead);

                mEvent.onProgress(this, mModel, bytesWritten += bytesRead, contentLength);
            }

            outputStream.flush();

            mEvent.onSuccess(this, mModel, storeFile(tempFile));
        } catch (IOException exception) {
            mEvent.onFailure(this, mModel, exception);
        } finally {
            FileUtils.deleteQuietly(tempFile);

            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);

            mEvent.onComplete(this, mModel);
        }
    }

    public void abort() {
        mAborted = true;
    }

    private File storeFile(File tempFile) throws IOException {
        // if (!mModel.getAudio().getHash().equals(StorageUtils.md5(tempFile))) {
        //     throw new IOException("Invalid checksum, download could be corrupted.");
        // }

        File destinationFile = mModel.getFile(mContext);
        FileUtils.copyFile(tempFile, destinationFile);

        return destinationFile;
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (mAborted) throw new InterruptedIOException();
    }

    public interface OnProgressListener {
        public void onSuccess(Download download, AudioModel model, File file);

        public void onFailure(Download download, AudioModel model, IOException exception);

        public void onComplete(Download download, AudioModel model);

        public void onProgress(Download download, AudioModel model, int receivedBytes, int totalBytes);
    }
}