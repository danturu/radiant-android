package fm.radiant.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import fm.radiant.android.R;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.tasks.SubscribeTask;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.tasks.UnpairTask;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.MessagesUtils;
import fm.radiant.android.utils.StorageUtils;

public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AccountUtils.isLoggedIn()) {
            startLoginActivity(); return;
        }

        if (MessagesUtils.canReceiveMessages(this)) {
            new SyncTask().execute();
            new SubscribeTask().execute();
        }

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MessagesUtils.canReceiveMessages(this);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        startActivity(intent);
        finish();
    }

    public void logout(View view) {
        new UnpairTask(this, AccountUtils.getPassword()).execute();
    }

    public void logout2(View view) {
        intent = new Intent(getApplicationContext(), DownloadService.class);
        getApplicationContext().startService(intent);

       // getApplicationContext().stopService(intent);
    }

    public void logout3(View view) {
        intent = new Intent(getApplicationContext(), DownloadService.class);
        getApplicationContext().stopService(intent);
    }
}
