package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import com.example.androidase_.R;

public class SplashScreenActivity extends AppCompatActivity {

    private boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
        loggedIn = pref.getBoolean("loggedIn", false);

        int SPLASH_TIME_OUT = 2000;

        if (loggedIn) {

            //It means User is already Logged in so I will take the user to Select_College Screen

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    Intent intent = new Intent(SplashScreenActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();

                }

            }, SPLASH_TIME_OUT);

        } else {

            //It means User is not Logged in so I will take the user to Login Screen

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {

                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();

                }

            }, SPLASH_TIME_OUT);

        }

    }
}
