package fm.radiant.android.models;

import com.google.gson.annotations.Expose;

public class Device extends Model {
    @Expose
    private String placeId;

    public String getPlaceId() {
        return placeId;
    }
}