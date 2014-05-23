package fm.radiant.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

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
            Log.d(TAG, "sdckmsdkcmdk");

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

        setContentView(R.layout.activity_main);

        // BOOTSTRAP

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
        LibraryUtils.getPlayer().enqueuePeriod();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessagesUtils.canReceiveMessages(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (broadcastManager == null) {
            return;
        }

        broadcastManager.unregisterReceiver(resyncReceiver);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
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
