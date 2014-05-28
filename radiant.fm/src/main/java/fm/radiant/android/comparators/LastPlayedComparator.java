package fm.radiant.android.comparators;


import android.util.SparseIntArray;

import java.util.Comparator;

import fm.radiant.android.models.Track;

public class LastPlayedComparator implements Comparator<Track> {
    SparseIntArray index;

    public LastPlayedComparator(SparseIntArray index) {
        super();

        this.index = index;
    }

    @Override
    public int compare(Track first, Track second) {
        return ((Integer) index.get(first.getId())).compareTo(index.get(second.getId()));
    }
}