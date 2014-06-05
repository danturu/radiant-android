package fm.radiant.android.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.radiant.android.Events;
import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.MainActivity;
import fm.radiant.android.lib.EventBusFragment;
import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.lib.TypefaceSpan;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Style;
import fm.radiant.android.utils.ParseUtils;

import static fm.radiant.android.lib.TypefaceCache.FONT_MUSEO_500;

public class SchedulerFragment extends EventBusFragment {
    public static String TAG = PlayerFragment.class.getSimpleName();

    private PeriodsAdapter mPeriodsAdapter;
    private ListView mPeriodsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scheduler, container, false);

        setHasOptionsMenu(true);

        mPeriodsAdapter = new PeriodsAdapter(getActivity());

        mPeriodsView = (ListView) view.findViewById(R.id.periods);
        mPeriodsView.setAdapter(mPeriodsAdapter);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(Radiant.formatHeader(getString(R.string.title_scheduler)));

        return view;
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

    private void openPreferencesFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(MainActivity.getContentViewCompat(), new PreferencesFragment(), PreferencesFragment.TAG);
        transaction.addToBackStack(SchedulerFragment.TAG);
        transaction.commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onEventMainThread(Events.PlaceChangedEvent event) {
        List<Period> periods = event.getPlace().getPeriods();

        mPeriodsAdapter.reset(periods);
    }

    private class PeriodsAdapter extends BaseAdapter {
        Context context;
        Map<Integer, Collection<Period>> mapped;

        List<Object> stored = new ArrayList<Object>();

        LayoutInflater inflater;

        public PeriodsAdapter(Context context) {
            this.context = context;

            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void reset(List<Period> items) {
            ArrayList<Period> clone = new ArrayList<Period>(items);
            Collections.sort(clone, new Period.NextPeriodComparator());

            stored.clear();

            mapped = new HashMap<Integer, Collection<Period>>();

            for (int i = 0; i < 7; i++) {
                mapped.put(i, new ArrayList<Period>());
            }

            for (Period period : clone) {
                mapped.get(period.getDay()).add(period);
            }

            for (Integer day : mapped.keySet()) {
                stored.add(day);

                Collection<Period> periods = mapped.get(day);

                if (periods.isEmpty()) {
                    stored.add(-1);
                } else {
                    for (Period p : periods) {
                        stored.add(p);
                    }
                }
            }

            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return stored.size();
        }

        @Override
        public Object getItem(int position) {
            return stored.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            Object item = getItem(position);

            if (item instanceof Integer) {
                if ((Integer) item == -1) {
                    return -1;
                } else {
                    return 0;
                }
            } else {
                return 1;
            }
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            switch (getItemViewType(position)) {
                case 0:
                    return getHeaderView(position, convertView, parent);

                case -1:
                    return getEmptyView(position, convertView, parent);

                case 1:
                    return getItemView(position, convertView, parent);
            }

            throw new IllegalArgumentException("Invalid view: " + String.valueOf(position));
        }

        private View getHeaderView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            if (view == null) view = inflater.inflate(R.layout.partial_scheduler_day, null);

            TextView dayNameView = (TextView) view.findViewById(R.id.text_day_name);

            dayNameView.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));
            dayNameView.setText(ParseUtils.humanizeDay((Integer) getItem(position)));

            return view;
        }

        private View getEmptyView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) convertView;

            if (view== null) view = new TextView(context);

            view.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));
            view.setText("ТИШИНА");
            view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
            view.setTextColor(Color.parseColor("#999999"));
            view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics())));
            view.setGravity(Gravity.CENTER);

            return view;
        }

        private View getItemView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            final Period period = (Period) getItem(position);

            if (view == null) view = inflater.inflate(R.layout.partial_scheduler_period, null);

            // color

            int[] colors = getResources().getIntArray(R.array.covers);
            int color = colors[period.getGenre().getColorIndex()];

            view.setBackgroundColor(color);

            // time

            TextView timeText = (TextView) view.findViewById(R.id.text_time);

            timeText.setTypeface(TypefaceCache.get(TypefaceCache.FONT_PLUMB_LIGHT));
            timeText.setTextColor(color);

            final String time = ParseUtils.humanizeTimeRange(period.getStartAt(), period.getEndAt());
            timeText.setText(time);

            // style

            TextView stylesText = (TextView) view.findViewById(R.id.text_styles);

            stylesText.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));
            stylesText.setText(TextUtils.join(" · ", Style.collectNames(period.getGenre().getStyles())));

            ImageButton infoButton = (ImageButton) view.findViewById(R.id.button_info);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SpannableString title = new SpannableString(ParseUtils.humanizeDay(period.getDay()) + " " + time);
                    title.setSpan(new TypefaceSpan(FONT_MUSEO_500), 0, title.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    SpannableString description = new SpannableString(period.getGenre().getDescription());
                    description.setSpan(new TypefaceSpan(TypefaceCache.FONT_MUSEO_300), 0, description.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    SpannableString ok = new SpannableString(getString(R.string.button_ok));
                    ok.setSpan(new TypefaceSpan(TypefaceCache.FONT_MUSEO_500), 0, ok.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    AlertDialog.Builder infoDialog = new AlertDialog.Builder(getActivity());
                    infoDialog.setTitle(title);
                    infoDialog.setMessage(description);
                    infoDialog.setPositiveButton(ok, null);
                    infoDialog.setCancelable(true);
                    infoDialog.show();
                }
            });

            return view;
        }
    }
}
