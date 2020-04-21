package com.example.androidase_.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.activities.MapsActivity;
import com.example.androidase_.chatbox.ChatActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class VerificationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    public static GoogleMap verification_mMap;
    public static ArrayList<Marker> verificationMarkerListCurrentLocation;
    public static ArrayList<Marker> markerListPossibleDisaster;
    Activity a = this;
    public static boolean verificationSubmissionConfirmation = false;
    boolean isStartCurrentLocationSet_verification = false;
    public static LatLng possibleDisasterLocation;

    public static boolean isDisasterReported = false;

    //for mqtt
    public static MqttAndroidClient client;
    private static String mqttTopicPublish;
    private static String mqttTopicSubscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Intent intent = getIntent();
        isDisasterReported = intent.getBooleanExtra("isDisasterReported", false);
        double lng = intent.getDoubleExtra("lng", 0);
        double lat = intent.getDoubleExtra("lat", 0);
        possibleDisasterLocation = new LatLng(lat, lng);


        /* START - setting up Maps */
        //init
        //API_KEY = "AIzaSyBPOVbWCZG6Weeunh-J2-t3NiyG_1-NXpQ";
        verificationMarkerListCurrentLocation = new ArrayList<>();
        markerListPossibleDisaster = new ArrayList<>();

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
        /* END - setting up Maps */

        Log.d("tag_verification_42", "Started connection");
        connectMQTT();
        Log.d("tag_verification_42", "Finished connection");

        Button submitVerification = findViewById(R.id.verification_buttonVerify);
        submitVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText radius = findViewById(R.id.verification_EditTextRadius);
                boolean flg = !radius.getText().toString().isEmpty();
                if (flg) {
                    Spinner scale = findViewById(R.id.verification_SpinnerScale);
                    radius = findViewById(R.id.verification_EditTextRadius);
                    CheckBox isInfoTrue = findViewById(R.id.verification_CheckBoxIsInfoTrue);
                    EditText landmark = findViewById(R.id.verification_EditTextLandmark);
                    boolean isInfoTrueBool = isInfoTrue.isChecked();
                    String landmarkString = landmark.getText().toString();
                    double radiusDouble = Double.parseDouble(radius.getText().toString());
                    String scaleString = scale.getSelectedItem().toString();
                    VerificationAlertBox verificationAlertBox = new VerificationAlertBox();
                    verificationAlertBox.createAlert(VerificationActivity.this, isInfoTrueBool, landmarkString, radiusDouble, scaleString, possibleDisasterLocation.latitude, possibleDisasterLocation.longitude, a);
                } else {
                    Toast.makeText(getApplicationContext(), "Enter radius", Toast.LENGTH_SHORT).show();
                }
            }
        });

//        Button goToMaps = findViewById(R.id.verification_buttonMaps);
//        goToMaps.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent myIntent = new Intent(VerificationActivity.this, MapsActivity.class);
//                VerificationActivity.this.startActivity(myIntent);
//            }
//        });
    }

//    public static Context getAppContext(){
//        return VerificationActivity.getAppContext();
//    }

    public static void createThreadPostToVerify(final String url, final JSONObject object, final Activity a) throws NullPointerException {
        final int[] response = new int[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = postRestApi(url, object);
                } finally {
                    if (response[0] == 200) {
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(a, "Verification Successful\nPlease wait for further instructions", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent myIntent = new Intent(a, MapsActivity.class);
                        a.startActivity(myIntent);
                    } else {
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(a, "Error while verification", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    private static int postRestApi(String url, JSONObject object) throws NullPointerException {
        final MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("RSCD-Token", "DynattralL1TokenKey12345")
                .addHeader("RSCD-JWT-Token", "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJJc3N1ZXIiOiJEeW5hdHRyYWwgVGVjaCIsIklzc3VlZFRvIjoiWWVra28iLCJFbXBsb3llZUNvZGUiOiJFTVAyNTM1NjciLCJQYXlsb2FkS2V5IjoiMTJkMDhlYjBhYTkyYjk0NTk2NTU2NWIyOWQ1M2FkMWYxNWE1NTE0NGVkMDcxNGFjNTZjMzQ2NzdjY2JjYjQwMCIsIklzc3VlZEF0IjoiMTktMDQtMjAxOSAyLjU0LjIzIFBNIiwiQ2hhbm5lbCI6InNpdGUifQ.Rf7szVWkGiSXHXfGW-xj4TRIw3VQRAySrt9kaEk1kuM")
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.code();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 404;
    }

    @Override
    public void onLocationChanged(Location location) {
        //removed for BlueStack
//        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//        Marker marker = verification_mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
//
//        for (Marker m : verificationMarkerListCurrentLocation)
//            m.remove();
//        verificationMarkerListCurrentLocation.add(marker);
//        marker.setTag(-1);
//        marker.setTitle("USER LOCATION");
//
//        if (!isStartCurrentLocationSet_verification) {
//            isStartCurrentLocationSet_verification = true;
//            if (isDisasterReported) {
//                animateUsingBound(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), possibleDisasterLocation, 100);
//            } else {
//                verification_mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), 15.0f));
//            }
//        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        verification_mMap = googleMap;
        createDummyLocation();
    }

    private void createDummyLocation() throws NullPointerException {
        Random r = new Random();
        double lng = -6.310015 + r.nextDouble() * (-6.230852 + 6.310015);
        double lat = 53.330091 + r.nextDouble() * (53.359967 - 53.330091);

        LatLng currentLocation = new LatLng(lat, lng);
        Marker marker = verification_mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        for (Marker m : verificationMarkerListCurrentLocation)
            m.remove();
        verificationMarkerListCurrentLocation.add(marker);
        marker.setTag(-1);
        marker.setTitle("USER LOCATION");
        if (isDisasterReported) {
            Marker possibleDisasterMarker = verification_mMap.addMarker(new MarkerOptions().position(possibleDisasterLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            for (Marker m : markerListPossibleDisaster)
                m.remove();
            markerListPossibleDisaster.add(possibleDisasterMarker);
            possibleDisasterMarker.setTitle("Reported Disaster");
            animateUsingBound(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), markerListPossibleDisaster.get(markerListPossibleDisaster.size() - 1).getPosition(), 100);
        } else {
            Toast.makeText(getApplicationContext(), "Else", Toast.LENGTH_SHORT).show();
            verification_mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), 15.0f));
        }
    }

    public static void animateUsingBound(LatLng pos1, LatLng pos2, int padding) {
        LatLngBounds bounds;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(pos1);
        builder.include(pos2);
        bounds = builder.build();
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        verification_mMap.animateCamera(cu);
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

    public void connectMQTT() {
        mqttTopicSubscribe = "ase/guroosh/reportingDisaster";
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
                    Log.d("tag_verification_42", "Started subscription");
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

    private void subscribeMQTT() {
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(mqttTopicSubscribe, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
//                    status.setText("subscription is successful.");
                    Log.d("tag_verification_42", "Started setting callback");
                    setMQTTCallback();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
//                    status.setText("subscription is failed.");
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

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
            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                status.setText("message is received.");
                Log.d("tag_verification_42", topic + ": " + message);
                String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
                String[] arr = msg.split(",");
                double lat = Double.parseDouble(arr[0]);
                double lng = Double.parseDouble(arr[1]);
                makeNotification("New Disaster Reported", msg, lat, lng);
                possibleDisasterLocation = new LatLng(lat, lng);
                isDisasterReported = true;
                for (Marker m : markerListPossibleDisaster)
                    m.remove();
                Marker possibleDisasterMarker = verification_mMap.addMarker(new MarkerOptions().position(possibleDisasterLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                markerListPossibleDisaster.add(possibleDisasterMarker);
                possibleDisasterMarker.setTitle("Reported Disaster");
                animateUsingBound(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), markerListPossibleDisaster.get(markerListPossibleDisaster.size() - 1).getPosition(), 100);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
//                status.setText("delivery completed.");
            }
        });
    }

    public static void sendMessage(String payload) {
        mqttTopicPublish = "ase/guroosh/verifiedDisaster";
        if (payload.length() > 0) {
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                Log.d("CircleDrawing42", mqttTopicPublish + ": " + message);
                client.publish(mqttTopicPublish, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeNotification(String title, String msg, double lat, double lng) {
        Intent notificationIntent = new Intent(getApplicationContext(), VerificationActivity.class);
        notificationIntent.putExtra("isDisasterReported", true);
        notificationIntent.putExtra("current_lat", lat);
        notificationIntent.putExtra("current_lng", lng);
        notificationIntent.putExtra("lat", lat);
        notificationIntent.putExtra("lng", lng);
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
}
