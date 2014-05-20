package fm.radiant.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    private static Context context;
    private static ConnectivityManager connectivityManager;

    public static void initialize(Context context) {
        NetworkUtils.context             = context;
        NetworkUtils.connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isNetworkConnected() {
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        // there can be no active network...

        return activeNetwork != null && activeNetwork.isConnected();
    }
}
