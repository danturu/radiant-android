package fm.radiant.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.classes.syncer.Syncer;
import fm.radiant.android.utils.LibraryUtils;

public class PlayerStatusFragment extends Fragment {
    TextView syncedPercent;

    private BroadcastReceiver renderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            render();
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_status, container, false);

        Typeface museo300 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/museo_sans_300.ttf");
        Typeface museo500 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/museo_sans_500.ttf");

        syncedPercent = (TextView) view.findViewById(R.id.percent);
        syncedPercent.setTypeface(museo500);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(renderReceiver,  new IntentFilter(Radiant.INTENT_SYNCER_STATE_CHANGED));
        render();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(renderReceiver);
    }

    public void render() {
        Syncer syncer = LibraryUtils.getSyncer();

        if (syncer == null) {
            return;
        }

        syncedPercent.setText(syncer.getSyncedPercent() + "%");

        switch (syncer.getState()) {
            case Syncer.STATE_SYNCED:
                syncedPercent.setTextColor(Color.parseColor("#4cd864"));
                break;
            case Syncer.STATE_STOPPED:
                syncedPercent.setTextColor(Color.parseColor("#BFBFBF"));
                break;
            case Syncer.STATE_IDLE:
                syncedPercent.setTextColor(Color.parseColor("#ffc700"));
                break;
            case Syncer.STATE_FAILED:
                syncedPercent.setTextColor(Color.parseColor("#e5000f"));
                break;
            case Syncer.STATE_INDEXING: case Syncer.STATE_SYNCING:
                syncedPercent.setTextColor(Color.parseColor("#157dfb"));
                break;
        }

        // Fragment statusMessageFragment = new PlayerStatusMessageFragment();
        // FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        // transaction.replace(R.id.fragment_status, statusMessageFragment);
        // transaction.commit();
    }
}
