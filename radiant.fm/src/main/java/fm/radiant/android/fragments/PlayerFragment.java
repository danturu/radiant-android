package fm.radiant.android.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.util.concurrent.RateLimiter;

import java.util.concurrent.Semaphore;

import fm.radiant.android.Events;
import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.lib.EventBusFragment;
import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;
import fm.radiant.android.lib.widgets.AutoScrollTextView;
import fm.radiant.android.lib.widgets.CircleProgressBar;
import fm.radiant.android.lib.widgets.ImageSquareButton;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Place;
import fm.radiant.android.models.Style;
import fm.radiant.android.services.DownloadService;
import fm.radiant.android.utils.NetworkUtils;
import fm.radiant.android.utils.ParseUtils;

public class PlayerFragment extends EventBusFragment implements View.OnClickListener {
    public static String TAG = PlayerFragment.class.getSimpleName();

    private FrameLayout mPeriodFading;

    private TextView mEstimatedTime;
    private TextView mDownloadSpeed;
    private CircleProgressBar mSyncerProgress;
    private TextView mStatusMessage;

    private Player mPlayer = Player.getInstance();
    private Syncer mSyncer = Syncer.getInstance();

    private ImageSquareButton mScheduleButton;
    private ImageSquareButton mPlayButton;
    private ImageSquareButton mAnnounceButton;
    private SeekBar mVolumeSlider;

    private TextView mEmptySchedule;

    private View     mPeriodView;
    private TextView mPeriodTime;
    private AutoScrollTextView mPeriodDescription;
    private AutoScrollTextView mPeriodStyles;

    private Handler playInTimer = new Handler() {
        int time = 0;

        @Override
        public void handleMessage(Message msg) {
            if (mPlayer.getState() != Player.STATE_WAITING) {
                return;
            }

            if (msg.what == 0) {
                removeCallbacksAndMessages(null);
                time = (int) ((mPlayer.getPeriod().getDelay() - System.currentTimeMillis()) / 1000);
            }

            mPlayButton.setText(ParseUtils.humanizeDurationDigit((time)));
            sendEmptyMessageDelayed(1, 1000);

            time--;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(false);

        setHasOptionsMenu(true);

        // status
        Typeface museo300 = TypefaceCache.get(TypefaceCache.FONT_MUSEO_300);
        Typeface museo500 = TypefaceCache.get(TypefaceCache.FONT_MUSEO_500);
        Typeface plumbLight = TypefaceCache.get(TypefaceCache.FONT_PLUMB_LIGHT);

        mEstimatedTime = (TextView) view.findViewById(R.id.text_estimated_time);
        mEstimatedTime.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));

        mDownloadSpeed = (TextView) view.findViewById(R.id.text_download_speed);
        mDownloadSpeed.setTypeface(museo500);

        mSyncerProgress = (CircleProgressBar) view.findViewById(R.id.progress_syncer_progress);
        mSyncerProgress.setTypeface(museo500);

        mStatusMessage = (TextView) view.findViewById(R.id.text_status_message);
        mStatusMessage.setTypeface(museo500);

        // controls

        mScheduleButton = (ImageSquareButton) view.findViewById(R.id.button_schedule);
        mScheduleButton.setOnClickListener(this);

        mPlayButton = (ImageSquareButton) view.findViewById(R.id.button_play);
        mPlayButton.setOnClickListener(this);
        mPlayButton.setTypeface(museo500);

        mAnnounceButton = (ImageSquareButton) view.findViewById(R.id.button_announce);
        mAnnounceButton.setOnClickListener(this);

        mVolumeSlider = (SeekBar) view.findViewById(R.id.slider_volume);
        mVolumeSlider.setMax(mPlayer.getMaxVolume() * 5);
        mVolumeSlider.setProgress(mPlayer.getVolume() * 5);
        mVolumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int currentVolume = 0;

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                currentVolume = mPlayer.getVolume();
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int volume, boolean fromUser) {
                if (currentVolume != volume / 5) mPlayer.setVolume(volume / 5);
            }
        });

        // period

        mPeriodFading = (FrameLayout) view.findViewById(R.id.view_period_fading);

        mEmptySchedule = (TextView) view.findViewById(R.id.view_empty_period);
        mEmptySchedule.setTypeface(museo300);

        mPeriodView = view.findViewById(R.id.view_period);

        mPeriodTime = (TextView) view.findViewById(R.id.text_period_time);
        mPeriodTime.setTypeface(TypefaceCache.get(TypefaceCache.FONT_PLUMB_LIGHT));

        mPeriodStyles = (AutoScrollTextView) view.findViewById(R.id.text_period_styles);
        mPeriodStyles.setTypeface(TypefaceCache.get(TypefaceCache.FONT_PLUMB_LIGHT));
        mPeriodStyles.setTimePerLetter(600);

        mPeriodDescription = (AutoScrollTextView) view.findViewById(R.id.text_period_description);
        mPeriodDescription.setTypeface(museo500);
        mPeriodDescription.setTimePerLetter(250);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        mVolumeSlider.setProgress(mPlayer.getVolume() * 5);
    }

    @Override
    public void onPause() {
        super.onPause();

        playInTimer.removeCallbacksAndMessages(null);
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

    public void onEventMainThread(Events.PlaceChangedEvent event) {
        Place place = event.getPlace();

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(Radiant.formatHeader(place.getName()));
    }

    public synchronized void onEventMainThread(Events.SyncerStateChanged event) {
        // layout

        if (event.getState() == Syncer.STATE_INDEXING) return;

        FrameLayout.MarginLayoutParams layoutParams = (FrameLayout.MarginLayoutParams) mSyncerProgress.getLayoutParams();

        if (event.getState() == Syncer.STATE_SYNCING) {
            mStatusMessage.setVisibility(View.GONE);
            mDownloadSpeed.setVisibility(View.VISIBLE);
            mEstimatedTime.setVisibility(View.VISIBLE);

            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    8,
                    getResources().getDisplayMetrics());

            layoutParams.setMargins(px, 0, px, 0);
        } else {
            mStatusMessage.setVisibility(View.VISIBLE);
            mDownloadSpeed.setVisibility(View.GONE);
            mEstimatedTime.setVisibility(View.GONE);

            int px = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    16,
                    getResources().getDisplayMetrics());

            layoutParams.setMargins(0, 0, px, 0);
        }

        mSyncerProgress.setLayoutParams(layoutParams);

        // color

        int color = R.color.state_blue;
        int status = R.string.empty;

        switch (event.getState()) {
            case Syncer.STATE_SYNCED:
                color = R.color.state_green;
                status = R.string.message_syncer_state_synced;
                break;

            case Syncer.STATE_STOPPED:
                color = R.color.state_yellow;
                status = R.string.message_syncer_state_stopped;
                break;

            case Syncer.STATE_IDLE_NO_INTERNET:
                color = R.color.state_yellow;
                status = R.string.message_syncer_state_idle_no_internet;
                break;

            case Syncer.STATE_FAILED_NO_SPACE:
                color = R.color.state_red;
                status = R.string.message_syncer_state_failed_no_space;
                break;

            case Syncer.STATE_FAILED_NO_STORAGE:
                color = R.color.state_red;
                status = R.string.message_syncer_state_failed_no_storage;
                break;

            case Syncer.STATE_SYNCING:
                if (mPlayer.isMusicEnough()) {
                    color = R.color.state_green;
                } else {
                    color = R.color.state_blue;
                }
                break;
        }

        mSyncerProgress.setTextColor(getResources().getColor(color));
        mSyncerProgress.setProgressForegroundColor(getResources().getColor(color));

        mStatusMessage.setTextColor(getResources().getColor(color));
        mStatusMessage.setText(status);
    }

    public void onEventMainThread(Events.SyncerProgressChanged event) {
        mDownloadSpeed.setText(ParseUtils.humanizeSpeed(event.getDownloadSpeed()));
        mEstimatedTime.setText(ParseUtils.humanizeDuration(event.getEstimatedTime()));
    }

    public void onEventMainThread(Events.SyncerSyncedPercentChanged event) {
        mSyncerProgress.setText(event.getSyncedPercent() + "%");
        mSyncerProgress.setProgress(event.getSyncedPercent());

        // color wheel

        if (mPlayer.getState() != Syncer.STATE_SYNCING) return;

        int color;

        if (mPlayer.isMusicEnough()) {
            color = R.color.state_green;
        } else {
            color = R.color.state_blue;
        }

        mSyncerProgress.setTextColor(getResources().getColor(color));
        mSyncerProgress.setProgressForegroundColor(getResources().getColor(color));
    }

    public void onEventMainThread(Events.PlayerStateChanged event) {
        switch (event.getState()) {
            case Player.STATE_STOPPED:
                mPlayButton.mimicImageButton();
                mPlayButton.setDrawableCenter(R.drawable.player_button_play);
                break;

            case Player.STATE_INDEXING:
                mPlayButton.mimicImageButton();
                mPlayButton.setDrawableCenter(R.drawable.player_button_pause);
                break;

            case Player.STATE_IDLE_PERIODS_REQUIRED:
                mPlayButton.mimicImageButton();
                mPlayButton.setDrawableCenter(R.drawable.player_button_pause);
                break;

            case Player.STATE_PLAYING:
                mPlayButton.mimicImageButton();
                mPlayButton.setDrawableCenter(R.drawable.player_button_pause);
                break;

            case Player.STATE_WAITING:
                mPlayButton.mimicTextButton();
                mPlayButton.setDrawableTop(R.drawable.player_button_wait);
                playInTimer.sendEmptyMessage(0);
                break;
        }
    }

    public void onEventMainThread(Events.PlayerVolumeChanged event) {
        mVolumeSlider.setProgress(event.getVolume() * 5);
    }

    private void togglePlayerState() {

        if (mPlayer.getState() != Player.STATE_STOPPED) {
            mPlayer.stop();
        } else {
            // music enought

            if (mPlayer.isMusicEnough()) {
                mPlayer.play();
            } else {
                // sync off and not enough music

                if (mSyncer.getState() == Syncer.STATE_STOPPED) {
                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(getActivity());
                    errorDialog.setMessage(R.string.message_turn_on_sync_to_play);
                    errorDialog.setPositiveButton(R.string.button_turn_on, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent service = new Intent(getActivity(), DownloadService.class);
                            getActivity().startService(service);

                            mSyncer.stop(Syncer.STATE_PREPARING);
                            togglePlayerState();
                        }
                    });
                    errorDialog.setNegativeButton(R.string.button_cancel, null);
                    errorDialog.show();
                } else

                // has internet and not enough music

                if (NetworkUtils.isNetworkConnected()) {
                    mPlayer.play();

                    Toast.makeText(getActivity(), R.string.message_player_will_stream, Toast.LENGTH_LONG).show();
                } else

                // no internet and not enough music

                {
                    mPlayer.stop();

                    Toast.makeText(getActivity(), R.string.message_player_no_internet, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void openSchedulerFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(android.R.id.content, new PeriodsFragment(), PeriodsFragment.TAG);
        transaction.addToBackStack(PeriodsFragment.TAG);
        transaction.commit();
    }

    private void openPreferencesFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(android.R.id.content, new PreferencesFragment(), PreferencesFragment.TAG);
        transaction.addToBackStack(PeriodsFragment.TAG);
        transaction.commit();
    }

    // PERIOD

    public void onEventMainThread(Events.PlayerPeriodChanged event) {
        playInTimer.sendEmptyMessage(0);

        Period period = event.getPeriod();

        colorize(period);

        if (period == null) {
            getView().findViewById(R.id.view_empty_period).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.view_normal_period).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.view_empty_period).setVisibility(View.GONE);
            getView().findViewById(R.id.view_normal_period).setVisibility(View.VISIBLE);

            mPeriodTime.setText(ParseUtils.humanizeDay(period.getDay(), true).toUpperCase() + " / " + ParseUtils.humanizeTimeRange(period.getStartAt(), period.getEndAt()));

            mPeriodDescription.setText(period.getGenre().getDescription());
            mPeriodDescription.startScroll();

            mPeriodStyles.setText(TextUtils.join(" / ", Style.collectNames(period.getGenre().getStyles())));
            mPeriodStyles.startScroll();
        }
    }

    private void colorize(Period period) {
        if (period == null) {
            mPeriodView.setBackgroundColor(getResources().getColor(R.color.empty));
            mPeriodFading.setForeground(null);
        } else {
            int   index  = period.getGenre().getColorIndex();
            int[] colors = getResources().getIntArray(R.array.covers);
            int[] colorsFading = getResources().getIntArray(R.array.covers_fading);

            mPeriodView.setBackgroundColor(colors[index]);

            int d = 0;

            switch (index) {
                case 0:
                    d = R.drawable.player_cover_fade_1;
                    break;
                case 1:
                    d = R.drawable.player_cover_fade_2;
                    break;
                case 2:
                    d = R.drawable.player_cover_fade_3;
                    break;
                case 3:
                    d = R.drawable.player_cover_fade_4;
                    break;
                case 4:
                    d = R.drawable.player_cover_fade_5;
                    break;
                case 5:
                    d = R.drawable.player_cover_fade_6;
                    break;
            }

            mPeriodFading.setForeground(getResources().getDrawable(d));
        }
    }
}
