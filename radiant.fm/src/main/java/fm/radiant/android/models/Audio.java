package fm.radiant.android.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Audio {
    private String label;
    private String url;

    @JsonProperty("md5")
    private String hash;

    @JsonProperty("duration_in_ms")
    private int time;

    @JsonProperty("size_in_bytes")
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