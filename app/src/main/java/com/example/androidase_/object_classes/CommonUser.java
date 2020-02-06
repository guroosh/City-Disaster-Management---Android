package com.example.androidase_.object_classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonUser {
    public Name name;
    public String emailId;
    public String password;
    public String phoneNumber;
    public String governmentIdType;
    public String governmentIdNumber;
    public boolean isVolunteering;
    public String volunteeringField;

    public JSONObject objToJson() {
        JSONObject json = new JSONObject();
        try {
            // Convert POJO to JSON
            json.put("EmailId", this.emailId);
            json.put("Password", this.password);
            json.put("GovernmentIdNumber", this.governmentIdNumber);
            json.put("PhoneNumber", this.phoneNumber);
            json.put("IsVolunteering", this.isVolunteering);
            json.put("GovernmentIdType", this.governmentIdType);
            json.put("VolunteeringField", this.volunteeringField);
            json.put("Name", this.getNameString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject objToJson(String emailId, String password) {
        JSONObject json = new JSONObject();
        try {
            // Convert POJO to JSON
            json.put("EmailId", emailId);
            json.put("Password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void setName(String firstName, String lastName) {
        this.name.firstName = firstName;
        this.name.lastName = lastName;
    }

    public String getNameString() {
        return this.name.firstName + " " + this.name.lastName;
    }
}

class Name {
    public String firstName;
    public String lastName;
}