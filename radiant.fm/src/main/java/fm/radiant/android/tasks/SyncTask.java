package fm.radiant.android.tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.IOException;

import fm.radiant.android.utils.AccountUtils;

public class SyncTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = "SyncTask";

    private static final int RESULT_SUCCESS = 200;
    private static final int RESULT_FAIL    = 0;

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
        if (resultCode == RESULT_SUCCESS) {
            Log.d(TAG, "Place successfully synced"); return;
        }

        Runnable resync = new Runnable(){
            public void run() {
                new SyncTask().execute();
            }
        };

        backoff.postDelayed(resync, 10000);
    }
}