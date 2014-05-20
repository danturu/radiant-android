package fm.radiant.android.utils;

import android.content.Context;

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
    }

    public static void teardown() {
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
