package fm.radiant.android.lib;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;

public class TypefaceCache {
    public static final String FONT_MUSEO_300   = "museo_sans_300.ttf";
    public static final String FONT_MUSEO_500   = "museo_sans_500.ttf";
    public static final String FONT_MUSEO_700   = "museo_sans_700.ttf";
    public static final String FONT_PLUMB_LIGHT = "plumb_condensed_light.ttf";

    private static Context sContext;
    private static LruCache<String, Typeface> sCache;

    public static void initialize(Context context) {
        sContext = context;
        sCache   = new LruCache<String, Typeface>(12);
    }

    public static Typeface get(String typefaceName) {
        synchronized (sCache) {
            Typeface typeface = sCache.get(typefaceName);

            if (typeface == null) {
                typeface = Typeface.createFromAsset(sContext.getAssets(), String.format("fonts/%s", typefaceName));
                sCache.put(typefaceName, typeface);
            }

            return typeface;
        }
    }
}