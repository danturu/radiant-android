package fm.radiant.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import de.greenrobot.event.EventBus;
import fm.radiant.android.lib.syncer.Syncer;

public class DownloadService extends IntentService {
    private static final String TAG = DownloadService.class.getSimpleName();

    private boolean mStopped = false;
    private Syncer mSyncer = Syncer.getInstance();

    public DownloadService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (mStopped) return;

        mSyncer.start(); stopSelf();
    }

    @Override
    public void onDestroy() {
        mStopped = true;
    }
}
