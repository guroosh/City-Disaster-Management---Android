package com.example.androidase_.verification;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;

import com.example.androidase_.R;
import com.example.androidase_.object_classes.VerifyingDisasterPOJO;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.example.androidase_.reportingDisaster.DisasterReport.getExitEntryRoutesAndPost;

public class VerificationAlertBox {
    public static VerifyingDisasterPOJO verifyingDisasterPOJO = new VerifyingDisasterPOJO();

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

                        verifyingDisasterPOJO.referenceId = referenceId;
                        verifyingDisasterPOJO.verifiedBy = verifiedBy;
                        verifyingDisasterPOJO.verifiedTime = verifiedTime;
                        verifyingDisasterPOJO.isInfoTrue = isInfoTrue;
                        verifyingDisasterPOJO.landmark = landmark;
                        verifyingDisasterPOJO.radius = radius;
                        verifyingDisasterPOJO.scale = scale;
                        verifyingDisasterPOJO.latitude = latitude;
                        verifyingDisasterPOJO.longitude = longitude;
                        if (isInfoTrue) {
                            //For backend
                            getExitEntryRoutesAndPost(new LatLng(latitude, longitude), a, radius);
                            //For demo
                            Log.d("CircleDrawing42", "sending message");
                            sendRequestToFirebase("MY_TOPIC", "Alert", "There is a disaster near you. Please be careful.", latitude, longitude, radius);
                            VerificationActivity.sendMessage("ase/persona/verifiedDisaster", latitude + "," + longitude + "," + radius);
                        }
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

    private void sendRequestToFirebase(final String topic, final String title, final String message, final double latitude, final double longitude, final double radius) {
        final int[] code = new int[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    code[0] = pushToFirebase(topic, title, message, latitude, longitude, radius);
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    Log.d("Firebase42", "done?: " + code[0]);
                }
            }
        });
        thread.start();
    }

    private int pushToFirebase(String topic, String title, String message, double latitude, double longitude, double radius) throws JSONException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        JSONObject object = new JSONObject();
        JSONObject innerObject = new JSONObject();
        innerObject.put("title", title);
        innerObject.put("body", message);
        innerObject.put("content_available", true);
        innerObject.put("priority", "high");
        innerObject.put("disaster_lat", latitude);
        innerObject.put("disaster_lng", longitude);
        innerObject.put("radius", radius);
        object.put("to", "/topics/" + topic);
        object.put("notification", innerObject);
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .addHeader("Authorization", "key=AAAAq9ahUsk:APA91bGV_3DhYo-HRdPJjQ-Bfj6iKV1odIPzGSAPnIb1wL40k3aHMCwB_Q86nkqU_Gkfy7pwvZXXCu941GMqaVVqu6e2VYkJMO_P5FD_ey-12AqjNlqPC5fA7c_LmwkmpmOKr6bY-_Vr")
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert response != null;
        return response.code();
    }
}
