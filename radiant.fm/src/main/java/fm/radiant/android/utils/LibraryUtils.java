package fm.radiant.android.utils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang.StringUtils;

import fm.radiant.android.classes.indexer.AbstractIndexer;
import fm.radiant.android.classes.indexer.AdsIndexer;
import fm.radiant.android.classes.indexer.TracksIndexer;
import fm.radiant.android.classes.player.Player;
import fm.radiant.android.classes.syncer.Syncer;

public class LibraryUtils {
    private static final String TAG = "LibraryUtils";

    private static Context context;
    private static Syncer syncer;
    private static Player player;

    private static TracksIndexer tracksIndexer; private static AdsIndexer adsIndexer;

    public static void initialize(Context context) {
        LibraryUtils.context = context;
        LibraryUtils.player  = new Player(context);
    }

    public static void teardown() {

    }

    public static void inspect(AbstractIndexer indexer) {
        String tag = indexer.getClass().getSimpleName();

        String[] counts = new String[] {
                StringUtils.leftPad(Integer.toString(indexer.getPersistedCount()), 8),
                StringUtils.leftPad(Integer.toString(indexer.getRemotedCount()),   8),
                StringUtils.leftPad(Integer.toString(indexer.getTotalCount()),     8),
        };

        String[] sizes = new String[] {
                StringUtils.leftPad(Long.toString(indexer.getPersistedBytes()), 12),
                StringUtils.leftPad(Long.toString(indexer.getRemotedBytes()),   12),
                StringUtils.leftPad(Long.toString(indexer.getTotalBytes()),     12),
        };

        Log.i(tag, "+===+=========+==============+");
        Log.i(tag, "|   |   Count |         Size |");
        Log.i(tag, "+===+=========+==============+");
        Log.i(tag, "| P | " + counts[0]  + " | " + sizes[0] + "|");
        Log.i(tag, "| R | " + counts[1]  + " | " + sizes[1] + "|");
        Log.i(tag, "| T | " + counts[2]  + " | " + sizes[2] + "|");
        Log.i(tag, "+===+========================+");
    }

    public static Syncer getSyncer() {
        return syncer;
    }

    public static void setSyncer(Syncer syncer) {
        LibraryUtils.syncer = syncer;
    }

    public static Player getPlayer() {
        return player;
    }

    public static void setPlayer(Player player) {
        LibraryUtils.player = player;
    }

    public static TracksIndexer getTracksIndexer() {
        return tracksIndexer;
    }

    public static void setTracksIndexer(TracksIndexer tracksIndexer) {
        LibraryUtils.tracksIndexer = tracksIndexer;
    }

    public static AdsIndexer getAdsIndexer() {
        return adsIndexer;
    }

    public static void setAdsIndexer(AdsIndexer adsIndexer) {
        LibraryUtils.adsIndexer = adsIndexer;
    }
}
