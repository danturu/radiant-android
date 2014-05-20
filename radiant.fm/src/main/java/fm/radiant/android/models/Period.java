package fm.radiant.android.models;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Seconds;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fm.radiant.android.comparators.CurrentPeriodComparator;
import fm.radiant.android.interfaces.Modelable;

public class Period extends Modelable {
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
        DateTime startTime = new DateTime().withDayOfWeek(day + 1).withMillisOfDay(startAt * 60000);
        DateTime endTime   = new DateTime().withDayOfWeek(day + 1).withMillisOfDay(endAt   * 60000 - 1);

        int isNextWeek = endTime.isBeforeNow() ? 1 : 0;

        return new Interval(startTime.plusWeeks(isNextWeek), endTime.plusWeeks(isNextWeek));
    }

    public long getDelay() {
        Interval interval = getInterval();

        return interval.getStart().isAfterNow() ? interval.getStartMillis() : interval.getEndMillis();
    }

    public boolean isNow() {
        return getInterval().containsNow();
    }
}