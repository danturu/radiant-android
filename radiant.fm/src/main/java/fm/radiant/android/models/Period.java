package fm.radiant.android.models;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Collections;
import java.util.List;

import fm.radiant.android.comparators.CurrentPeriodComparator;
import fm.radiant.android.interfaces.Model;

import static org.joda.time.DateTimeFieldType.dayOfWeek;
import static org.joda.time.DateTimeFieldType.millisOfDay;

public class Period extends Model {
    private int day;
    private int startAt;
    private int endAt;

    private Genre genre;

    public static Period findCurrent(List<Period> periods) {
        Collections.sort(periods, new CurrentPeriodComparator());

        return periods.get(0);
    }

    public int getDay() {
        return day;
    }

    public int getStartAt() {
        return startAt;
    }

    public int getEndAt() {
        return endAt;
    }

    public Genre getGenre() {
        return genre;
    }

    public Interval getInterval() {
        DateTime startTime = new DateTime().withField(dayOfWeek(), day + 1).withField(millisOfDay(), startAt * 60 * 1000);
        DateTime endTime   = new DateTime().withField(dayOfWeek(), day + 1).withField(millisOfDay(), endAt   * 60 * 1000 - 1);

        if (endTime.isBeforeNow()) {
            startTime = startTime.plusDays(7); endTime = endTime.plusDays(7);
        }

        return new Interval(startTime, endTime);
    }

    public long getDelay() {
        Interval interval = getInterval();

        if (interval.isBeforeNow()) {
            return interval.getStartMillis();
        } else {
            return interval.getEndMillis();
        }
    }

    public boolean isNow() {
        return getInterval().containsNow();
    }

    public List<Integer> collectStyleIds() {
        Iterable<Integer> styleIds = Iterables.transform(genre.getStyles(), new Function<Style, Integer>() {
            @Override
            public Integer apply(Style style) {
                return style.getId();
            }
        });

        return Lists.newArrayList(styleIds);
    }
}