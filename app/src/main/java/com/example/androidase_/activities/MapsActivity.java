package com.example.androidase_.activities;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.chatbox.ChatActivity;
import com.example.androidase_.chatbox.Message;
import com.example.androidase_.drivers.MapsDriver;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.reportingDisaster.DisasterReportAlert;
import com.example.androidase_.drivers.HttpDriver;
import com.example.androidase_.verification.VerificationActivity;
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
import com.google.android.material.navigation.NavigationView;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.jetbrains.annotations.NotNull;

import java.io.UnsupportedEncodingException;
import java.lang.annotation.Target;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.example.androidase_.navigation.RouteBetweenTwoPoints.createThreadGetForRouteBetweenTwoLocations;
import static com.example.androidase_.drivers.MapsDriver.animateUsingBound;
import static com.example.androidase_.reportingDisaster.DisasterReport.startCircleDrawingProcess;

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
    public static ArrayList<Polyline> fireBrigadeRoutePolylines = new ArrayList<>();
    public static ArrayList<Polyline> policeStationRoutePolylines = new ArrayList<>();
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

    public static ArrayList<LatLng> fireStationsList = new ArrayList<>();
    public static ArrayList<LatLng> policeStationsList = new ArrayList<>();

    //for mqtt
    public static MqttAndroidClient client;

    public static boolean isNavigating = false;

    //for notification
    public static Intent globalIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        globalIntent = getIntent();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
        username = pref.getString("username", "null");

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

        connectMQTT();

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
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                boolean fromVerification = globalIntent.getBooleanExtra("fromVerification", false);
                if (fromVerification) {
                    double disasterLat = globalIntent.getDoubleExtra("disaster_lat", 0);
                    double disasterLng = globalIntent.getDoubleExtra("disaster_lng", 0);
                    double userLat = globalIntent.getDoubleExtra("user_lat", 0);
                    double userLng = globalIntent.getDoubleExtra("user_lng", 0);
                    int radius = (int) globalIntent.getDoubleExtra("radius", 0);
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(userLat, userLng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    for (Marker m : markerListCurrentLocation)
                        m.remove();
                    markerListCurrentLocation.add(marker);
                    marker.setTag(-1);
                    marker.setTitle("USER LOCATION");
                    startCircleDrawingProcess(new LatLng(disasterLat, disasterLng), new LatLng(userLat, userLng), (int) radius);
                } else {
                    // add more if-else for (fromNotification)
                    createDummyLocation();
                }
                updateFireStationsListAndUI();
                updatePoliceStationsListAndUI();
//        MapsDriver.initiateRandomCircleCreation(new ReportedDisaster(), a);
                HttpDriver.createThreadGetForBusStops(a, mMap);

                mMap.setTrafficEnabled(true);

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
//                    MapsDriver.plotBusStopsOnScreen(busStopsOnScreenMap, a);
                        }
                        Log.d("camera42", String.valueOf(mMap.getCameraPosition().zoom));
                    }
                });

            }
        });
    }

    private void updatePoliceStationsListAndUI() {
        policeStationsList.add(new LatLng(53.61437815, -6.191052919));
        policeStationsList.add(new LatLng(53.57959038, -6.10696161));
        policeStationsList.add(new LatLng(53.52343539, -6.167335346));
        policeStationsList.add(new LatLng(53.45110927, -6.151897022));
        policeStationsList.add(new LatLng(53.45606714, -6.221164734));
        policeStationsList.add(new LatLng(53.42977837, -6.245041004));
        policeStationsList.add(new LatLng(53.38735506, -6.068977549));
        policeStationsList.add(new LatLng(53.56705171, -6.383869161));
        policeStationsList.add(new LatLng(53.38978396, -6.380770745));
        policeStationsList.add(new LatLng(53.51138166, -6.396980308));
        policeStationsList.add(new LatLng(53.42095137, -6.476963398));
        policeStationsList.add(new LatLng(53.36746653, -6.498336241));
        policeStationsList.add(new LatLng(53.35608506, -6.450659464));
        policeStationsList.add(new LatLng(53.38967142, -6.306821102));
        policeStationsList.add(new LatLng(53.39457808, -6.263771779));
        policeStationsList.add(new LatLng(53.38969671, -6.250576875));
        policeStationsList.add(new LatLng(53.39033034, -6.201255612));
        policeStationsList.add(new LatLng(53.37895784, -6.178143019));

        for (LatLng latLng : policeStationsList) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            BitmapDrawable bitmapDrawable;
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(a.getApplicationContext(), R.drawable.police_logo);
            assert bitmapDrawable != null;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            Marker marker = mMap.addMarker(markerOptions);
        }
    }

    private void updateFireStationsListAndUI() {
        fireStationsList.add(new LatLng(53.321988, -6.237410));
        fireStationsList.add(new LatLng(53.331522, -6.292684));
        fireStationsList.add(new LatLng(53.358579, -6.274136));
        fireStationsList.add(new LatLng(53.359941, -6.239062));
        fireStationsList.add(new LatLng(53.390037, -6.316544));
        fireStationsList.add(new LatLng(53.390857, -6.167643));
        fireStationsList.add(new LatLng(53.307392, -6.383083));
        fireStationsList.add(new LatLng(53.291810, -6.264472));
        fireStationsList.add(new LatLng(53.384652, -6.395923));
        fireStationsList.add(new LatLng(53.345754, -6.254028));
        fireStationsList.add(new LatLng(53.281724, -6.151476));
        fireStationsList.add(new LatLng(53.465351, -6.220321));

        for (LatLng latLng : fireStationsList) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            BitmapDrawable bitmapDrawable;
            bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(a.getApplicationContext(), R.drawable.fire_station_logo);
            assert bitmapDrawable != null;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
            Marker marker = mMap.addMarker(markerOptions);
        }
    }

    private void apiCallForRealTimeDetailsForBusStopOnScreen(ArrayList<String> busStopsOnScreen) {
        HttpDriver.createThreadGetForRealTimeBusStopDetails(busStopsOnScreen, a);
    }

    private void createDummyLocation() throws NullPointerException {
        Random r = new Random();
        double lng = -6.310015 + r.nextDouble() * (-6.230852 + 6.310015);
        double lat = 53.330091 + r.nextDouble() * (53.359967 - 53.330091);


        boolean fromNotification = globalIntent.getBooleanExtra("fromNotification", false);
        Log.d("FromNotification42", String.valueOf(fromNotification));
//        if (fromNotification) {
//            LatLng disasterLocationFromIntent = new LatLng(globalIntent.getDoubleExtra("lat", 0), globalIntent.getDoubleExtra("lng", 0));
//            int radius = globalIntent.getIntExtra("radius", 0);
//            SharedPreferences pref = getApplicationContext().getSharedPreferences("ReportingDisasterData", MODE_PRIVATE);
//            Log.d("Debug42", String.valueOf(lng));
//            Log.d("Debug42", String.valueOf(lat));
//            lat = Double.parseDouble(pref.getString("user_lat", String.valueOf(lat)));
//            lng = Double.parseDouble(pref.getString("user_lng", String.valueOf(lng)));
//            Log.d("Debug42", String.valueOf(lng));
//            Log.d("Debug42", String.valueOf(lat));
//            startCircleDrawingProcess(disasterLocationFromIntent, new LatLng(lat, lng), radius);
//        }
        LatLng currentLocation = new LatLng(lat, lng);
        globalCurrentLocation = currentLocation;

        SharedPreferences.Editor editor = getSharedPreferences("ReportingDisasterData", MODE_PRIVATE).edit();
        editor.putString("user_lat", String.valueOf(lat));
        editor.putString("user_lng", String.valueOf(lng));
        editor.apply();
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
//        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//        globalCurrentLocation = currentLocation;
//        Marker marker = mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//        for (Marker m : markerListCurrentLocation)
//            m.remove();
//        markerListCurrentLocation.add(marker);
//        marker.setTag(-1);
//        marker.setTitle("USER LOCATION");
//        if (!isStartCurrentLocationSet) {
//            isStartCurrentLocationSet = true;
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15.0f));
//        }
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

    public void logoutFunction(MenuItem item) {
        SharedPreferences.Editor editor = getSharedPreferences("LoginData", MODE_PRIVATE).edit();
        editor.putBoolean("loggedIn", false);
        editor.apply();
        Intent myIntent = new Intent(MapsActivity.this, LoginActivity.class);
        MapsActivity.this.startActivity(myIntent);
    }

    public void openChatBoxFunction(MenuItem item) {
        Intent myIntent = new Intent(MapsActivity.this, ChatActivity.class);
        MapsActivity.this.startActivity(myIntent);
    }

    public void openPhoneApplicationFunction(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:1234567"));
        startActivity(intent);
    }

    private void subscribeMQTT() {
        Log.d("CircleDrawing42", "String.valueOf(radius)");
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(new String[]{"ase/persona/verifiedDisaster", "ase/persona/reportingDisaster"}, new int[]{qos, qos});
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
//                    status.setText("subscription is successful.");
                    Log.d("CircleDrawing42", "Success");
                    setMQTTCallback();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
//                    status.setText("subscription is failed.");
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                    Log.d("CircleDrawing42", "Failure");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setMQTTCallback() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
//                status.setText("connection is lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) {
                String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
                String[] arr = msg.split(",");
                double lat = Double.parseDouble(arr[0]);
                double lng = Double.parseDouble(arr[1]);
                double radius = Double.parseDouble(arr[2]);
                if (topic.equals("ase/persona/verifiedDisaster")) {
                    Log.d("CircleDrawing42", topic);
                    Log.d("CircleDrawing42", msg);
                    LatLng disasterLocation = new LatLng(lat, lng);
                    Log.d("CircleDrawing42", String.valueOf(radius));
                    makeNotification("Disaster alert", "There is a disaster near you, please travel careful", disasterLocation, (int) radius);
                    startCircleDrawingProcess(disasterLocation, globalCurrentLocation, (int) radius);
                    Log.d("Navigation42", String.valueOf(isNavigating));
                    if (isNavigating) {
                        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                                "origin=" + globalCurrentLocation.latitude + "," + globalCurrentLocation.longitude +
                                "&destination=" + searchedDestination.latitude + "," + searchedDestination.longitude +
                                "&key=" + API_KEY;
                        createThreadGetForRouteBetweenTwoLocations(url, a);
                    }
                } else if (topic.equals("ase/persona/reportingDisaster")) {
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
                    boolean isCommonUser = pref.getBoolean("isCommonUser", false);
                    if (!isCommonUser) {
                        Intent myIntent = new Intent(MapsActivity.this, VerificationActivity.class);
                        myIntent.putExtra("lat", lat);
                        myIntent.putExtra("lng", lng);
                        myIntent.putExtra("user_lat", globalCurrentLocation.latitude);
                        myIntent.putExtra("user_lng", globalCurrentLocation.longitude);
                        MapsActivity.this.startActivity(myIntent);
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
//                status.setText("delivery completed.");
            }
        });
    }

    public static void sendMessage(String topic, String payload) {
        if (payload.length() > 0) {
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(topic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeNotification(String title, String msg, LatLng disasterLocation, int radius) {
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);
        notificationIntent.putExtra("NotificationMessage", "I am from Notification");
        notificationIntent.putExtra("fromNotification", true);
        notificationIntent.putExtra("lat", disasterLocation.latitude);
        notificationIntent.putExtra("lng", disasterLocation.longitude);
        notificationIntent.putExtra("radius", radius);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                    "YOUR_CHANNEL_NAME",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
            mNotificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(msg)// message for notification
                .setAutoCancel(true); // clear notification after click
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }

    public void connectMQTT() {
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
//                    status.setText("connection is successful.");
                    subscribeMQTT();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
//                    status.setText("connection is failed.");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
