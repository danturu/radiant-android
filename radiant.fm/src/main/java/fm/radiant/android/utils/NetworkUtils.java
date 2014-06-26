package fm.radiant.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import fm.radiant.android.lib.syncer.Syncer;

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

        if (activeNetwork != null && activeNetwork.isConnected()) {
            Log.d("sdcsdc", "connected");
            Log.d("sdcsdc", "connected");
            Log.d("sdcsdc", "connected");
        } else {
            Log.d("sdcsdc", "noconnected");
            Log.d("sdcsdc", "noconnected");
            Log.d("sdcsdc", "noconnected");
        }
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean canUseNetwork() {
        return Syncer.getInstance().getState() != STATE_STOPPED;
    }
}