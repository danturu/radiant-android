package fm.radiant.android.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import fm.radiant.android.Config;

public class MessagesUtils {
    private static final String TAG = "MessagesUtils";

    private static final String PROPERTY_REGISTRATION_ID      = "registration_id";
    private static final String PROPERTY_REGISTRATION_VERSION = "registration_version";
    private static final String PROPERTY_REGISTRATION_SENDED  = "registration_sended";
    private static final String PROPERTY_RECEIVER_ID          = "receiver_id";
    private static final String PROPERTY_RECEIVER_PLATFORM    = "receiver_platform";

    private static final String PLATFORM_NAME = "android";

    private static Context context;
    private static SharedPreferences preferences;

    public static void initialize(Context context) {
        MessagesUtils.context     = context;
        MessagesUtils.preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public static boolean canReceiveMessages() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static boolean canReceiveMessages(Activity activity) {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);

        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

        recoverError(activity, resultCode);
        return false;
    }

    public static void registerInCloud() throws IOException {
        String registrationId = GoogleCloudMessaging.getInstance(context).register(Config.MESSAGES_SENDER_ID);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PROPERTY_REGISTRATION_ID, registrationId);
        editor.putInt(PROPERTY_REGISTRATION_VERSION, CommonUtils.getAppVersion());
        editor.commit();
    }

    public static int sendRegistrationToBackend(String registrationId) throws IOException {
        HttpRequest request = HttpRequest.put(Config.API_ENDPOINT + "/devices/subscribe", true).basic(AccountUtils.getUUID(), AccountUtils.getPassword());
        request.part(PROPERTY_RECEIVER_ID, registrationId);
        request.part(PROPERTY_RECEIVER_PLATFORM, PLATFORM_NAME);

        if (request.ok()) {
            preferences.edit().putBoolean(PROPERTY_REGISTRATION_SENDED, true).commit();
        }

        return request.code();
    }

    public static void unregister() throws IOException {
        GoogleCloudMessaging.getInstance(context).unregister();
    }

    public static void teardown() {
        preferences.edit().clear().commit();
    }

    public static String getRegistrationId() {
        return preferences.getString(PROPERTY_REGISTRATION_ID, "");
    }

    public static String getMessageType(Intent intent) {
        return GoogleCloudMessaging.getInstance(context).getMessageType(intent);
    }

    public static boolean isRegistered() {
        return preferences.getInt(PROPERTY_REGISTRATION_VERSION, Integer.MIN_VALUE) == CommonUtils.getAppVersion();
    }

    public static boolean isRegistrationSendedToBackend() {
        return preferences.getBoolean(PROPERTY_REGISTRATION_SENDED, false);
    }

    private static void recoverError(Activity activity, int errorCode) {
        if (GooglePlayServicesUtil.isUserRecoverableError(errorCode)) {
            GooglePlayServicesUtil.getErrorDialog(errorCode, activity, 9000).show();
        } else {
            activity.finish();
        }
    }
}