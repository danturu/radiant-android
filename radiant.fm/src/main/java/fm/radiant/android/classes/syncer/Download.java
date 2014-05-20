package fm.radiant.android.classes.syncer;

import android.content.Context;
import android.util.Log;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import fm.radiant.android.interfaces.Audioable;
import fm.radiant.android.interfaces.DownloadEventListener;
import fm.radiant.android.lib.TimeoutInputStream;

public class Download {
    private static final String TAG = "Download";

    private Context context;
    private Audioable model;
    private File directory;
    private DownloadEventListener downloadEventListener;

    private boolean aborted = false;

    public Download(Context context, Audioable model, File directory, DownloadEventListener downloadEventListener) {
        this.context               = context;
        this.model                 = model;
        this.directory             = directory;
        this.downloadEventListener = downloadEventListener;
    }

    public void start() {
        File tempFile = null; URL url = null; URLConnection connection = null; TimeoutInputStream inputStream = null; OutputStream outputStream = null;

        try {
            throwOnInterrupt();

            tempFile = File.createTempFile(model.getStringId(), ".mp3", context.getExternalCacheDir());

            url = new URL(model.getAudio().getURL());
            connection = url.openConnection();

            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();

            inputStream  = new TimeoutInputStream(url.openStream(), 4000);
            outputStream = new FileOutputStream(tempFile);

            byte data[] = new byte[1024]; int bytesRead = 0; int bytesWritten = 0;

            while ((bytesRead = inputStream.read(data)) != -1) {
                throwOnInterrupt();
                outputStream.write(data, 0, bytesRead);

                downloadEventListener.onProgress(model, bytesWritten += bytesRead, connection.getContentLength());
            }

            outputStream.flush();

            downloadEventListener.onSuccess(model, storeFile(tempFile));
        } catch (IOException exception) {
            downloadEventListener.onFailure(model, exception);
        } finally {
            FileUtils.deleteQuietly(tempFile);

            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);

            downloadEventListener.onComplete(model);
        }
    }

    public void abort() {
        this.aborted = true;
    }

    private File storeFile(File sourceFile) throws IOException {
        String hash = new String(Hex.encodeHex(DigestUtils.md5(FileUtils.readFileToByteArray(sourceFile))));

        if (!model.getAudio().getHash().equals(hash)) {
            throw new IOException("Invalid checksum, download could be corrupted.");
        }

        File destinationFile = model.getFile(directory);
        FileUtils.copyFile(sourceFile, destinationFile);

        return destinationFile;
    }

    private void throwOnInterrupt() throws InterruptedIOException {
        if (aborted) throw new InterruptedIOException();
    }
}