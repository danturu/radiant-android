package fm.radiant.android.lib.cleaner;

import android.content.Context;

import java.util.List;

import fm.radiant.android.models.Ad;

public class AdsCleaner extends AbstractCleaner {
    public AdsCleaner(Context context, List<Ad> queue) {
        super(queue, new Ad().getDirectory(context));
    }
}
