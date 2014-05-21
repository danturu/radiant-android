package fm.radiant.android.models;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fm.radiant.android.interfaces.Audioable;

public class Track extends Audioable {
    private int styleId;

    public static Track findRandom(List<Track> tracks, final Collection<Integer> styleIds) {
        Collections.shuffle(tracks);

        return Iterables.find(tracks, new Predicate<Track>() {
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