package fm.radiant.android;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;

import fm.radiant.android.lib.cleaner.AdsCleaner;
import fm.radiant.android.lib.cleaner.TracksCleaner;
import fm.radiant.android.lib.indexer.AdsIndexer;
import fm.radiant.android.lib.indexer.TracksIndexer;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.fragments.PlayerFragment;
import fm.radiant.android.lib.EventBusActivity;
import fm.radiant.android.models.Place;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.tasks.CleanTask;
import fm.radiant.android.tasks.SubscribeTask;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.tasks.UnpairTask;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

public class MainActivity extends EventBusActivity {
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
        openPlayerFragment();

        if (MessagesUtils.canReceiveMessages(this)) {
            new SubscribeTask().execute();
            new SyncTask().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
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
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(PlayerFragment.TAG);

        if (fragment != null && fragment.isVisible()) switch (keyCode) {
            case KEYCODE_VOLUME_UP:
                return true;

            case KEYCODE_VOLUME_DOWN:
                return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    public void onEvent(final Events.PlaceChangedEvent event) {
        if (!AccountUtils.isLoggedIn()) return;

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Place place = event.getPlace();

                // indexers

                TracksIndexer tracksIndexer = new TracksIndexer(getApplicationContext(), place.getTracks());
                AdsIndexer    adsIndexer    = new AdsIndexer(getApplicationContext(), place.getAds());

                // syncer

                final Syncer syncer = LibraryUtils.getSyncer();
                syncer.setIndexers(tracksIndexer, adsIndexer);

                if (syncer.getState() != Syncer.STATE_STOPPED) {
                    Intent service = new Intent(getApplicationContext(), DownloadService.class);
                    stopService(service); startService(service);
                } else {
                    syncer.touch();
                }

                // player

                final Player player = LibraryUtils.getPlayer();
                player.setIndexers(tracksIndexer, adsIndexer);
                player.setPeriods(place.getPeriods());
                player.setCampaigns(place.getCampaigns());
                player.setSyncer(syncer);
                player.schedule();

                // cleaner

                new CleanTask().execute(new TracksCleaner(getApplicationContext(), place.getTracks()), new AdsCleaner(getApplicationContext(), place.getAds()));
            }
        }).start();
    }

    public void onEvent(Events.PlaceUnpairedEvent event) {
        new UnpairTask(this, AccountUtils.getPassword()).execute();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
    }

    private void openPlayerFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(getContentViewCompat(), new PlayerFragment(), PlayerFragment.TAG);
        transaction.commit();
    }
}