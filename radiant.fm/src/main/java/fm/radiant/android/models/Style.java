package fm.radiant.android.models;

import com.google.gson.annotations.Expose;

import fm.radiant.android.lib.Model;

public class Style extends Model {
    @Expose
    private String name;

    public String getName() {
        return name;
    }
}