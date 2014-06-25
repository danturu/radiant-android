package fm.radiant.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang.math.RandomUtils;

import java.util.UUID;

import fm.radiant.android.tasks.PairTask;

public class AuthActivity extends ActionBarActivity implements TextView.OnEditorActionListener, Button.OnClickListener {
    public static final String EXTRA_UNPAIRED_BY_USER = "1";

    private EditText inputPassword;
    private Button buttonSignin;
    private Button buttonSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_auth);

        colorize();

        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/museo_sans_500.ttf");

        inputPassword = (EditText) findViewById(R.id.input_password);
        inputPassword.setTypeface(font);
        inputPassword.setOnEditorActionListener(this);

        buttonSignin = (Button) findViewById(R.id.button_signin);
        buttonSignin.setTypeface(font);
        buttonSignin.setOnClickListener(this);

        buttonSignup = (Button) findViewById(R.id.button_signup);
        buttonSignup.setTypeface(font);
        buttonSignup.setOnClickListener(this);

        if (!getIntent().getBooleanExtra(EXTRA_UNPAIRED_BY_USER, true)) {
            showUnpairExplanation();
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.button_signin:
                signin();
                break;

            case R.id.button_signup:
                signup();
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        signin(); return false;
    }

    private void colorize() {
        int[] colors = getResources().getIntArray(R.array.covers);
        int   color  = colors[RandomUtils.nextInt(colors.length)];

        findViewById(R.id.layout_cover).setBackgroundColor(color);
        findViewById(R.id.underlay_password).setBackgroundColor(color);
        findViewById(R.id.underlay_signin).setBackgroundColor(color);
    }

    private void signin() {
        new PairTask(this, UUID.randomUUID().toString(), inputPassword.getText().toString(), false).execute();
    }

    private void signup() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://help.radiant.fm"));
        startActivity(browserIntent);
    }

    private void showUnpairExplanation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(R.string.message_device_was_unpaired_by_server);
        alertDialog.setNeutralButton(R.string.button_ok, null);
        alertDialog.show();
    }
}