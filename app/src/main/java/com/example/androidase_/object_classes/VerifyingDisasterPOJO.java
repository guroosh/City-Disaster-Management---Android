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
    public ArrayList<ArrayList<LatLng>> exitEntryRoutes;


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
            JSONArray exitEntryList = new JSONArray();
            for (ArrayList<LatLng> list : exitEntryRoutes) {
                JSONArray innerList = new JSONArray();
                for (LatLng latLng : list) {
                    JSONObject position = new JSONObject();
                    position.put("lat", latLng.latitude);
                    position.put("lng", latLng.longitude);
                    innerList.put(position);
                }
                exitEntryList.put(innerList);
            }
            json.put("ExitEntryRoutes", exitEntryList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
