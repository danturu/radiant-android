package fm.radiant.android.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.comparators.NextPeriodComparator;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Style;
import fm.radiant.android.utils.AccountUtils;

public class SchedulerFragment extends Fragment implements View.OnClickListener {
    PeriodsAdapter periodsAdapter;
    ListView periodsView;


    private BroadcastReceiver renderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            render();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scheduler, container, false);

        periodsAdapter = new PeriodsAdapter(getActivity());

        periodsView = (ListView) view.findViewById(R.id.periods);
        periodsView.setAdapter(periodsAdapter);

        return view;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onResume() {
        super.onResume();

        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle("Расписание");

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(renderReceiver,  new IntentFilter(Radiant.INTENT_PLACE_CHANGED));
        render();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(renderReceiver);
    }


    private void render() {
        List<Period> periods = AccountUtils.getPlace().getPeriods();

        periodsAdapter.reset(periods);
    }

    private class PeriodsAdapter extends BaseAdapter {
        int size = 0;
        Context context;
        //List<Period> items = new ArrayList<Period>();
       // ImmutableListMultimap<Integer, Period> items;
        Map<Integer, Collection<Period>> mapped;
        ImmutableListMultimap<Integer, Period> multimap;
        Typeface museo;
        Typeface plumb;

        List<Object> stored = new ArrayList<Object>();

        LayoutInflater inflater;

        public PeriodsAdapter(Context context) {
            this.context = context;

            inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            museo = Typeface.createFromAsset(context.getAssets(), "fonts/museo_sans_500.ttf");
            plumb = Typeface.createFromAsset(context.getAssets(), "fonts/plumb_condensed_light.ttf");

          //  reset(new ArrayList<Period>());
        }

        public void reset(List<Period> items) {

            ArrayList<Period> clone = new ArrayList<Period>(items);
            Collections.sort(clone, new NextPeriodComparator());

            stored.clear();

            mapped = new HashMap<Integer, Collection<Period>>();

            for (int i = 0; i < 7; i++) {
                mapped.put(i, new ArrayList<Period>());
                size++;
            }

            for (Period period : clone) {
                Log.d("day", "is" + period.getDay());
                mapped.get(period.getDay()).add(period);
            }

            for (Integer day : mapped.keySet()) {
                stored.add(day);

                Collection<Period> periods = mapped.get(day);

                if (periods.isEmpty()) {
                    stored.add(-1);
                } else {
                    int i = 0;
                    for (Period p : periods) {
                        stored.add(p);
                    //    if (i != periods.size()-1) stored.add(-2);
                      //  i++;
                    }
                }
            }
/*
            for (Collection<Period> periods : mapped.values()) {
                if (periods.isEmpty()) size++;
            }

/*
            List<Period> clone = new ArrayList<Period>(items);

            Collections.sort(clone, new PeriodPositionComparator());

            multimap = Multimaps.index(clone, new Function<Period, Integer>() {
                @Override
                public Integer apply(Period period) {
                    return period.getDay();
                }
            });

            mapped = multimap.asMap();
 */
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
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d("getView", ""+position + " type: " + getItemViewType(position));

            switch (getItemViewType(position)) {
                case 0:
                    return getHeaderView(position, convertView, parent);
                case -1:
                    return getEmptyView(position, convertView, parent);

                case 1:
                    return getItemView(position, convertView, parent);
            }

            throw new IllegalArgumentException("Invalid view: " + String.valueOf(position));

            /*
                return(getHeaderView(position, convertView, parent)); }
                View row=convertView;

            View view = convertView;

            if (view == null) {
                view = inflater.inflate(R.layout.partial_period, null);
            }*/

            //if (style != null) {
            //    Long availableMusic = (style.getDownloadedMusicAmount() < style.getRequiredMusicAmount() ? style.getDownloadedMusicAmount() : style.getRequiredMusicAmount());
            //    Integer percentage = (int) ((double) availableMusic / (double) style.getRequiredMusicAmount() * 100);
//
            //    TextView nameView = (TextView) view.findViewById(R.id.text_name);
            //    TextView downloadedView = (TextView) view.findViewById(R.id.text_downloaded);
            //    ProgressBar downloadedProgress = (ProgressBar) view.findViewById(R.id.downloaded);
//
            //    nameView.setText(style.getName());
            //    downloadedView.setText(percentage.toString() + "%");
//
            //    downloadedProgress.setMax(100);
            //    downloadedProgress.setProgress(percentage);
            //}
        }

        private View getHeaderView(int position, View convertView, ViewGroup parent) {
            View view = convertView;


           //if (view == null || i2 != R.layout.partial_day) {
                view = inflater.inflate(R.layout.partial_day, null);
           //}

            Integer day = (Integer) getItem(position);

            DateTime now = new DateTime().withDayOfWeek(day + 1);
            String dayName = WordUtils.capitalize((DateTimeFormat.forPattern("EEEE").print(now)));


            TextView dayNameView = (TextView) view.findViewById(R.id.text_day_name);

            if (dayNameView == null) {
                return view;
            }
            dayNameView.setTypeface(museo);
            dayNameView.setText(dayName);

            return view;
        }

        private View getEmptyView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

//            if (view == null) {
                view = inflater.inflate(R.layout.period_empty, null);
  //          }

            ((TextView) view.findViewById(R.id.text_empty)).setTypeface(museo);

            return view;
        }

        private View getItemView(int position, View convertView, ViewGroup parent) {
            View view = convertView;

            Period period = (Period) getItem(position);

            view = inflater.inflate(R.layout.partial_period, null);

            if (stored.size() < position && (stored.get(position + 1) instanceof Period)) {


               // view.setLayoutParams(params);
            }

            // color

            int[] colors = getResources().getIntArray(R.array.covers);
            int color = colors[period.getGenre().getColorIndex()];

            view.setBackgroundColor(color);

            // time

            TextView timeText = (TextView) view.findViewById(R.id.text_time);

            timeText.setTypeface(plumb);
            timeText.setTextColor(color);

            String startAt = StringUtils.leftPad(Integer.toString(period.getStartAt() / 60), 2, '0') + ":" + StringUtils.rightPad(Integer.toString(period.getStartAt() % 60), 2, '0');
            String endAt = StringUtils.leftPad(Integer.toString(period.getEndAt() / 60), 2, '0') + ":" + StringUtils.rightPad(Integer.toString(period.getEndAt() % 60), 2, '0');

            timeText.setText( startAt + " - " + endAt);

            // style

            List<String> stylesNames = Lists.newArrayList(Iterables.transform(period.getGenre().getStyles(),
                    new Function<Style, String>() {
                        @Override
                        public String apply(Style style) {
                            return style.getName();
                        }
                    }
            ));

            TextView stylesText = (TextView) view.findViewById(R.id.text_styles);

            stylesText.setTypeface(museo);
            stylesText.setText(TextUtils.join(" · ", stylesNames));

            return view;
        }
    }
}
