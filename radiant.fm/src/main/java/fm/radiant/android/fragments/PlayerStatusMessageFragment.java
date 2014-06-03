package fm.radiant.android.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import fm.radiant.android.R;

/**
 * Created by kochnev on 03/06/14.
 */
public class PlayerStatusMessageFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_status_message, container, false);

        ProgressBar pb = (ProgressBar) view.findViewById(R.id.pb_test);

        //         ((ShapeDrawable) ((RotateDrawable) pb.getIndeterminateDrawable()).getDrawable()).set

     //   ((GradientDrawable) ((RotateDrawable) pb.getIndeterminateDrawable()).getDrawable()).setColor(getResources().getColor(R.color.cover_3));
        return view;
    }
}
