package fm.radiant.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fm.radiant.android.Radiant;
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

        LibraryUtils.setTracksIndexer(new TracksIndexer(getApplicationContext(), place.getTracks()));
        LibraryUtils.setAdsIndexer(new AdsIndexer(getApplicationContext(), place.getAds()));
    }

    private void buildSyncer() {
        LibraryUtils.setSyncer(new Syncer(getApplicationContext(), LibraryUtils.getTracksIndexer(), LibraryUtils.getAdsIndexer()));
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