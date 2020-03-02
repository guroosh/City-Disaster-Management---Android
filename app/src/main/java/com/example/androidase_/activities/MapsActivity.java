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
import com.example.androidase_.drivers.MapsDriver;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.ReportingDisaster.DisasterReportAlert;
import com.example.androidase_.drivers.HttpDriver;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.example.androidase_.Navigation.RouteBetweenTwoPoints.createThreadGetForRouteBetweenTwoLocations;
import static com.example.androidase_.drivers.MapsDriver.animateUsingBound;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static GoogleMap mMap;
    public static ArrayList<Marker> markerListCurrentLocation;
    public static ArrayList<Marker> markerListDisaster;
    public static LatLng globalCurrentLocation;
    public static HashMap<String, LatLng> busStopList = new HashMap<>();
    public static String API_KEY;
    public static String globalPotentialDisasterName;

    //These lists are to keep track of icons on the map and remove them if necessary
    public static ArrayList<Circle> circleArrayList = new ArrayList<>();
    public static ArrayList<Polyline> exitRoutePolylines = new ArrayList<>();
    public static ArrayList<Polyline> routeBetweenTwoPointsPolylines = new ArrayList<>();
    public static ArrayList<Polyline> routeBetweenThreePointsPolylines = new ArrayList<>();
    public static ArrayList<Marker> busStopsOnScreenMarkers = new ArrayList<>();

    public static String username;
    public static LatLng searchedDestination;
    public static LatLng circleCenter;
    public static double circleRadius;

    boolean isStartCurrentLocationSet = false;
    Activity a = this;
    DisasterReportAlert disasterReportAlert;
    ReportedDisaster reportedDisaster;
    public Button getDirectionsBetweenTwoLocations;

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
                disasterReportAlert = new DisasterReportAlert();
                disasterReportAlert.createAlert(MapsActivity.this, "Are you sure to report disaster at " + potentialDisasterName + "?", "Yes", "No", tempLocation, reportedDisaster, isDisasterOnUserLocation, a);
            }
        });

        getDirectionsBetweenTwoLocations = findViewById(R.id.button_get_direction);
        getDirectionsBetweenTwoLocations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + globalCurrentLocation.latitude + "," + globalCurrentLocation.longitude +
                        "&destination=" + searchedDestination.latitude + "," + searchedDestination.longitude +
                        "&key=" + API_KEY;
                createThreadGetForRouteBetweenTwoLocations(url, a);
            }
        });

//       //Option 1 to start Mqtt listener
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
                getDirectionsBetweenTwoLocations.setVisibility(View.VISIBLE);
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
        MapsDriver.initiateRandomCircleCreation(new ReportedDisaster(), a);
        HttpDriver.createThreadGetForBusStops(a, mMap);


        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {

            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                LatLng southWest = bounds.southwest;
                LatLng northEast = bounds.northeast;
                double screenWidth = Math.abs(northEast.longitude - southWest.longitude);
                LatLng newSouthWest = new LatLng(southWest.latitude - screenWidth, southWest.longitude - screenWidth);
                LatLng newNorthEast = new LatLng(northEast.latitude + screenWidth, northEast.longitude + screenWidth);
                bounds = new LatLngBounds(newSouthWest, newNorthEast);

                // code for plotting bus stops on screen
                HashMap<String, LatLng> busStopsOnScreenMap = new HashMap<>();
                for (Map.Entry<String, LatLng> entry : busStopList.entrySet()) {
                    LatLng busStop = entry.getValue();
                    if (bounds.contains(busStop)) {
                        busStopsOnScreenMap.put(entry.getKey(), entry.getValue());
                    }
                }
                MapsDriver.deleteBusStopsPreviouslyOnScreen();
                if (mMap.getCameraPosition().zoom > 13) {
                    MapsDriver.plotBusStopsOnScreen(busStopsOnScreenMap, a);
                }
                Log.d("camera42", String.valueOf(mMap.getCameraPosition().zoom));
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;

                /* coded for simulating buses based on bus stops on screen */
//                int countBusStopsOnScreen = 0;
//                ArrayList<String> busStopsOnScreenList = new ArrayList<>();
//                for (Map.Entry<String, LatLng> entry : busStopList.entrySet()) {
//                    LatLng busStop = entry.getValue();
//                    if (bounds.contains(busStop)) {
//                        countBusStopsOnScreen++;
//                        busStopsOnScreenList.add(entry.getKey());
//                        Log.d("output42", busStop.latitude + ", " + busStop.longitude);
//                    } else {
//                        Log.d("error42", busStop.latitude + ", " + busStop.longitude);
//                    }
//                }
//                Toast.makeText(getApplicationContext(), "Moving: " + countBusStopsOnScreen + " / " + busStopList.size(), Toast.LENGTH_SHORT).show();
//                if (countBusStopsOnScreen <= 40) {
//                    apiCallForRealTimeDetailsForBusStopOnScreen(busStopsOnScreenList);
//                }
            }
        });
    }

    private void apiCallForRealTimeDetailsForBusStopOnScreen(ArrayList<String> busStopsOnScreen) {
        HttpDriver.createThreadGetForRealTimeBusStopDetails(busStopsOnScreen, a);
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
