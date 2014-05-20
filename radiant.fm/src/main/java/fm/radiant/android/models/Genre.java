package fm.radiant.android.models;

import java.util.ArrayList;
import java.util.List;

import fm.radiant.android.interfaces.Modelable;

public class Genre extends Modelable {
    private String description;

    private List<Style> styles = new ArrayList<Style>();

    public Genre() {
    }

    public String getDescription() {
        return description;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public int getColor() {
        return Integer.valueOf(getId()) % 5 + 1;
    }
}
