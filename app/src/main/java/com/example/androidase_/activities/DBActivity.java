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
    }
}
