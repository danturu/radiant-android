package fm.radiant.android;

import android.app.Application;
import android.text.Spannable;
import android.text.SpannableString;

import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.lib.TypefaceSpan;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.CommonUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;
import fm.radiant.android.utils.NetworkUtils;

import static fm.radiant.android.lib.TypefaceCache.FONT_MUSEO_500;

public class Radiant extends Application {
    public static SpannableString formatHeader(String header) {
        SpannableString formatted = new SpannableString(header);
        formatted.setSpan(new TypefaceSpan(FONT_MUSEO_500), 0, header.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return formatted;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        TypefaceCache.initialize(this);
        AccountUtils.initialize(this);
        CommonUtils.initialize(this);
        LibraryUtils.initialize(this);
        MessagesUtils.initialize(this);
        NetworkUtils.initialize(this);
    }
}
