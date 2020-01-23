package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androidase_.R;

public class NotificationActivity extends AppCompatActivity {

    EditText ed1, ed2, ed3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        ed1 = findViewById(R.id.editText);
        ed2 = findViewById(R.id.editText2);
        ed3 = findViewById(R.id.editText3);
        Button b1 = findViewById(R.id.button);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = ed1.getText().toString().trim();
                String subject = ed2.getText().toString().trim();
                String body = ed3.getText().toString().trim();

//                NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                Notification notify = new Notification.Builder
//                        (getApplicationContext()).setContentTitle(tittle).setContentText(body).
//                        setContentTitle(subject).setSmallIcon(R.drawable.yellow).build();
//
//                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
//                        .setSmallIcon(R.drawable.yellow)
//                        .setContentTitle("Test1")
//                        .setContentText("Hello Guroosh!!!")
//                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//                notify.flags |= Notification.FLAG_AUTO_CANCEL;
//                notif.notify(0, notify);
                Toast.makeText(getApplicationContext(), "Click", Toast.LENGTH_SHORT).show();
                showNotification("Test", "Hello Guroosh!!!");
            }
        });
    }
    void showNotification(String title, String message) {
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
                .setContentTitle(title) // title for notification
                .setContentText(message)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
