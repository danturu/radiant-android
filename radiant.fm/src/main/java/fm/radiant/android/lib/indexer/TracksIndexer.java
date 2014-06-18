package fm.radiant.android.lib.indexer;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fm.radiant.android.lib.ExtendedSparseIntArray;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.models.Period;
import fm.radiant.android.models.Style;
import fm.radiant.android.models.Track;

public class TracksIndexer extends AbstractIndexer {
    private ExtendedSparseIntArray mPersistedMinutes = new ExtendedSparseIntArray();
    private ExtendedSparseIntArray mTotalMinutes     = new ExtendedSparseIntArray();
    private Set<Integer> mStyleIds = new HashSet<Integer>();

    public TracksIndexer(Context context, List<Track> queue) {
        super(context, queue);

        for (Track track : queue) {
            mStyleIds.add(track.getStyleId()); mTotalMinutes.inc(track.getStyleId(), track.getAudio().getTimeInSeconds());
        }
    }

    @Override
    public List<Track> getPersistedQueue() {
        return (List<Track>) super.getPersistedQueue();
    }

    @Override
    public List<Track> getRemotedQueue() {
        return (List<Track>) super.getRemotedQueue();
    }

    @Override
    public List<Track> getQueue() {
        return (List<Track>) super.getQueue();
    }

    @Override
    protected void onPersistedModel(AudioModel model) {
        Track track = (Track) model;

        mPersistedMinutes.inc(track.getStyleId(), track.getAudio().getTimeInSeconds());
    }

    @Override
    protected void onRemotedModel(AudioModel model) {
        // no implementation necessary...
    }

    public List<Track> getBalancedRemotedQueue() {
        List<Track> remotedQueue = getRemotedQueue();

        Collections.shuffle(remotedQueue);

        return remotedQueue;
    }

    public Integer getTotalMinutes(int styleId) {
        return mTotalMinutes.get(styleId);
    }

    public Integer getPersistedMinutes(int styleId) {
        return mPersistedMinutes.get(styleId);
    }

    public Set<Integer> getStyleIds() {
        return mStyleIds;
    }
}