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
    /*
        final EditText editTextEmail = findViewById(R.id.editTextEmail);
        final EditText editTextPassword = findViewById(R.id.editTextPassword);
        final EditText editTextFirstName = findViewById(R.id.editTextFirstName);
        final EditText editTextLastName = findViewById(R.id.editTextLastName);
        final EditText editTextPhone = findViewById(R.id.editTextPhone);
        final EditText editTextGovernmentIDNumber = findViewById(R.id.editTextGovernmentIDNumber);
        final CheckBox checkBoxVolunteer = findViewById(R.id.checkBoxVolunteer);
        final Spinner spinnerVolunteeringField = findViewById(R.id.spinnerVolunteeringField);
        final Spinner spinnerGovernmentIDType = findViewById(R.id.spinnerGovernmentIDType);
        */
        final TextView textViewMessage = findViewById(R.id.register_messageTextView);

        Button registerButton = findViewById(R.id.buttonRegister);
        registerButton.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v){
                _message = "";
                String email = getStringFromView(R.id.editTextEmail,0);
                String phone = getStringFromView(R.id.editTextPhone,0);
                String firstName = getStringFromView(R.id.editTextFirstName,0);
                String lastName = getStringFromView(R.id.editTextLastName,0);
                String password = getStringFromView(R.id.editTextPassword,0);
                String governmentIDNumber = getStringFromView(R.id.editTextGovernmentIDNumber,0);
                String isVolunteer = getStringFromView(R.id.checkBoxVolunteer,1);
                String volunteeringField = getStringFromView(R.id.spinnerVolunteeringField,2);
                String governmentIDType = getStringFromView(R.id.spinnerGovernmentIDType,2);
                Spinner spinnerGovernmentIDType = findViewById(R.id.spinnerGovernmentIDType);
                if(email.isEmpty()){
                    showEmpty(R.id.editTextEmail,"Email");
                }
                if(!email.contains("@")){
                    _message += "The format of Email is incorrect.\n";
                }
                if(firstName.isEmpty()){
                    showEmpty(R.id.editTextFirstName,"First Name");
                }
                if(lastName.isEmpty()){
                    showEmpty(R.id.editTextLastName,"Last Name");
                }
                if(governmentIDNumber.isEmpty()){
                    showEmpty(R.id.editTextGovernmentIDNumber,"Government ID Number");
                }
                System.out.println(spinnerGovernmentIDType.getSelectedItemPosition());
                if(spinnerGovernmentIDType.getSelectedItemPosition() == 0){
                    _message += "Please select government ID type.\n";
                }
                textViewMessage.setText(_message);
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

    protected String getStringFromView(int ID,int kind){
        String text = "";
        switch (kind){
            case 0:
                EditText editText = findViewById(ID);
                text = editText.getText().toString();
                break;
            case 1:
                CheckBox checkBox = findViewById(ID);
                text = checkBox.getText().toString();
                break;
            case 2:
                Spinner spinner = findViewById(ID);
                text = spinner.getContext().toString();
        }

        return text;
    }

    private void showEmpty(int ID,String name){
        //final TextView textViewMessage = findViewById(R.id.textViewMessage);
        //textViewMessage.setText(name+" is empty.");
        _message += name+" is empty.\n";
        EditText editText = findViewById(ID);
        editText.setText(name+"*");
    }

}
