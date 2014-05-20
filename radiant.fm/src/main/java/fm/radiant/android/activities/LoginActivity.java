package fm.radiant.android.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.EditText;

import java.util.UUID;

import fm.radiant.android.R;
import fm.radiant.android.tasks.PairTask;

public class LoginActivity extends ActionBarActivity {
    private EditText passwordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        passwordInput = (EditText) findViewById(R.id.input_password);
    }

    public void login(View view) {
        new PairTask(this, UUID.randomUUID().toString(), passwordInput.getText().toString(), false).execute();
    }
}
