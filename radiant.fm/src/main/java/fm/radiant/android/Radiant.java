package fm.radiant.android;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;

import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.lib.TypefaceSpan;
import fm.radiant.android.services.SetupService;
import fm.radiant.android.tasks.SyncTask;
import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.CommonUtils;
import fm.radiant.android.utils.MessagesUtils;
import fm.radiant.android.utils.NetworkUtils;

import static fm.radiant.android.lib.TypefaceCache.FONT_MUSEO_500;

public class Radiant extends Application {
    public static SpannableString formatHeader(String header) {
        SpannableString formatted = new SpannableString(header);
        formatted.setSpan(new TypefaceSpan(FONT_MUSEO_500), 0, header.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return formatted;
    }

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();

        attachContextToUtils();

        if (AccountUtils.isLoggedIn()) {
            Intent intent = new Intent(sContext, SetupService.class);
            intent.putExtra("first", true);
            sContext.startService(intent);
        }
    }

    private void attachContextToUtils() {
        sContext = getApplicationContext();

        TypefaceCache.initialize(sContext);
        AccountUtils.initialize(sContext);
        CommonUtils.initialize(sContext);
        MessagesUtils.initialize(sContext);
        NetworkUtils.initialize(sContext);
    }

    public static Context getContext() {
        return sContext;
    }
}
