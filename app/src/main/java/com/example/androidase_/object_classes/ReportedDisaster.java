package com.example.androidase_.object_classes;

import com.google.android.gms.maps.model.LatLng;

public class ReportedDisaster {
    private LatLng location;
    private double radius = 0;  // in meters
    private String time;
    private String verifiedBy;

    public LatLng getLocation() {
        return location;
    }
    public void setLocation(LatLng location) {
        this.location = location;
    }
    public String getTime(){return time;}
    public void setTime(String time){this.time = time;}
    public double getRadius() {
        return radius;
    }
    public void setRadius(double radius) {
        this.radius = radius;
    }
    public String getVerifiedBy(){return verifiedBy;}
    public void setVerifiedBy(String verifiedBy){this.verifiedBy = verifiedBy;}


    public boolean amIInsideTheDisaster(LatLng userLocation)
    {
        return false;
    }

    public void createTestValue(){
        setLocation(new LatLng(100,200));
        //setRadius(1000);
        setTime("1/2/2020");
        setVerifiedBy("Groosh");
    }
}
