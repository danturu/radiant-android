package fm.radiant.android.utils;

import android.os.Environment;

/**
 * Created by kochnev on 19/05/14.
 */
public class StorageUtils {
    private static final String TAG = "StorageUtils";

    private static long VALUE_MIN_REQUIRED_SPACE = 100;

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static long getFreeSpaceInMegabytes() {
        return Environment.getExternalStorageDirectory().getUsableSpace() / 1024 / 1024;
    }

    public static boolean isFreeSpaceEnough() {
        return getFreeSpaceInMegabytes() > VALUE_MIN_REQUIRED_SPACE;
    }
}
