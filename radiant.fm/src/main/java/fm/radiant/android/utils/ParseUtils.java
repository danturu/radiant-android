package fm.radiant.android.utils;

import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

public class ParseUtils {
    private static final Gson parser = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

    private static String[] sSizeUnits = new String[] { "КБ/c", "МБ/c" };
    private static String[] sTimeUnits = new String[] { "с", "м", "ч" };
    private static DecimalFormat unitsFormatter = new DecimalFormat("#,##0.#");

    public static Gson getParser() {
        return parser;
    }

    public static <T> T fromJSON(File file, Class<T> type) throws IOException {
        return parser.fromJson(FileUtils.readFileToString(file), type);
    }

    public static <T> T fromJSON(String string, Class<T> type) throws IOException {
        return parser.fromJson(string, type);
    }

    public static String toJSON(Object object) throws IOException {
        return parser.toJson(object);
    }

    public static String humanizeDay(Integer day) {
        DateTime time = new DateTime().withDayOfWeek(day + 1);
        return WordUtils.capitalize((DateTimeFormat.forPattern("EEEE").print(time)));
    }

    public static String humanizeTime(int time) {
        String hours   = Integer.toString(time / 60);
        String minutes = Integer.toString(time % 60);

        return StringUtils.leftPad(hours, 2, '0') + ':' + StringUtils.rightPad(minutes, 2, '0');
    }

    public static String humanizeTimeRange(int startTime, int endTime) {
        return humanizeTime(startTime) + " – " + humanizeTime(endTime);
    }

    public static String humanizeDuration(long seconds) {
        if (seconds < 0) return "N/A";

        if (seconds > 3660) {
            return (seconds / 3600) + " " + sTimeUnits[2] + " " + (seconds % 3600 / 60) + " " + sTimeUnits[1];
        }

        if (seconds > 3600) {
            return (seconds / 3600) + " " + sTimeUnits[2];
        }

        if (seconds > 60) {
            return (seconds / 60) + " " + sTimeUnits[1] + " " + (seconds % 60) + " " + sTimeUnits[0];
        }

        return seconds + " " + sTimeUnits[0];
    }

    public static String humanizeSpeed(long bytes) {
        if (bytes <= 1000) return "1 " + sSizeUnits[0];

        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1000));

        return unitsFormatter.format(bytes / Math.pow(1000, digitGroups)) + " " + sSizeUnits[digitGroups - 1];
    }
}