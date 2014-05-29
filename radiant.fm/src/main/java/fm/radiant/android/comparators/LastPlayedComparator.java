package fm.radiant.android.comparators;


import android.util.SparseIntArray;

import org.apache.commons.lang.ObjectUtils;

import java.util.Comparator;

import fm.radiant.android.models.Track;

public class LastPlayedComparator implements Comparator<Track> {
    SparseIntArray dates;

    public LastPlayedComparator(SparseIntArray dates) {
        this.dates = dates;
    }

    @Override
    public int compare(Track first, Track second) {
        return ObjectUtils.compare(dates.get(first.getId()), dates.get(second.getId()));
    }
}