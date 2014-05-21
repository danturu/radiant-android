package fm.radiant.android.comparators;

import java.util.Comparator;

import fm.radiant.android.models.Period;

public class CurrentPeriodComparator implements Comparator<Period> {
    @Override
    public int compare(Period first, Period second) {
        return first.getInterval().getStart().compareTo(second.getInterval().getStart());
    }
}