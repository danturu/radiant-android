package fm.radiant.android.classes.optimizer;

import android.content.Context;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Track;

public class TracksOptimizer extends AbstractOptimizer{
    private Context context;

    public TracksOptimizer(Context context, Collection<Track> queue) {
        super(queue);

        this.context = context;
    }

    @Override
    protected File getDirectory() {
        return new File(context.getExternalFilesDir(null), Track.getDirectoryName());
    }
}
