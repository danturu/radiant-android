package fm.radiant.android.utils;

import android.os.Environment;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by kochnev on 19/05/14.
 */
public class StorageUtils {
    private static long VALUE_MIN_REQUIRED_SPACE = 150;

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public static long getFreeSpaceInMegabytes() {
        return Environment.getExternalStorageDirectory().getUsableSpace() / 1024 / 1024;
    }

    public static boolean isFreeSpaceEnough() {
        return getFreeSpaceInMegabytes() > VALUE_MIN_REQUIRED_SPACE;
    }

    public static String md5(File file) throws IOException {
        return new String(Hex.encodeHex(DigestUtils.md5(FileUtils.readFileToByteArray(file))));
    }
}
