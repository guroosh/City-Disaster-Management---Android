package com.example.androidase_.drivers;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;

import androidx.core.content.ContextCompat;

import com.example.androidase_.R;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.androidase_.activities.MapsActivity.*;
import static com.example.androidase_.drivers.MathOperationsDriver.isLocationInsideCircle;

public class MapsDriver {

    public static void drawCircle(LatLng point, double radius1, Activity a) {
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
    }

    public static void changeCameraBound(LatLng center, double radius) {
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

    public static void initiateRandomCircleCreation(ReportedDisaster reportedDisaster, Activity a) {
        /* START code to create random circle*/
        LatLng disasterLocation = new LatLng(053.3498, -6.2603);
        reportedDisaster.setLocation(disasterLocation);
        circleCenter = disasterLocation;
        Random r = new Random();
        int radius = 200 + r.nextInt(1000);
        reportedDisaster.setRadius(radius);
        circleRadius = radius;
        drawCircle(disasterLocation, radius, a);
        changeCameraBound(disasterLocation, radius);
        /* END */
    }

    public static void plotBusStopsOnScreen(HashMap<String, LatLng> busStopsOnScreen, Activity a) {
        for (Map.Entry<String, LatLng> entry : busStopsOnScreen.entrySet()) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(entry.getValue());
            BitmapDrawable bitmapDrawable;
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(a.getApplicationContext(), R.drawable.yellow);
            assert bitmapDrawable != null;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 20, 20, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            Marker marker = mMap.addMarker(markerOptions);
            busStopsOnScreenMarkers.add(marker);
        }
    }

    public static void deleteBusStopsPreviouslyOnScreen() {
        for (Marker m : busStopsOnScreenMarkers) {
            m.remove();
        }
        busStopsOnScreenMarkers.clear();
    }
}
