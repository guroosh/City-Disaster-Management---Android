package com.example.androidase_.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidase_.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class LocationActivity extends AppCompatActivity implements LocationListener {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        textView = this.findViewById(R.id.user_location);
        Intent intent = getIntent();
        String value = intent.getStringExtra("key");
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        createDummyLocation();
    }

    private void createDummyLocation() {
        double lat = 53.341556;
        double lng = -6.253232;
        String latLng = "Latitude: " + lat + "\nLongitude: " + lng;
        textView.setText(latLng);
    }

    @Override
    public void onLocationChanged(Location location) {
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        String latLng = "Latitude: " + lat + "\nLongitude: " + lng;
        textView.setText(latLng);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
