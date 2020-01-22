package com.example.androidase_.drivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.androidase_.object_classes.ReportedDisaster;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import static com.example.androidase_.activities.MapsActivity.username;

public class AlertDriver {
    public void createAlert(final Context context, String message, String text1, String text2, final LatLng disasterLocation, final ReportedDisaster reportedDisaster, final boolean isDisasterOnUserLocation, final Activity a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                text1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        reportedDisaster.setLocation(disasterLocation);
                        Random r = new Random();
                        int radius = 200 + r.nextInt(1000);
                        reportedDisaster.setRadius(radius);
                        MapsDriver.drawCircle(disasterLocation, radius, isDisasterOnUserLocation, a);

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("Latitude", disasterLocation.latitude);
                            jsonObject.put("Longitude", disasterLocation.longitude);
                            jsonObject.put("ReportedTime", String.valueOf(System.currentTimeMillis() / 1000));
                            jsonObject.put("ReportedBy", username);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        HttpDriver.createThreadPostDisaster("http://10.6.36.104:8080/services/ds/DisasterReport/reportDisaster", jsonObject, a);

                    }
                });

        builder.setNegativeButton(
                text2,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
