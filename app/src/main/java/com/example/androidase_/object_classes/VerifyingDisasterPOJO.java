package com.example.androidase_.object_classes;

import org.json.JSONException;
import org.json.JSONObject;

public class VerifyingDisasterPOJO {
    public String referenceId;
    public String latitude;
    public String longitude;
    public String landmark;
    public String verifiedTime;
    public String verifiedBy;
    public boolean isInfoTrue;
    public String radius;
    public String scale;

    public JSONObject objToJson() {
        JSONObject json = new JSONObject();
        try {
            // Convert POJO to JSON
            json.put("ReferenceId", this.referenceId);
            json.put("Latitude", this.latitude);
            json.put("Longitude", this.longitude);
            json.put("Landmark", this.landmark);
            json.put("VerifiedTime", this.verifiedTime);
            json.put("VerifiedBy", this.verifiedBy);
            json.put("IsInfoTrue", this.isInfoTrue);
            json.put("Radius", this.radius);
            json.put("Scale", this.scale);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
