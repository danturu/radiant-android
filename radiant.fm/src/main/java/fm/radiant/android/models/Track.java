package fm.radiant.android.models;

import android.util.SparseIntArray;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import org.apache.commons.lang.math.RandomUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fm.radiant.android.comparators.LastPlayedComparator;
import fm.radiant.android.lib.AudioModel;

public class Track extends AudioModel {
    @Expose
    private int styleId;

    public static Track selectRandom(List<Track> tracks, final Collection<Integer> styleIds, SparseIntArray p) {
        List<Track> cloned = Lists.newArrayList(Iterables.filter(tracks, new Predicate<Track>() {
            @Override
            public boolean apply(Track track) {
                return styleIds.contains(track.getStyleId());
            }
        }));

        Collections.sort(cloned, new LastPlayedComparator(p));

        cloned = cloned.subList(0, 5);

        return cloned.get(RandomUtils.nextInt(cloned.size()));
    }

    public Integer getStyleId() {
        return styleId;
    }
}