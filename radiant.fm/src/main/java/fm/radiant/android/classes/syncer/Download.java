package fm.radiant.android.classes.syncer;

import android.content.Context;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import fm.radiant.android.interfaces.DownloadEventListener;
import fm.radiant.android.lib.TimeoutInputStream;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.utils.StorageUtils;

public class Download {
    private final Context mContext;

    private final AudioModel            mModel;
    private final DownloadEventListener mEvent;

    private boolean mAborted = false;

    public Download(Context context, AudioModel model, DownloadEventListener event) {
        mContext = context;
        mModel   = model;
        mEvent   = event;
    }

    public void start() {
        File tempFile = null; URL url = null; URLConnection connection = null; TimeoutInputStream inputStream = null; OutputStream outputStream = null;

        try {
            throwOnInterrupt();

            tempFile = File.createTempFile(mModel.getStringId(), ".mp3", mContext.getExternalCacheDir());

            url = new URL(mModel.getAudio().getURL());
            connection = url.openConnection();

            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();

            inputStream  = new TimeoutInputStream(url.openStream(), 4000);
            outputStream = new FileOutputStream(tempFile);

            byte data[] = new byte[1024]; int bytesRead = 0; int bytesWritten = 0;

            while ((bytesRead = inputStream.read(data)) != -1) {
                throwOnInterrupt(); outputStream.write(data, 0, bytesRead);

                mEvent.onProgress(this, mModel, bytesWritten += bytesRead, connection.getContentLength());
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
        if (!mModel.getAudio().getHash().equals(StorageUtils.md5(tempFile))) {
            throw new IOException("Invalid checksum, download could be corrupted.");
        }

        File destinationFile = mModel.getFile(mContext);
        FileUtils.copyFile(tempFile, destinationFile);

        return destinationFile;
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (mAborted) throw new InterruptedIOException();
    }
}