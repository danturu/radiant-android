package fm.radiant.android.lib.cleaner;

import android.content.Context;

import java.util.List;

import fm.radiant.android.models.Track;

public class TracksCleaner extends AbstractCleaner {
    public TracksCleaner(Context context, List<Track> queue) {
        super(queue, new Track().getDirectory(context));
    }
}