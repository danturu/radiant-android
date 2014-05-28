package fm.radiant.android.classes.indexer;

import android.content.Context;

import java.util.List;

import fm.radiant.android.lib.AudioModel;
import fm.radiant.android.models.Ad;

public class AdsIndexer extends AbstractIndexer {
    public AdsIndexer(Context context, List<Ad> queue) {
        super(context, queue);
    }

    @Override
    public Class getModelClass() {
        return Ad.class;
    }

    @Override
    public boolean isFrontQueue() {
        return true;
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
    protected void onPersistentModel(AudioModel model) {
        // no implementation necessary...
    }
}