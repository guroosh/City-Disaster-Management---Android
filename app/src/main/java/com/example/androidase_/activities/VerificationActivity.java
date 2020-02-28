package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.object_classes.VerifyingDisasterPOJO;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VerificationActivity extends AppCompatActivity {

    Activity a = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Button submitVerification = findViewById(R.id.verification_buttonVerify);
        submitVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText radius = findViewById(R.id.verification_EditTextRadius);
                boolean flg = !radius.getText().toString().isEmpty();
                if (flg) {
                    Spinner scale = findViewById(R.id.verification_SpinnerScale);
                    radius = findViewById(R.id.verification_EditTextRadius);
                    CheckBox isInfoTrue = findViewById(R.id.verification_CheckBoxIsInfoTrue);
                    EditText landmark = findViewById(R.id.verification_EditTextLandmark);
                    VerifyingDisasterPOJO verifyingDisasterPOJO = new VerifyingDisasterPOJO();
                    verifyingDisasterPOJO.referenceId = "RD260599";
                    verifyingDisasterPOJO.verifiedBy = "CurrentUser";
                    verifyingDisasterPOJO.verifiedTime = String.valueOf(System.currentTimeMillis() / 1000);
                    verifyingDisasterPOJO.isInfoTrue = isInfoTrue.isChecked();
                    verifyingDisasterPOJO.landmark = landmark.getText().toString();
                    verifyingDisasterPOJO.radius = Double.parseDouble(radius.getText().toString());
                    verifyingDisasterPOJO.scale = scale.getSelectedItem().toString();
                    verifyingDisasterPOJO.latitude = 12.43;
                    verifyingDisasterPOJO.longitude = 12.43;
                    Log.d("OUTPUT42", verifyingDisasterPOJO.objToJson().toString());
                    createThreadPostToVerify("http://" + R.string.ip_address + "/services/ds/disasterReport/verifiedDisaster", verifyingDisasterPOJO.objToJson());
                } else {
                    Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button goToMaps = findViewById(R.id.verification_buttonMaps);
        goToMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(VerificationActivity.this, MapsActivity.class);
                VerificationActivity.this.startActivity(myIntent);
            }
        });
    }

    public void createThreadPostToVerify(final String url, final JSONObject object) throws NullPointerException {
        final int[] response = new int[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = postRestApi(url, object);
                } finally {
                    if (response[0] == 200) {
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Verification Successful\nPlease wait for further instructions", Toast.LENGTH_SHORT).show();
                            }
                        });
                        Intent myIntent = new Intent(VerificationActivity.this, MapsActivity.class);
                        startActivity(myIntent);
                    } else {
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error while verification", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
        thread.start();
    }

    private int postRestApi(String url, JSONObject object) throws NullPointerException {
        final MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("RSCD-Token", "DynattralL1TokenKey12345")
                .addHeader("RSCD-JWT-Token", "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJJc3N1ZXIiOiJEeW5hdHRyYWwgVGVjaCIsIklzc3VlZFRvIjoiWWVra28iLCJFbXBsb3llZUNvZGUiOiJFTVAyNTM1NjciLCJQYXlsb2FkS2V5IjoiMTJkMDhlYjBhYTkyYjk0NTk2NTU2NWIyOWQ1M2FkMWYxNWE1NTE0NGVkMDcxNGFjNTZjMzQ2NzdjY2JjYjQwMCIsIklzc3VlZEF0IjoiMTktMDQtMjAxOSAyLjU0LjIzIFBNIiwiQ2hhbm5lbCI6InNpdGUifQ.Rf7szVWkGiSXHXfGW-xj4TRIw3VQRAySrt9kaEk1kuM")
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.code();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 404;
    }

}
