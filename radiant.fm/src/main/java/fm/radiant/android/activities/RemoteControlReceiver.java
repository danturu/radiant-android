package fm.radiant.android.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

public class RemoteControlReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ACTION_MEDIA_BUTTON", "ACTION_MEDIA_BUTTON");

        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if (KeyEvent.KEYCODE_MEDIA_PLAY == event.getKeyCode()) {
            }
        }
    }
}