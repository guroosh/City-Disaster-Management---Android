package com.example.androidase_.Verification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.example.androidase_.R;
import com.example.androidase_.ReportingDisaster.DisasterReport;

public class VerificationAlertBox {
    public static void createAlert(Context context)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?");
        builder.setCancelable(true);

        builder.setPositiveButton(
                "Proceed",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        VerificationActivity.verificationSubmissionConfirmation = true;
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
