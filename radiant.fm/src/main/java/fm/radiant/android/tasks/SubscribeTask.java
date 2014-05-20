package fm.radiant.android.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import fm.radiant.android.utils.MessagesUtils;


public class SubscribeTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "SubscribeTask";

    private static Handler backoff = new Handler();

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (!MessagesUtils.isRegistered())                  MessagesUtils.registerInCloud();
            if (!MessagesUtils.isRegistrationSendedToBackend()) MessagesUtils.sendRegistrationToBackend(MessagesUtils.getRegistrationId());
        } catch (IOException e) {
            Log.e(TAG, "Could not be subscribed: ", e);
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        backoff.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPostExecute(Void nothing) {
        if (MessagesUtils.isRegistered() && MessagesUtils.isRegistrationSendedToBackend()) {
            Log.i(TAG, "Subscribed to push notifications."); return;
        }

        Runnable resubscribe = new Runnable(){
            public void run() {
                new SubscribeTask().execute();
            }
        };

        backoff.postDelayed(resubscribe, 10000);
    }
}
