package com.example.androidase_.mqtt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.nio.charset.StandardCharsets;

import static com.example.androidase_.mqtt.MqttActivity.MQTT_BROKER_URL;

public class PahoMqttClient {

    private static final String TAG = "TAGG";
    private MqttAndroidClient mqttAndroidClient;

    public MqttAndroidClient getMqttClient(final Context context, String brokerUrl, final int flg) {
        /*flg:
            0 for returning client and subscribing to #
            -1 for only returning the client
        */

        String clientId = String.valueOf(System.currentTimeMillis());
        mqttAndroidClient = new MqttAndroidClient(context, brokerUrl, clientId);
        try {
            IMqttToken token = mqttAndroidClient.connect(getMqttConnectionOption());
            token.setActionCallback(new IMqttActionListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    mqttAndroidClient.setBufferOpts(getDisconnectedBufferOptions());
                    Log.d(TAG, "Success");
                    try {
                        if (flg == 0) {
                            subscribe(mqttAndroidClient);
                        } else if (flg == -1) {
                            // do nothing
                        }
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.d(TAG, "Failure " + exception.toString());
                }
            });
        } catch (MqttException e) {
            Log.d("TAGG", e.toString());
            e.printStackTrace();
        }
        return mqttAndroidClient;
    }

    public void disconnect(@NonNull MqttAndroidClient client) throws MqttException {
        IMqttToken mqttToken = client.disconnect();
        mqttToken.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Successfully disconnected");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.d(TAG, "Failed to disconnected " + throwable.toString());
            }
        });
    }

    @NonNull
    private DisconnectedBufferOptions getDisconnectedBufferOptions() {
        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
        disconnectedBufferOptions.setBufferEnabled(true);
        disconnectedBufferOptions.setBufferSize(100);
        disconnectedBufferOptions.setPersistBuffer(false);
        disconnectedBufferOptions.setDeleteOldestMessages(false);
        return disconnectedBufferOptions;
    }

    @NonNull
    private MqttConnectOptions getMqttConnectionOption() {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setAutomaticReconnect(true);
        return mqttConnectOptions;
    }


    public void publishMessage(@NonNull MqttAndroidClient client, @NonNull String msg, int qos, @NonNull String topic) throws MqttException {
        byte[] encodedPayload;
        encodedPayload = msg.getBytes(StandardCharsets.UTF_8);
        MqttMessage message = new MqttMessage(encodedPayload);
        message.setId(320);
        message.setRetained(true);
        message.setQos(qos);
        client.publish(topic, message);
    }

    private void subscribe(@NonNull MqttAndroidClient client) throws MqttException {
        final String topic = "#";
        IMqttToken token = client.subscribe(topic, 1);
        token.setActionCallback(new IMqttActionListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "Subscribe Successfully: " + "default");
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "Subscribe Failed: " + "default");
            }
        });
    }

    public void unSubscribe(@NonNull MqttAndroidClient client, @NonNull final String topic) throws MqttException {
        IMqttToken token = client.unsubscribe(topic);
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken iMqttToken) {
                Log.d(TAG, "UnSubscribe Successfully" + topic);
            }

            @Override
            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                Log.e(TAG, "UnSubscribe Failed" + topic);
            }
        });
    }

}
