package fm.radiant.android.models;

import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Collections;
import java.util.List;

import fm.radiant.android.comparators.CurrentPeriodComparator;

import static org.joda.time.DateTimeFieldType.dayOfWeek;
import static org.joda.time.DateTimeFieldType.millisOfDay;

public class Period extends Model {
    @Expose
    private int day;

    @Expose
    private int duration;

    @Expose
    private int startAt;

    @Expose
    private int endAt;

    @Expose
    private Genre genre;

    public static Period findCurrent(List<Period> periods) {
        Collections.sort(periods, new CurrentPeriodComparator());

        return periods.get(0);
    }

    public Integer getDay() {
        return day;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public Integer getEndAt() {
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
}