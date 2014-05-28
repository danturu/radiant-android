package fm.radiant.android.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import fm.radiant.android.receivers.MessagesReceiver;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.utils.MessagesUtils;

public class MessagesService extends IntentService {
    private static final String TAG = "NotificationsService";

    private static final String PROPERTY_ACTION = "action";

    private static final String ACTION_RESYNC = "resync";
    private static final String ACTION_UNPAIR = "unpair";

    public MessagesService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Bundle extras      = intent.getExtras();
            String messageType = MessagesUtils.getMessageType(intent);

            Log.i(TAG, "Message received: " + extras.toString());

            if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                return;
            }

            String action = extras.getString(PROPERTY_ACTION);

            if (ACTION_RESYNC.equals(action)) {
                new SyncTask().execute();
                return;
            }

            if (ACTION_UNPAIR.equals(action)) {
                return;
            }
        } finally {
            MessagesReceiver.completeWakefulIntent(intent);
        }
    }
}
