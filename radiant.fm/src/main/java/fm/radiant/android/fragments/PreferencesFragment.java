package fm.radiant.android.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import fm.radiant.android.Events;
import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.lib.EventBusFragment;
import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.tasks.UnpairTask;
import fm.radiant.android.utils.AccountUtils;

public class PreferencesFragment extends EventBusFragment implements View.OnClickListener {
    public static String TAG = PreferencesFragment.class.getSimpleName();

    private Player mPlayer = Player.getInstance();
    private Syncer mSyncer = Syncer.getInstance();

    private TextView mSyncHeader;
    private TextView mInfoHeader;

    // media

    private TextView mHeaderSyncOn;
    private ToggleButton mSwitchSyncOn;

    private TextView mHeaderSyncedPercent;
    private TextView mValueSyncedPercent;

    // info

    private TextView mHeaderPlaceName;
    private TextView mHeaderUpdatedAt;

    private TextView mValuePlaceName;
    private TextView mValueUpdatedAt;

    // logout

    Button mLogoutButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);

        setHasOptionsMenu(false);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Radiant.formatHeader(getString(R.string.title_preferences)));

        Typeface museo500 = TypefaceCache.get(TypefaceCache.FONT_MUSEO_500);

        // media

        mSyncHeader = (TextView) view.findViewById(R.id.text_header_sync);
        mSyncHeader.setTypeface(museo500);

        mHeaderSyncOn = (TextView) view.findViewById(R.id.header_sync_on);
        mHeaderSyncOn.setTypeface(museo500);

        mSwitchSyncOn = (ToggleButton) view.findViewById(R.id.button_sync_on);
        mSwitchSyncOn.setChecked(mSyncer.getState() != Syncer.STATE_STOPPED);
        mSwitchSyncOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton compoundButton, boolean checked) {
                if (checked == false && mSyncer.getState() == Syncer.STATE_STOPPED) return;

                if (checked) {
                    new SyncTask().execute();
                    mSyncer.startService();
                } else {
                    if (mPlayer.getState() != Player.STATE_STOPPED && !mPlayer.isMusicEnough()) {
                        compoundButton.toggle();

                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(getActivity());
                        errorDialog.setMessage(R.string.message_turn_off_will_stop_music);
                        errorDialog.setPositiveButton(R.string.button_turn_off, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                mSyncer.stopService();
                                mPlayer.stop();

                                compoundButton.setChecked(false);
                            }
                        });
                        errorDialog.setNegativeButton(R.string.button_cancel, null);
                        errorDialog.show();
                    } else {
                        mSyncer.stopService();
                    }
                }
            }
        });

        mHeaderSyncedPercent = (TextView) view.findViewById(R.id.header_synced_percent);
        mHeaderSyncedPercent.setTypeface(museo500);

        mValueSyncedPercent = (TextView) view.findViewById(R.id.value_synced_percent);
        mValueSyncedPercent.setTypeface(museo500);

        // info

        mInfoHeader = (TextView) view.findViewById(R.id.text_header_info);
        mInfoHeader.setTypeface(museo500);

        mHeaderPlaceName = (TextView) view.findViewById(R.id.header_place_name);
        mHeaderPlaceName.setTypeface(museo500);

        mHeaderUpdatedAt = (TextView) view.findViewById(R.id.header_updated_at);
        mHeaderUpdatedAt.setTypeface(museo500);

        mValuePlaceName = (TextView) view.findViewById(R.id.value_place_name);
        mValuePlaceName.setTypeface(museo500);

        mValueUpdatedAt = (TextView) view.findViewById(R.id.value_updated_at);
        mValueUpdatedAt.setTypeface(museo500);

        // logout

        mLogoutButton = (Button) view.findViewById(R.id.button_logout);
        mLogoutButton.setTypeface(museo500);
        mLogoutButton.setOnClickListener(this);

        return view;
    }

    public void onEventMainThread(Events.PlaceChangedEvent event) {
        mValuePlaceName.setText(event.getPlace().getName());
        mValueUpdatedAt.setText(DateTimeFormat.forPattern("dd.MM.yy HH:mm").print(new DateTime(AccountUtils.getSyncedAt())));
    }

    public void onEventMainThread(Events.SyncerSyncedPercentChanged event) {
        mValueSyncedPercent.setText(event.getSyncedPercent() + "%");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_logout:
                logout();
                break;
        }
    }

    private void logout() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setMessage(R.string.message_unpair_will_destroy_data);
        alertDialog.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new UnpairTask(getActivity(), AccountUtils.getPassword(), true).execute();
            }
        });
        alertDialog.setNegativeButton(R.string.button_cancel, null);
        alertDialog.show();
    }
}