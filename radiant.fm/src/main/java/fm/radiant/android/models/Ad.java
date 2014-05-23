package fm.radiant.android.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fm.radiant.android.interfaces.AudioModel;

public class Ad extends AudioModel {
    public static List<Ad> selectRandom(List<Ad> ads, int count) {
        List<Ad> cloned = new ArrayList<Ad>(ads);
        Collections.shuffle(cloned);

        return cloned.subList(0, count);
    }
}
