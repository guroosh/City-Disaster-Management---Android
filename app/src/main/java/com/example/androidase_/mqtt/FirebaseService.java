package com.example.androidase_.mqtt;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseService extends FirebaseMessagingService {
    public FirebaseService() {
    }
    @Override
    public void onNewToken(String token) {
        Log.d("Token", "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

}
