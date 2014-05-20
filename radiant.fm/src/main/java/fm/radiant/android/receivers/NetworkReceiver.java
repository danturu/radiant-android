package fm.radiant.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fm.radiant.android.classes.syncer.Syncer;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.NetworkUtils;

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent) {
        if (NetworkUtils.isNetworkConnected()) {
            new SyncTask().execute();

            if (LibraryUtils.getSyncer().getState() != Syncer.STATE_STOPPED) {
                Intent service = new Intent(context, DownloadService.class);
                context.startService(service);
            }
        }
    }
}
