package fm.radiant.android.models;

import java.util.ArrayList;
import java.util.List;

import fm.radiant.android.lib.Model;

public class Genre extends Model {
    private String description;

    private List<Style> styles = new ArrayList<Style>();

    public String getDescription() {
        return description;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public int getColorIndex() {
        return getId() % 5 + 1;
    }
}