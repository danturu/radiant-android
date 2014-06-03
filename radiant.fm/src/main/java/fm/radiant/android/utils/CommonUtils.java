package fm.radiant.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;

public class CommonUtils {
    private static Context sContext;

    public static void initialize(Context context) {
        sContext = context;
    }

    public static int getAppVersion() {
        try {
            return sContext.getPackageManager().getPackageInfo(sContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen...

            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}
