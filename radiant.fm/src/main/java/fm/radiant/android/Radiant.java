package fm.radiant.android;

import android.app.Application;

import fm.radiant.android.utils.AccountUtils;
import fm.radiant.android.utils.CommonUtils;
import fm.radiant.android.utils.LibraryUtils;
import fm.radiant.android.utils.MessagesUtils;
import fm.radiant.android.utils.NetworkUtils;

public class Radiant extends Application {
    public static final String INTENT_PLACE_PAIRED            = "fm.radiant.action.INTENT_PLACE_PAIRED";
    public static final String INTENT_PLACE_UNPAIRED          = "fm.radiant.action.INTENT_PLACE_UNPAIRED";
    public static final String INTENT_PLACE_CHANGED           = "fm.radiant.action.INTENT_PLACE_CHANGED";
    public static final String INTENT_SYNCER_STATE_CHANGED    = "fm.radiant.action.INTENT_SYNCER_STATE_CHANGED";
    public static final String INTENT_SYNCER_PROGRESS_CHANGED = "fm.radiant.action.INTENT_SYNCER_PROGRESS_CHANGED";
    public static final String INTENT_PLAYER_STATE_CHANGED    = "fm.radiant.action.INTENT_PLAYER_STATE_CHANGED";
    public static final String INTENT_PLAYER_PERIOD_CHANGED   = "fm.radiant.action.INTENT_PLAYER_PERIOD_CHANGED";

    @Override
    public void onCreate() {
        super.onCreate();

        AccountUtils.initialize(this); CommonUtils.initialize(this); LibraryUtils.initialize(this); MessagesUtils.initialize(this); NetworkUtils.initialize(this);
    }
}
