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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final EditText username = findViewById(R.id.username);
        final EditText password = findViewById(R.id.password);

        final String tableName = "user_data";
        final String[] columnNames = {"username", "password"};

        Button signIn = findViewById(R.id.login);
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameString = username.getText().toString();
                String passwordString = password.getText().toString();
                BaseDataHelper userDatabase = new BaseDataHelper(LoginActivity.this);
                userDatabase.createTable(tableName, new ArrayList<String>(Arrays.asList(columnNames)));
                HashMap<String, String> row = userDatabase.getRow(tableName, usernameString);
                if (usernameString.equals(row.get(columnNames[0])) && passwordString.equals(row.get(columnNames[1]))) {
                    Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
                    myIntent.putExtra("username", usernameString);
                    LoginActivity.this.startActivity(myIntent);
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid Username/Password", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button testingPage = findViewById(R.id.testing_page);
        testingPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        });
    }
}
