package enghack.uwaterloo.ca.enghack;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;


/**
 * Created by Midori on 2017-02-04.
 */

public class CalculateRouteListener implements View.OnClickListener {
    protected Biking mainActivity;
    final List<Marker> markers = new ArrayList<Marker>();
    List<Marker> orange = new ArrayList<Marker>();
    List<Polyline> blue = new ArrayList<Polyline>();
    private String data = "";
    private int ways = 3;

    public CalculateRouteListener(Biking main) {
        mainActivity = main;
    }

    public void onClick(View v) {
        if (mainActivity.startMarker == null || mainActivity.destMarker == null || mainActivity.pts == null) {
            return;
        }

        for (int i = 0; i < markers.size(); i++) {
            markers.get(i).remove();
        }

        for (int i = 0; i < orange.size(); i++) {
            orange.get(i).remove();
        }

        for (int i = 0; i < blue.size(); i++) {
            blue.get(i).remove();
        }

        blue.clear();
        orange.clear();
        markers.clear();
        data = "";

        LatLng pos = mainActivity.startMarker.getPosition();
        double startLat = pos.latitude, startLong = pos.longitude;

        pos = mainActivity.destMarker.getPosition();
        double destLat = pos.latitude, destLong = pos.longitude;

        double leftTopLat = 0.0045, leftTopLong, rightBottomLat, rightBottomLong = 0.0045;

        if (startLat > destLat) {
            leftTopLat += startLat;
            rightBottomLat = destLat - 0.0045;
        } else {
            leftTopLat += destLat;
            rightBottomLat = startLat - 0.0045;
        }

        if (startLong < destLong) {
            leftTopLong = startLong - 0.0045;
            rightBottomLong += destLong;

        } else {
            leftTopLong = destLong - 0.0045;
            rightBottomLong += startLong;
        }

        try {
            final JSONArray features = new JSONArray(mainActivity.pts.get("features").toString()), inBounds = new JSONArray();

            for (int  i = 0; i < features.length(); i++) {
                final JSONObject obj = new JSONObject(features.getJSONObject(i).getString("properties"));
                final double lat = Double.parseDouble(obj.getString("LATITUDE")), lon = Double.parseDouble(obj.getString("LONGITUDE"));
                if (lat <= leftTopLat && lat >= rightBottomLat && lon >= leftTopLong && lon <= rightBottomLong) {
                    inBounds.put(obj);

                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                LatLng latlng = new LatLng(lat, lon);
                                Marker marker = mainActivity.mMap.addMarker(new MarkerOptions().position(latlng)
                                        .title(obj.getString("FACILITY"))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                markers.add(marker);
                            } catch (JSONException e) {
                                System.err.println("Exception");
                            }
                        }
                    });
                }
            }

            System.out.println("Length: " + inBounds.length());

            List<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < inBounds.length(); i++) {
                list.add(new Integer(i));
            }
            Collections.shuffle(list);

            ways = mainActivity.numWays;

            if (ways > inBounds.length()) {
                ways = inBounds.length();
            }

            for (int i=0; i < ways; i++) {
                Marker curr = markers.get(list.get(i));
                Marker next = mainActivity.mMap.addMarker(new MarkerOptions().position(curr.getPosition()).title(curr.getTitle()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                orange.add(next);
                curr.remove();
            }

            String url = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                        Double.toString(mainActivity.startMarker.getPosition().latitude) + "," +
                        Double.toString(mainActivity.startMarker.getPosition().longitude) + "&";

            url += "destination=" +
                    Double.toString(mainActivity.destMarker.getPosition().latitude) + "," +
                    Double.toString(mainActivity.destMarker.getPosition().longitude);

            if (ways != 0) {
                url += "&waypoints=optimize:true%7C";
                for (int i = 0; i < ways; i++) {
                    System.out.println(url);
                    url += "via:" + Double.toString(orange.get(i).getPosition().latitude) + "%2C" + Double.toString(orange.get(i).getPosition().longitude) + "%7C";
                }

                url = url.substring(0, url.length() - 3); // getting rid of trailing %7C
            }

            url += "&mode=bicycling&key=AIzaSyCJ1bY0wslGILXSABYjueDJf33Ne6JLiT8";

            final String sendURL = url;

            System.out.println(sendURL);

            final Thread pathThread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL URL = new URL(sendURL);
                        Scanner scan = new Scanner(URL.openStream());
                        scan(scan);

                    } catch (IOException e) {
                        System.err.println("URL is malformed");
                    }
                }
            });
            pathThread1.start();

            try {
                pathThread1.join();
                JSONObject results = new JSONObject(data);
                drawPath(results.toString());
                System.out.println(results);
            } catch (InterruptedException | JSONException e) {
                System.err.println("Exception");
            }

        } catch (JSONException e) {
            System.err.println("Exception");
        }

    }

    public void drawPath(String result) {

        try {
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString, orange);
            Polyline line = mainActivity.mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(20)
                    .color(Color.parseColor("#39add1"))
                    .geodesic(true)
            );

            Polyline line2 = mainActivity.mMap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(5)
                    .color(Color.parseColor("#ffffff"))
                    .geodesic(true)
            );
            blue.add(line);
            blue.add(line2);
        }
        catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded, List points) {

        List<LatLng> allPoly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;


        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            allPoly.add(p);
        }

        return allPoly;
    }

    private void scan(Scanner s) {
        while (s.hasNext()) {
            data += s.nextLine();
        }

        s.close();
    }

}
