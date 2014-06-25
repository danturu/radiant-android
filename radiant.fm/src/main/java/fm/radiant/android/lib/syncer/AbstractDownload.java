package fm.radiant.android.lib.syncer;

import android.content.Context;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

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
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fm.radiant.android.lib.CorruptedFileException;
import fm.radiant.android.models.AudioModel;

public abstract class AbstractDownload {
    public static final String TAG = "Download";

    private final Context mContext;

    protected final AudioModel         mModel;
    protected final OnProgressListener mEvent;

    private boolean mAborted = false;

    public AbstractDownload(Context context, AudioModel model, OnProgressListener event) {
        mContext = context;
        mModel   = model;
        mEvent   = event;
    }

    public void start() {
        File tempFile = null;

        try {
            tempFile = download();

            check(tempFile);
            store(tempFile);
        } catch (IOException exception) {
            Log.e(TAG, "Could not download " + mModel.getClass().getSimpleName() + " (id=" + mModel.getStringId() + "): ", exception);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }
    }

    public void abort() {
        mAborted = true;
    }

    protected File download() throws IOException {
        File tempFile = null; URL url = null; URLConnection connection = null; BufferedInputStream inputStream = null; OutputStream outputStream = null;

        try {
            tempFile = File.createTempFile("audio" + mModel.getStringId(), ".mp3", mContext.getExternalCacheDir());

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

                mEvent.onDownloadProgress(this, mModel, bytesWritten += bytesRead, contentLength);
            }

            outputStream.flush();

            mEvent.onDownloadSuccess(this, mModel, tempFile);
        } catch (IOException exception) {
            mEvent.onDownloadFailure(this, mModel, exception); throw exception;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return tempFile;
    }

    protected void check(File tempFile) throws IOException {
        if (mModel.getAudio().getSize() == tempFile.length()) {
            mEvent.onCheckSuccess(this, mModel, tempFile);
        } else {
            CorruptedFileException exception = new CorruptedFileException("Invalid checksum");

            mEvent.onCheckFailure(this, mModel, exception); throw exception;
        }
    }

    protected void store(File tempFile) throws IOException {
        File audioFile = mModel.getFile(mContext);

        try {
            FileUtils.copyFile(tempFile, audioFile);

            mEvent.onStoreSuccess(this, mModel, audioFile);
        } catch (IOException exception) {
            mEvent.onStoreFailure(this, mModel, exception);
        }
    }

    protected void throwOnInterrupt() throws InterruptedIOException {
        if (mAborted) throw new InterruptedIOException();
    }

    public interface OnProgressListener {
        public void onDownloadSuccess(AbstractDownload download, AudioModel model, File tempFile);


        public void onDownloadProgress(AbstractDownload download, AudioModel model, int receivedBytes, int totalBytes);

        public void onCheckSuccess(AbstractDownload download, AudioModel model, File tempFile);

        public void onCheckFailure(AbstractDownload download, AudioModel model, IOException exception);

        public void onStoreSuccess(AbstractDownload download, AudioModel model, File audioFile);

        public void onStoreFailure(AbstractDownload download, AudioModel model, IOException exception);
    }
}