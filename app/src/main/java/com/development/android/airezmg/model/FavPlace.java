package com.development.android.airezmg.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by penaen on 11/09/2016.
 */
public class FavPlace {

    private static final String JLAT ="lat";
    private static final String JLON ="lon";
    private static final String JNAME ="names";

    private double lat;
    private double lon;
    private String name;

    public FavPlace(double lat, double lon, String name) {
        this.lat = lat;
        this.lon = lon;
        this.name = name;
    }

    public FavPlace() {
        this.lat = 0;
        this.lon = 0;
        this.name = "";
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getFavObj(){
        JSONObject obj = new JSONObject();
        try {
            obj.put(JLAT, lat);
            obj.put(JLON, lon);
            obj.put(JNAME, name);
        }catch (JSONException e){
            return null;
        }
        return obj;
    }

    public static FavPlace fromJSON(JSONObject obj){
        FavPlace p = new FavPlace();
        p.setName(obj.optString(JNAME));
        p.setLat(obj.optDouble(JLAT));
        p.setLon(obj.optDouble(JLON));
        return p;
    }
}
