package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.androidase_.R;
import com.example.androidase_.database.BaseDataHelper;
import com.example.androidase_.object_classes.CommonUserAfterLoginPOJO;
import com.example.androidase_.object_classes.CommonUserRegistrationPOJO;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
                CommonUserRegistrationPOJO commonUser = new CommonUserRegistrationPOJO();
                CommonUserAfterLoginPOJO commonUserAfterLoginPOJO = createThreadPostToLogin("http://" + getResources().getString(R.string.ip_address) + "/login/login", commonUser.objToJson(usernameString, passwordString));
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

    public CommonUserAfterLoginPOJO createThreadPostToLogin(final String url, final JSONObject object) throws NullPointerException {
        final Response[] returnValue = new Response[1];
        final CommonUserAfterLoginPOJO[] commonUserAfterLoginPOJO = new CommonUserAfterLoginPOJO[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("response42", "about to try");
                    returnValue[0] = postRestApi(url, object);
                } finally {
                    Log.d("response42", "after response");
                    try {
                        String returnValueString = returnValue[0].body().string();
                        Log.d("response42", returnValueString);
                        String jsonData = returnValueString;
                        JSONObject jsonObject = new JSONObject(jsonData);
                        Log.d("response42", jsonObject.getString("referenceCode"));
                        commonUserAfterLoginPOJO[0] = new CommonUserAfterLoginPOJO();
                        commonUserAfterLoginPOJO[0].accessToken = jsonObject.getString("accessToken");
                        commonUserAfterLoginPOJO[0].isCommonUser = jsonObject.getBoolean("isCommonUser");
                        commonUserAfterLoginPOJO[0].referenceCode = jsonObject.getString("referenceCode");
                        boolean isCommonUser = commonUserAfterLoginPOJO[0].isCommonUser;
                        if (isCommonUser) {
                            Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
//                            myIntent.putExtra("username", usernameString);
                            LoginActivity.this.startActivity(myIntent);
                        } else {
                            Intent myIntent = new Intent(LoginActivity.this, VerificationActivity.class);
//                            myIntent.putExtra("username", usernameString);
                            LoginActivity.this.startActivity(myIntent);
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
        return commonUserAfterLoginPOJO[0];
    }

    private Response postRestApi(String url, JSONObject object) throws NullPointerException {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("RSCD-Token", "DynattralL1TokenKey12345")
                .addHeader("RSCD-JWT-Token", "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJJc3N1ZXIiOiJEeW5hdHRyYWwgVGVjaCIsIklzc3VlZFRvIjoiWWVra28iLCJFbXBsb3llZUNvZGUiOiJFTVAyNTM1NjciLCJQYXlsb2FkS2V5IjoiMTJkMDhlYjBhYTkyYjk0NTk2NTU2NWIyOWQ1M2FkMWYxNWE1NTE0NGVkMDcxNGFjNTZjMzQ2NzdjY2JjYjQwMCIsIklzc3VlZEF0IjoiMTktMDQtMjAxOSAyLjU0LjIzIFBNIiwiQ2hhbm5lbCI6InNpdGUifQ.Rf7szVWkGiSXHXfGW-xj4TRIw3VQRAySrt9kaEk1kuM")
                .addHeader("Content-Type", "application/json")
                .addHeader("Channel", "Android")
                .build();
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("response42", e.toString());
        }
        Log.d("response42", "inside the api function");
        return response;
    }
}
