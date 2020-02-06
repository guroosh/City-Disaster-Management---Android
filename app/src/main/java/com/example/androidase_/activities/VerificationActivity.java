package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.androidase_.R;
import com.example.androidase_.object_classes.ReportedDisaster;

public class VerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ReportedDisaster reportedDisaster = new ReportedDisaster();
        reportedDisaster.createTestValue();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);
        this.loadValue(reportedDisaster);

        Button verificationButton = findViewById(R.id.buttonVerification);
        verificationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Intent myIntent = new Intent(MainActivity.this, VerificationActivity.class);
               // myIntent.putExtra("key", "test");
               // MainActivity.this.startActivity(myIntent);
            }
        });
    }
    protected void loadValue(ReportedDisaster reportedDisaster){
        TextView latitude = findViewById(R.id.verification_TextViewLatitude);
        latitude.setText(String.valueOf(reportedDisaster.getLocation().latitude));
        TextView longtitude = findViewById(R.id.verification_TextViewLongtitude);
        longtitude.setText(String.valueOf(reportedDisaster.getLocation().longitude));
        //TextView radius = findViewById(R.id.verification_EditTextRadius);
        //radius.setText(String.valueOf(reportedDisaster.getRadius()));
        TextView verifiedBy = findViewById(R.id.verification_TextViewVerifiedBy);
        verifiedBy.setText(reportedDisaster.getVerifiedBy());
        TextView time = findViewById(R.id.verification_TextViewVerifiedTime);
        time.setText(String.valueOf(reportedDisaster.getTime()));
    }
}
