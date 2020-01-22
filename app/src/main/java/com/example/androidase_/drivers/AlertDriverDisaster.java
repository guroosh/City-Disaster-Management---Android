package com.example.androidase_.drivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.androidase_.object_classes.ReportedDisaster;
import com.google.android.gms.maps.model.LatLng;

import java.util.Random;

public class AlertDriverDisaster {
    public void createAlert(final Context context, String message, String text1, String text2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                text1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

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
