package com.example.androidase_.object_classes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonUserRegistrationPOJO {
    public String firstName;
    public String lastName;
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

            JSONObject nameJson = new JSONObject();
            nameJson.put("FirstName", this.firstName);
            nameJson.put("LastName", this.lastName);

            json.put("Name", nameJson);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public JSONObject objToJson(String emailId, String password) {
        JSONObject json = new JSONObject();
        try {
            // Convert POJO to JSON
            json.put("LoginId", emailId);
            json.put("Password", password);
            json.put("Channel", "Android");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
