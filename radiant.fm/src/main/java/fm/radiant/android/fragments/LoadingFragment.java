package fm.radiant.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.lang.math.RandomUtils;

import fm.radiant.android.R;
import fm.radiant.android.lib.TypefaceCache;

/**
 * Created by kochnev on 21/06/14.
 */
public class LoadingFragment extends Fragment {
    public static String TAG = LoadingFragment.class.getSimpleName();

    ImageView cover;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_loading, container, false);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.hide();

        cover = (ImageView) view.findViewById(R.id.layout_cover);

        ((TextView) view.findViewById(R.id.text_loading)).setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));

        colorize();

        return view;
    }

    private void colorize() {
        int[] colors = getResources().getIntArray(R.array.covers);
        int   color  = colors[RandomUtils.nextInt(colors.length)];

        cover.setBackgroundColor(color);
    }

}