package com.example.androidase_.verification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

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
