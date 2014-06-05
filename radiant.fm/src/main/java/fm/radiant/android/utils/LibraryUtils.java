package fm.radiant.android.utils;

import android.content.Context;
import android.util.Log;

import org.apache.commons.lang.StringUtils;

import fm.radiant.android.lib.indexer.AbstractIndexer;
import fm.radiant.android.lib.player.Player;
import fm.radiant.android.lib.syncer.Syncer;

public class LibraryUtils {
    private static Syncer sSyncer;
    private static Player sPlayer;

    public static void initialize(Context context) {
        sPlayer = new Player(context);
        sSyncer = new Syncer(context);
    }

    public static void teardown() {
        sPlayer.stop();
        sSyncer.stop();
    }

    public static Syncer getSyncer() {
        return sSyncer;
    }

    public static Player getPlayer() {
        return sPlayer;
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

}
