package com.example.androidase_.verification;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
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
import com.example.androidase_.object_classes.VerifyingDisasterPOJO;
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

import org.json.JSONObject;

import java.io.IOException;
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
    Activity a = this;
    public static boolean verificationSubmissionConfirmation = false;
    boolean isStartCurrentLocationSet_verification = false;
    public static LatLng possibleDisasterLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        // this should come from the server (using MQTT?)
        Random r = new Random();
        double lng = -6.310015 + r.nextDouble() * (-6.230852 + 6.310015);
        double lat = 53.330091 + r.nextDouble() * (53.359967 - 53.330091);
        possibleDisasterLocation = new LatLng(lat, lng);

        /* START - setting up Maps */
        //init
//        API_KEY = "AIzaSyBPOVbWCZG6Weeunh-J2-t3NiyG_1-NXpQ";
        verificationMarkerListCurrentLocation = new ArrayList<>();

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
                    VerifyingDisasterPOJO verifyingDisasterPOJO = new VerifyingDisasterPOJO();
                    verifyingDisasterPOJO.referenceId = "RD260599";
                    verifyingDisasterPOJO.verifiedBy = "CurrentUser";
                    verifyingDisasterPOJO.verifiedTime = String.valueOf(System.currentTimeMillis() / 1000);
                    verifyingDisasterPOJO.isInfoTrue = isInfoTrue.isChecked();
                    verifyingDisasterPOJO.landmark = landmark.getText().toString();
                    verifyingDisasterPOJO.radius = Double.parseDouble(radius.getText().toString());
                    verifyingDisasterPOJO.scale = scale.getSelectedItem().toString();
                    verifyingDisasterPOJO.latitude = 12.43;
                    verifyingDisasterPOJO.longitude = 12.43;
                    Log.d("OUTPUT42", verifyingDisasterPOJO.objToJson().toString());
                    VerificationAlertBox verificationAlertBox = new VerificationAlertBox();
                    verificationAlertBox.createAlert(VerificationActivity.this);
                    if (verificationSubmissionConfirmation) {
                        createThreadPostToVerify("http://" + R.string.ip_address + "/services/ds/disasterReport/verifiedDisaster", verifyingDisasterPOJO.objToJson());
                    }
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

    public void createThreadPostToVerify(final String url, final JSONObject object) throws NullPointerException {
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
                                Toast.makeText(getApplicationContext(), "Verification Successful\nPlease wait for further instructions", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent myIntent = new Intent(VerificationActivity.this, MapsActivity.class);
                        startActivity(myIntent);
                    } else {
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error while verification", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    private int postRestApi(String url, JSONObject object) throws NullPointerException {
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
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Marker marker = verification_mMap.addMarker(new MarkerOptions().position(currentLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        for (Marker m : verificationMarkerListCurrentLocation)
            m.remove();
        verificationMarkerListCurrentLocation.add(marker);
        marker.setTag(-1);
        marker.setTitle("USER LOCATION");
        if (!isStartCurrentLocationSet_verification) {
            isStartCurrentLocationSet_verification = true;
            animateUsingBound(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), possibleDisasterLocation, 100);
        }
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
        Marker possibleDisasterMarker = verification_mMap.addMarker(new MarkerOptions().position(possibleDisasterLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        possibleDisasterMarker.setTitle("Reported Disaster");
        animateUsingBound(verificationMarkerListCurrentLocation.get(verificationMarkerListCurrentLocation.size() - 1).getPosition(), possibleDisasterLocation, 100);
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
}
