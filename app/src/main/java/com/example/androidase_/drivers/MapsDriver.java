package com.example.androidase_.drivers;

import android.app.Activity;
import android.graphics.Color;

import com.example.androidase_.other_classes.MathOperations;
import com.example.androidase_.other_classes.PathJSONParser;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.example.androidase_.activities.MapsActivity.*;
import static com.example.androidase_.other_classes.MathOperations.getRandomExitPointNearCircleCircumference;
import static com.example.androidase_.other_classes.MathOperations.isLocationInsideCircle;

public class MapsDriver {

    static void drawCircle(LatLng point, double radius1, boolean isDisasterOnUserLocation, Activity a) {
        // Instantiating CircleOptions to draw a circle around the marker
        CircleOptions circleOptions = new CircleOptions();
        // Specifying the center of the circle
        circleOptions.center(point);
        // Radius of the circle
        circleOptions.radius(radius1);
        // Border color of the circle
        circleOptions.strokeColor(Color.BLACK);
        // Fill color of the circle
        circleOptions.fillColor(0x30ff0000);
        // Border width of the circle
        circleOptions.strokeWidth(2);
        // Adding the circle to the GoogleMap
        for (Circle c : circleArrayList) {
            c.remove();
        }
        Circle circle = mMap.addCircle(circleOptions);
        circleArrayList.add(circle);
        changeBound(point, radius1);
        showExitRoute(point, radius1, isDisasterOnUserLocation, a);
    }

    private static void changeBound(LatLng center, double radius) {
        if (isLocationInsideCircle(globalCurrentLocation.latitude, globalCurrentLocation.longitude, radius, center)) {
            double leftLng = center.longitude - radius * 0.1 / 6644.971989103;
            double rightLng = center.longitude + radius * 0.1 / 6644.971989103;
            LatLng leftBound = new LatLng(center.latitude, leftLng);
            LatLng rightBound = new LatLng(center.latitude, rightLng);
            animateUsingBound(leftBound, rightBound, 100);
        } else {
            double leftLng = center.longitude - radius * 0.1 / 6644.971989103;
            double rightLng = center.longitude + radius * 0.1 / 6644.971989103;
            LatLng leftBound = new LatLng(center.latitude, leftLng);
            LatLng rightBound = new LatLng(center.latitude, rightLng);
            animateUsingBound(new ArrayList<>(Arrays.asList(leftBound, rightBound, globalCurrentLocation)), 100);
        }
    }

    public static void animateUsingBound(LatLng pos1, LatLng pos2, int padding) {
        LatLngBounds bounds;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pos1);
        builder.include(pos2);
        bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    public static void animateUsingBound(ArrayList<LatLng> positions, int padding) {
        LatLngBounds bounds;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng pos : positions) {
            builder.include(pos);
        }
        bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private static void showExitRoute(LatLng disasterLocation, double radius, boolean isDisasterOnUserLocation, Activity a) {
        double lng1 = globalCurrentLocation.longitude;
        double lat1 = globalCurrentLocation.latitude;
        double lng2 = disasterLocation.longitude;
        double lat2 = disasterLocation.latitude;
        double userDistanceFromDisaster = MathOperations.measureDistanceInMeters(lat1, lng1, lat2, lng2);
        if (userDistanceFromDisaster <= radius) {
            ArrayList<LatLng> randomExitLocations = getRandomExitPointNearCircleCircumference(disasterLocation, radius, isDisasterOnUserLocation);
            for (LatLng randomExitLocation : randomExitLocations) {
                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + globalCurrentLocation.latitude + "," + globalCurrentLocation.longitude +
                        "&destination=" + randomExitLocation.latitude + "," + randomExitLocation.longitude +
                        "&key=" + API_KEY;
                HttpDriver.createThreadGetForRoute(url, a);
            }
        }
        deleteOldRoute();
    }

    private static void deleteOldRoute() throws NullPointerException {
        if (!previousRoute.equals("")) {
            for (Polyline p : polylines) {
                p.remove();
            }
            polylines.clear();
        }
    }

    static void renderRoute(String jsonData) throws NullPointerException {
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

        // traversing through routes
        assert routes != null;
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
        polylines.add(mMap.addPolyline(polyLineOptions));
    }
}
