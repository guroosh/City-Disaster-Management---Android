package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.androidase_.R;
import com.example.androidase_.database.BaseDataHelper;
import com.example.androidase_.object_classes.CommonUserRegistrationPOJO;

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

public class RegistrationActivity extends AppCompatActivity {
    String _message = "";
    Activity a = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final TextView textViewMessage = findViewById(R.id.register_messageTextView);

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                _message = "";
                boolean flg = (checkCondition(R.id.editTextEmail, 0, "Email")) &&
                        (checkCondition(0, 1, "The format of Email is incorrect.")) &&
                        (checkCondition(R.id.editTextFirstName, 0, "First Name")) &&
                        (checkCondition(R.id.editTextLastName, 0, "Last Name")) &&
                        (checkCondition(R.id.editTextGovernmentIDNumber, 0, "Government ID Number")) &&
                        (checkCondition(R.id.registration_editTextPassword, 0, "Password")) &&
                        (checkCondition(R.id.registration_editTextConfirmPassword, 0, "Confirm Password")) &&
                        checkCondition(0, 2, "Password does't match.");


                //System.out.println(sin.isChecked());
                textViewMessage.setText(_message);
                if (flg) {
                    CommonUserRegistrationPOJO commonUser = new CommonUserRegistrationPOJO();

                    EditText emailId = findViewById(R.id.editTextEmail);
                    EditText password = findViewById(R.id.registration_editTextPassword);
                    EditText phoneNumber = findViewById(R.id.editTextPhone);
                    EditText governmentIdNumber = findViewById(R.id.editTextGovernmentIDNumber);
                    CheckBox checkBoxIsVolunteer = findViewById(R.id.checkBoxVolunteer);
                    Spinner volunteeringField = findViewById(R.id.spinnerVolunteeringField);
                    Spinner governmentIdType = findViewById(R.id.spinnerGovernmentIDType);
                    EditText firstName = findViewById(R.id.editTextFirstName);
                    EditText lastName = findViewById(R.id.editTextLastName);

                    commonUser.emailId = emailId.getText().toString();
                    commonUser.password = password.getText().toString();
                    commonUser.phoneNumber = phoneNumber.getText().toString();
                    commonUser.governmentIdNumber = governmentIdNumber.getText().toString();
                    commonUser.isVolunteering = checkBoxIsVolunteer.isChecked();
                    commonUser.volunteeringField = volunteeringField.getSelectedItem().toString();
                    commonUser.governmentIdType = governmentIdType.getSelectedItem().toString();
                    commonUser.firstName = firstName.getText().toString();
                    commonUser.lastName = lastName.getText().toString();

//                    Toast.makeText(getApplicationContext(), commonUser.objToJson().toString(), Toast.LENGTH_SHORT).show();
                    Log.d("OUTPUT42", commonUser.objToJson().toString());

                    //For backend
//                    createThreadPostToSignup("http://" + getResources().getString(R.string.ip_address) + "/services/rs/registration/registerCu", commonUser.objToJson());
                    //For demo
                    startNextActivity(commonUser.emailId, commonUser.password);

                } else {
                    Toast.makeText(getApplicationContext(), "Nope", Toast.LENGTH_SHORT).show();
                }
            }

        });

        Button cancelButton = findViewById(R.id.register_buttonCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(RegistrationActivity.this, LoginActivity.class);
                RegistrationActivity.this.startActivity(myIntent);
            }
        });
    }

    private void startNextActivity(String emailId, String password) {
        HashMap<String, String> data = new HashMap<>();
        data.put("username", emailId);
        data.put("password", password);

        String tableName = "user_data";
        String[] columnNames = {"username", "password"};
        BaseDataHelper userDatabase = new BaseDataHelper(this);
        userDatabase.createTable(tableName, new ArrayList<>(Arrays.asList(columnNames)));
        userDatabase.insertRow(tableName, data.get("username"), data);

        String username = emailId.split("@")[0];
        SharedPreferences.Editor editor = getSharedPreferences("LoginData", MODE_PRIVATE).edit();
        editor.putString("username", username);
        editor.putBoolean("loggedIn", true);
        editor.putBoolean("isCommonUser", true);
        editor.apply();

        Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
        Intent myIntent = new Intent(RegistrationActivity.this, MapsActivity.class);
        RegistrationActivity.this.startActivity(myIntent);
    }

    protected boolean checkCondition(int ID, int kind, String wrongMsg) {
        boolean flg = true;
        EditText editText;
        switch (kind) {
            case 0:
                editText = findViewById(ID);
                String text = editText.getText().toString();
                if (text.isEmpty()) {
                    _message += wrongMsg + " is empty.\n";
                    flg = false;
                }
                break;
            case 1:
                editText = findViewById(R.id.editTextEmail);
                if (!editText.getText().toString().contains("@")) {
                    _message += wrongMsg + "\n";
                    flg = false;
                }
                break;
            case 2:
                EditText password1 = findViewById(R.id.registration_editTextPassword);
                EditText password2 = findViewById(R.id.registration_editTextConfirmPassword);
                if (!password1.getText().toString().equals(password2.getText().toString())) {
                    _message += wrongMsg + "\n";
                    flg = false;
                }
                break;
        }
        return flg;
    }

    public void createThreadPostToSignup(final String url, final JSONObject object) throws NullPointerException {
        final Response[] response = new Response[1];
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    response[0] = postRestApi(url, object);
                } finally {
                    if (response[0] == null) {
                        Log.d("response42", "Connectivity error");
                        a.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Connectivity error", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        if (response[0].code() == 200) {
                            a.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Registration Successful", Toast.LENGTH_SHORT).show();
                                }
                            });
//                            Intent myIntent = new Intent(RegistrationActivity.this, MapsActivity.class);
//                            RegistrationActivity.this.startActivity(myIntent);
                        } else {
                            a.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Server error while registration: " + response[0].code(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }
        });
        thread.start();
    }

    private Response postRestApi(String url, JSONObject object) throws NullPointerException {
        final MediaType JSON = MediaType.parse("application/json");
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(object.toString(), JSON);
        Response response = null;
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("RSCD-Token", "DynattralL1TokenKey12345")
                .addHeader("RSCD-JWT-Token", "eyJhbGciOiJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNobWFjLXNoYTI1NiIsInR5cCI6IkpXVCJ9.eyJJc3N1ZXIiOiJEeW5hdHRyYWwgVGVjaCIsIklzc3VlZFRvIjoiWWVra28iLCJFbXBsb3llZUNvZGUiOiJFTVAyNTM1NjciLCJQYXlsb2FkS2V5IjoiMTJkMDhlYjBhYTkyYjk0NTk2NTU2NWIyOWQ1M2FkMWYxNWE1NTE0NGVkMDcxNGFjNTZjMzQ2NzdjY2JjYjQwMCIsIklzc3VlZEF0IjoiMTktMDQtMjAxOSAyLjU0LjIzIFBNIiwiQ2hhbm5lbCI6InNpdGUifQ.Rf7szVWkGiSXHXfGW-xj4TRIw3VQRAySrt9kaEk1kuM")
                .addHeader("Content-Type", "application/json")
                .build();
        try {
            response = client.newCall(request).execute();
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }
}
