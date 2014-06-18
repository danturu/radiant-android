package fm.radiant.android.lib.indexer;

import android.content.Context;

import java.util.List;

import fm.radiant.android.models.Ad;
import fm.radiant.android.models.AudioModel;

public class AdsIndexer extends AbstractIndexer {
    public AdsIndexer(Context context, List<Ad> queue) {
        super(context, queue);
    }

    @Override
    public List<Ad> getPersistedQueue() {
        return (List<Ad>) super.getPersistedQueue();
    }

    @Override
    public List<Ad> getRemotedQueue() {
        return (List<Ad>) super.getRemotedQueue();
    }

    @Override
    protected void onPersistedModel(AudioModel model) {
        // no implementation necessary...
    }

    @Override
    protected void onRemotedModel(AudioModel model) {
        // no implementation necessary...
    }
}