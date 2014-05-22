package fm.radiant.android.models;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fm.radiant.android.interfaces.AudioModel;

public class Track extends AudioModel {
    private int styleId;

    public static Track selectRandom(List<Track> tracks, final Collection<Integer> styleIds) {
        List<Track> cloned = new ArrayList<Track>(tracks);
        Collections.shuffle(cloned);

        return Iterables.find(cloned, new Predicate<Track>() {
            @Override
            public boolean apply(Track track) {
                 return styleIds.contains(track.getStyleId());
            }
        }, null);
    }

    public Integer getStyleId() {
        return styleId;
    }
}