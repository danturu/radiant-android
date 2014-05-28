package fm.radiant.android.models;

import com.google.gson.annotations.Expose;

import fm.radiant.android.lib.Model;

public class Device extends Model {
    @Expose
    private String placeId;

    public String getPlaceId() {
        return placeId;
    }
}