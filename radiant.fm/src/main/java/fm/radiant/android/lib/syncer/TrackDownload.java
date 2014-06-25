package fm.radiant.android.lib.syncer;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import fm.radiant.android.Config;
import fm.radiant.android.models.Track;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.CryptUtils;

public class TrackDownload extends AbstractDownload {
    private final Context mContext;
    private final Track   mTrack;

    public TrackDownload(Context context, Track track, OnProgressListener event) {
        super(context, track, event);

        mContext = context;
        mTrack   = track;
    }

    boolean play = true;
    @Override
    protected File download() throws IOException {
        File tempFile = null; URL url = null; URLConnection connection = null; BufferedInputStream inputStream = null; OutputStream outputStream = null;

        /*
            Cipher cipher= Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, CiferKey);

                        byte[] buffer=new byte[BUFFER];

                        FileInputStream fin=new FileInputStream(fileIn);
                        BufferedInputStream bufin = new BufferedInputStream(fin, BUFFER);

                        FileOutputStream fout = new FileOutputStream(fileOut);
                        CipherOutputStream sout = new CipherOutputStream(fout, cipher);

                        int iCount;
                        while ((iCount = bufin.read(buffer, 0, BUFFER)) != -1) {
                                sout.write(buffer, 0, iCount);
                        }
                        fin.close();
                        sout.close();
                        fout.close();
         */
        CipherOutputStream cos = null;
        try {
            tempFile = File.createTempFile("audio" + mModel.getStringId(), ".mp3", mContext.getExternalCacheDir());

            byte[] key = CryptUtils.getKey("test");

            url = new URL(mModel.getAudio().getURL());
            connection = url.openConnection();
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);
            connection.connect();

            inputStream  = new BufferedInputStream(url.openStream(), 4000);
            outputStream = new FileOutputStream(tempFile);
            cos = new CipherOutputStream(outputStream, CryptUtils.encryptor(key));

            byte data[] = new byte[1024]; int bytesRead = 0; int bytesWritten = 0; int contentLength = connection.getContentLength();

            while ((bytesRead = inputStream.read(data)) != -1) {
                throwOnInterrupt(); cos.write(data, 0, bytesRead);

                // mEvent.onDownloadProgress(this, mModel, bytesWritten += bytesRead, contentLength);
            }

            cos.flush();
            IOUtils.closeQuietly(cos);
            IOUtils.closeQuietly(outputStream);
            IOUtils.closeQuietly(inputStream);

            // decrypt start
            /*
            File tempFile2 = File.createTempFile("dec" + mModel.getStringId(), ".mp3", mContext.getExternalCacheDir());
            FileInputStream fin=new FileInputStream(tempFile);
            final CipherInputStream sin = new CipherInputStream(fin, CryptUtils.decryptor(key));

            final FileOutputStream fout = new FileOutputStream(tempFile2);

            while ((bytesRead = sin.read(data)) != -1) {
                fout.write(data, 0, bytesRead);
            }

            fout.flush();

            IOUtils.closeQuietly(sin);
            IOUtils.closeQuietly(fin);
            IOUtils.closeQuietly(fout);
            */
            // decrypt end

            /*
            new Thread(new Runnable() {
                byte data[] = new byte[1024]; int bytesRead = 0; int bytesWritten = 0;

                @Override
                public void run() {
                    try {
                        while ((bytesRead = sin.read(data)) != -1) {
                            fout.write(data, 0, bytesRead);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }); */

            //if (play) {
            //    play = false;
            //    MediaPlayer mediaPlayer = new MediaPlayer();
//
            //    mediaPlayer.reset();
            //    mediaPlayer.setDataSource(tempFile2.getAbsolutePath());
            //    mediaPlayer.prepare();
            //    mediaPlayer.start();
            //}

/*
            // decrypt
            FileInputStream fis = new FileInputStream(tempFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            FileOutputStream fos = new FileOutputStream(tempFile2);
            CipherOutputStream cos = new CipherOutputStream(fos, CryptUtils.encryptor(CryptUtils.getKey("test")));

            int read;
            while ((read = fis.read()) != -1)
            {
                cos.write(read);
                cos.flush();
            }
 */
            mEvent.onDownloadSuccess(this, mModel, tempFile);
        } catch (IOException exception) {
            mEvent.onDownloadFailure(this, mModel, exception);
            throw exception;
        } catch (GeneralSecurityException exception) {
            IOException ioException = new IOException(exception);

            mEvent.onDownloadFailure(this, mModel, ioException);
            throw ioException;
        } finally {
            // IOUtils.closeQuietly(cos);
            // IOUtils.closeQuietly(inputStream);
            // IOUtils.closeQuietly(outputStream);
        }

        return tempFile;
    }

    @Override
    protected void check(File tempFile) throws IOException {
    }

   /* @Override
    protected void store(File tempFile) throws IOException {
        try {
            HttpRequest request = HttpRequest.put(Config.API_ENDPOINT + "/downloads/" + mTrack.getDownloadId() + "/sign", true).basic(AccountUtils.getUUID(), AccountUtils.getPassword());

            if (request.ok()) {
                File encryptedFile = null;
                File audioFile = null;
                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;

                byte[] decryptedData = new byte[0];
                byte[] encryptedData = new byte[0];

                try {
                    encryptedFile = File.createTempFile("encrypted" + mModel.getStringId(), ".mp3", mContext.getExternalCacheDir());
                    audioFile     = mTrack.getFile(mContext);

                    inputStream = new FileInputStream(tempFile);
                    outputStream = new FileOutputStream(encryptedFile);

                    decryptedData = IOUtils.toByteArray(inputStream);
                    encryptedData = CryptUtils.encrypt(CryptUtils.getKey(CryptUtils.KEY), decryptedData);
                    outputStream.write(encryptedData);
                    outputStream.flush();

                    FileUtils.copyFile(encryptedFile, audioFile);

                    super.mEvent.onStoreSuccess(this, mTrack, audioFile);
                } catch (GeneralSecurityException exception) {
                    IOException ioException = new IOException(exception);

                    super.mEvent.onStoreFailure(this, mTrack, ioException);
                    throw ioException;
                } catch (IOException exception) {
                    super.mEvent.onStoreFailure(this, mTrack, exception);
                    throw exception;
                } finally {
                    decryptedData = null;
                    encryptedData = null;

                    FileUtils.deleteQuietly(encryptedFile);

                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(outputStream);

                    System.gc();
                }
            } else {
                IOException exception = new IOException("Could not sign download: " + request.code());

                super.mEvent.onStoreFailure(TrackDownload.this, mModel, exception);
                throw exception;
            }
        } catch (HttpRequest.HttpRequestException exception) {
            IOException ioException = new IOException(exception);

            super.mEvent.onStoreFailure(TrackDownload.this, mModel, ioException);
        }
    } */
}
/*
public boolean DecodeFile(String fileIn, String fileOut){
                try
                {
                        Cipher cipher=Cipher.getInstance("AES");
                        cipher.init(Cipher.DECRYPT_MODE, CiferKey);

                        byte[] buffer=new byte[BUFFER];

                        FileInputStream fin=new FileInputStream(fileIn);
                        CipherInputStream sin = new CipherInputStream(fin, cipher);

                        FileOutputStream fout = new FileOutputStream(fileOut);

                        int iCount;
                        while ((iCount = sin.read(buffer, 0, BUFFER)) != -1) {
                                fout.write(buffer, 0, iCount);
                        }

                        fin.close();
                        sin.close();
                        fout.close();
                }
                catch (Exception E)
                {
                        Log.v("Cipher", E.getMessage());
                        E.printStackTrace();
                        return false;
                }
                return true;
        }

 */