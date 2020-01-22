package com.example.androidase_.mqtt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidase_.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MqttActivity extends AppCompatActivity {
    public static String IP_ADDRESS = "10.6.38.181";
    public static String MQTT_BROKER_URL = "tcp://" + IP_ADDRESS + ":1883";
    @SuppressLint("StaticFieldLeak")
    public static TextView mqttInfo;
    @SuppressLint("StaticFieldLeak")
    public static TextView mqttMetaInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt);
        mqttInfo = findViewById(R.id.mqtt_info);
        mqttMetaInfo = findViewById(R.id.mqtt_meta_info);
        final EditText sendMessage = findViewById(R.id.publishMessage);
        Button sendMessageButton = findViewById(R.id.publishMessageButton);

        final PahoMqttClient pahoMqttClient = new PahoMqttClient();
        final MqttAndroidClient client = pahoMqttClient.getMqttClient(getApplicationContext(), MQTT_BROKER_URL, -1);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendMessage.getText().toString().trim();
                if (!message.isEmpty())
                {
                    try {
                        pahoMqttClient.publishMessage(client, message, 1, "default");
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }
                sendMessage.setText("");
            }
        });

        Intent intent = new Intent(MqttActivity.this, MqttMessageService.class);
        startService(intent);
    }
}