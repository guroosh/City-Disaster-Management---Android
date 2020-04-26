package com.example.androidase_.mqtt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FirebaseService extends FirebaseMessagingService {

    public FirebaseService() {
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d("Token42", "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(final RemoteMessage var1) {
//        super.onMessageReceived(var1);

        Log.d("Token42", "Refreshed message: " + var1.getNotification().getTitle() + " " + var1.getNotification().getBody());
//        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
                boolean isCommonUser = pref.getBoolean("isCommonUser", true);
                String title = var1.getNotification().getTitle();
                if (!title.equals("New Disaster Reported") || !isCommonUser) {
                    Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);
                    notificationIntent.putExtra("fromVerification", true);
                    notificationIntent.putExtra("disaster_lat", String.valueOf(var1.getData().get("disaster_lat")));
                    notificationIntent.putExtra("disaster_lng", String.valueOf(var1.getData().get("disaster_lng")));
                    notificationIntent.putExtra("radius", String.valueOf(var1.getData().get("radius")));

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
                            .setContentTitle(var1.getNotification().getTitle()) // title for notification
                            .setContentText(var1.getNotification().getBody())// message for notification
                            .setAutoCancel(true); // clear notification after click


                    PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(pi);
                    mNotificationManager.notify(0, mBuilder.build());
                }
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
