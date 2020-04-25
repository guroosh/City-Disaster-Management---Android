package com.example.androidase_.reportingDisaster;

import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.activities.MapsActivity;
import com.example.androidase_.drivers.HttpDriver;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.drivers.MathOperationsDriver;
import com.example.androidase_.other_classes.PathJSONParser;
import com.example.androidase_.verification.VerificationActivity;
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

import okhttp3.Response;

import static com.example.androidase_.activities.MapsActivity.API_KEY;
import static com.example.androidase_.activities.MapsActivity.circleRadius;
import static com.example.androidase_.activities.MapsActivity.circleCenter;
import static com.example.androidase_.activities.MapsActivity.fireBrigadeRoutePolylines;
import static com.example.androidase_.activities.MapsActivity.policeStationRoutePolylines;
import static com.example.androidase_.activities.MapsActivity.fireStationsList;
import static com.example.androidase_.activities.MapsActivity.globalCurrentLocation;
import static com.example.androidase_.activities.MapsActivity.mMap;
import static com.example.androidase_.activities.MapsActivity.exitRoutePolylines;
//import static com.example.androidase_.activities.MapsActivity.previousExitRoute;
import static com.example.androidase_.activities.MapsActivity.policeStationsList;
import static com.example.androidase_.activities.MapsActivity.username;
import static com.example.androidase_.drivers.MapsDriver.changeCameraBound;
import static com.example.androidase_.drivers.MapsDriver.drawCircle;
import static com.example.androidase_.drivers.MathOperationsDriver.getRandomExitPointNearCircleCircumference;
import static com.example.androidase_.verification.VerificationAlertBox.verifyingDisasterPOJO;

public class DisasterReport {
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
        MapsActivity.sendMessage("ase/persona/reportingDisaster", disasterLocation.latitude + "," + disasterLocation.longitude + "," + (System.currentTimeMillis() / 1000) + "," + potentialDisasterName);
    }

    public static void startCircleDrawingProcess(LatLng disasterLocation, LatLng userLocation, int radius) {
        Log.d("CircleDrawing42", String.valueOf(radius));
        /* START code to create random circle*/
        Activity a = new MapsActivity();
//        reportedDisaster.setLocation(disasterLocation);
//        Random r = new Random();
//        int radius = 200 + r.nextInt(1000);
//        reportedDisaster.setRadius(radius);
        circleCenter = disasterLocation;
        circleRadius = radius;
        drawCircle(disasterLocation, radius, a);
        changeCameraBound(disasterLocation, radius);
        // todo: check if is User is Inside the circle and pass that along with isDisasterOnUserLocation
        // todo: it will also help while creating route and rerouting so that instead of rerouting it will exit, if the user is inside
        boolean isDisasterOnUserLocation = disasterLocation.latitude == userLocation.latitude && disasterLocation.longitude == userLocation.longitude;
        showExitRoute(disasterLocation, radius, isDisasterOnUserLocation, a);
//        showFireBrigadeRoute(disasterLocation, a);
//        showPoliceBrigadeRoute(disasterLocation, a);
        /* END */
    }

    private static void showPoliceBrigadeRoute(LatLng disasterLocation, Activity a) {
        double minDistance = Double.MAX_VALUE;
        LatLng minPoliceStation = null;
        for (LatLng latLng : policeStationsList) {
            double distance = MathOperationsDriver.measureDistanceInMeters(disasterLocation.latitude, disasterLocation.longitude, latLng.latitude, latLng.longitude);
            if (distance < minDistance) {
                minDistance = distance;
                minPoliceStation = latLng;
            }
        }
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + minPoliceStation.latitude + "," + minPoliceStation.longitude +
                "&destination=" + disasterLocation.latitude + "," + disasterLocation.longitude +
                "&key=" + API_KEY;
        createThreadGetForPoliceStationRouteRoute(url, a);
    }

    private static void showFireBrigadeRoute(LatLng disasterLocation, Activity a) {
        double minDistance = Double.MAX_VALUE;
        LatLng minFireBrigade = null;
        for (LatLng latLng : fireStationsList) {
            double distance = MathOperationsDriver.measureDistanceInMeters(disasterLocation.latitude, disasterLocation.longitude, latLng.latitude, latLng.longitude);
            if (distance < minDistance) {
                minDistance = distance;
                minFireBrigade = latLng;
            }
        }
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + minFireBrigade.latitude + "," + minFireBrigade.longitude +
                "&destination=" + disasterLocation.latitude + "," + disasterLocation.longitude +
                "&key=" + API_KEY;
        createThreadGetForFireBrigadeRouteRoute(url, a);
    }

    public static ArrayList<LatLng> getPoliceBrigadeRoute(LatLng disasterLocation, Activity a) {
        double minDistance = Double.MAX_VALUE;
        LatLng minPoliceStation = null;
        for (LatLng latLng : policeStationsList) {
            double distance = MathOperationsDriver.measureDistanceInMeters(disasterLocation.latitude, disasterLocation.longitude, latLng.latitude, latLng.longitude);
            if (distance < minDistance) {
                minDistance = distance;
                minPoliceStation = latLng;
            }
        }
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + minPoliceStation.latitude + "," + minPoliceStation.longitude +
                "&destination=" + disasterLocation.latitude + "," + disasterLocation.longitude +
                "&key=" + API_KEY;
        return createThreadGetForGettingPoliceStationRouteRoute(url, a);
    }

    private static ArrayList<LatLng> createThreadGetForGettingPoliceStationRouteRoute(final String url, final Activity a) {
        final String[] result = new String[1];
        final ArrayList<LatLng>[] retList = new ArrayList[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    retList[0] = getLatLngRoute(result[0]);
                }
            }
        });
        thread.start();
        return retList[0];
    }

    public static ArrayList<LatLng> getFireBrigadeRoute(LatLng disasterLocation, Activity a) {
        double minDistance = Double.MAX_VALUE;
        LatLng minFireBrigade = null;
        for (LatLng latLng : fireStationsList) {
            double distance = MathOperationsDriver.measureDistanceInMeters(disasterLocation.latitude, disasterLocation.longitude, latLng.latitude, latLng.longitude);
            if (distance < minDistance) {
                minDistance = distance;
                minFireBrigade = latLng;
            }
        }
        String url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + minFireBrigade.latitude + "," + minFireBrigade.longitude +
                "&destination=" + disasterLocation.latitude + "," + disasterLocation.longitude +
                "&key=" + API_KEY;
        return createThreadGetForGettingFireBrigadeRouteRoute(url, a, disasterLocation);
    }

    private static ArrayList<LatLng> createThreadGetForGettingFireBrigadeRouteRoute(final String url, final Activity a, final LatLng disasterLocation) {
        final String[] result = new String[1];
        final ArrayList<LatLng>[] retList = new ArrayList[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    retList[0] = getLatLngRoute(result[0]);
                    verifyingDisasterPOJO.fireRoute = retList[0];

                    //testing
                    JSONObject testingObject = verifyingDisasterPOJO.objToJson();
                    try {
                        JSONArray jsonArray = testingObject.getJSONArray("FireRoute");
                        for (int jl = 0; jl < jsonArray.length(); jl++) {
                            Log.d("RoutePrinting42", jsonArray.getString(jl));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //end
//                    VerificationActivity.createThreadPostToVerify("http://" + a.getResources().getString(R.string.ip_address) + "/services/ds/disasterReport/verifiedDisaster", verifyingDisasterPOJO.objToJson(), a);
                }
            }
        });
        thread.start();
        return retList[0];
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
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(a, "Connectivity error: " + url, Toast.LENGTH_SHORT).show();
                            }
                        });
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

    public static void showExitRoute(LatLng disasterLocation, double radius, boolean isDisasterOnUserLocation, Activity a) {
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
                createThreadGetForExitRoute(url, a);
            }
        }
    }

    public static ArrayList<LatLng> getExitRoute(LatLng disasterLocation, double radius, boolean isDisasterOnUserLocation, Activity a) {
        ArrayList<LatLng> retList = new ArrayList<>();
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
                retList = createThreadGetForGettingExitRoute(url, a);
            }
        }
        return retList;
    }

    private static ArrayList<LatLng> createThreadGetForGettingExitRoute(final String url, final Activity a) {
        final String[] result = new String[1];
        final ArrayList<LatLng>[] retList = new ArrayList[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    retList[0] = getLatLngRoute(result[0]);
                }
            }
        });
        thread.start();
        return retList[0];
    }

    private static void plotFireBrigadeRoute(Activity a, String result) {
        deleteOldFireBrigadeRoute();
        renderFireBrigadeRoute(result);
    }

    private static void plotPoliceStationRoute(Activity a, String result) {
        deleteOldPoliceStationRoute();
        renderPoliceStationRoute(result);
    }


    private static void plotExitRoute(Activity a, final String result) {
        deleteOldExitRoute();
//        previousExitRoute = result;
        renderExitRoute(result);
    }

    private static void renderPoliceStationRoute(String jsonData) {
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
            polyLineOptions.width(15);
            polyLineOptions.color(Color.BLACK);
        }
        policeStationRoutePolylines.add(mMap.addPolyline(polyLineOptions));
    }

    private static void renderFireBrigadeRoute(String jsonData) throws NullPointerException {
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
            polyLineOptions.width(15);
            polyLineOptions.color(Color.YELLOW);
        }
        fireBrigadeRoutePolylines.add(mMap.addPolyline(polyLineOptions));
    }

    private static void renderExitRoute(String jsonData) throws NullPointerException {
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
            polyLineOptions.width(15);
            polyLineOptions.color(Color.BLUE);
        }
        exitRoutePolylines.add(mMap.addPolyline(polyLineOptions));
    }

    private static void createThreadGetForFireBrigadeRouteRoute(final String url, final Activity a) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    a.runOnUiThread(new Runnable() {
                        public void run() {
                            plotFireBrigadeRoute(a, result[0]);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private static void createThreadGetForPoliceStationRouteRoute(final String url, final Activity a) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    a.runOnUiThread(new Runnable() {
                        public void run() {
                            plotPoliceStationRoute(a, result[0]);
                        }
                    });
                }
            }
        });
        thread.start();
    }


    private static void createThreadGetForExitRoute(final String url, final Activity a) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result[0] = HttpDriver.getRestApi(url);
                } finally {
                    a.runOnUiThread(new Runnable() {
                        public void run() {
                            plotExitRoute(a, result[0]);
                        }
                    });
                }
            }
        });
        thread.start();
    }

    private static void deleteOldFireBrigadeRoute() throws NullPointerException {
        for (Polyline p : fireBrigadeRoutePolylines) {
            p.remove();
        }
        fireBrigadeRoutePolylines.clear();
    }

    private static void deleteOldPoliceStationRoute() {
        for (Polyline p : policeStationRoutePolylines) {
            p.remove();
        }
        policeStationRoutePolylines.clear();
    }

    private static void deleteOldExitRoute() throws NullPointerException {
//        if (!previousExitRoute.equals("")) {
        for (Polyline p : exitRoutePolylines) {
            p.remove();
        }
        exitRoutePolylines.clear();
//        }
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
//                Log.d("position42", lat + ", " + lng);
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
        }
        return points;
    }

}
