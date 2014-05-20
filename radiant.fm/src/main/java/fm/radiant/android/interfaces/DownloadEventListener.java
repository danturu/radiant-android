package fm.radiant.android.interfaces;

import java.io.File;
import java.io.IOException;

public interface DownloadEventListener {
    public void onSuccess(Audioable model, File file);

    public void onFailure(Audioable model, IOException exception);

    public void onComplete(Audioable model);

    public void onProgress(Audioable model, int receivedBytes, int totalBytes);
}