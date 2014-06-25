package fm.radiant.android.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fm.radiant.android.Events;
import fm.radiant.android.R;
import fm.radiant.android.Radiant;
import fm.radiant.android.lib.EventBusFragment;
import fm.radiant.android.lib.TypefaceCache;
import fm.radiant.android.models.Campaign;


public class CampaignsFragment extends EventBusFragment {
    public static String TAG = CampaignsFragment.class.getSimpleName();

    private CampaignsAdapter mCampaignsAdapter;
    private ListView mCampaignsView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_campaigns, container, false);

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(Radiant.formatHeader(getString(R.string.title_campaigns)));

        setHasOptionsMenu(true);

        mCampaignsAdapter = new CampaignsAdapter(getActivity().getApplicationContext());

        TextView emptyView = (TextView) view.findViewById(R.id.empty_periods);
        emptyView.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));

        mCampaignsView = (ListView) view.findViewById(R.id.campaigns);
        mCampaignsView.setAdapter(mCampaignsAdapter);
        mCampaignsView.setEmptyView(emptyView);

        return view;
    }

    public void onEventMainThread(Events.PlaceChangedEvent event) {
        List<Campaign> campaigns = event.getPlace().getCampaigns();

        mCampaignsAdapter.clear();

        for (Campaign campaign : campaigns) {
            mCampaignsAdapter.add(campaign);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_player, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_preferences:
                openPreferencesFragment();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferencesFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right);
        transaction.replace(android.R.id.content, new PreferencesFragment(), PreferencesFragment.TAG);
        transaction.addToBackStack(PeriodsFragment.TAG);
        transaction.commit();
    }


    private class CampaignsAdapter extends ArrayAdapter<Campaign> {
        public CampaignsAdapter(Context context) {
            super(context, R.layout.partial_campaigns_campaign, new ArrayList<Campaign>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Campaign campaign = getItem(position);

            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.partial_campaigns_campaign, null);
            }

            TextView nameText = (TextView) view.findViewById(R.id.text_name);
            nameText.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));
            nameText.setText(campaign.getName());

            TextView adsText = (TextView) view.findViewById(R.id.text_ads);
            adsText.setTypeface(TypefaceCache.get(TypefaceCache.FONT_MUSEO_500));
            adsText.setText("Ролики " + campaign.getAdsCountPerBlock() + " из " + campaign.getAds().size() + " каждые " + campaign.getPeriodicity() + " минут");

            return view;
        }
    }
}
