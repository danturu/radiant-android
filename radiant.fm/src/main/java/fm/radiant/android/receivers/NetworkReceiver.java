package fm.radiant.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.NetworkUtils;

public class NetworkReceiver extends BroadcastReceiver {
    private Player mPlayer = Player.getInstance();
    private Syncer mSyncer = Syncer.getInstance();

    @Override
    public void onReceive(final Context context, Intent intent) {
        // restart download service

        if (NetworkUtils.isNetworkConnected() && AccountUtils.isLoggedIn() && mPlayer.getState() != Syncer.STATE_STOPPED) {
            Intent service = new Intent(context, DownloadService.class);
            context.startService(service);
        }
    }
}