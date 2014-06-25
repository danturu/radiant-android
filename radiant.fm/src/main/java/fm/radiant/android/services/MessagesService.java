package fm.radiant.android.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.syncer.Syncer;
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

            Log.d(TAG, "Push message!");

            if (!MessagesUtils.isRegistered()) {
                Log.i(TAG, "Push message received, but app should be unregistered");

                try {
                    MessagesUtils.unregister();
                } catch (IOException e) {
                    Log.e(TAG, "Could not be unregistered", e);
                }

                return;
            }

            if (!GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                return;
            }

            String action = extras.getString(PROPERTY_ACTION);

            Log.i(TAG, "Push message received: " + action);

            if (ACTION_RESYNC.equals(action) && Syncer.getInstance().getState() != Syncer.STATE_STOPPED) {
                new SyncTask().execute();
                return;
            }

            if (ACTION_UNPAIR.equals(action)) {
                EventBus.getDefault().postSticky(new Events.PlaceUnpairedEvent());
                return;
            }
        } finally {
            MessagesReceiver.completeWakefulIntent(intent);
        }
    }
}
