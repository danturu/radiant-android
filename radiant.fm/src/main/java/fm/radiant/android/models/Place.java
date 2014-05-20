package fm.radiant.android.models;

import android.content.SharedPreferences;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import fm.radiant.android.interfaces.Modelable;
import fm.radiant.android.utils.ParseUtils;

public class Place extends Modelable {
    private static final String TAG = "Place";

    private String name;

    private Collection<Period> periods     = new ArrayList<Period>();
    private Collection<Campaign> campaigns = new ArrayList<Campaign>();
    private Collection<Track> tracks       = new ArrayList<Track>();

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

    public Collection<Period> getPeriods() {
        return periods;
    }

    public Collection<Campaign> getCampaigns() {
        return campaigns;
    }

    public Collection<Track> getTracks() {
        return tracks;
    }

    public Collection<Ad> getAds() {
        Collection<Ad> ads = new ArrayList<Ad>();

        for (Campaign campaign : campaigns) {
            ads.addAll(campaign.getAds());
        }

        return ads;
    }
}
