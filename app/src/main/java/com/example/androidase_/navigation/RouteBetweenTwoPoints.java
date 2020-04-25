package com.example.androidase_.navigation;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;

import com.example.androidase_.other_classes.PathJSONParser;
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

import static com.example.androidase_.activities.MapsActivity.isNavigating;
import static com.example.androidase_.navigation.RouteBetweenThreePoints.initialiseProcessForRouteBetweenThreePoints;
import static com.example.androidase_.activities.MapsActivity.circleCenter;
import static com.example.androidase_.activities.MapsActivity.circleRadius;
import static com.example.androidase_.activities.MapsActivity.globalCurrentLocation;
import static com.example.androidase_.activities.MapsActivity.mMap;
import static com.example.androidase_.activities.MapsActivity.routeBetweenTwoPointsPolylines;
//import static com.example.androidase_.activities.MapsActivity.previousRouteBetweenTwoPoints;
import static com.example.androidase_.activities.MapsActivity.searchedDestination;
import static com.example.androidase_.drivers.HttpDriver.getRestApi;
import static com.example.androidase_.drivers.MathOperationsDriver.isLocationInsideCircle;
import static com.example.androidase_.drivers.MathOperationsDriver.measureDistanceInMeters;
import static com.example.androidase_.drivers.MathOperationsDriver.minOfTwoDoubles;

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
        renderRouteBetweenTwoLocations(result, a);
    }

    private static void renderRouteBetweenTwoLocations(String jsonData, Activity a) throws NullPointerException {
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
                if (circleCenter == null) {
                    isRerouteRequired = false;
                } else {
                    if (isLocationInsideCircle(lat, lng, circleRadius, circleCenter)) {
                        isRerouteRequired = true;
                    }
                }
                points.add(position);
            }
            polyLineOptions.addAll(points);
            polyLineOptions.width(15);
//            if (!isRerouteRequired) {
//                polyLineOptions.color(Color.BLUE);
//            } else {
//                polyLineOptions.color(Color.RED);
//            }
            if (!isRerouteRequired && isNavigating) {
                return;
            }
        }
        if (isRerouteRequired) {
            LatLng currentLocation = globalCurrentLocation;
            double deltaLat = (currentLocation.latitude - searchedDestination.latitude) * 11131.94907932;
            double deltaLng = (currentLocation.longitude - searchedDestination.longitude) * 6644.971989103;
            double angleBetweenTwoPoints;
            if (deltaLng != 0) {
                angleBetweenTwoPoints = Math.toDegrees(Math.atan(deltaLat / deltaLng));
            } else {
                angleBetweenTwoPoints = 90;
            }
            double perpendicularAngle, angleAt45_1, angleAt45_2;
            if (angleBetweenTwoPoints > 0) {
                perpendicularAngle = angleBetweenTwoPoints - 90;
            } else if (angleBetweenTwoPoints < 0) {
                perpendicularAngle = angleBetweenTwoPoints + 90;
            } else {
                perpendicularAngle = 90;
            }

            if (-45 < angleBetweenTwoPoints && angleBetweenTwoPoints < 45) {
                angleAt45_1 = angleBetweenTwoPoints + 45;
                angleAt45_2 = angleBetweenTwoPoints - 45;
            } else if (angleBetweenTwoPoints >= 45) {
                angleAt45_1 = angleBetweenTwoPoints - 135;
                angleAt45_2 = angleBetweenTwoPoints - 45;
            } else {
                angleAt45_1 = angleBetweenTwoPoints + 45;
                angleAt45_2 = angleBetweenTwoPoints + 135;
            }

            Log.d("angle42", angleBetweenTwoPoints + ", " + perpendicularAngle);
            double newLat1;
            double newLng1;
            double newLat2;
            double newLng2;

            double extendedPointLat1;
            double extendedPointLng1;
            double extendedPointLat2;
            double extendedPointLng2;

            if (angleBetweenTwoPoints > 0) {
                extendedPointLat1 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleBetweenTwoPoints)));
                extendedPointLng1 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleBetweenTwoPoints)));
                extendedPointLat2 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleBetweenTwoPoints)));
                extendedPointLng2 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleBetweenTwoPoints)));
            } else {
                extendedPointLat1 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleBetweenTwoPoints)));
                extendedPointLng1 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleBetweenTwoPoints)));
                extendedPointLat2 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleBetweenTwoPoints)));
                extendedPointLng2 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleBetweenTwoPoints)));
            }

            if (measureDistanceInMeters(extendedPointLat1, extendedPointLng1, currentLocation.latitude, currentLocation.longitude) > measureDistanceInMeters(extendedPointLat2, extendedPointLng2, currentLocation.latitude, currentLocation.longitude)) {
                double temp = extendedPointLat1;
                extendedPointLat1 = extendedPointLat2;
                extendedPointLat2 = temp;

                temp = extendedPointLng1;
                extendedPointLng1 = extendedPointLng2;
                extendedPointLng2 = temp;
            }

            if (perpendicularAngle > 0) {
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
//            mMap.addMarker(markerOptions1);
//            mMap.addMarker(markerOptions2);
            double minLat;
            double minLng;
            if ((measureDistanceInMeters(newLat1, newLng1, currentLocation.latitude, currentLocation.longitude)) < (measureDistanceInMeters(newLat2, newLng2, currentLocation.latitude, currentLocation.longitude))) {
                minLat = newLat1;
                minLng = newLng1;
            } else {
                minLat = newLat2;
                minLng = newLng2;
            }
            // new locations at 45 are between start and min location, and, between min and end location
            double newLatAt45_1;
            double newLngAt45_1;
            double newLatAt45_2;
            double newLngAt45_2;
            double newLatAt45_3;
            double newLngAt45_3;
            double newLatAt45_4;
            double newLngAt45_4;
            if (angleAt45_1 > 0) {
                newLatAt45_1 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_1)));
                newLngAt45_1 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_1)));
                newLatAt45_2 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_1)));
                newLngAt45_2 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_1)));
            } else {
                newLatAt45_1 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_1)));
                newLngAt45_1 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_1)));
                newLatAt45_2 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_1)));
                newLngAt45_2 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_1)));
            }
            if (angleAt45_1 > 0) {
                newLatAt45_3 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_2)));
                newLngAt45_3 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_2)));
                newLatAt45_4 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_2)));
                newLngAt45_4 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_2)));
            } else {
                newLatAt45_3 = circleCenter.latitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_2)));
                newLngAt45_3 = circleCenter.longitude + 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_2)));
                newLatAt45_4 = circleCenter.latitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(angleAt45_2)));
                newLngAt45_4 = circleCenter.longitude - 2 * ((circleRadius + (circleRadius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(angleAt45_2)));
            }
            MarkerOptions markerOptions3 = new MarkerOptions();
            MarkerOptions markerOptions4 = new MarkerOptions();
            MarkerOptions markerOptions5 = new MarkerOptions();
            MarkerOptions markerOptions6 = new MarkerOptions();
            markerOptions3.position(new LatLng(newLatAt45_1, newLngAt45_1));
            markerOptions4.position(new LatLng(newLatAt45_2, newLngAt45_2));
            markerOptions5.position(new LatLng(newLatAt45_3, newLngAt45_3));
            markerOptions6.position(new LatLng(newLatAt45_4, newLngAt45_4));
            markerOptions3.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            markerOptions4.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
            markerOptions5.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            markerOptions6.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//            mMap.addMarker(markerOptions3);
//            mMap.addMarker(markerOptions4);
//            mMap.addMarker(markerOptions5);
//            mMap.addMarker(markerOptions6);

            double minLatFor45_1 = 0;
            double minLngFor45_1 = 0;
            double minLatFor45_2 = 0;
            double minLngFor45_2 = 0;
            double d1 = measureDistanceInMeters(currentLocation.latitude, currentLocation.longitude, newLatAt45_1, newLngAt45_1);
            double d2 = measureDistanceInMeters(currentLocation.latitude, currentLocation.longitude, newLatAt45_2, newLngAt45_2);
            double d3 = measureDistanceInMeters(currentLocation.latitude, currentLocation.longitude, newLatAt45_3, newLngAt45_3);
            double d4 = measureDistanceInMeters(currentLocation.latitude, currentLocation.longitude, newLatAt45_4, newLngAt45_4);

            double d5 = measureDistanceInMeters(searchedDestination.latitude, searchedDestination.longitude, newLatAt45_1, newLngAt45_1);
            double d6 = measureDistanceInMeters(searchedDestination.latitude, searchedDestination.longitude, newLatAt45_2, newLngAt45_2);
            double d7 = measureDistanceInMeters(searchedDestination.latitude, searchedDestination.longitude, newLatAt45_3, newLngAt45_3);
            double d8 = measureDistanceInMeters(searchedDestination.latitude, searchedDestination.longitude, newLatAt45_4, newLngAt45_4);

            double minD1 = minOfTwoDoubles(d1, minOfTwoDoubles(d2, minOfTwoDoubles(d3, d4)));
            double minD2 = minOfTwoDoubles(d5, minOfTwoDoubles(d6, minOfTwoDoubles(d7, d8)));

            if (minD1 == d1) {
                minLatFor45_1 = newLatAt45_1;
                minLngFor45_1 = newLngAt45_1;
            }
            if (minD1 == d2) {
                minLatFor45_1 = newLatAt45_2;
                minLngFor45_1 = newLngAt45_2;
            }
            if (minD1 == d3) {
                minLatFor45_1 = newLatAt45_3;
                minLngFor45_1 = newLngAt45_3;
            }
            if (minD1 == d4) {
                minLatFor45_1 = newLatAt45_4;
                minLngFor45_1 = newLngAt45_4;
            }

            if (minD2 == d5) {
                minLatFor45_2 = newLatAt45_1;
                minLngFor45_2 = newLngAt45_1;
            }
            if (minD2 == d6) {
                minLatFor45_2 = newLatAt45_2;
                minLngFor45_2 = newLngAt45_2;
            }
            if (minD2 == d7) {
                minLatFor45_2 = newLatAt45_3;
                minLngFor45_2 = newLngAt45_3;
            }
            if (minD2 == d8) {
                minLatFor45_2 = newLatAt45_4;
                minLngFor45_2 = newLngAt45_4;
            }
            Log.d("debugd42", d1 + ", " + d2 + ", " + d3 + ", " + d4 + ", " + minD1 + ", " + d5 + ", " + d6 + ", " + d7 + ", " + d8 + ", " + minD2);


            ArrayList<LatLng> locations = new ArrayList<>();
            locations.add(new LatLng(currentLocation.latitude, currentLocation.longitude));
            if (isLocationInsideCircle(currentLocation.latitude, currentLocation.longitude, 2 * circleRadius, circleCenter)) {
                locations.add(new LatLng(extendedPointLat1, extendedPointLng1));
            }
            locations.add(new LatLng(minLatFor45_1, minLngFor45_1));
            locations.add(new LatLng(minLat, minLng));
            locations.add(new LatLng(minLatFor45_2, minLngFor45_2));
            if (isLocationInsideCircle(searchedDestination.latitude, searchedDestination.longitude, 2 * circleRadius, circleCenter)) {
                locations.add(new LatLng(extendedPointLat2, extendedPointLng2));
            }
            locations.add(new LatLng(searchedDestination.latitude, searchedDestination.longitude));
            initialiseProcessForRouteBetweenThreePoints(locations, a);
        }
        if (!isRerouteRequired) {
            polyLineOptions.color(Color.BLUE);
            routeBetweenTwoPointsPolylines.add(mMap.addPolyline(polyLineOptions));
        } else {
            //don't plot route through the disaster
        }

        isNavigating = true;
        Log.d("Navigation42", String.valueOf(isNavigating));
    }

    private static void deleteOldRoute() throws NullPointerException {
//        if (!previousRouteBetweenTwoPoints.equals("")) {
        for (Polyline p : routeBetweenTwoPointsPolylines) {
            p.remove();
        }
        routeBetweenTwoPointsPolylines.clear();
//        }
    }
}
