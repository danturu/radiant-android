package fm.radiant.android.receivers;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fm.radiant.android.classes.player.Player;
import fm.radiant.android.utils.LibraryUtils;

public class MediaReceiver extends BroadcastReceiver {
    public static PendingIntent getBroadcast(Context context) {
        Intent receiverIntent = new Intent(context, MediaReceiver.class);
        return PendingIntent.getBroadcast(context, 0, receiverIntent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Player player = LibraryUtils.getPlayer();

        player.schedule();

        if (player.getState() != Player.STATE_STOPPED) {
            player.play();
        }
    }
}