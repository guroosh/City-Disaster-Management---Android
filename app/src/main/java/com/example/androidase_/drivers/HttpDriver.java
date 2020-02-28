package com.example.androidase_.drivers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.androidase_.R;
import com.example.androidase_.ReportingDisaster.DisasterReport;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.androidase_.activities.MapsActivity.*;

public class HttpDriver {

    public static void createThreadGetForBusStops(final Activity a, final GoogleMap mMap) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = getRestApi("https://wzk9mu5t95.execute-api.us-east-1.amazonaws.com/Prod");
                } finally {
                    plotBusStopMarkers(result[0], a, mMap);
                }
            }
        });
        thread.start();
    }


    public static void createThreadGetForLocation(final String url, final String potentialDisasterName, final Activity a, final GoogleMap mMap) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = getRestApi(url);
                } finally {
                    placeSearchedLocationMarker(result[0], a, mMap, potentialDisasterName);
                }
            }
        });
        thread.start();
    }



    public static void createThreadGetForRealTimeBusStopDetails(final ArrayList<String> busStopsOnScreen, final Activity a) {
        final String[] result = new String[busStopsOnScreen.size()];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int i = 0;
                    for (String id : busStopsOnScreen) {
                        result[i] = getRestApi("https://data.smartdublin.ie/cgi-bin/rtpi/realtimebusinformation?stopid=" + id);
                        i++;
                    }
                } finally {
                    simulateBuses(a, result);
                }
            }
        });
        thread.start();
    }

    private static void simulateBuses(Activity a, String[] s) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");   // 13/02/2020 21:07:00
        HashMap<String, Long> upcomingBuses = new HashMap<>();
        HashMap<String, ArrayList<String>> groupByBusRoute = new HashMap<>();
        for (String j : s) {
            // for loop iterating over bus stops on screen
            try {
                JSONObject jsonObject = new JSONObject(j);
                String busStop = jsonObject.getString("stopid");
                int numberOfResults = jsonObject.getInt("numberofresults");
                JSONArray results = jsonObject.getJSONArray("results");
                for (int i = 0; i < numberOfResults; i++) {
                    // for loop iterating over upcoming bus timings for each bus stop.
                    JSONObject innerJSONObject = (JSONObject) results.get(i);
                    String arrivalTime = innerJSONObject.getString("scheduledarrivaldatetime");
                    Date arrivalDateTime = formatter.parse(arrivalTime);
                    String busRoute = innerJSONObject.getString("route");
                    if (!upcomingBuses.containsKey(busStop + "-" + busRoute)) {
                        // todo: also check if the direction i the same (destination == destination for comapring the bus stops)
                        upcomingBuses.put(busStop + "-" + busRoute, arrivalDateTime.getTime());
                        Log.d("simulation42", busStop + ", " + arrivalDateTime.getTime() + ", " + busRoute);
                    }
                    if (!groupByBusRoute.containsKey(busRoute)) {
                        groupByBusRoute.put(busRoute, new ArrayList<String>());
                    }
                    groupByBusRoute.get(busRoute).add(busStop);
                }
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, ArrayList<String>> entry : groupByBusRoute.entrySet()) {
            ArrayList<String> busStops = entry.getValue();
            String busRoute = entry.getKey();
            for (int i = 0; i < busStops.size() - 1; i++) {
                String stop1 = busStops.get(i);
                String stop2 = busStops.get(i + 1);
                String key1 = stop1 + "-" + busRoute;
                String key2 = stop2 + "-" + busRoute;
                long time1 = upcomingBuses.get(key1);
                long time2 = upcomingBuses.get(key2);
                if (time2 > currentTime && currentTime > time1) {
                    LatLng pos1 = busStopList.get(stop1);
                    LatLng pos2 = busStopList.get(stop2);
                    double midLat = (pos1.latitude + pos2.latitude) / 2;
                    double midLng = (pos1.longitude + pos2.longitude) / 2;
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(a, R.drawable.bus_cartoon);
                    assert bitmapDrawable != null;
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 100, 100, false);
                    final MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(midLat, midLng));
                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mMap.addMarker(markerOptions);
                        }
                    });
                }
            }
        }
    }

    private static void plotBusStopMarkers(String result, final Activity a, final GoogleMap mMap) {
        try {
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String busStopId = (String) jsonObject.get("id");
                String lat = (String) jsonObject.get("lat");
                String lng = (String) jsonObject.get("lng");
                busStopList.put(busStopId, new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)));
            }
            a.runOnUiThread(new Runnable() {
                public void run() {
                    for (Map.Entry<String, LatLng> entry : busStopList.entrySet()) {
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(entry.getValue());
                        BitmapDrawable bitmapDrawable;
                        bitmapDrawable = (BitmapDrawable) ContextCompat.getDrawable(a.getApplicationContext(), R.drawable.yellow);
                        assert bitmapDrawable != null;
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, 20, 20, false);
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                        /* START: code to add bus stops, do not remove */
                        /* Marker marker = mMap.addMarker(markerOptions); */
                        /* END */
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void placeSearchedLocationMarker(String result, final Activity a, final GoogleMap mMap, final String potentialDisasterName) {
        try {
            JSONObject jsonObject = new JSONObject(result);
            String lat = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getString("lat");
            String lng = jsonObject.getJSONObject("result").getJSONObject("geometry").getJSONObject("location").getString("lng");
            final LatLng finalLocation = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            searchedDestination = finalLocation;
            a.runOnUiThread(new Runnable() {
                public void run() {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(finalLocation));
                    globalPotentialDisasterName = potentialDisasterName;
                    for (Marker m : markerListDisaster)
                        m.remove();
                    markerListDisaster.add(marker);
                    marker.setTag(-2);
                    marker.setTitle("DESTINATION");
                    LatLngBounds bounds;
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(markerListCurrentLocation.get(markerListCurrentLocation.size() - 1).getPosition());
                    builder.include(markerListDisaster.get(markerListDisaster.size() - 1).getPosition());
                    bounds = builder.build();
                    int padding = 100; // offset from edges of the map in pixels
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                    mMap.animateCamera(cu);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public static String getRestApi(String url) throws NullPointerException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
            return "IO-Error";
        }
    }

    public static Response postRestApi(String url, JSONObject object, Activity a) throws NullPointerException {
        final MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("RSCD-Token", "DynattralL1TokenKey12345")
                .addHeader("RSCD-JWT-Token", "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJJc3N1ZXIiOiJEeW5hdHRyYWwgVGVjaCIsIklzc3VlZFRvIjoiWWVra28iLCJFbXBsb3llZUNvZGUiOiJFTVAyNTM1NjciLCJQYXlsb2FkS2V5IjoiMTJkMDhlYjBhYTkyYjk0NTk2NTU2NWIyOWQ1M2FkMWYxNWE1NTE0NGVkMDcxNGFjNTZjMzQ2NzdjY2JjYjQwMCIsIklzc3VlZEF0IjoiMTktMDQtMjAxOSAyLjU0LjIzIFBNIiwiQ2hhbm5lbCI6InNpdGUifQ.Rf7szVWkGiSXHXfGW-xj4TRIw3VQRAySrt9kaEk1kuM")
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

}