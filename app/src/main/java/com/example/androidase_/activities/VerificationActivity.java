package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.androidase_.R;
import com.example.androidase_.object_classes.ReportedDisaster;
import com.example.androidase_.object_classes.VerifyingDisasterPOJO;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Spinner scale = findViewById(R.id.verification_SpinnerScale);
        EditText radius = findViewById(R.id.verification_EditTextRadius);
        CheckBox isInfoTrue = findViewById(R.id.verification_CheckBoxIsInfoTrue);
        EditText landmark = findViewById(R.id.verification_EditTextLandmark);

        VerifyingDisasterPOJO verifyingDisasterPOJO = new VerifyingDisasterPOJO();
        verifyingDisasterPOJO.verifiedBy = "CurrentUser";
        verifyingDisasterPOJO.verifiedTime = String.valueOf(System.currentTimeMillis() / 1000);
        verifyingDisasterPOJO.isInfoTrue = isInfoTrue.isChecked();
        verifyingDisasterPOJO.landmark = landmark.getText().toString();
        verifyingDisasterPOJO.radius = radius.getText().toString();
        verifyingDisasterPOJO.scale = scale.getSelectedItem().toString();
        verifyingDisasterPOJO.latitude = "current lat";
        verifyingDisasterPOJO.longitude = "current lng";

        Button submitVerification = findViewById(R.id.verification_buttonVerify);
        submitVerification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo
            }
        });
    }

}
