package fm.radiant.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;

import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.classes.cleaner.AdsCleaner;
import fm.radiant.android.classes.cleaner.TracksCleaner;
import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.classes.player.Player;
import fm.radiant.android.classes.syncer.Syncer;
import fm.radiant.android.fragments.PlayerFragment;
import fm.radiant.android.models.Place;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.tasks.CleanTask;
import fm.radiant.android.tasks.SubscribeTask;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

public class MainActivity extends ActionBarActivity {
    private BroadcastReceiver mPlaceChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onPlaceChanged();
        }
    };

    public static int getContentViewCompat() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? R.id.action_bar_activity_content : android.R.id.content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AccountUtils.isLoggedIn()) {
            startLoginActivity(); return;
        }

        getSupportActionBar().setDisplayShowHomeEnabled(false);
        setContentView(R.layout.activity_main);

        if (MessagesUtils.canReceiveMessages(this)) {
            new SubscribeTask().execute();
            new SyncTask().execute();
        }

        onPlaceChanged();

        // EVENTS

        LocalBroadcastManager mBroadcastManager = LocalBroadcastManager.getInstance(this);

        mBroadcastManager.registerReceiver(mPlaceChangedReceiver, new IntentFilter(Radiant.INTENT_PLACE_CHANGED));
    }

    @Override
    protected void onResume() {
        super.onResume();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // EVENTS

        LocalBroadcastManager mBroadcastManager = LocalBroadcastManager.getInstance(this);

        try { mBroadcastManager.unregisterReceiver(mPlaceChangedReceiver); } catch (IllegalArgumentException ignored) {};
    }

    @Override
    public boolean onSupportNavigateUp() {
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PlayerFragment.TAG);

        if (fragment != null && fragment.isVisible()) switch (keyCode) {
            case KEYCODE_VOLUME_UP:
                LibraryUtils.getPlayer().adjustVolume(AudioManager.ADJUST_RAISE);
                return true;

            case KEYCODE_VOLUME_DOWN:
                LibraryUtils.getPlayer().adjustVolume(AudioManager.ADJUST_LOWER);
                return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag("player");

        if (fragment != null && fragment.isVisible()) switch (keyCode) {
            case KEYCODE_VOLUME_UP:
                return true;

            case KEYCODE_VOLUME_DOWN:
                return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void onPlaceChanged() {
        Place place = AccountUtils.getPlace();

        new CleanTask().execute(new TracksCleaner(this, place.getTracks()), new AdsCleaner(this, place.getAds()));

        TracksIndexer tracksIndexer = new TracksIndexer(this, place.getTracks());
        AdsIndexer    adsIndexer    = new AdsIndexer(this, place.getAds());

        final Syncer syncer = LibraryUtils.getSyncer();
        syncer.setIndexers(tracksIndexer, adsIndexer);

        if (syncer.getState() != Syncer.STATE_STOPPED) {
            Intent service = new Intent(getApplicationContext(), DownloadService.class);
            stopService(service);
            startService(service);
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    syncer.touch();
                }
            });
        }

        Player player = LibraryUtils.getPlayer();
        player.setAdsIndexer(adsIndexer);
        player.setTracksIndexer(tracksIndexer);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
    }
}