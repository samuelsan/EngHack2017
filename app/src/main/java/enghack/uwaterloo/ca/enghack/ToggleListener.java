package enghack.uwaterloo.ca.enghack;

import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Midori on 2017-02-05.
 */

public class ToggleListener implements View.OnClickListener {
    protected Biking mainActivity;

    public ToggleListener(Biking main) {
        mainActivity = main;
    }

    public void onClick(View v) {
        TextView textView = (TextView) mainActivity.findViewById(R.id.currNum);
        if (textView.isShown()) {
            textView.setVisibility(View.GONE);
            mainActivity.findViewById(R.id.currNum).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.waypoints).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.destination).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.start).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.route).setVisibility(View.GONE);
            mainActivity.findViewById(R.id.numSites).setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.currNum).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.waypoints).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.destination).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.start).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.route).setVisibility(View.VISIBLE);
            mainActivity.findViewById(R.id.numSites).setVisibility(View.VISIBLE);
        }
    }
}
