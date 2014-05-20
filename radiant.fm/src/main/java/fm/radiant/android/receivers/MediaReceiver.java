package fm.radiant.android.receivers;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import fm.radiant.android.utils.LibraryUtils;

public class MediaReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        LibraryUtils.getPlayer().enqueue();

        Log.d("MediaReceiver", "receive");
    }
}