package fm.radiant.android.fragments;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import fm.radiant.android.Events;
import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.lib.EventBusFragment;
import fm.radiant.android.models.Period;

public class PreferencesFragment extends EventBusFragment {
    public static String TAG = PlayerFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);

        setHasOptionsMenu(false);

        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setTitle(Radiant.formatHeader(getString(R.string.title_preferences)));

        return view;
    }

    public void onEventMainThread(Events.PlaceChangedEvent event) {
    }
}
