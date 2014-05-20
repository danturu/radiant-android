package fm.radiant.android.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import java.io.File;
import java.io.IOException;

public class ParseUtils {
    private static final ObjectMapper parser = new ObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES).disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    public static ObjectMapper getParser() {
        return parser;
    }

    public static <T> T fromJSON(File file, Class<T> type) throws IOException {
        return parser.readValue(file, type);
    }

    public static <T> T fromJSON(String string, Class<T> type) throws IOException {
        return parser.readValue(string, type);
    }

    public static String toJSON(Object object) throws IOException {
        return parser.writeValueAsString(object);
    }

    public static void writeJSON(File file, Object object) throws IOException {
        parser.writeValue(file, object);
    }
}