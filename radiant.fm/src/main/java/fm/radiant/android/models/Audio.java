package fm.radiant.android.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Audio {
    @Expose
    private String label;

    @Expose
    private String url;

    @Expose @SerializedName("md5")
    private String hash;

    @Expose @SerializedName("duration_in_ms")
    private int time;

    @Expose @SerializedName("size_in_bytes")
    private int size;

    public String getLabel() {
        return label;
    }

    public String getURL() {
        return url;
    }

    public String getHash() {
        return hash;
    }

    public int getTime() {
        return time;
    }

    public int getSize() {
        return size;
    }
}