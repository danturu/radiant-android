package fm.radiant.android.models;

import java.util.ArrayList;
import java.util.Collection;

import fm.radiant.android.interfaces.Modelable;

public class Campaign extends Modelable {
    private Collection<Ad> ads = new ArrayList<Ad>();

    public Collection<Ad> getAds() {
        return ads;
    }
}
