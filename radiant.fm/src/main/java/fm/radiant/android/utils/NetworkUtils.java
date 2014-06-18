package fm.radiant.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static fm.radiant.android.lib.syncer.Syncer.STATE_STOPPED;

public class NetworkUtils {
    private static Context             sContext;
    private static ConnectivityManager sConnectivityManager;

    public static void initialize(Context context) {
        sContext             = context;
        sConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isNetworkConnected() {
        NetworkInfo activeNetwork = sConnectivityManager.getActiveNetworkInfo();

        // there can be no active network...

        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean canUseNetwork() {
        return LibraryUtils.getSyncer().getState() != STATE_STOPPED;
    }
}