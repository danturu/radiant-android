package fm.radiant.android.fragments;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;

import fm.radiant.android.R;
import fm.radiant.android.activities.AuthActivity;
import fm.radiant.android.activities.MainActivity;
import fm.radiant.android.lib.widgets.SquareButton;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.LibraryUtils;

public class PlayerFragment extends Fragment implements View.OnClickListener {
    View period;
    SeekBar volumeSlider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        period = view.findViewById(R.id.period);

        SquareButton button;

        button = (SquareButton) view.findViewById(R.id.button_play);
        button.setOnClickListener(this);

        button = (SquareButton) view.findViewById(R.id.button_schedule);
        button.setOnClickListener(this);

        button = (SquareButton) view.findViewById(R.id.button_announce);
        button.setOnClickListener(this);

        volumeSlider = (SeekBar) view.findViewById(R.id.slider_volume);
        volumeSlider.setMax(LibraryUtils.getPlayer().getMaxVolume());
        volumeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int level, boolean fromUser) {
                if (fromUser) {
                    LibraryUtils.getPlayer().setVolume(level);
                }
            }
        });

        render();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_play:
                ((ImageButton) view).setImageResource(R.drawable.player_button_pause);
                LibraryUtils.getPlayer().setTracksIndexer(LibraryUtils.getTracksIndexer());
                LibraryUtils.getPlayer().setAdsIndexer(LibraryUtils.getAdsIndexer());
                LibraryUtils.getPlayer().play();
                break;

            case R.id.button_schedule:
                showSchedulerFragment();
                break;
        }
    }

    private void render() {
        volumeSlider.setProgress(LibraryUtils.getPlayer().getVolume());

        int[] colors = getResources().getIntArray(R.array.covers);

        period.setBackgroundColor(colors[LibraryUtils.getPlayer().getPeriod().getGenre().getColorIndex()]);
    }

    private void showSchedulerFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        transaction.replace(MainActivity.getContentViewCompat(), new SchedulerFragment(), "scheduler");
        transaction.commit();
    }
}
