package com.example.androidase_.reportingDisaster;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.example.androidase_.activities.MapsActivity;
import com.example.androidase_.drivers.HttpDriver;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.drivers.MathOperationsDriver;
import com.example.androidase_.other_classes.PathJSONParser;
import com.example.androidase_.verification.VerificationActivity;
import com.example.androidase_.verification.VerificationAlertBox;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import okhttp3.Response;

import static com.example.androidase_.activities.MapsActivity.API_KEY;
import static com.example.androidase_.activities.MapsActivity.circleRadius;
import static com.example.androidase_.activities.MapsActivity.circleCenter;
import static com.example.androidase_.activities.MapsActivity.entryExitRoutesPolylines;
import static com.example.androidase_.activities.MapsActivity.globalCurrentLocation;
import static com.example.androidase_.activities.MapsActivity.mMap;
import static com.example.androidase_.activities.MapsActivity.exitRoutePolylines;
//import static com.example.androidase_.activities.MapsActivity.previousExitRoute;
import static com.example.androidase_.activities.MapsActivity.username;
import static com.example.androidase_.drivers.MapsDriver.changeCameraBound;
import static com.example.androidase_.drivers.MapsDriver.drawCircle;
import static com.example.androidase_.drivers.MathOperationsDriver.getExitPointNearCircleCircumferenceAtAngle;
import static com.example.androidase_.drivers.MathOperationsDriver.getRandomExitPointNearCircleCircumference;
import static com.example.androidase_.verification.VerificationAlertBox.verifyingDisasterPOJO;


public class DisasterReport {
    public static ArrayList<String> results = new ArrayList<>();

    public static void initialiseDisasterReport(LatLng disasterLocation, ReportedDisaster reportedDisaster, boolean isDisasterOnUserLocation, Activity a, String potentialDisasterName) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Latitude", disasterLocation.latitude);
            jsonObject.put("Longitude", disasterLocation.longitude);
            jsonObject.put("ReportedTime", String.valueOf(System.currentTimeMillis() / 1000));
            jsonObject.put("ReportedBy", username);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //For backend
//        createThreadPostDisaster("http://" + a.getResources().getString(R.string.ip_address) + "/services/ds/DisasterReport/reportDisaster", jsonObject, a);
        //For demo
        new VerificationAlertBox().sendRequestToFirebase("MY_TOPIC", "New Disaster Reported", "Please verify the reported disaster.", 0, 0, 0);
        MapsActivity.sendMessage("ase/persona/reportingDisaster", disasterLocation.latitude + "," + disasterLocation.longitude + "," + (System.currentTimeMillis() / 1000) + "," + potentialDisasterName);
    }

    public static void startCircleDrawingProcess(LatLng disasterLocation, LatLng userLocation, int radius, String listOfLists) {
        Log.d("CircleDrawingLog42", String.valueOf(radius));
        Log.d("CircleDrawingLog42", listOfLists);
        Activity a = new MapsActivity();
        circleCenter = disasterLocation;
        circleRadius = radius;
        drawCircle(disasterLocation, radius, a);
        changeCameraBound(disasterLocation, radius);
        // todo: check if is User is Inside the circle and pass that along with isDisasterOnUserLocation
        // todo: it will also help while creating route and rerouting so that instead of rerouting it will exit, if the user is inside
        boolean isDisasterOnUserLocation = disasterLocation.latitude == userLocation.latitude && disasterLocation.longitude == userLocation.longitude;
        showExitRoute(disasterLocation, radius, isDisasterOnUserLocation, a, listOfLists);
    }

    public static void getExitEntryRoutesAndPost(LatLng disasterLocation, Activity a, double radius) {
        Random r = new Random();
        int degree = r.nextInt(360);
        int[] fourExits = new int[4];
        for (int i = 0; i < 4; i++) {
            fourExits[i] = degree + (i * 90);
        }
        for (int i = 0; i < 4; i++) {
            if (fourExits[i] >= 360) {
                fourExits[i] -= 360;
            }
            Log.d("RoutePrinting42", String.valueOf(fourExits[i]));
        }
        ArrayList<String> urls = new ArrayList<>();
        String url;
        for (int i = 0; i < 4; i++) {
            LatLng randomPoint = getExitPointNearCircleCircumferenceAtAngle(disasterLocation, radius, fourExits[i]);
            Log.d("RoutePrinting42", randomPoint.latitude + ", " + randomPoint.longitude);
            url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=" + disasterLocation.latitude + "," + disasterLocation.longitude +
                    "&destination=" + randomPoint.latitude + "," + randomPoint.longitude +
                    "&key=" + API_KEY;
            urls.add(url);
        }
        createThreadGetForGettingEntryExitRoutes(urls, a, disasterLocation, radius);
    }

    private static void createThreadGetForGettingEntryExitRoutes(final ArrayList<String> urls, final Activity a, final LatLng disasterLocation, final double radius) {
        final String[] result = new String[4];
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int iq = 0; iq < urls.size(); iq++) {
                        String url = urls.get(iq);
                        result[iq] = HttpDriver.getRestApi(url);
                    }
                } finally {
                    ArrayList<ArrayList<LatLng>> list = new ArrayList<>();
                    for (int iq = 0; iq < 4; iq++) {
                        list.add(getLatLngRoute(result[iq]));
                        results.add(result[iq]);
                    }
                    verifyingDisasterPOJO.exitEntryRoutes = list;

                    //testing
                    JSONObject testingObject = verifyingDisasterPOJO.objToJson();
                    try {
                        JSONArray jsonArray = testingObject.getJSONArray("ExitEntryRoutes");
                        Log.d("RoutePrinting42", String.valueOf(jsonArray));
//                        for (int jl = 0; jl < jsonArray.length(); jl++) {
//                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //end
                    // For backend
//                    VerificationActivity.createThreadPostToVerify("http://" + a.getResources().getString(R.string.ip_address) + "/services/ds/disasterReport/verifiedDisaster", verifyingDisasterPOJO.objToJson(), a);
                    //For demo
                    Log.d("CircleDrawing42", "sending message");

                    JSONArray arrayOfArray = listToJSONArray(list);


                    new VerificationAlertBox().sendRequestToFirebase("MY_TOPIC", "Alert", "There is a disaster near you. Please be careful.", 0, 0, 0);
                    VerificationActivity.sendMessage("ase/persona/verifiedDisaster", disasterLocation.latitude + "," + disasterLocation.longitude + "," + radius + "," + String.valueOf(arrayOfArray).replaceAll(",", "!"));
                }
            }
        });
        thread.start();
    }

    private static JSONArray listToJSONArray(ArrayList<ArrayList<LatLng>> list) {
        JSONArray exitEntryList = new JSONArray();
        try {
            for (ArrayList<LatLng> l : list) {
                JSONArray innerList = new JSONArray();
                for (LatLng latLng : l) {
                    JSONObject position = new JSONObject();
                    position.put("lat", latLng.latitude);
                    position.put("lng", latLng.longitude);
                    innerList.put(position);
                }
                exitEntryList.put(innerList);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return exitEntryList;
    }

    public static void showExitRoute(LatLng disasterLocation, double radius, boolean isDisasterOnUserLocation, Activity a, String listOfLists) {
        double lng1 = globalCurrentLocation.longitude;
        double lat1 = globalCurrentLocation.latitude;
        double lng2 = disasterLocation.longitude;
        double lat2 = disasterLocation.latitude;
        double userDistanceFromDisaster = MathOperationsDriver.measureDistanceInMeters(lat1, lng1, lat2, lng2);
        if (userDistanceFromDisaster <= radius) {
            ArrayList<LatLng> randomExitLocations = getRandomExitPointNearCircleCircumference(disasterLocation, radius, isDisasterOnUserLocation);
            for (LatLng randomExitLocation : randomExitLocations) {
                String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                        "origin=" + globalCurrentLocation.latitude + "," + globalCurrentLocation.longitude +
                        "&destination=" + randomExitLocation.latitude + "," + randomExitLocation.longitude +
                        "&key=" + API_KEY;
                createThreadGetForExitRoute(url, a, listOfLists);
            }
        }
    }

    private static void plotExitRoute(Activity a, final String result, int color) {
        deleteOldExitRoute();
        renderExitRoute(result, color);
    }

    private static void renderExitRoute(String jsonData, int color) throws NullPointerException {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<LatLng> points;
        PolylineOptions polyLineOptions = null;

        // traversing through routes
        assert routes != null;
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<>();
            polyLineOptions = new PolylineOptions();
            List<HashMap<String, String>> path = routes.get(i);

            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
//                Log.d("position42", lat + ", " + lng);
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            polyLineOptions.addAll(points);
            polyLineOptions.width(20);
            polyLineOptions.color(color);
        }
        exitRoutePolylines.add(mMap.addPolyline(polyLineOptions));
    }

    private static void createThreadGetForExitRoute(final String url, final Activity a, final String listOfLists) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    a.runOnUiThread(new Runnable() {
                        public void run() {
                            plotExitRoute(a, result[0], Color.BLUE);
                            plotEntryExitRoutes(listOfLists);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private static void plotEntryExitRoutes(String listOfLists) {
        deleteOldEntryExitRoutes();
        renderEntryExitRoutes(listOfLists);
    }

    private static void renderEntryExitRoutes(String jsonData) throws NullPointerException {
        jsonData = jsonData.replaceAll("!", ",");
        PolylineOptions polyLineOptions;
        ArrayList<LatLng> points;
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONArray inner = jsonArray.getJSONArray(i);
                polyLineOptions = new PolylineOptions();
                points = new ArrayList<>();
                for (int j = 0; j < inner.length(); j++) {
                    JSONObject obj = inner.getJSONObject(j);
                    double lat = obj.getDouble("lat");
                    double lng = obj.getDouble("lng");
                    points.add(new LatLng(lat, lng));
                }
                polyLineOptions.addAll(points);
                polyLineOptions.width(10);
                if (i % 2 == 0) {
                    polyLineOptions.color(Color.GREEN);
                } else {
                    polyLineOptions.color(Color.RED);
                }
                entryExitRoutesPolylines.add(mMap.addPolyline(polyLineOptions));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private static void deleteOldExitRoute() throws NullPointerException {
        for (Polyline p : exitRoutePolylines) {
            p.remove();
        }
        exitRoutePolylines.clear();
    }

    private static void deleteOldEntryExitRoutes() throws NullPointerException {
        for (Polyline p : entryExitRoutesPolylines) {
            p.remove();
        }
        entryExitRoutesPolylines.clear();
    }

    private static ArrayList<LatLng> getLatLngRoute(String jsonData) {
        JSONObject jObject;
        List<List<HashMap<String, String>>> routes = null;

        try {
            jObject = new JSONObject(jsonData);
            PathJSONParser parser = new PathJSONParser();
            routes = parser.parse(jObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<LatLng> points = new ArrayList<>();

        // traversing through routes
        assert routes != null;
        for (int i = 0; i < routes.size(); i++) {
            List<HashMap<String, String>> path = routes.get(i);
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);

                double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
        }
        return points;
    }

    public static void createThreadPostDisaster(final String url, final JSONObject object, final Activity a) throws NullPointerException {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = HttpDriver.postRestApi(url, object, a);
                } finally {
                    if (response == null) {

                    } else {
                        final int finalCode = response.code();
                        a.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (finalCode == 200) {
                                    Toast.makeText(a, "DisasterReport Reported Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(a, String.valueOf(finalCode), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }


}
