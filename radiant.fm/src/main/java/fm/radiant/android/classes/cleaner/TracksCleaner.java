package fm.radiant.android.classes.cleaner;

import android.content.Context;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Track;

public class TracksCleaner extends AbstractCleaner {
    private Context context;

    public TracksCleaner(Context context, Collection<Track> queue) {
        super(queue);

        this.context = context;
    }

    @Override
    protected File getDirectory() {
        return new File(context.getExternalFilesDir(null), Track.getDirectoryName());
    }
}
