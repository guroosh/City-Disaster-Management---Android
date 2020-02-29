package com.example.androidase_.Navigation;

import android.app.Activity;
import android.graphics.Color;

import com.example.androidase_.other_classes.PathJSONParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.example.androidase_.activities.MapsActivity.API_KEY;
import static com.example.androidase_.activities.MapsActivity.mMap;
import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenThreePoints;
import static com.example.androidase_.activities.MapsActivity.routeBetweenThreePointsPolylines;
import static com.example.androidase_.drivers.HttpDriver.getRestApi;

public class RouteBetweenThreePoints {
    public static void initialiseProcessForRouteBetweenThreePoints(double lat1, double lng1, double lat2, double lng2, double lat3, double lng3, Activity a) {
        String url1 = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + lat1 + "," + lng1 +
                "&destination=" + lat2 + "," + lng2 +
                "&key=" + API_KEY;
        String url2 = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + lat2 + "," + lng2 +
                "&destination=" + lat3 + "," + lng3 +
                "&key=" + API_KEY;
        createThreadGetForRouteBetweenThreeLocations(url1, url2, a);
    }

    public static void createThreadGetForRouteBetweenThreeLocations(final String url1, final String url2, final Activity a) {
        final String[] result = new String[2];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = getRestApi(url1);
                } finally {
                    try {
                        result[1] = getRestApi(url2);
                    } finally {
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                plotRouteBetweenThreeLocations(result[0], result[1], a);
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    private static void plotRouteBetweenThreeLocations(String routeJson1, String routeJson2, Activity a) {
        deleteOldRoute();
        previousRouteBetweenThreePoints = routeJson1 + routeJson2;
        renderRouteBetweenTwoLocations(routeJson1, a);
        renderRouteBetweenTwoLocations(routeJson2, a);
    }

    private static void renderRouteBetweenTwoLocations(String jsonData, Activity a) {
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
        PolylineOptions polyLineOptions = null;
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<>();
            polyLineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            polyLineOptions.addAll(points);
            polyLineOptions.width(15);
            polyLineOptions.color(Color.BLUE);
        }
        routeBetweenThreePointsPolylines.add(mMap.addPolyline(polyLineOptions));
    }

    private static void deleteOldRoute() {
        if (!previousRouteBetweenThreePoints.equals("")) {
            for (Polyline p : routeBetweenThreePointsPolylines) {
                p.remove();
            }
            routeBetweenThreePointsPolylines.clear();
        }
    }
}
