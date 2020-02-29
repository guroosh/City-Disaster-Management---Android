package com.example.androidase_.Navigation;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.other_classes.PathJSONParser;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
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
import static com.example.androidase_.activities.MapsActivity.globalCurrentLocation;
import static com.example.androidase_.activities.MapsActivity.mMap;
import static com.example.androidase_.activities.MapsActivity.routeBetweenTwoPointsPolylines;
import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenTwoPoints;
import static com.example.androidase_.activities.MapsActivity.searchedDestination;
import static com.example.androidase_.drivers.HttpDriver.getRestApi;
import static com.example.androidase_.drivers.MathOperationsDriver.isLocationInsideCircle;
import static com.example.androidase_.drivers.MathOperationsDriver.measureDistanceInMeters;

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
                            plotRouteBetweenTwoLocations(result[0], a);
                        }
                    });
                }
            }
        });
        thread.start();
    }


    private static void plotRouteBetweenTwoLocations(final String result, Activity a) {
        deleteOldRoute();
        previousRouteBetweenTwoPoints = result;
        renderRouteBetweenTwoLocations(result, a);
    }

    static void renderRouteBetweenTwoLocations(String jsonData, Activity a) throws NullPointerException {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        boolean isRerouteRequired = false;
        try {
            jObject = new JSONObject(jsonData);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<LatLng> points;
        ArrayList<PolylineOptions> polyLineOptionsList = new ArrayList<>();
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
                if (isLocationInsideCircle(lat, lng, circleRadius, circleCenter)) {
                    isRerouteRequired = true;
                }
                points.add(position);
            }
            polyLineOptions.addAll(points);
            polyLineOptions.width(15);
            if (!isRerouteRequired) {
                polyLineOptions.color(Color.BLUE);
            } else {
                polyLineOptions.color(Color.RED);
            }
        }
        if (isRerouteRequired) {
            double deltaLat = (globalCurrentLocation.latitude - searchedDestination.latitude) * 11131.94907932;
            double deltaLng = (globalCurrentLocation.longitude - searchedDestination.longitude) * 6644.971989103;
            double angleBetweenTwoPoints = Math.toDegrees(Math.atan(deltaLat / deltaLng));
            double perpendicularAngle;
            if (angleBetweenTwoPoints > 0) {
                perpendicularAngle = angleBetweenTwoPoints - 90;
            } else if (angleBetweenTwoPoints < 0) {
                perpendicularAngle = angleBetweenTwoPoints + 90;
            } else {
                perpendicularAngle = 90;
            }
            Log.d("angle42", angleBetweenTwoPoints + ", " + perpendicularAngle);
            double newLat1;
            double newLng1;
            double newLat2;
            double newLng2;
            if (angleBetweenTwoPoints < 0) {
                newLat1 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(perpendicularAngle)));
                newLng1 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(perpendicularAngle)));
                newLat2 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(perpendicularAngle)));
                newLng2 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(perpendicularAngle)));
            } else {
                newLat1 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(perpendicularAngle)));
                newLng1 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(perpendicularAngle)));
                newLat2 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(perpendicularAngle)));
                newLng2 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(perpendicularAngle)));
            }
            MarkerOptions markerOptions1 = new MarkerOptions();
            MarkerOptions markerOptions2 = new MarkerOptions();
            markerOptions1.position(new LatLng(newLat1, newLng1));
            markerOptions2.position(new LatLng(newLat2, newLng2));
            markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
            markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            mMap.addMarker(markerOptions1);
            mMap.addMarker(markerOptions2);
            double minLat;
            double minLng;
            if ((measureDistanceInMeters(newLat1, newLng1, globalCurrentLocation.latitude, globalCurrentLocation.longitude)) < (measureDistanceInMeters(newLat2, newLng2, globalCurrentLocation.latitude, globalCurrentLocation.longitude))) {
                minLat = newLat1;
                minLng = newLng1;
            } else {
                minLat = newLat2;
                minLng = newLng2;
            }
            RouteBetweenThreePoints.initialiseProcessForRouteBetweenThreePoints(globalCurrentLocation.latitude, globalCurrentLocation.longitude, minLat, minLng, searchedDestination.latitude, searchedDestination.longitude, a);
        }
        routeBetweenTwoPointsPolylines.add(mMap.addPolyline(polyLineOptions));

        /* START code to plot blue and red route, depending on inside or outside the circle */
//        // traversing through routes
//        assert routes != null;
//        for (int i = 0; i < routes.size(); i++) {
//            points = new ArrayList<>();
//            List<HashMap<String, String>> path = routes.get(i);
//
//            for (int j = 0; j < path.size(); j++) {
//                HashMap<String, String> point = path.get(j);
//                double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
//                double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
//                LatLng position = new LatLng(lat, lng);
//                points.add(position);
//            }
//
//            for (int k = 0; k < points.size() - 1; k++) {
//                LatLng point1 = points.get(k);
//                LatLng point2 = points.get(k + 1);
//                boolean bool1 = isLocationInsideCircle(point1.latitude, point1.longitude, circleRadius, circleCenter);
//                boolean bool2 = isLocationInsideCircle(point2.latitude, point2.longitude, circleRadius, circleCenter);
//                if (bool1 && bool2) {
//                    PolylineOptions polylineOptions = new PolylineOptions();
//                    polylineOptions.add(point1);
//                    polylineOptions.add(point2);
//                    polylineOptions.width(15);
//                    polylineOptions.color(Color.RED);
//                    polyLineOptionsList.add(polylineOptions);
//                } else {
//                    PolylineOptions polylineOptions = new PolylineOptions();
//                    polylineOptions.add(point1);
//                    polylineOptions.add(point2);
//                    polylineOptions.width(15);
//                    polylineOptions.color(Color.BLUE);
//                    polyLineOptionsList.add(polylineOptions);
//                }
//            }
//        }
//        for (PolylineOptions p : polyLineOptionsList) {
//            routeBetweenTwoPointsPolylines.add(mMap.addPolyline(p));
//        }
        /* END */
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
