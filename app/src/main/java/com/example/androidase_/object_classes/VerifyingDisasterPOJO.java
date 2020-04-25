package com.example.androidase_.object_classes;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class VerifyingDisasterPOJO {
    public String referenceId;
    public double latitude;
    public double longitude;
    public String landmark;
    public String verifiedTime;
    public String verifiedBy;
    public boolean isInfoTrue;
    public double radius;
    public String scale;
    public boolean MedicalAssistanceRequired = false;
    public boolean TrafficPoliceAssistanceRequired = false;
    public boolean FireBrigadeAssistanceRequired = false;
    public String OtherResponseTeamRequired = "";
    //    public ArrayList<LatLng> policeRoute;
    public ArrayList<LatLng> fireRoute;
//    public ArrayList<LatLng> ambulanceRoute;

    public JSONObject objToJson() {
        JSONObject json = new JSONObject();
        try {
            // Convert POJO to JSON
            json.put("ReferenceCode", this.referenceId);
            json.put("Latitude", this.latitude);
            json.put("Longitude", this.longitude);
            json.put("Landmark", this.landmark);
            json.put("VerifiedTime", this.verifiedTime);
            json.put("VerifiedBy", this.verifiedBy);
            json.put("Radius", this.radius);
            json.put("ScaleOfDisaster", this.scale);
            json.put("IsInfoTrue", this.isInfoTrue);
            json.put("MedicalAssistanceRequired", this.MedicalAssistanceRequired);
            json.put("TrafficPoliceAssistanceRequired", this.TrafficPoliceAssistanceRequired);
            json.put("FireBrigadeAssistanceRequired", this.FireBrigadeAssistanceRequired);
            json.put("OtherResponseTeamRequired", this.OtherResponseTeamRequired);
            JSONArray fireRouteJSON = new JSONArray();
            for (LatLng latLng : fireRoute) {
                JSONObject position = new JSONObject();
                position.put("lat", latLng.latitude);
                position.put("lng", latLng.longitude);
                fireRouteJSON.put(position);
            }
            json.put("FireRoute", fireRouteJSON);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
