package fm.radiant.android;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;

import de.greenrobot.event.EventBus;
import fm.radiant.android.fragments.LoadingFragment;
import fm.radiant.android.fragments.PlayerFragment;
import fm.radiant.android.lib.EventBusActivity;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.services.SetupService;
import fm.radiant.android.tasks.SubscribeTask;
import fm.radiant.android.tasks.UnpairTask;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.MessagesUtils;

import static android.view.KeyEvent.KEYCODE_VOLUME_DOWN;
import static android.view.KeyEvent.KEYCODE_VOLUME_UP;

public class MainActivity extends EventBusActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayShowHomeEnabled(false);

        if (!AccountUtils.isLoggedIn()) {
            startLoginActivity(); return;
        }

        if (!SetupService.isComplete()) {
            openLoadingFragment();
        }

        if (MessagesUtils.canReceiveMessages(this)) {
            new SubscribeTask().execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MessagesUtils.canReceiveMessages(this);
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
                Player.getInstance().adjustVolume(AudioManager.ADJUST_RAISE);
                return true;

            case KEYCODE_VOLUME_DOWN:
                Player.getInstance().adjustVolume(AudioManager.ADJUST_LOWER);
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

    public void onEventMainThread(final Events.PlaceChangedEvent event) {
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) openPlayerFragment();
    }

    public void onEventMainThread(Events.PlaceUnpairedEvent event) {
        EventBus.getDefault().removeStickyEvent(event);
        new UnpairTask(this, false).execute();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
    }

    private void openLoadingFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, new LoadingFragment(), LoadingFragment.TAG);
        transaction.commit();
    }

    private void openPlayerFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(android.R.id.content, new PlayerFragment(), PlayerFragment.TAG);
        transaction.commit();
    }
}