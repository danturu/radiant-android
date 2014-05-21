package fm.radiant.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.classes.syncer.Syncer;
import fm.radiant.android.models.Place;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.LibraryUtils;

public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";

    private boolean isStopped = false;

    public DownloadService() {
        super("IntentService");
    }

    @Override
    public void onDestroy() {
        this.isStopped = true;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isStopped) return;
        Log.d(TAG, "1");

        buildIndexers(); buildSyncer();
        Log.d(TAG, "2");

        LibraryUtils.getSyncer().sync();
        Log.d(TAG, "3");

        stopSelf();
    }

    private void buildIndexers() {
        Place place = AccountUtils.getCurrentPlace();
        Context context = getApplicationContext();

        LibraryUtils.setTracksIndexer(new TracksIndexer(context, place.getTracks()));
        LibraryUtils.setAdsIndexer(new AdsIndexer(context, place.getAds()));
    }

    private void buildSyncer() {
        Syncer syncer = new Syncer(getApplicationContext());
        syncer.setIndexers(LibraryUtils.getTracksIndexer(), LibraryUtils.getAdsIndexer());
        LibraryUtils.setSyncer(syncer);
    }
}


/*
private boolean isMyServiceRunning() {
    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
        if (MyService.class.getName().equals(service.service.getClassName())) {
            return true;
        }
    }
    return false;
 */