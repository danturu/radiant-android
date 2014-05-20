package fm.radiant.android.classes.indexer;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Track;

public class TracksIndexer extends AbstractIndexer {
    private static final String TAG = "TracksIndexer";

    private Context context;

    public TracksIndexer(Context context, Collection<Track> queue) {
        super(queue);

        this.context = context;
    }

    @Override
    public File getDirectory() {
        return new File(context.getExternalFilesDir(null), Track.getDirectoryName());
    }

    @Override
    public Class getModelClass() {
        return Track.class;
    }

    @Override
    public String getIndexerName() {
        return TAG;
    }

    @Override
    public boolean isBalancedQueue() {
        return false;
    }
}