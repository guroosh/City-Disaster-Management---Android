package com.example.androidase_.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidase_.R;
import com.example.androidase_.database.BaseDataHelper;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static java.util.Base64.getEncoder;

public class DBActivity extends AppCompatActivity {
    private MqttAndroidClient client;
    private String mqttTopic;
    private TextView status;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db);

        final String tableName = "user_data";
        String[] columnNames = {"username", "password"};

        final BaseDataHelper userDatabase = new BaseDataHelper(this);
        userDatabase.createTable(tableName, new ArrayList<>(Arrays.asList(columnNames)));

        final EditText username = findViewById(R.id.database_username_update);
        final EditText password = findViewById(R.id.database_password_update);
        final TextView textView = findViewById(R.id.database_text_all);
        Button updateDatabase = findViewById(R.id.database_button_update);
        updateDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uName = username.getText().toString();
                String pWord = password.getText().toString();
                if (uName.trim().equals("") || pWord.trim().equals(""))
                {
                    Toast.makeText(DBActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                }
                else {
                HashMap<String, String> data = new HashMap<>();
                data.put("username", uName);
                data.put("password", pWord);
                userDatabase.insertRow(tableName, data.get("username"), data);
                username.setText("");
                password.setText("");
                Toast.makeText(DBActivity.this, "Username and password updated", Toast.LENGTH_SHORT).show();}
            }
        });
        Button getAllRows = findViewById(R.id.database_button_get_all);
        getAllRows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<HashMap<String, String>> data = userDatabase.getAllRows(tableName);
                StringBuilder s = new StringBuilder();
                for (HashMap<String, String> row : data) {
                    for (Map.Entry<String, String> entry : row.entrySet()) {
                        s.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
                    }
                    s.append("\n");
                }
                textView.setText(s.toString());
            }
        });

        status = findViewById(R.id.database_textView_status);
        connectMQTT();
        Button btnSend = findViewById(R.id.database_button_send);
        btnSend.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                EditText et = findViewById(R.id.database_editText_message);
                String payload = et.getText().toString();
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(mqttTopic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void connectMQTT(){
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
                    status.setText("connection is successful.");
                    subscribeMQTT();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    status.setText("connection is failed.");
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void subscribeMQTT(){
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(mqttTopic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published
                    status.setText("subscription is successful.");
                    setMQTTCallback();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    status.setText("subscription is failed.");
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void setMQTTCallback(){
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                status.setText("connection is lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                status.setText("message is received.");
                String msg = new String(message.getPayload(), StandardCharsets.UTF_8);
                TextView tv = findViewById(R.id.database_textView_chat);
                tv.setText(msg);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                status.setText("delivery completed.");
            }
        });
    }
}
