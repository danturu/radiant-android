package fm.radiant.android.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.SeekBar;

import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.classes.cleaner.AdsCleaner;
import fm.radiant.android.classes.cleaner.TracksCleaner;
import fm.radiant.android.classes.syncer.Syncer;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.tasks.CleanTask;
import fm.radiant.android.tasks.SubscribeTask;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.tasks.UnpairTask;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    private LocalBroadcastManager broadcastManager;

    BroadcastReceiver resyncReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            performCleanTask();
            restartDownloadService();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AccountUtils.isLoggedIn()) {
            startLoginActivity(); return;
        }

        //setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.activity_main);

        // BOOTSTRAP

        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);

        int result = manager.requestAudioFocus(new AudioManager.OnAudioFocusChangeListener() {
                                                   @Override
                                                   public void onAudioFocusChange(int i) {

                                                   }
                                               },
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            manager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));
            // Start playback.
        }

       // manager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));

        if (MessagesUtils.canReceiveMessages(this)) {
            new SyncTask().execute();
            new SubscribeTask().execute();
        }

        performCleanTask();
        restartDownloadService();

        // RECEIVERS

        broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        broadcastManager.registerReceiver(resyncReceiver, new IntentFilter(Radiant.INTENT_RESYNC));

        // PLAYEr

        LibraryUtils.getPlayer().setPeriods(AccountUtils.getCurrentPlace().getPeriods());
        LibraryUtils.getPlayer().setCampaigns(AccountUtils.getCurrentPlace().getCampaigns());
        LibraryUtils.getPlayer().schedule();

        SeekBar volumeSlider = (SeekBar) findViewById(R.id.slider_volume);
        volumeSlider.setMax(100);
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int level, boolean fromUser) {
                if (fromUser) {
                    LibraryUtils.getPlayer().setVolume(level);
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        MessagesUtils.canReceiveMessages(this);

        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        manager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        AudioManager manager = (AudioManager) getSystemService(AUDIO_SERVICE);
        manager.unregisterMediaButtonEventReceiver(new ComponentName(getPackageName(), RemoteControlReceiver.class.getName()));

        if (broadcastManager == null) {
            return;
        }
        broadcastManager.unregisterReceiver(resyncReceiver);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
    }

    private void performCleanTask() {
        Context context = getApplicationContext();

        new CleanTask(context).execute(
                new TracksCleaner(context, AccountUtils.getCurrentPlace().getTracks()),
                new AdsCleaner(context, AccountUtils.getCurrentPlace().getAds())
        );
    }

    private void restartDownloadService() {
        if (LibraryUtils.getSyncer() != null && LibraryUtils.getSyncer().getState() == Syncer.STATE_STOPPED) {
            return;
        }

        Intent service = new Intent(getApplicationContext(), DownloadService.class);
        stopService(service); startService(service);
    }
















    /////////////////////////////
    Intent intent;

    public void volume(View view) {
        new UnpairTask(this, AccountUtils.getPassword()).execute();
    }

    public void logout(View view) {
        new UnpairTask(this, AccountUtils.getPassword()).execute();
    }

    public void logout2(View view) {
        intent = new Intent(getApplicationContext(), DownloadService.class);
        getApplicationContext().startService(intent);

       // getApplicationContext().stopService(intent);
    }

    public void logout3(View view) {
        intent = new Intent(getApplicationContext(), DownloadService.class);
        getApplicationContext().stopService(intent);
    }

    public void play(View view) {
        LibraryUtils.getPlayer().setTracksIndexer(LibraryUtils.getTracksIndexer());
        LibraryUtils.getPlayer().play();
    }

    public void pause(View view) {
        LibraryUtils.getPlayer().stop();
    }
}
