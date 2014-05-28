package fm.radiant.android.utils;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class ParseUtils {
    private static final Gson parser = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();

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
}