package fm.radiant.android.classes.optimizer;

import android.content.Context;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Ad;

public class AdsOptimizer extends AbstractOptimizer{
    private Context context;

    public AdsOptimizer(Context context, Collection<Ad> queue) {
        super(queue);

        this.context = context;
    }

    @Override
    protected File getDirectory() {
        return new File(context.getExternalFilesDir(null), Ad.getDirectoryName());
    }
}
