package fm.radiant.android.lib;

import android.support.v4.app.Fragment;

import de.greenrobot.event.EventBus;

public class EventBusFragment extends Fragment {
    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        EventBus.getDefault().unregister(this);
    }
}
