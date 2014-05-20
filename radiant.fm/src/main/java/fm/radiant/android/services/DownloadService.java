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

    private boolean isInterrupted = false;
    private Syncer syncer;

    public DownloadService() {
        super("IntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (syncer != null) return;

        Log.d(TAG, "new syncer");

        Place place = AccountUtils.getCurrentPlace();

        TracksIndexer tracksIndexer = new TracksIndexer(getApplicationContext(), place.getTracks());
        AdsIndexer adsIndexer       = new AdsIndexer(getApplicationContext(), place.getAds());

        LibraryUtils.setTracksIndexer(tracksIndexer);
        LibraryUtils.setAdsIndexer(adsIndexer);
        Log.d(TAG, "new syncer 1");

        this.syncer = new Syncer(getApplicationContext(), tracksIndexer, adsIndexer);

        syncer.sync();
        LibraryUtils.setSyncer(syncer);

        stopSelf();
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