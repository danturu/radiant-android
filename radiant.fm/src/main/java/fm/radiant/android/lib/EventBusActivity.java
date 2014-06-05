package fm.radiant.android.lib;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import de.greenrobot.event.EventBus;

/**
 * Created by kochnev on 04/06/14.
 */
public class EventBusActivity extends ActionBarActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().registerSticky(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EventBus.getDefault().unregister(this);
    }
}
