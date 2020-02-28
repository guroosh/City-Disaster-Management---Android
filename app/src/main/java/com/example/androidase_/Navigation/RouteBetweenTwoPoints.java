package com.example.androidase_.Navigation;

import android.app.Activity;
import android.graphics.Color;

import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.other_classes.PathJSONParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static com.example.androidase_.activities.MapsActivity.circleCenter;
import static com.example.androidase_.activities.MapsActivity.circleRadius;
import static com.example.androidase_.activities.MapsActivity.mMap;
import static com.example.androidase_.activities.MapsActivity.routeBetweenTwoPointsPolylines;
import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenTwoPoints;
import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenTwoPoints;
import static com.example.androidase_.drivers.HttpDriver.getRestApi;
import static com.example.androidase_.drivers.MathOperationsDriver.isLocationInsideCircle;

public class RouteBetweenTwoPoints {
    public static void createThreadGetForRouteBetweenTwoLocations(final String url, final Activity a) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = getRestApi(url);
                } finally {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            plotRouteBetweenTwoLocations(result[0]);
                        }
                    });
                }
            }
        });
        thread.start();
    }


    private static void plotRouteBetweenTwoLocations(final String result) {
        deleteOldRoute();
        previousRouteBetweenTwoPoints = result;
        renderRouteBetweenTwoLocations(result);
    }

    static void renderRouteBetweenTwoLocations(String jsonData) throws NullPointerException {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<LatLng> points;
        ArrayList<PolylineOptions> polyLineOptionsList = new ArrayList<>();

        // traversing through routes
        assert routes != null;
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<>();
            List<HashMap<String, String>> path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }

            for (int k = 0; k < points.size() - 1; k++) {
                LatLng point1 = points.get(k);
                LatLng point2 = points.get(k + 1);
                boolean bool1 = isLocationInsideCircle(point1.latitude, point1.longitude, circleRadius, circleCenter);
                boolean bool2 = isLocationInsideCircle(point2.latitude, point2.longitude, circleRadius, circleCenter);
                if (bool1 && bool2) {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.add(point1);
                    polylineOptions.add(point2);
                    polylineOptions.width(15);
                    polylineOptions.color(Color.RED);
                    polyLineOptionsList.add(polylineOptions);
                } else {
                    PolylineOptions polylineOptions = new PolylineOptions();
                    polylineOptions.add(point1);
                    polylineOptions.add(point2);
                    polylineOptions.width(15);
                    polylineOptions.color(Color.BLUE);
                    polyLineOptionsList.add(polylineOptions);
                }
            }
        }
        for (PolylineOptions p : polyLineOptionsList) {
            routeBetweenTwoPointsPolylines.add(mMap.addPolyline(p));
        }
    }

    private static void deleteOldRoute() throws NullPointerException {
        if (!previousRouteBetweenTwoPoints.equals("")) {
            for (Polyline p : routeBetweenTwoPointsPolylines) {
                p.remove();
            }
            routeBetweenTwoPointsPolylines.clear();
        }
    }
}
