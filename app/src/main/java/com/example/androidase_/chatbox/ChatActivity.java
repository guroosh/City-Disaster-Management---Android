package com.example.androidase_.chatbox;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.androidase_.R;
import com.example.androidase_.activities.MapsActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {


    private EditText editText;
    private MessageAdapter messageAdapter;
    private ListView messagesView;

    public String randomString;
    private MqttAndroidClient client;
    private String mqttTopic;

    public static String username;

    public static HashMap<String, String> nameToColor = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatting);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginData", MODE_PRIVATE);
        username = pref.getString("username", "null");

        editText = findViewById(R.id.editText);
        messageAdapter = new MessageAdapter(this);
        messagesView = findViewById(R.id.messages_view);
        messagesView.setAdapter(messageAdapter);
        randomString = UUID.randomUUID().toString();

//        data = new MemberData(getRandomName(), getRandomColor());

        connectMQTT();
    }

    public void sendMessage(View view) {
        String payload = editText.getText().toString();
        payload = randomString + username + ":" + payload;
        if (payload.length() > 0) {
            byte[] encodedPayload;
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                client.publish(mqttTopic, message);
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
            // this method call shows the message twice
//            publishMessage(payload, true);
            editText.getText().clear();
        }
    }


    private void showMessageOnScreen(String receivedMessage, boolean isItMyMessage) {
        String[] arr = receivedMessage.split(":");
        String messageText = arr[1];
        String theirUsername = arr[0];
        MemberData data;
        if (nameToColor.containsKey(theirUsername)) {
            data = new MemberData(theirUsername, nameToColor.get(theirUsername));
        } else {
            String randomColor = getRandomColor();
            data = new MemberData(theirUsername, randomColor);
            nameToColor.put(theirUsername, randomColor);
        }
        Random r = new Random();
        int i = r.nextInt(2);
        final Message message;
        if (i == 0) {
            message = new Message(messageText, data, isItMyMessage);
        } else {
            message = new Message(messageText, data, isItMyMessage);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                messageAdapter.add(message);
                messagesView.setSelection(messagesView.getCount() - 1);
            }
        });
    }

    private String getRandomColor() {
        Random r = new Random();
        StringBuilder sb = new StringBuilder("#");
        while (sb.length() < 7) {
            sb.append(Integer.toHexString(r.nextInt()));
        }
        return sb.toString().substring(0, 7);
    }

    public void connectMQTT() {
        mqttTopic = "ase/android";
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883",
                clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
//                    status.setText("connection is successful.");
                    subscribeMQTT();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
//                    status.setText("connection is failed.");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribeMQTT() {
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(mqttTopic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
//                    status.setText("subscription is successful.");
                    setMQTTCallback();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
//                    status.setText("subscription is failed.");
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void setMQTTCallback() {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
//                status.setText("connection is lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
//                status.setText("message is received.");
                String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
                boolean isItMyMessage;
                if (msg.startsWith(randomString)) {
                    isItMyMessage = true;
                    msg = msg.replace(randomString, "");
                } else {
                    isItMyMessage = false;
                    msg = msg.substring(36);
                    makeNotification("Chat support", msg);
                }
                showMessageOnScreen(msg, isItMyMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
//                status.setText("delivery completed.");
            }
        });
    }

    private void makeNotification(String title, String msg) {
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
                .setContentTitle(title) // title for notification
                .setContentText(msg)// message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pi);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
