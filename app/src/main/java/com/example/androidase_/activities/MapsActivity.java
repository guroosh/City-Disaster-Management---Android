package com.example.androidase_.activities;

import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.mqtt.MqttActivity;
import com.example.androidase_.mqtt.MqttMessageService;
import com.example.androidase_.mqtt.MqttMessageServiceForMaps;
import com.example.androidase_.mqtt.PahoMqttClient;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.drivers.AlertDriver;
import com.example.androidase_.drivers.HttpDriver;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.example.androidase_.drivers.MapsDriver.animateUsingBound;
import static com.example.androidase_.mqtt.MqttActivity.MQTT_BROKER_URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static GoogleMap mMap;
    public static String previousRoute = "";
    public static ArrayList<Marker> markerListCurrentLocation;
    public static ArrayList<Marker> markerListDisaster;
    public static LatLng globalCurrentLocation;
    public static ArrayList<LatLng> busStopList = new ArrayList<>();
    public static String API_KEY;
    public static String globalPotentialDisasterName;
    public static ArrayList<Circle> circleArrayList = new ArrayList<>();
    public static ArrayList<Polyline> polylines = new ArrayList<>();
    public static String username;

    boolean isStartCurrentLocationSet = false;
    Activity a = this;
    AlertDriver alertDriver;
    ReportedDisaster reportedDisaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        username = intent.getStringExtra("username");

        //init
        API_KEY = "AIzaSyBPOVbWCZG6Weeunh-J2-t3NiyG_1-NXpQ";
        markerListCurrentLocation = new ArrayList<>();
        markerListDisaster = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //location setup
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        initializePlaces();

        Button buttonShowCurrentLoc = findViewById(R.id.button_show_current_location);
        buttonShowCurrentLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (globalCurrentLocation == null) {
                    Toast toast = Toast.makeText(getApplicationContext(), "GPS not connected", Toast.LENGTH_SHORT);
                    View view = toast.getView();
                    view.setBackgroundColor(Color.DKGRAY);
                    TextView text = view.findViewById(android.R.id.message);
                    text.setTextColor(Color.WHITE);
                    toast.show();
                } else {
                    if (markerListDisaster.size() > 0) {
                        animateUsingBound(markerListCurrentLocation.get(markerListCurrentLocation.size() - 1).getPosition(), markerListDisaster.get(markerListDisaster.size() - 1).getPosition(), 100);
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(globalCurrentLocation, 15.0f));
                    }
                }
            }
        });

        final Button reportDisaster = findViewById(R.id.button_report_disaster);
        reportDisaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String potentialDisasterName;
                LatLng tempLocation;
                boolean isDisasterOnUserLocation;
                if (markerListDisaster.size() > 0) {
                    tempLocation = markerListDisaster.get(markerListDisaster.size() - 1).getPosition();
                    potentialDisasterName = globalPotentialDisasterName;
                    isDisasterOnUserLocation = false;
                } else {
                    tempLocation = markerListCurrentLocation.get(markerListCurrentLocation.size() - 1).getPosition();
                    potentialDisasterName = "your current location";
                    isDisasterOnUserLocation = true;
                }
                reportedDisaster = new ReportedDisaster();
                alertDriver = new AlertDriver();
                alertDriver.createAlert(MapsActivity.this, "Are you sure to report disaster at " + potentialDisasterName + "?", "Yes", "No", tempLocation, reportedDisaster, isDisasterOnUserLocation, a);
            }
        });

//        //Option 1 to start Mqtt listener
//        PahoMqttClient pahoMqttClient = new PahoMqttClient();
//        MqttAndroidClient client = pahoMqttClient.getMqttClient(getApplicationContext(), MQTT_BROKER_URL, 0);

        //Option 2 to start Mqtt listener
//        Intent intent2 = new Intent(MapsActivity.this, MqttMessageServiceForMaps.class);
//        startService(intent2);
    }

    private void initializePlaces() throws NullPointerException {
        Places.initialize(getApplicationContext(), API_KEY);
        PlacesClient placesClient = Places.createClient(this);
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                HttpDriver.createThreadGetForLocation("https://maps.googleapis.com/maps/api/place/details/json?placeid=" +
                        place.getId() + "&key=" +
                        API_KEY, place.getName(), a, mMap);
            }

            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i("TAG", "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        createDummyLocation();
        HttpDriver.createThreadGetForBusStops(a, mMap);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
//                Log.d("output42", bounds.northeast.latitude + ", " + bounds.northeast.longitude + "; " + bounds.southwest.latitude + ", "  + bounds.southwest.longitude);
                for (LatLng busStop : busStopList) {
                    if (bounds.contains(busStop)) {
                        Log.d("output42", busStop.latitude + ", " + busStop.longitude);
                    } else {
                        Log.d("error42", busStop.latitude + ", " + busStop.longitude);
                    }
                }
                Toast.makeText(getApplicationContext(), "Moving: " + busStopList.size(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createDummyLocation() throws NullPointerException {
        Random r = new Random();
        double lng = -6.310015 + r.nextDouble() * (-6.230852 + 6.310015);
        double lat = 53.330091 + r.nextDouble() * (53.359967 - 53.330091);

        LatLng currentLocation = new LatLng(lat, lng);
        globalCurrentLocation = currentLocation;
        Marker marker = mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        for (Marker m : markerListCurrentLocation)
            m.remove();
        markerListCurrentLocation.add(marker);
        marker.setTag(-1);
        marker.setTitle("USER LOCATION");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        globalCurrentLocation = currentLocation;
        Marker marker = mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        for (Marker m : markerListCurrentLocation)
            m.remove();
        markerListCurrentLocation.add(marker);
        marker.setTag(-1);
        marker.setTitle("USER LOCATION");
        if (!isStartCurrentLocationSet) {
            isStartCurrentLocationSet = true;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

}
