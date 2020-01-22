package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.androidase_.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpActivity extends AppCompatActivity {

    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        textView = findViewById(R.id.http_view);
        String randomString = createRandomString();
        String jsonData = "{\"name\":\"" + randomString + "\",\"salary\":\"\",\"age\":\"\"}";

        textView.setText("Setting random string: " + randomString);
        createThreadForPost("http://dummy.restapiexample.com/api/v1/create", jsonData);
    }

    private String createRandomString() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = upper.toLowerCase();
        String digits = "0123456789";
        String alphanum = upper + lower + digits;
        int length = alphanum.length();
        Random r = new Random();
        StringBuilder randomString = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            int nextInt = r.nextInt(length);
            char ch = alphanum.charAt(nextInt);
            randomString.append(ch);
        }
        return randomString.toString();
    }

    public void createThreadForPost(final String url, final String jsonString) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(textView.getText() + "\n\nStarting POST request on: " + url);
                        }
                    });
                    result[0] = postRestApi(url, jsonString);
                } finally {
                    try {

                        Log.d("Test", result[0]);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText(textView.getText() + "\n\nResponse from POST request: " + result[0].replace("\n", " "));
                            }
                        });
                        JSONObject jsonObject = new JSONObject(result[0]);
                        String id = (String) jsonObject.get("id");
                        Log.d("Test", id);
                        createThreadForGet("http://dummy.restapiexample.com/api/v1/employee/" + id);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void createThreadForGet(final String url) {
        final String[] result = new String[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(textView.getText() + "\n\nStarting GET request on: " + url);
                        }
                    });
                    result[0] = getRestApi(url);
                } finally {
                    Log.d("Test", result[0]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textView.setText(textView.getText() + "\n\nResponse from GET request: " + result[0].replace("\n", " "));
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public String getRestApi(String url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
            return "IO-Error";
        }
    }

    public String postRestApi(String url, String jsonString) {
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();
        JSONObject object;
        try {
            object = new JSONObject(jsonString);
            RequestBody body = RequestBody.create(JSON, object.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                return Objects.requireNonNull(response.body()).string();
            } catch (IOException e) {
                e.printStackTrace();
                return "IO-Error";
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "JSON-Error";
        }
    }
}
