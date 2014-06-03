package fm.radiant.android.comparators;


import org.apache.commons.lang.ObjectUtils;

import java.util.Comparator;

import fm.radiant.android.models.Period;

public class NextPeriodComparator implements Comparator<Period> {
    @Override
    public int compare(Period first, Period second) {
        return ObjectUtils.compare(first.getDay() * 10000 + first.getStartAt(), second.getDay() * 10000 + second.getStartAt());
    }
}
