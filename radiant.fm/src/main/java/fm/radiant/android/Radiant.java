package fm.radiant.android;

import android.app.Application;
import android.content.Context;

import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.CommonUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;
import fm.radiant.android.utils.NetworkUtils;

public class Radiant extends Application {
    private static final String TAG = "Radiant";

    public static final String INTENT_RESYNC               = "fm.radiant.action.INTENT_RESYNC";
    public static final String INTENT_SYNCER_STATE_CHANGED = "fm.radiant.action.INTENT_SYNCER_STATE_CHANGED";
    public static final String INTENT_PLAYER_STATE_CHANGED = "fm.radiant.action.INTENT_PLAYER_STATE_CHANGED";

    @Override
    public void onCreate() {
        super.onCreate();

        Context context = getApplicationContext();

        AccountUtils.initialize(context);
        CommonUtils.initialize(context);
        LibraryUtils.initialize(context);
        MessagesUtils.initialize(context);
        NetworkUtils.initialize(context);
    }
}
