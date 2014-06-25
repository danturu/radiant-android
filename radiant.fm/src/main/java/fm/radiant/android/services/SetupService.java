package fm.radiant.android.services;

import android.app.IntentService;
import android.content.Intent;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.cleaner.AdsCleaner;
import fm.radiant.android.lib.cleaner.TracksCleaner;
import fm.radiant.android.lib.indexer.AdsIndexer;
import fm.radiant.android.lib.indexer.TracksIndexer;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.models.Place;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.utils.AccountUtils;

public class SetupService extends IntentService {
    private static final String TAG = SetupService.class.getSimpleName();
    private static boolean sComplete = false;

    private Player mPlayer = Player.getInstance();
    private Syncer mSyncer = Syncer.getInstance();

    public SetupService() {
        super(TAG);
    }

    public static boolean isComplete() {
        return sComplete;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        sComplete = false;

        Place place = AccountUtils.getPlace();

        // indexers

        TracksIndexer tracksIndexer = new TracksIndexer(getApplicationContext(), place.getTracks());
        AdsIndexer adsIndexer = new AdsIndexer(getApplicationContext(), place.getAds());

        tracksIndexer.index();
        adsIndexer.index();

        // syncer

        mSyncer.setIndexers(tracksIndexer, adsIndexer);

        if (mSyncer.getState() != Syncer.STATE_STOPPED) {
            mSyncer.startService();
        }

        // player

        mPlayer.setIndexers(tracksIndexer, adsIndexer);
        mPlayer.setPeriods(place.getPeriods());
        mPlayer.setCampaigns(place.getCampaigns());
        mPlayer.setSyncer(mSyncer);
        mPlayer.schedule();

        // setup complete

        sComplete = true;

        EventBus.getDefault().postSticky(new Events.PlaceChangedEvent(place));

        // cleaners

        new TracksCleaner(getApplicationContext(), place.getTracks()).clean();
        new AdsCleaner(getApplicationContext(), place.getAds()).clean();

        if (intent.getBooleanExtra("first", false)) new SyncTask().execute();
    }
}


