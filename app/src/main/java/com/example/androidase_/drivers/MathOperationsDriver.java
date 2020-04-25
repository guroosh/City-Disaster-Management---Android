package com.example.androidase_.drivers;

import com.example.androidase_.object_classes.ReturnShift;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Random;

import static com.example.androidase_.activities.MapsActivity.globalCurrentLocation;

public class MathOperationsDriver {

    public static double measureDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double R = 6378.137; // Radius of earth in KM
        double dLat = (lat2 * Math.PI / 180) - (lat1 * Math.PI / 180);
        double dLon = (lon2 * Math.PI / 180) - (lon1 * Math.PI / 180);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d * 1000; // meters
    }

    public static boolean isLocationInsideCircle(double lat, double lng, double radius, LatLng center) {
        double distanceFromCenter = measureDistanceInMeters(lat, lng, center.latitude, center.longitude);
        return distanceFromCenter <= radius;
    }

    public static LatLng getExitPointNearCircleCircumferenceAtAngle(LatLng disasterLocation, double radius, int degree)
    {
        double lat, lng, newLat, newLng;
        double angleInRadians;
        angleInRadians = Math.toRadians(degree);
        lat = disasterLocation.latitude;
        lng = disasterLocation.longitude;
        newLat = lat + ((2 * radius + (radius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(angleInRadians));
        newLng = lng + ((2 * radius + (radius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(angleInRadians));
        return new LatLng(newLat, newLng);
    }

    public static ArrayList<LatLng> getRandomExitPointNearCircleCircumference(LatLng disasterLocation, double radius, boolean isDisasterOnUserLocation) {
        Random r = new Random();
        double degree, lat, lng, newLat, newLng;
        ArrayList<LatLng> returnArray = new ArrayList<>();
        if (isDisasterOnUserLocation) {
            for (int i = 0; i < 1; i++) {
                degree = r.nextInt(360);
                degree = Math.toRadians(degree);
                lat = disasterLocation.latitude;
                lng = disasterLocation.longitude;
                newLat = lat + ((2 * radius + (radius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(degree));
                newLng = lng + ((2 * radius + (radius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(degree));
                returnArray.add(new LatLng(newLat, newLng));
            }
        } else {
            ReturnShift returnShift = getExitingDegree(disasterLocation);
            int[] arr = {0};
            degree = returnShift.degree;
            for (int shift : arr) {
                double newDegree = degree + shift;
                lat = disasterLocation.latitude;
                lng = disasterLocation.longitude;
                newLat = lat + returnShift.shiftForSecondAndThirdQuadrant * ((2 * radius + (radius * 0.1)) * (0.1 / 11131.94907932) * Math.sin(Math.toRadians(newDegree)));
                newLng = lng + returnShift.shiftForSecondAndThirdQuadrant * ((2 * radius + (radius * 0.1)) * (0.1 / 6644.971989103) * Math.cos(Math.toRadians(newDegree)));
                returnArray.add(new LatLng(newLat, newLng));
            }
        }
        return returnArray;
    }

    private static ReturnShift getExitingDegree(LatLng disasterLocation) {
        double deltaLat = (globalCurrentLocation.latitude - disasterLocation.latitude) * 11131.94907932;
        double deltaLng = (globalCurrentLocation.longitude - disasterLocation.longitude) * 6644.971989103;
        ReturnShift returnShift = new ReturnShift();
        returnShift.degree = Math.toDegrees(Math.atan(deltaLat / deltaLng));

        //second quadrant
        if (globalCurrentLocation.latitude > disasterLocation.latitude && globalCurrentLocation.longitude < disasterLocation.longitude) {
            returnShift.shiftForSecondAndThirdQuadrant = -1;
        }

        //third quadrant
        if (globalCurrentLocation.latitude < disasterLocation.latitude && globalCurrentLocation.longitude < disasterLocation.longitude) {
            returnShift.shiftForSecondAndThirdQuadrant = -1;
        }
        return returnShift;
    }

    public static LatLng getRandomExitPoint(double radius) {
        Random r = new Random();
        double lng = -6.310015 + r.nextDouble() * (-6.230852 + 6.310015);
        double lat = 53.330091 + r.nextDouble() * (53.359967 - 53.330091);
        double lng1 = globalCurrentLocation.longitude;
        double lat1 = globalCurrentLocation.latitude;
        LatLng randomExitPoint = new LatLng(lat, lng);
        if (MathOperationsDriver.measureDistanceInMeters(lat, lng, lat1, lng1) <= radius) {
            return getRandomExitPoint(radius);
        } else {
            return randomExitPoint;
        }
    }

    public static double minOfTwoDoubles(double d1, double d2) {
        if (d1 < d2) {
            return d1;
        }
        return d2;
    }
}

