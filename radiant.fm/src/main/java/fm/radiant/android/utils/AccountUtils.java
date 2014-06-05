package fm.radiant.android.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;

import java.io.IOException;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Config;
import fm.radiant.android.Events;
import fm.radiant.android.models.Device;
import fm.radiant.android.models.Place;

public class AccountUtils {
    private static final String TAG = "AccountUtils";

    private static final String PROPERTY_UUID     = "uuid";
    private static final String PROPERTY_PASSWORD = "password";
    private static final String PROPERTY_PLACE_ID = "place_id";

    private static Context context;
    private static SharedPreferences preferences;

    private static Place currentPlace;

    public static void initialize(Context context) {
        AccountUtils.context     = context;
        AccountUtils.preferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public static synchronized int pair(String uuid, String password) throws IOException {
        HttpRequest request = HttpRequest.put(Config.API_ENDPOINT + "/devices/pair", true).connectTimeout(7000);
        request.part(PROPERTY_UUID,     uuid);
        request.part(PROPERTY_PASSWORD, password.toLowerCase());

        if (request.ok()) {
            Device device = ParseUtils.fromJSON(request.body(), Device.class);

            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(PROPERTY_UUID,     uuid);
            editor.putString(PROPERTY_PASSWORD, password.toLowerCase());
            editor.putString(PROPERTY_PLACE_ID, device.getPlaceId());
            editor.commit();

            sync(getPlaceId());
        }

        return request.code();
    }

    public static synchronized int unpair(String password) throws IOException {
        HttpRequest request = HttpRequest.put(Config.API_ENDPOINT + "/devices/unpair", true).connectTimeout(7000);
        request.part("password", password.toLowerCase());

        return request.code();
    }

    public static synchronized int sync(String placeId) throws IOException {
        HttpRequest request = HttpRequest.get(Config.API_ENDPOINT + "/places/" + placeId + "/cached", true).basic(getUUID(), getPassword());

        if (request.ok()) {
            String data = request.body(); Place place = Place.parse(data);

            if (getPlace() == null || !getPlace().equals(place)) {
                currentPlace = place;
                Place.store(preferences, data);

                EventBus.getDefault().postSticky(new Events.PlaceChangedEvent(place));

                Log.i(TAG, "Place was synced");
            } else {
                Log.i(TAG, "Place was newest");
            }
        }

        return request.code();
    }

    public static boolean isValidPair(String uuid, String password) {
        return true;
    }

    public static void teardown() {
        preferences.edit().clear().commit();
    }

    public static String getUUID() {
        return preferences.getString(PROPERTY_UUID, "");
    }

    public static String getPassword() {
        return preferences.getString(PROPERTY_PASSWORD, "");
    }

    public static String getPlaceId() {
        return preferences.getString(PROPERTY_PLACE_ID, "");
    }

    public static Boolean isLoggedIn() {
        return preferences.contains(PROPERTY_UUID) && preferences.contains(PROPERTY_PASSWORD);
    }

    public static synchronized Place getPlace() {
        if (currentPlace == null) {
            try {
                currentPlace = Place.retrieve(preferences);
            } catch (IOException e) {
                Log.e(TAG, "Can't retrieve place: ", e);
            }
        }

        return currentPlace;
    }
}