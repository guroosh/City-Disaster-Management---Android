package com.example.androidase_.mqtt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Objects;

public class MqttMessageService extends Service {

    private static final String TAG = "TAGG";

    public MqttMessageService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        //##############################
        PahoMqttClient pahoMqttClient = new PahoMqttClient();
        MqttAndroidClient mqttAndroidClient = pahoMqttClient.getMqttClient(getApplicationContext(), MqttActivity.MQTT_BROKER_URL, 0);
        //##############################

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }

            @Override
            public void connectionLost(Throwable throwable) {
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {
                setMessageNotification(s, new String(mqttMessage.getPayload()));
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void setMessageNotification(@NonNull String topic, @NonNull String msg) {
        MqttActivity.mqttInfo.setText( "\n\n" + topic + " ---> " + msg);

        AlertDialog alertDialog = new AlertDialog.Builder(getApplicationContext())
                .setTitle("WARNING!!!!")
                .setMessage("You are in a Disaster Zone!!!\nIts time to PANIC!!!")
                .create();

        Objects.requireNonNull(alertDialog.getWindow()).setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        alertDialog.show();
    }
}