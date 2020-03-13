package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.androidase_.mqtt.MqttActivity;
import com.example.androidase_.p2p.P2PActivity;
import com.example.androidase_.R;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
//import com.google.firebase.quickstart.fcm.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonDB = this.findViewById(R.id.button_db);
        buttonDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, DBActivity.class);
                myIntent.putExtra("key", "database");
                MainActivity.this.startActivity(myIntent);
            }
        });

        Button buttonLocation = this.findViewById(R.id.button_location);
        buttonLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, LocationActivity.class);
                myIntent.putExtra("key", "location");
                MainActivity.this.startActivity(myIntent);
            }
        });

        Button buttonP2P = this.findViewById(R.id.button_p2p);
        buttonP2P.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, P2PActivity.class);
                myIntent.putExtra("key", "p2p");
                MainActivity.this.startActivity(myIntent);
            }
        });


        Button buttonMqtt = this.findViewById(R.id.button_mqtt);
        buttonMqtt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, MqttActivity.class);
                myIntent.putExtra("key", "mqtt");
                MainActivity.this.startActivity(myIntent);
            }
        });

        final EditText ipEditText = findViewById(R.id.ip_change_text);
        Button ipEditButton = findViewById(R.id.ip_change_button);

        ipEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MqttActivity.IP_ADDRESS = ipEditText.getText().toString();
                ipEditText.setText("");
            }
        });

        Button notificationButton = findViewById(R.id.notification_button);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, NotificationActivity.class);
                myIntent.putExtra("key", "test");
                MainActivity.this.startActivity(myIntent);
            }
        });

        Button verificationButton = findViewById(R.id.buttonVerification);
        verificationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, VerificationActivity.class);
                myIntent.putExtra("key", "test");
                MainActivity.this.startActivity(myIntent);
            }
        });

        Button mapsButton = findViewById(R.id.buttonMapsFromTesting);
        mapsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                myIntent.putExtra("key", "test");
                MainActivity.this.startActivity(myIntent);
            }
        });

        Button tokenButton = findViewById(R.id.buttonToken);
        tokenButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("test", "this is token:"+FirebaseInstanceId.getInstance().getInstanceId());

               // Log.d("test","hihihi");

                /*
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                            @Override
                            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "getInstanceId failed", task.getException());
                                    return;
                                }

                                // Get new Instance ID token
                                String token = task.getResult().getToken();

                                // Log and toast
                                String msg = getString(R.string.msg_token_fmt, token);
                                Log.d(TAG, msg);
                                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        });*/
            }
        });
//        Button buttonHttp = this.findViewById(R.id.button_http);
//        buttonHttp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent myIntent = new Intent(MainActivity.this, HttpActivity.class);
//                myIntent.putExtra("key", "mqtt");
//                MainActivity.this.startActivity(myIntent);
//            }
//        });

    }
}