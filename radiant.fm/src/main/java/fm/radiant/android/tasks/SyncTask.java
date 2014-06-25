package fm.radiant.android.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.utils.AccountUtils;

public class SyncTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = SyncTask.class.getSimpleName();

    private static final int RESULT_SUCCESS      = 200;
    private static final int RESULT_UNAUTHORIZED = 401;
    private static final int RESULT_FAIL         = 0;

    private static Handler backoff = new Handler();

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            return AccountUtils.sync(AccountUtils.getPlaceId());
        } catch (IOException e) {
            Log.e(TAG, "Could not be synced: ", e);
        } catch (HttpRequest.HttpRequestException e) {
            Log.e(TAG, "Could not be synced: ", e);
        }

        return RESULT_FAIL;
    }

    @Override
    protected void onPreExecute() {
        backoff.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPostExecute(Integer resultCode) {
        switch (resultCode) {
            case RESULT_UNAUTHORIZED:
                EventBus.getDefault().postSticky(new Events.PlaceUnpairedEvent());
                return;

            case RESULT_SUCCESS:
                return;

            default:
                resync();
                return;
        }
    }

    private void resync() {
        Runnable resync = new Runnable(){
            public void run() {
                if (Syncer.getInstance().getState() != Syncer.STATE_STOPPED && AccountUtils.isLoggedIn()) {
                    new SyncTask().execute();
                }
            }
        };

        backoff.postDelayed(resync, 10000);
    }
}