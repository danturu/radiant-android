package fm.radiant.android.models;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import fm.radiant.android.interfaces.Modelable;

public class Period extends Modelable {
    private int day;
    private int startAt;
    private int endAt;

    private Genre genre;

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

    public void timeUntil() {

    }

    public void isNow() {

    }

}

/*
    timeRange: (currentTime) ->
      startTime = Moment(currentTime).startOf("week").day(1 + @get "day").minutes(@get "startAt")
      endTime   = Moment(currentTime).startOf("week").day(1 + @get "day").minutes(@get "endAt")

      [
        startTime.add "week", endTime.isBefore currentTime
        endTime.add   "week", endTime.isBefore currentTime
      ]

    timeUntil: (currentTime) ->
      currentTime = Moment     currentTime
      timeRange   = @timeRange currentTime

      [
        timeRange[0] - currentTime
        timeRange[1] - currentTime + 1
      ]

    isNow: (currentTime) ->
      currentTime = Moment     currentTime
      timeRange   = @timeRange currentTime

      timeRange[0] <= currentTime <= timeRange[1]
@Expose
    private int day;

    @Expose
    private int startAt;

    @Expose
    private int endAt;

    @Expose
    private int duration;

    @Expose
    private Genre genre = new Genre();

    @Override
    public int compareTo(Period another) {
        return getSecondsUntilStart().compareTo(another.getSecondsUntilStart());
    }

    public static Period findNext(List<Period> periods) {
        try {
            return Collections.min(periods);
        } catch (NoSuchElementException exception) {
            return null;
        }
    }

    public int getDay() {
        return day + 1;
    }

    public DateTime getStartAt() {
        return getInterval().getStart();
    }

    public DateTime getEndAt() {
        return getInterval().getEnd();
    }

    public Seconds getSecondsUntilStart() {
        return Seconds.secondsBetween(new DateTime(), getStartAt());
    }

    public Seconds getSecondsUntilEnd() {
        return Seconds.secondsBetween(new DateTime(), getEndAt());


    public Genre getGenre() {
        return genre;
    }

    private Interval getInterval() {
        DateTime startTime = new DateTime().withDayOfWeek(getDay()).withMillisOfDay(startAt * 60000);
        DateTime endTime   = new DateTime().withDayOfWeek(getDay()).withMillisOfDay(endAt   * 60000 - 1);

        int offset = endTime.isBeforeNow() ? 7 : 0;

        return new Interval(startTime.plusDays(offset), endTime.plusDays(offset));
    }

 */