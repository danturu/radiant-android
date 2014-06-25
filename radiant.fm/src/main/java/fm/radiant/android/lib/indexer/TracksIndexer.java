package fm.radiant.android.lib.indexer;

import android.content.Context;

import org.apache.commons.lang.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fm.radiant.android.lib.ExtendedSparseIntArray;
import fm.radiant.android.models.AudioModel;
import fm.radiant.android.models.Track;

public class TracksIndexer extends AbstractIndexer {
    public static final float VALUE_STREAM_TRACKS_RATIO = 0.5f;

    private ExtendedSparseIntArray mPersistedSeconds = new ExtendedSparseIntArray();
    private ExtendedSparseIntArray mTotalSeconds     = new ExtendedSparseIntArray();
    private Set<Integer> mStyleIds = new HashSet<Integer>();

    private Comparator<Track> trackByStyleIdComparator = new Comparator<Track>() {
        public int compare(Track first, Track second) {
            return ObjectUtils .compare(first.getStyleId(), second.getStyleId());
        }
    };

    public TracksIndexer(Context context, List<Track> queue) {
        super(context, queue);

        for (Track track : queue) {
            mStyleIds.add(track.getStyleId()); mTotalSeconds.inc(track.getStyleId(), track.getAudio().getTimeInSeconds());
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

        mPersistedSeconds.inc(track.getStyleId(), track.getAudio().getTimeInSeconds());
    }

    @Override
    protected void onRemotedModel(AudioModel model) {
        // no implementation necessary...
    }

    public List<Track> getBalancedRemotedQueue() {
        List<Track> cloned = new ArrayList<Track>(getRemotedQueue());
        List<Track> result = new ArrayList<Track>();

        if (cloned.isEmpty()) return cloned;

        Collections.sort(cloned, trackByStyleIdComparator);

        for (Integer styleId : mStyleIds) {
            int requiredSeconds = (int) (getTotalSeconds(styleId) * VALUE_STREAM_TRACKS_RATIO) - getPersistedSeconds(styleId);
            Track searchFor = new Track(styleId);

            while (requiredSeconds > 0) {
                int index = Collections.binarySearch(cloned, searchFor, trackByStyleIdComparator);

                if (index < 0) {
                    break;
                } else {
                    Track track = cloned.remove(index);
                    result.add(track);
                    requiredSeconds -= track.getAudio().getTimeInSeconds();
                }
            }
        }

        Collections.shuffle(cloned);
        Collections.shuffle(result);
        result.addAll(cloned);

        return result;
    }

    public Integer getTotalSeconds(int styleId) {
        return mTotalSeconds.get(styleId);
    }

    public Integer getPersistedSeconds(int styleId) {
        return mPersistedSeconds.get(styleId);
    }

    public Set<Integer> getStyleIds() {
        return mStyleIds;
    }

    public boolean isMusicEnough() {
        for (Integer styleId : mStyleIds) {
            if ((int) (getTotalSeconds(styleId) * VALUE_STREAM_TRACKS_RATIO) - getPersistedSeconds(styleId) > 0) return false;
        }

        return true;
    }
}