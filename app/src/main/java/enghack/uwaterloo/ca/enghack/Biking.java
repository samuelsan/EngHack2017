package enghack.uwaterloo.ca.enghack;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.*;

public class Biking extends FragmentActivity implements OnMapReadyCallback {

    public GoogleMap mMap;
    public int numWays = 3;
    private JSON json = new JSON();
    private EditText editText;
    public Marker startMarker, destMarker;
    public JSONObject trails = new JSONObject(), pts = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biking);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        SeekBar seekBar = (SeekBar)findViewById(R.id.waypoints);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                numWays = progress;
                TextView textView = (TextView) findViewById(R.id.currNum);
                textView.setText(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // Implementing start autocomplete
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.start);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (startMarker != null) startMarker.remove();
                System.out.println(place.toString());

                LatLng latlng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                startMarker = mMap.addMarker(new MarkerOptions().position(latlng)
                                                                .title(place.getName().toString()));
                startMarker.setSnippet("Origin");
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            }

            @Override
            public void onError(Status status) { }
        });

        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(43.4255, -80.582486),
                new LatLng(43.521164, -80.500299)));
        autocompleteFragment.setHint("Enter origin");

        // Implementing destination autocomplete
        autocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.destination);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (destMarker != null) destMarker.remove();
                System.out.println(place.toString());

                LatLng latlng = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);
                destMarker = mMap.addMarker(new MarkerOptions().position(latlng)
                        .title(place.getName().toString())
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                destMarker.setSnippet("Destination");
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            }

            @Override
            public void onError(Status status) { }
        });

        autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(43.4255, -80.582486),
                new LatLng(43.521164, -80.500299)));
        autocompleteFragment.setHint("Enter destination");

        //Button to cycle between the different maps
        Button btn = (Button) findViewById(R.id.route);
        //instantiate a cycle button listener
        CalculateRouteListener route = new CalculateRouteListener(this);
        btn.setOnClickListener(route);

        Button btn2 = (Button) findViewById(R.id.toggle);
        ToggleListener toggle = new ToggleListener(this);
        btn2.setOnClickListener(toggle);

        // Grab heritage, historical streets, and points of interest data and combine
        pts = json.getJSON("http://opendata.city-of-waterloo.opendata.arcgis.com/datasets/04a8276e6c074a6b8a2a158cc1a1d81e_0.geojson");

        // Grab tree data
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_biking, parent, false);
        editText = (EditText) view.findViewById(R.id.map);
        SharedPreferences settings = this.getSharedPreferences("PREFS", 0);
        editText.setText(settings.getString("value", ""));

        return view;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        /*

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {

            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getSnippet().equals("Tap here to remove")) {
                    marker.remove();
                }
            }
        });

        */
    }
}
