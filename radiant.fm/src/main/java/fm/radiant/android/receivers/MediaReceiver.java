package fm.radiant.android.receivers;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fm.radiant.android.classes.player.Player;
import fm.radiant.android.utils.LibraryUtils;

public class MediaReceiver extends BroadcastReceiver {
    private static final String TAG = "MediaReceiver";

    public static PendingIntent getBroadcast(Context context) {
        Intent receiver = new Intent(context, MediaReceiver.class);
        return PendingIntent.getBroadcast(context, 0, receiver, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Player player = LibraryUtils.getPlayer();

        player.enqueue();
        player.play();
    }
}