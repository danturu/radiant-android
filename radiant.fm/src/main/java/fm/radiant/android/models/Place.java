package fm.radiant.android.models;

import android.content.SharedPreferences;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import fm.radiant.android.Events;
import fm.radiant.android.utils.ParseUtils;

public class Place extends Model {
    private static final String TAG = "Place";

    @Expose
    private String name;

    @Expose
    private String cachedAt;

    @Expose
    private List<Period> periods = Collections.emptyList();

    @Expose
    private List<Campaign> campaigns = Collections.emptyList();

    @Expose
    private List<Track> tracks = Collections.emptyList();

    public static Place parse(String data) throws IOException {
        return ParseUtils.fromJSON(data, Place.class);
    }

    public static Place retrieve(SharedPreferences storage) throws IOException {
        Place place = ParseUtils.fromJSON(storage.getString(TAG, ""), Place.class);

        EventBus.getDefault().postSticky(new Events.PlaceChangedEvent(place));

        return place;
    }

    public static void store(SharedPreferences storage, String data) {
        storage.edit().putString(TAG, data).commit();
    }

    @Override
    protected String version() {
        return cachedAt;
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
        Iterable<Ad> ads = Iterables.concat(Iterables.transform(campaigns, new Function<Campaign, List<Ad>>() {
            @Override
            public List<Ad> apply(Campaign campaign) {
                return campaign.getAds();
            }
        }));

        return Lists.newArrayList(ads);
    }
}
