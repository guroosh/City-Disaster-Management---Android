package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.androidase_.R;
import com.example.androidase_.verification.VerificationActivity;

import java.util.Random;

public class SplashScreenActivity extends AppCompatActivity {

    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
        loggedIn = pref.getBoolean("loggedIn", false);

        int SPLASH_TIME_OUT = 3000;

//        Random r = new Random();
//        int rInt = r.nextInt(2);
        final boolean[] isCommonUser = new boolean[1];
//        isCommonUser[0] = (rInt != 0);
        isCommonUser[0] = true;
//        Button button = findViewById(R.id.isAdmin);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isCommonUser[0] = false;
//            }
//        });

        if (loggedIn) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isCommonUser[0]) {
                        Intent intent = new Intent(SplashScreenActivity.this, MapsActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Intent intent = new Intent(SplashScreenActivity.this, VerificationActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }, SPLASH_TIME_OUT);
        } else {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
//                    intent.putExtra("isCommonUser", isCommonUser[0]);
                    startActivity(intent);
                    finish();
                }

            }, SPLASH_TIME_OUT);
        }
    }
}
