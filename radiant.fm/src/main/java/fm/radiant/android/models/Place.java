package fm.radiant.android.models;

import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fm.radiant.android.interfaces.Modelable;
import fm.radiant.android.utils.ParseUtils;

public class Place extends Modelable {
    private static final String TAG = "Place";

    private String name;

    private List<Period> periods     = new ArrayList<Period>();
    private List<Campaign> campaigns = new ArrayList<Campaign>();
    private List<Track> tracks       = new ArrayList<Track>();

    public static Place parse(String data) throws IOException {
        return ParseUtils.fromJSON(data, Place.class);
    }

    public static Place retrieve(SharedPreferences storage) throws IOException {
        return ParseUtils.fromJSON(storage.getString(TAG, ""), Place.class);
    }

    public static void store(SharedPreferences storage, String data) {
        storage.edit().putString(TAG, data).commit();
    }

    public String getName() {
        return name;
    }

    public List<Period> getPeriods() {
        return periods;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public List<Ad> getAds() {
        List<Ad> ads = new ArrayList<Ad>();

        for (Campaign campaign : campaigns) {
            ads.addAll(campaign.getAds());
        }

        return ads;
    }
}
