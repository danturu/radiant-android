package fm.radiant.android.tasks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

import java.io.IOException;

import fm.radiant.android.R;
import fm.radiant.android.MainActivity;
import fm.radiant.android.utils.AccountUtils;

public class PairTask extends AsyncTask<Void, Void, Integer> implements DialogInterface.OnClickListener {
    private static final String TAG = PairTask.class.getSimpleName();

    public static final int RESULT_SUCCESS        = 200;
    public static final int RESULT_ALREADY_PAIRED = 403;
    public static final int RESULT_WRONG_PASSWORD = 404;
    public static final int RESULT_SERVER_FAULT   = 500;
    public static final int RESULT_FAIL           = 0;

    private Activity activity;

    private String uuid;
    private String password;

    private boolean unpairBefore;

    public PairTask(Activity activity, String uuid, String password, Boolean unpairBefore) {
        this.activity     = activity;
        this.uuid         = uuid;
        this.password     = password;
        this.unpairBefore = unpairBefore;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            if (unpairBefore) AccountUtils.unpair(password);

            return AccountUtils.pair(uuid, password);
        } catch (IOException e) {
            Log.e(TAG, "Could not be paired: ", e);
        } catch (HttpRequestException e) {
            Log.e(TAG, "Could not be paired: ", e);

        }

        return RESULT_FAIL;
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
    protected void onPostExecute(Integer resultCode) {
        progressDialog.dismiss();

        if (resultCode == RESULT_SUCCESS) {
            startMainActivity();
        } else {
            getErrorDialog(resultCode);
        }
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int which) {
        new PairTask(activity, uuid, password, true).execute();
    }

    private void startMainActivity() {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        activity.startActivity(intent);
        activity.finish();
    }

    private void getErrorDialog(int errorCode) {
        AlertDialog.Builder errorDialog = new AlertDialog.Builder(activity);

        switch (errorCode) {
            case RESULT_ALREADY_PAIRED:
                errorDialog.setMessage(R.string.message_already_paired);
                errorDialog.setPositiveButton(R.string.button_repair, this);
                errorDialog.setNegativeButton(R.string.button_cancel, null);
                break;

            case RESULT_WRONG_PASSWORD:
                errorDialog.setMessage(R.string.message_wrong_password);
                errorDialog.setNeutralButton(R.string.button_ok, null);
                break;

            case RESULT_SERVER_FAULT:
                errorDialog.setMessage(R.string.message_server_fault);
                errorDialog.setNeutralButton(R.string.button_ok, null);
                break;

            case RESULT_FAIL:
                errorDialog.setMessage(R.string.message_network_error);
                errorDialog.setNeutralButton(R.string.button_ok, null);
                break;
        }

        errorDialog.show();
    }
}
