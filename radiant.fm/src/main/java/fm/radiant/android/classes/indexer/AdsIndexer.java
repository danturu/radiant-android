package fm.radiant.android.classes.indexer;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;
import java.util.Collection;

import fm.radiant.android.models.Ad;

public class AdsIndexer extends AbstractIndexer {
    private static final String TAG = "AdsIndexer";

    private Context context;

    public AdsIndexer(Context context, Collection<Ad> queue) {
        super(queue);

        this.context = context;
    }

    @Override
    public File getDirectory() {
        return new File(context.getExternalFilesDir(null), Ad.getDirectoryName());
    }

    @Override
    public Class getModelClass() {
        return Ad.class;
    }

    @Override
    public String getIndexerName() {
        return TAG;
    }

    @Override
    public boolean isBalancedQueue() {
        return true;
    }
}