package fm.radiant.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.util.concurrent.RateLimiter;

import fm.radiant.android.Events;
import fm.radiant.android.MainActivity;
import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.lib.EventBusFragment;
import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.lib.widgets.CircleProgressBar;
import fm.radiant.android.lib.widgets.SquareButton;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Place;
import fm.radiant.android.models.Style;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.ParseUtils;

import static fm.radiant.android.lib.TypefaceCache.FONT_MUSEO_300;
import static fm.radiant.android.lib.TypefaceCache.FONT_MUSEO_500;

public class PlayerFragment extends EventBusFragment implements View.OnClickListener {
    public static String TAG = PlayerFragment.class.getSimpleName();

    private TextView mSyncedPercent;
    private TextView mEstimatedTime;
    private TextView mDownloadSpeed;
    private CircleProgressBar mSyncerProgress;

    private Player mPlayer = LibraryUtils.getPlayer();

    private SquareButton mScheduleButton;
    private SquareButton mPlayButton;
    private SquareButton mAnnounceButton;
    private SeekBar mVolumeSlider;

    private RateLimiter mTrackLimiter = RateLimiter.create(0.1);

    private View     mPeriodView;
    private TextView mPeriodTime;
    private TextView mPeriodDescription;
    private TextView mPeriodStyles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setHasOptionsMenu(true);

        // status
        Typeface museo500 = TypefaceCache.get(TypefaceCache.FONT_MUSEO_500);

        mEstimatedTime = (TextView) view.findViewById(R.id.text_estimated_time);
        mEstimatedTime.setTypeface(museo500);

        mDownloadSpeed = (TextView) view.findViewById(R.id.text_download_speed);
        mDownloadSpeed.setTypeface(museo500);

        mSyncerProgress = (CircleProgressBar) view.findViewById(R.id.progress_syncer_progress);
        mSyncerProgress.setTypeface(museo500);
        // controls

        mScheduleButton = (SquareButton) view.findViewById(R.id.button_schedule);
        mScheduleButton.setOnClickListener(this);

        mPlayButton = (SquareButton) view.findViewById(R.id.button_play);
        mPlayButton.setOnClickListener(this);

        mAnnounceButton = (SquareButton) view.findViewById(R.id.button_announce);
        mAnnounceButton.setOnClickListener(this);

        mVolumeSlider = (SeekBar) view.findViewById(R.id.slider_volume);
        mVolumeSlider.setMax(mPlayer.getMaxVolume() * 10);
        mVolumeSlider.setProgress(mPlayer.getVolume() * 10);
        mVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int volume, boolean fromUser) {
                if (mTrackLimiter.tryAcquire() && fromUser) {
                    mPlayer.setVolume(volume / 10);
                }
            }
        });

        // period

        mPeriodView = view.findViewById(R.id.view_period);

        Typeface museo300 = TypefaceCache.get(FONT_MUSEO_300);

        mPeriodTime = (TextView) view.findViewById(R.id.text_period_time);
        mPeriodTime.setTypeface(museo500);

        mPeriodDescription = (TextView) view.findViewById(R.id.text_period_description);
        mPeriodDescription.setTypeface(museo300);

        mPeriodStyles = (TextView) view.findViewById(R.id.text_period_styles);
        mPeriodStyles.setTypeface(museo500);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                openPreferencesFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onEventMainThread(Events.PlaceChangedEvent event) {
        Place place = event.getPlace();

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(Radiant.formatHeader(place.getName()));
    }


    public synchronized void onEventMainThread(Events.SyncerStateChanged event) {
        int color = 0;
        int ring  = 0;

        switch (event.getState()) {
            case Syncer.STATE_SYNCED:
                color = R.color.state_green;
                break;

            case Syncer.STATE_STOPPED:
                color = R.color.state_yellow;
                break;

            case Syncer.STATE_IDLE:
                color = R.color.state_yellow;
                break;

            case Syncer.STATE_FAILED:
                color = R.color.state_red;
                break;

            case Syncer.STATE_INDEXING:
                color = R.color.state_blue;
                break;

            case Syncer.STATE_SYNCING:
                color = R.color.state_blue;
                break;
        }

        mSyncerProgress.setTextColor(getResources().getColor(color));
        mSyncerProgress.setProgressForegroundColor(getResources().getColor(color));
    }

    public void onEventMainThread(Events.SyncerProgressChanged event) {
        if (event.getSyncedPercent() != mSyncerProgress.getProgress()) {
            mSyncerProgress.setText(event.getSyncedPercent() + "%");
            mSyncerProgress.setProgress(event.getSyncedPercent());
        }

        mEstimatedTime.setText(ParseUtils.humanizeDuration(event.getEstimatedTime()));
        mDownloadSpeed.setText(ParseUtils.humanizeSpeed(event.getDownloadSpeed()));
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_play:
                togglePlayerState();
                break;

            case R.id.button_schedule:
                openSchedulerFragment();
                break;
        }
    }

    public void onEventMainThread(Events.PlayerStateChanged event) {
        switch (event.getState()) {
            case Player.STATE_FAILED:
                mPlayButton.setImageResource(R.drawable.player_button_play);
                break;

            case Player.STATE_STOPPED:
                mPlayButton.setImageResource(R.drawable.player_button_play);
                break;

            case Player.STATE_INDEXING:
                mPlayButton.setImageResource(R.drawable.player_button_pause);
                break;

            case Player.STATE_PLAYING:
                mPlayButton.setImageResource(R.drawable.player_button_pause);
                break;

            case Player.STATE_WAITING:
                mPlayButton.setImageResource(R.drawable.ic_launcher);
                break;
        }
    }

    public void onEventMainThread(Events.PlayerVolumeChanged event) {
        mVolumeSlider.setProgress(event.getVolume() * 10);
    }

    private void togglePlayerState() {
        Player player = LibraryUtils.getPlayer();

        if (player.getState() != Player.STATE_STOPPED) {
            LibraryUtils.getPlayer().stop();
        } else {
            LibraryUtils.getPlayer().play();
        }
    }

    private void openSchedulerFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(MainActivity.getContentViewCompat(), new SchedulerFragment(), SchedulerFragment.TAG);
        transaction.addToBackStack(SchedulerFragment.TAG);
        transaction.commit();
    }

    private void openPreferencesFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(MainActivity.getContentViewCompat(), new PreferencesFragment(), PreferencesFragment.TAG);
        transaction.addToBackStack(SchedulerFragment.TAG);
        transaction.commit();
    }

    // PERIOD


    public void onEventMainThread(Events.PlayerPeriodChanged event) {
        Period period = event.getPeriod();

        colorize(period);

        if (period == null) {
            getView().findViewById(R.id.view_empty_period).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.view_normal_period).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.view_empty_period).setVisibility(View.GONE);
            getView().findViewById(R.id.view_normal_period).setVisibility(View.VISIBLE);

            mPeriodTime.setText(ParseUtils.humanizeDay(period.getDay()) + " " + ParseUtils.humanizeTimeRange(period.getStartAt(), period.getEndAt()));

            mPeriodDescription.setText(period.getGenre().getDescription());

            mPeriodStyles.setText(TextUtils.join(" · ", Style.collectNames(period.getGenre().getStyles())) + TextUtils.join(" · ", Style.collectNames(period.getGenre().getStyles())) + TextUtils.join(" · ", Style.collectNames(period.getGenre().getStyles())) + TextUtils.join(" · ", Style.collectNames(period.getGenre().getStyles())));
        }
    }

    private void colorize(Period period) {
        if (period == null) {
            Log.d("empty colorize", "empty colorize");
        } else {
            int[] colors = getResources().getIntArray(R.array.covers);
            mPeriodView.setBackgroundColor(colors[period.getGenre().getColorIndex()]);
        }
    }

}
