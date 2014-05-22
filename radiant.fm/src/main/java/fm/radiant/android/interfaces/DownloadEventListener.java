package fm.radiant.android.interfaces;

import java.io.File;
import java.io.IOException;

import fm.radiant.android.classes.syncer.Download;

public interface DownloadEventListener {
    public void onSuccess(Download download, AudioModel model, File file);

    public void onFailure(Download download, AudioModel model, IOException exception);

    public void onComplete(Download download, AudioModel model);

    public void onProgress(Download download, AudioModel model, int receivedBytes, int totalBytes);
}