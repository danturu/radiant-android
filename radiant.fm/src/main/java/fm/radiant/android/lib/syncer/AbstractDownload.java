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

import fm.radiant.android.lib.CorruptedFileException;
import fm.radiant.android.models.AudioModel;

public abstract class AbstractDownload {
    public static final String TAG = "Download";

    private final Context mContext;

    protected final AudioModel         mModel;
    protected final OnProgressListener mEvent;
    protected File mFile;
    private boolean mAborted = false;

    public AbstractDownload(Context context, AudioModel model, OnProgressListener event) {
        mContext = context;
        mModel   = model;
        mEvent   = event;
        mFile    = mModel.getFile(context);
    }

    public void start() {
        File tempFile = null;

        try {
            tempFile = download();

            check(tempFile);
            store(tempFile);

            mEvent.onSuccess(this, mModel, mFile);
        } catch (IOException exception) {
            mEvent.onFailure(this, mModel, exception);
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }

        mEvent.onComplete(this, mModel);
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

                mEvent.onProgress(this, mModel, bytesWritten += bytesRead, contentLength);
            }

            outputStream.flush();
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return tempFile;
    }

    protected void check(File tempFile) throws IOException {
        if (mModel.getAudio().getSize() != tempFile.length()) {
            throw new CorruptedFileException();
        }
    }

    protected void store(File tempFile) throws IOException {
        FileUtils.copyFile(tempFile, mFile);
    }

    protected void throwOnInterrupt() throws InterruptedIOException {
        if (mAborted) throw new InterruptedIOException();
    }

    public interface OnProgressListener {
        public void onSuccess(AbstractDownload download, AudioModel model, File tempFile);

        public void onFailure(AbstractDownload download, AudioModel model, IOException exception);

        public void onComplete(AbstractDownload download, AudioModel model);

        public void onProgress(AbstractDownload download, AudioModel model, int receivedBytes, int totalBytes);
    }
}