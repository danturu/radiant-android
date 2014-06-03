package fm.radiant.android.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.List;

import fm.radiant.android.R;
import fm.radiant.android.activities.MainActivity;
import fm.radiant.android.lib.TypefaceSpan;
import fm.radiant.android.lib.widgets.SquareButton;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Style;
import fm.radiant.android.utils.LibraryUtils;

public class PlayerFragment extends Fragment implements View.OnClickListener {
    public  static String TAG = PlayerFragment.class.getSimpleName();
    View period;
    SeekBar volumeSlider;
    TextView periodTime;
    TextView periodDescription;
    TextView periodStyles;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        period = view.findViewById(R.id.period);

        setHasOptionsMenu(true);

        SquareButton button;

        button = (SquareButton) view.findViewById(R.id.button_play);
        button.setOnClickListener(this);

        button = (SquareButton) view.findViewById(R.id.button_schedule);
        button.setOnClickListener(this);

        button = (SquareButton) view.findViewById(R.id.button_announce);
        button.setOnClickListener(this);

        Typeface museo300 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/museo_sans_300.ttf");
        Typeface museo500 = Typeface.createFromAsset(getActivity().getAssets(), "fonts/museo_sans_500.ttf");

        periodTime = (TextView) view.findViewById(R.id.period_time);
        periodDescription = (TextView) view.findViewById(R.id.period_description);
        periodStyles = (TextView) view.findViewById(R.id.period_styles);

        periodTime.setTypeface(museo500);
        periodDescription.setTypeface(museo300);
        periodStyles.setTypeface(museo500);

        volumeSlider = (SeekBar) view.findViewById(R.id.slider_volume);
        volumeSlider.setMax(LibraryUtils.getPlayer().getMaxVolume() * 100);
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
                    LibraryUtils.getPlayer().setVolume(level / 100);
                }
            }
        });

        render();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        SpannableString s = new SpannableString("Расписание");
        s.setSpan(new TypefaceSpan(getActivity(), "museo_sans_500.ttf"), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(s);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        // switch (item.getItemId()) {
        //     case R.id.action_search:
        //         openSearch();
        //         return true;
        //     case R.id.action_compose:
        //         composeMessage();
        //         return true;
        //     default:
        //         return super.onOptionsItemSelected(item);
        // }
        return super.onOptionsItemSelected(item);
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
        Period currentPeriod = LibraryUtils.getPlayer().getPeriod();

        volumeSlider.setProgress(LibraryUtils.getPlayer().getVolume());

        int[] colors = getResources().getIntArray(R.array.covers);

        period.setBackgroundColor(colors[currentPeriod.getGenre().getColorIndex()]);


        //
        DateTime now = new DateTime().withDayOfWeek(currentPeriod.getDay());

        String dayName = WordUtils.capitalize((DateTimeFormat.forPattern("EEEE").print(now)));
        periodTime.setText(dayName.toUpperCase() + " · " + Integer.toString(currentPeriod.getStartAt() / 60) + ":00 - " + Integer.toString(currentPeriod.getEndAt() / 60) + ":00");

        periodDescription.setText(currentPeriod.getGenre().getDescription() + currentPeriod.getGenre().getDescription());

        List<String> stylesNames = Lists.newArrayList(Iterables.transform(currentPeriod.getGenre().getStyles(),
                new Function<Style, String>() {
                    @Override
                    public String apply(Style style) {
                        return style.getName();
                    }
                }
        ));

        periodStyles.setText(TextUtils.join(" · ", stylesNames));

    }



    private void showSchedulerFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left);
        transaction.replace(MainActivity.getContentViewCompat(), new SchedulerFragment(), "scheduler");
        transaction.commit();
    }
}
