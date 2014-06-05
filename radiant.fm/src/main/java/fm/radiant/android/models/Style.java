package fm.radiant.android.models;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gson.annotations.Expose;

import java.util.List;

public class Style extends Model {
    @Expose
    private String name;

    public static List<Integer> collectIds(List<Style> styles) {
        Iterable<Integer> ids = Iterables.transform(styles, new Function<Style, Integer>() {
            @Override
            public Integer apply(Style style) {
                return style.getId();
            }
        });

        return Lists.newArrayList(ids);
    }

    public static List<String> collectNames(List<Style> styles) {
        Iterable<String> names = Iterables.transform(styles, new Function<Style, String>() {
            @Override
            public String apply(Style style) {
                return style.getName();
            }
        });

        return Lists.newArrayList(names);

    }

    public String getName() {
        return name;
    }
}