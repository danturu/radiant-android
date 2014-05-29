package fm.radiant.android.comparators;

import org.apache.commons.lang.ObjectUtils;

import java.util.Comparator;

import fm.radiant.android.models.Period;

public class CurrentPeriodComparator implements Comparator<Period> {
    @Override
    public int compare(Period first, Period second) {
        return ObjectUtils.compare(first.getInterval().getStart(), second.getInterval().getStart());
    }
}