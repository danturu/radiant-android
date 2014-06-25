package fm.radiant.android.lib;

import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.util.LruCache;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class TypefaceSpan extends MetricAffectingSpan {
    private static LruCache<String, Typeface> sTypefaceCache = new LruCache<String, Typeface>(12);
    private Typeface mTypeface;

    public TypefaceSpan(String typefaceName) {
        mTypeface = TypefaceCache.get(typefaceName);
    }

    @Override
    public void updateMeasureState(TextPaint textPaint) {
        textPaint.setTypeface(mTypeface);
        textPaint.setFlags(textPaint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

    @Override
    public void updateDrawState(TextPaint textPaint) {
        textPaint.setTypeface(mTypeface);
        textPaint.setFlags(textPaint.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }
}