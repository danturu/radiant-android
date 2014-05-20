package fm.radiant.android.receivers;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

import fm.radiant.android.services.MessagesService;

public class MessagesReceiver extends WakefulBroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName component = new ComponentName(context.getPackageName(), MessagesService.class.getName());
        startWakefulService(context, intent.setComponent(component));

        setResultCode(Activity.RESULT_OK);
    }
}
