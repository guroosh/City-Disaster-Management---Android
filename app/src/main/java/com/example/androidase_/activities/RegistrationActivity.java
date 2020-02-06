package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.example.androidase_.object_classes.CommonUser;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class RegistrationActivity extends AppCompatActivity {
    String _message = "";

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
                CheckBox checkBoxIsVolunteer = findViewById(R.id.checkBoxVolunteer);


                //System.out.println(sin.isChecked());
                textViewMessage.setText(_message);
                if (flg) {
                    CommonUser commonUser = new CommonUser();
                    commonUser.emailId = String.valueOf(R.id.editTextEmail);
                    commonUser.password = String.valueOf(R.id.registration_editTextPassword);
                    commonUser.phoneNumber = String.valueOf(R.id.editTextPhone);
                    commonUser.governmentIdNumber = String.valueOf(R.id.editTextGovernmentIDNumber);
                    commonUser.isVolunteering = checkBoxIsVolunteer.isChecked();
                    commonUser.volunteeringField = String.valueOf(R.id.spinnerVolunteeringField);
                    commonUser.governmentIdType = String.valueOf(R.id.spinnerGovernmentIDType);
                    commonUser.setName(String.valueOf(R.id.editTextFirstName), String.valueOf(R.id.editTextLastName));
                    createThreadPostToSignup("", commonUser.objToJson());
                    Toast.makeText(getApplicationContext(), "yes", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "nope", Toast.LENGTH_SHORT).show();
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
                if (editText.getText().toString().contains("@")) {
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    postRestApi(url, object);
                } finally {
                    //empty
                }
            }
        });
        thread.start();
    }

    private void postRestApi(String url, JSONObject object) throws NullPointerException {
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
    }
}
