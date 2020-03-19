package com.example.androidase_.verification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.activities.MapsActivity;
import com.example.androidase_.object_classes.VerifyingDisasterPOJO;

import org.json.JSONObject;

public class VerificationAlertBox {
    public void createAlert(Context context, final boolean isInfoTrue, final String landmark, final double radius, final String scale, final double latitude, final double longitude, final Activity a) {
        View view = View.inflate(context, R.layout.alert_dialog_verification, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setMessage("Are you sure?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Proceed",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        String referenceId = "RD260599";
                        String verifiedBy = "CurrentUser";
                        String verifiedTime = String.valueOf(System.currentTimeMillis() / 1000);
                        Log.d("after42", "after alert closed");
                        VerifyingDisasterPOJO verifyingDisasterPOJO = new VerifyingDisasterPOJO();
                        verifyingDisasterPOJO.referenceId = referenceId;
                        verifyingDisasterPOJO.verifiedBy = verifiedBy;
                        verifyingDisasterPOJO.verifiedTime = verifiedTime;
                        verifyingDisasterPOJO.isInfoTrue = isInfoTrue;
                        verifyingDisasterPOJO.landmark = landmark;
                        verifyingDisasterPOJO.radius = radius;
                        verifyingDisasterPOJO.scale = scale;
                        verifyingDisasterPOJO.latitude = latitude;
                        verifyingDisasterPOJO.longitude = longitude;
                        VerificationActivity.createThreadPostToVerify("http://" + R.string.ip_address + "/services/ds/disasterReport/verifiedDisaster", verifyingDisasterPOJO.objToJson(), a);
                    }
                });

        builder.setNegativeButton(
                "Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        VerificationActivity.verificationSubmissionConfirmation = false;
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }
}
