package fm.radiant.android.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.IOException;

import fm.radiant.android.AuthActivity;
import fm.radiant.android.R;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.MessagesUtils;

public class UnpairTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = PairTask.class.getSimpleName();

    private Activity activity;
    private String   password;
    private boolean  userAction;

    public UnpairTask(Activity activity, String password, Boolean userAction) {
        this.activity   = activity;
        this.password   = password;
        this.userAction = userAction;
    }

    public UnpairTask(Activity activity, Boolean userAction) {
        this.activity   = activity;
        this.userAction = userAction;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        MessagesUtils.teardown();

        Syncer.getInstance().reset();
        Player.getInstance().reset();

        try {
            MessagesUtils.unregister();
        } catch (IOException e) {
            Log.e(TAG, "Could not be unregistered: ", e);
        } catch (HttpRequest.HttpRequestException e) {
            Log.e(TAG, "Could not be unregistered: ", e);
        }

        if (password != null) try {
            AccountUtils.unpair(password);
        } catch (IOException e) {
            Log.e(TAG, "Could not be unpaired: ", e);
        } catch (HttpRequest.HttpRequestException e) {
            Log.e(TAG, "Could not be unpaired: ", e);
        }

        AccountUtils.teardown();

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
        intent.putExtra(AuthActivity.EXTRA_UNPAIRED_BY_USER, userAction);

        activity.startActivity(intent);
        activity.finish();
    }
}
