package com.example.androidase_.navigation;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import com.example.androidase_.other_classes.PathJSONParser;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.example.androidase_.activities.MapsActivity.API_KEY;
import static com.example.androidase_.activities.MapsActivity.isNavigating;
import static com.example.androidase_.activities.MapsActivity.mMap;
//import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenThreePoints;
//import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenTwoPoints;
import static com.example.androidase_.activities.MapsActivity.routeBetweenThreePointsPolylines;
import static com.example.androidase_.drivers.HttpDriver.getRestApi;

public class RouteBetweenThreePoints {
    public static void initialiseProcessForRouteBetweenThreePoints(ArrayList<LatLng> points, Activity a) {
        ArrayList<String> urls = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            LatLng latLng1 = points.get(i);
            LatLng latLng2 = points.get(i + 1);
            String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + latLng1.latitude + "," + latLng1.longitude +
                    "&destination=" + latLng2.latitude + "," + latLng2.longitude +
                    "&key=" + API_KEY;
            urls.add(url);
        }
        // todo: use urls in case of urlBuilder fails, because waypoints has a limit and Roads API may give too many points
        createThreadGetForRouteBetweenThreeLocations(urls, a);
//        alternateRoadsAndWaypointsApproach(points, a);
    }

    private static void alternateRoadsAndWaypointsApproach(ArrayList<LatLng> points, Activity a) {
        String urlMaker = "";
        for (int i = 0; i < points.size(); i++) {
            LatLng location = points.get(i);
            urlMaker += location.latitude + "," + location.longitude;
            if (i != (points.size() - 1)) {
                urlMaker += "|";
            }
        }
        urlMaker = "https://roads.googleapis.com/v1/snapToRoads?path=" + urlMaker + "&interpolate=true&key=" + API_KEY;
        createThreadGetForFindingRoad(urlMaker, a);
    }

    public static void createThreadGetForFindingRoad(final String url, final Activity a) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    result[0] = getRestApi(url);

                } finally {
                    Log.d("debuging42", url);
                    Log.d("debuging42", result[0]);
                    try {
                        ArrayList<LatLng> points = new ArrayList<>();
                        JSONObject root = new JSONObject(result[0]);
                        JSONArray snappedPoints = (JSONArray) root.get("snappedPoints");
                        for (int i = 0; i < snappedPoints.length(); i++) {
                            JSONObject point = snappedPoints.getJSONObject(i);
                            JSONObject location = (JSONObject) point.get("location");
                            double lat = (double) location.get("latitude");
                            double lng = (double) location.get("longitude");
                            points.add(new LatLng(lat, lng));
                            Log.d("raods_data_json", lat + ", " + lng);
                        }
                        String urlBuilder = "";
                        for (int i = 1; i < points.size() - 1; i++) {
                            LatLng latLng1 = points.get(i);
                            urlBuilder += latLng1.latitude + "," + latLng1.longitude;
                            if (i != (points.size() - 1)) {
                                urlBuilder += "|via:";
                            }
                        }
                        urlBuilder = "https://maps.googleapis.com/maps/api/directions/json?origin=" +
                                points.get(0).latitude + "," + points.get(0).longitude +
                                "&destination=" +
                                points.get(points.size() - 1).latitude + "," + points.get(points.size() - 1).longitude +
                                "&waypoints=via:" + urlBuilder +
                                "&key=" +
                                API_KEY;
                        createThreadGetForRouteBetweenThreeLocationsUsingWaypoints(urlBuilder, a);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public static void createThreadGetForRouteBetweenThreeLocationsUsingWaypoints(final String url, final Activity a) {
        final String[] results = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    results[0] = getRestApi(url);
                } finally {
                    Log.d("debuging42", url);
                    Log.d("debuging42", results[0]);
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("raods_data_json", results[0]);
                            plotRouteBetweenThreeLocations(results, a);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public static void createThreadGetForRouteBetweenThreeLocations(final ArrayList<String> urls, final Activity a) {
        final String[] results = new String[urls.size()];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < urls.size(); i++) {
                        String url = urls.get(i);
                        results[i] = getRestApi(url);
                    }
                } finally {
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            plotRouteBetweenThreeLocations(results, a);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private static void plotRouteBetweenThreeLocations(String[] routes, Activity a) {
        deleteOldRoute();
//        previousRouteBetweenThreePoints = "";
        for (String route : routes) {
//            previousRouteBetweenTwoPoints += route;
            renderRouteBetweenTwoLocations(route, a);
        }
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
        for (Polyline p : routeBetweenThreePointsPolylines) {
            p.remove();
        }
        routeBetweenThreePointsPolylines.clear();
    }
}
