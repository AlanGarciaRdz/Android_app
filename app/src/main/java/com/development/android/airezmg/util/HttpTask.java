package com.development.android.airezmg.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;

import com.development.android.airezmg.R;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by penaen on 15/06/2016.
 */
public class HttpTask extends AsyncTask<String, Integer, String> {

    private String mURLoc;
    private ProgressDialog mDialog;
    private Context mContext;
    private PowerManager.WakeLock mWakeLock;
    private ResultListener mListener;
    private boolean error;
    private boolean showDialog;

    private static final String STATIONS_URL = "http://149.56.132.38/kml/Estaciones_de_Monitoreo.kmz";

    private enum Type{
        kml,
        status,
        stations
    }
    private Type callType;

    public interface ResultListener {
        public void gotHttpResult(String path, boolean error);
    }

    private static HttpTask initTask(Context context, String mURLoc, ResultListener listener, boolean showDialog) {
        HttpTask task = new HttpTask();
        task.mDialog = new ProgressDialog(context);
        task.mDialog.setIndeterminate(true);
        task.mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        task.mDialog.setCancelable(true);
        task.mURLoc = mURLoc;
        task.mContext = context;
        task.mListener = listener;
        task.showDialog = showDialog;
        return task;
    }

    public static HttpTask getKMLPath(Context context, String mURLoc, ResultListener listener) {
        HttpTask task = initTask(context,mURLoc,listener, true);
        task.callType = Type.kml;
        task.mDialog.setMessage(context.getResources().getString(R.string.downloadingkml));
        return task;
    }

    public static HttpTask getStatusData(Context context, String mURLoc, ResultListener listener) {
        HttpTask task = initTask(context,mURLoc,listener, true);
        task.callType = Type.status;
        task.mDialog.setMessage(context.getResources().getString(R.string.downloadingdata));
        task.mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        return task;
    }

    public static HttpTask getStationsMarkers(Context context, ResultListener listener){
        HttpTask task = initTask(context, STATIONS_URL, listener, false);
        task.callType = Type.stations;
        return task;
    }


    @Override
    protected void onPreExecute() {
        error = false;
        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
        mDialog.setCancelable(false);
        if(showDialog)
            mDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        if(showDialog)
            mDialog.dismiss();
        mListener.gotHttpResult(result, error);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mDialog.setIndeterminate(false);
        mDialog.setMax(100);
        mDialog.setProgress(progress[0]);
    }

    @Override
    protected String doInBackground(String... providers) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        String returnString = "";

        try {
            URL murloc = new URL(mURLoc);

            connection = (HttpURLConnection) murloc.openConnection();
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                error = true;
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            switch (callType){
                case status:
                    BufferedReader r = new BufferedReader(new InputStreamReader(input));
                    StringBuilder totalbuilder = new StringBuilder();
                    String line;
                    while ((line = r.readLine()) != null) {
                        totalbuilder.append(line).append('\n');
                    }
                    returnString = totalbuilder.toString();
                    break;
                case kml:
                case stations:
                    returnString = mContext.getFilesDir() + "/kml_" + providers[0] + "." + (callType.equals(Type.kml)?"kml":"kmz");
                    output = new FileOutputStream(returnString);


                    while ((count = input.read(data)) != -1) {
                        // allow canceling with back button
                        if (isCancelled()) {
                            input.close();
                            error = true;
                            return "Canceled";
                        }
                        total += count;
                        // publishing the progress....
                        if (fileLength > 0) // only if total length is known
                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                    break;
            }

        } catch (Exception e) {
            error = true;
//            if(callType.equals(Type.status)) // DUMMY DATA
//                return "[{'PM10_5': 'Buena', 'PM10_4': 'Buena', 'PM10_7': 'Buena', 'PM10_6': 'Buena', 'PM10_1': 'Buena', 'PM10_0': 'Buena', 'PM10_3': 'Buena', 'PM10_2': 'Buena'}]";
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return returnString;
    }

    public String getmURLoc() {
        return mURLoc;
    }

    public void setmURLoc(String mURLoc) {
        this.mURLoc = mURLoc;
    }
}
