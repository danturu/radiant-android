package fm.radiant.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class CommonUtils {
    private static final String TAG = "CommonUtils";

    private static Context context;

    public static void initialize(Context context) {
        CommonUtils.context = context;
    }

    public static int getAppVersion() {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen...

            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
