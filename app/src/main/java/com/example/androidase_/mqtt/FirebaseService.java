package com.example.androidase_.mqtt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.androidase_.R;
import com.example.androidase_.activities.DBActivity;
import com.example.androidase_.activities.MainActivity;
import com.example.androidase_.activities.MapsActivity;
import com.example.androidase_.activities.NotificationActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseService extends FirebaseMessagingService {
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";

    public FirebaseService() {
    }

    @Override
    public void onNewToken(String token) {
//        super.onNewToken(token);
        Log.d("Token42", "Refreshed token: " + token);
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage var1) {
//        super.onMessageReceived(var1);
        Log.d("Token42", "Refreshed message: " + var1.getNotification().getTitle() + " " + var1.getNotification().getBody());
//        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);
                notificationIntent.putExtra("NotificationMessage", "I am from Notification");
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                            "YOUR_CHANNEL_NAME",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DISCRIPTION");
                    mNotificationManager.createNotificationChannel(channel);
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), "YOUR_CHANNEL_ID")
                        .setSmallIcon(R.mipmap.ic_launcher) // notification icon
                        .setContentTitle("title") // title for notification
                        .setContentText("message")// message for notification
                        .setAutoCancel(true); // clear notification after click
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pi);
                mNotificationManager.notify(0, mBuilder.build());
            }
        });

//        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        notificationIntent.setAction(Intent.ACTION_MAIN);
//        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        PendingIntent resultIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);
//        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(),
//                default_notification_channel_id)
//                .setContentTitle("Test")
//                .setContentText("Hello! This is my first push notification")
//                .setContentIntent(resultIntent);
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            int importance = NotificationManager.IMPORTANCE_HIGH;
//            NotificationChannel notificationChannel = new
//                    NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
//            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
//            assert mNotificationManager != null;
//            mNotificationManager.createNotificationChannel(notificationChannel);
//        }
//        assert mNotificationManager != null;
//
//        mNotificationManager.notify((int) System.currentTimeMillis(), mBuilder.build());
    }

}
