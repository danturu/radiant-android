package fm.radiant.android.models;

import com.google.gson.annotations.Expose;

import org.apache.commons.lang.math.RandomUtils;

import java.util.List;

import fm.radiant.android.lib.AudioModel;

public class Track extends AudioModel {
    @Expose
    private int styleId;

    public static Track getRandom(List<Track> tracks, List<Style> styles) {

        return tracks.get(RandomUtils.nextInt(tracks.size()));
    }

    public Integer getStyleId() {
        return styleId;
    }
}