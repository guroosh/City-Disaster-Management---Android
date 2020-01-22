package com.example.androidase_.object_classes;

import com.google.android.gms.maps.model.LatLng;

public class ReportedDisaster {
    private LatLng location;
    private double radius = 0;  // in meters

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public boolean amIInsideTheDisaster(LatLng userLocation)
    {
        return false;
    }
}
