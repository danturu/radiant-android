package fm.radiant.android.services;

import android.app.IntentService;
import android.content.Intent;

import fm.radiant.android.utils.LibraryUtils;

public class DownloadService extends IntentService {
    private boolean mStopped = false;

    public DownloadService() {
        super(DownloadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mStopped) return;

        LibraryUtils.getSyncer().start(); stopSelf();
    }

    @Override
    public void onDestroy() {
        mStopped = true;
    }
}
