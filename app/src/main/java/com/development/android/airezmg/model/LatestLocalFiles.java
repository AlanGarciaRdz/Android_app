package com.development.android.airezmg.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by penaen on 16/06/2016.
 */
public class LatestLocalFiles {

    public static final String FILE = "PREF_FILE";
    public static final String IMECA = "IMECA";
    public static final String RESPI = "RESPI";
    public static final String CONCE = "CONCE";
    public static final String STATIONS = "STATIONS";
    private static final String DATE = "DATE";

    private String latestImeca;
    private String latestRespira;
    private String latestConcentracion;
    private String latestStations;
    private Date latestDate;

    public LatestLocalFiles(){
        this.latestConcentracion = "";
        this.latestImeca = "";
        this.latestRespira = "";
        this.latestStations = "";
        this.latestDate = new Date();
    }
    public static LatestLocalFiles fromJSONString(String jsonString){
        LatestLocalFiles kmlFiles = null;
        try {
            kmlFiles = new LatestLocalFiles();
            JSONObject obj = new JSONObject(jsonString);
            kmlFiles.setLatestConcentracion(obj.optString(CONCE));
            kmlFiles.setLatestImeca(obj.optString(IMECA));
            kmlFiles.setLatestRespira(obj.optString(RESPI));
            kmlFiles.setLatestDate(obj.optLong(DATE));
            kmlFiles.setLatestStations(obj.optString(STATIONS));
        }catch (Exception e){
            return null;
        }
        return kmlFiles;
    }

    public static JSONObject toJSON(LatestLocalFiles lkmlf){
        JSONObject obj = null;
        try {
            obj = new JSONObject();
            obj.put(CONCE, lkmlf.getLatestConcentracion());
            obj.put(IMECA, lkmlf.getLatestImeca());
            obj.put(RESPI, lkmlf.getLatestRespira());
            obj.put(DATE, lkmlf.getLatestDate());
            obj.put(STATIONS, lkmlf.getLatestStations());
        }catch (JSONException je){
            return null;
        }
        return obj;
    }

    public long getLatestDate() {
        return latestDate.getTime();
    }

    public void setLatestDate(long millisecondLong) {
        Date d = new Date(millisecondLong);
        this.latestDate = d;
    }

    public String getLatestImeca() {
        return latestImeca;
    }

    public void setLatestImeca(String latestImeca) {
        this.latestImeca = latestImeca;
    }

    public String getLatestConcentracion() {
        return latestConcentracion;
    }

    public void setLatestConcentracion(String latestConcentracion) {
        this.latestConcentracion = latestConcentracion;
    }

    public String getLatestRespira() {
        return latestRespira;
    }

    public void setLatestRespira(String latestRespira) {
        this.latestRespira = latestRespira;
    }

    public String getLatestSelected(String selected) {
        switch(selected){
            case IMECA:
                return getLatestImeca();
            case CONCE:
                return getLatestConcentracion();
            case RESPI:
                return getLatestRespira();
            default:
                return null;
        }
    }

    public void setLatestPath(String path, String latestSelected) {
        switch(latestSelected){
            case IMECA:
                setLatestImeca(path);
                break;
            case CONCE:
                setLatestConcentracion(path);
                break;
            case RESPI:
                setLatestRespira(path);
                break;
        }
    }


    public String getLatestStations() {
        return latestStations;
    }

    public void setLatestStations(String latestStations) {
        this.latestStations = latestStations;
    }

}
