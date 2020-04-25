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

        SharedPreferences.Editor editor = getSharedPreferences("MapsData", MODE_PRIVATE).edit();
        editor.putBoolean("fromVerification", false);
        editor.apply();

        SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
        loggedIn = pref.getBoolean("loggedIn", false);

        int SPLASH_TIME_OUT = 3000;
        if (loggedIn) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(SplashScreenActivity.this, MapsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, SPLASH_TIME_OUT);
        } else {
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
