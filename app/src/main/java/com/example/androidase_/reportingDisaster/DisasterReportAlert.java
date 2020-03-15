package com.example.androidase_.reportingDisaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.androidase_.object_classes.ReportedDisaster;
import com.google.android.gms.maps.model.LatLng;

public class DisasterReportAlert {
    public void createAlert(final Context context, String message, String text1, String text2, final LatLng disasterLocation, final ReportedDisaster reportedDisaster, final boolean isDisasterOnUserLocation, final Activity a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message);
        builder.setCancelable(true);

        builder.setPositiveButton(
                text1,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        DisasterReport.initialiseDisasterReport(disasterLocation, reportedDisaster, isDisasterOnUserLocation, a);
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
