package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.database.BaseDataHelper;
import com.example.androidase_.mqtt.MqttActivity;
import com.example.androidase_.mqtt.MqttMessageService;
import com.example.androidase_.object_classes.CommonUserPOJO;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);

        final String tableName = "user_data";
        final String[] columnNames = {"username", "password"};

        Button signInButton = findViewById(R.id.login);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                BaseDataHelper userDatabase = new BaseDataHelper(LoginActivity.this);
                userDatabase.createTable(tableName, new ArrayList<String>(Arrays.asList(columnNames)));

                HashMap<String, String> row = userDatabase.getRow(tableName, usernameString);
                CommonUserPOJO commonUser = new CommonUserPOJO();
                boolean doCredentialsMatch = createThreadPostToLogin("", commonUser.objToJson(usernameString, passwordString));
                if (doCredentialsMatch) {
                    Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
                    myIntent.putExtra("username", usernameString);
                    LoginActivity.this.startActivity(myIntent);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Username/Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button signupButton = findViewById(R.id.signup);
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    Intent myIntent = new Intent(LoginActivity.this, RegistrationActivity.class);
                    LoginActivity.this.startActivity(myIntent);

            }
        });

        Button testingPageButton = findViewById(R.id.testing_page);
        testingPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        });
    }

    // function to check whether 2 set of username password match or do not match
    // returns true if they match, returns false otherwise
    public Boolean checkUsernameAndPassword(String username, String password, String u1, String p1) {
        return username.equals(u1) && password.equals(p1);
    }

    public boolean createThreadPostToLogin(final String url, final JSONObject object) throws NullPointerException {
        final boolean[] returnValue = new boolean[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    returnValue[0] = postRestApi(url, object);
                } finally {
                    //empty
                    returnValue[0] = true;
                }
            }
        });
        thread.start();
        return returnValue[0];
    }

    private boolean postRestApi(String url, JSONObject object) throws NullPointerException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
}
