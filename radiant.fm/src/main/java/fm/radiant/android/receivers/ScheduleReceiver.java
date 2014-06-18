package fm.radiant.android.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fm.radiant.android.utils.LibraryUtils;

public class ScheduleReceiver extends BroadcastReceiver {
    public static PendingIntent getBroadcast(Context context) {
        Intent intent = new Intent(context, ScheduleReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LibraryUtils.getPlayer().schedule();
    }
}