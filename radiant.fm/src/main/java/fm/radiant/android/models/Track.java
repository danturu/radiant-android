package fm.radiant.android.models;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import org.apache.commons.lang.math.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Track extends AudioModel {
    @Expose
    private int styleId;

    public static Track getRandom(List<Track> tracks, final List<Style> styles) {
        List<Track> cloned = new ArrayList<Track>(tracks);

        Collections.shuffle(cloned);

        return Iterables.find(cloned, new Predicate<Track>() {
            List<Integer> styleIds = Style.collectIds(styles);

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