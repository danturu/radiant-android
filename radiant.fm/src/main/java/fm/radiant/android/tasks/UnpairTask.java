package fm.radiant.android.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.IOException;

import fm.radiant.android.R;
import fm.radiant.android.AuthActivity;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;

public class UnpairTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = PairTask.class.getSimpleName();

    private Activity activity;

    private String password;

    public UnpairTask(Activity activity, String password) {
        this.activity = activity;
        this.password = password;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            MessagesUtils.unregister();
        } catch (IOException e) {
            Log.e(TAG, "Could not be unregistered: ", e);
        } catch (HttpRequest.HttpRequestException e) {
            Log.e(TAG, "Could not be unregistered: ", e);
        }

        try {
            AccountUtils.unpair(password);
        } catch (IOException e) {
            Log.e(TAG, "Could not be unpaired: ", e);
        } catch (HttpRequest.HttpRequestException e) {
            Log.e(TAG, "Could not be unpaired: ", e);
        }

        AccountUtils.teardown(); LibraryUtils.teardown(); MessagesUtils.teardown();

        return null;
    }

    private ProgressDialog progressDialog;

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(activity.getString(R.string.message_please_wait));
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void nothing) {
        progressDialog.dismiss();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(activity, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        activity.startActivity(intent);
        activity.finish();
    }
}
