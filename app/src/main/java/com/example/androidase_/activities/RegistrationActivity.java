package com.example.androidase_.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.androidase_.R;

public class RegistrationActivity extends AppCompatActivity {
    String _message = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        final TextView textViewMessage = findViewById(R.id.register_messageTextView);

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                _message = "";
                boolean flg = true;
                flg = checkCondition(R.id.editTextEmail,0,"Email");
                flg = checkCondition(0,1,"The format of Email is incorrect.");
                flg  = checkCondition(R.id.editTextFirstName,0,"First Name");
                flg = checkCondition(R.id.editTextLastName,0,"Last Name");
                flg = checkCondition(R.id.editTextGovernmentIDNumber,0,"Government ID Number");
                flg = checkCondition(R.id.registration_editTextPassword,0,"Password");
                flg = checkCondition(R.id.registration_editTextConfirmPassword,0,"Confirm Password");
                flg = checkCondition(0,2,"Password does't match.");
                textViewMessage.setText(_message);
                if (flg)
                {
                    //send api with data
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

    protected boolean checkCondition(int ID,int kind,String wrongMsg){
        boolean flg = true;
        EditText editText;
        switch (kind){
            case 0:
                editText = findViewById(ID);
                String text = editText.getText().toString();
                if (text.isEmpty()){
                    _message += wrongMsg+" is empty.\n";
                    flg = false;
                }
                break;
            case 1:
                editText = findViewById(R.id.editTextEmail);
                if (editText.getText().toString().contains("@")){
                    _message += wrongMsg+"\n";
                    flg = false;
                }
                break;
            case 2:
                EditText password1 = findViewById(R.id.registration_editTextPassword);
                EditText password2 = findViewById(R.id.registration_editTextConfirmPassword);
                if(!password1.getText().toString().equals(password2.getText().toString())){
                    _message += wrongMsg+"\n";
                    flg = false;
                }
                break;
        }
        return flg;
    }
}
