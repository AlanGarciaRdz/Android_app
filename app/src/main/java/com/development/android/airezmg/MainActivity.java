package com.development.android.airezmg;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.multidex.MultiDex;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.development.android.airezmg.drawer.CustomAdapter;
import com.development.android.airezmg.drawer.RowItem;
import com.development.android.airezmg.model.FavPlace;
import com.development.android.airezmg.model.GPSTracker;
import com.development.android.airezmg.model.LatestLocalFiles;
import com.development.android.airezmg.util.HttpTask;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.kml.KmlLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationManager lm;
    private GPSTracker tracker;

    private static final String LOG_TAG = "MainActivity";

    String[] menutitles;
//    TypedArray menuIcons;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private List<RowItem> rowItems;
    private CustomAdapter adapter;
    private LinearLayout mContentView;
    private Marker myMarker = null;

    // SEARCH BAR
    private EditText etSearch;
    private Button bSearch;

    private HttpTask kmlTask;
    private HttpTask stationTask;
    //    private static final String kmlURL = "http://149.56.132.38/kml/test.kml";
//    http://149.56.132.38/kml/Outkml/IMECA_PM10_6.kml
    private static final String kmlURL = "http://149.56.132.38/kml/Outkml/IMECA_PM10_6.kml";
//    private static final String dataURL = "http://149.56.132.38:3000/pm10?";
    private static final String dataURL = "http://149.56.132.38:3001/all?";
    private static final String KMLPREF = "kmlpref";
    private static final String KMLPREFJSON = "latestKML";
    private static final String KMLPREFSEL = "selectedSource";
    private static final int GPS_PERMISSION = 111111;

//    private static final String FAVPREF = "favpref";
    private static final String FAVPREFARRAY = "favprefarray";
    private static JSONArray favArray;

    private static String latestSelected = LatestLocalFiles.IMECA;
    private static LatestLocalFiles latestKMLPrefs;
    private static SharedPreferences mPrefs;

    private static boolean shouldLoadKML = false;
    private static boolean shouldLoadStations = false;
    private static String latestKMLPath = "";
    private static String latestStationPath = "";
    private static boolean waitForMapToLoadKML = false;
    private static boolean waitForMapToLoadStations = false;

    private static int CONTAINER_ID = R.id.fragmentContainer;
    private SupportMapFragment mapFragment;

    private static KmlLayer currentKMLLayer;
    private static KmlLayer stationLayer;

    PlaceAutocompleteFragment autocompleteFragment;

    ProgressDialog mDialog;


    @TargetApi(23)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);

        setContentView(R.layout.main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.hideOverflowMenu();

        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
//        decorView.setSystemUiVisibility(uiOptions);
        mContentView = (LinearLayout) findViewById(R.id.maincontainer);

        if (mContentView != null) {
//            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }

        mTitle = mDrawerTitle = getTitle();
        menutitles = getResources().getStringArray(R.array.titles);
//        menuIcons = getResources().obtainTypedArray(R.array.icons);
        int icons[] = {R.mipmap.h_icon_marker, R.mipmap.h_icon_star, R.mipmap.legalicon};

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.slider_list);

        rowItems = new ArrayList<>();
        for (int i = 0; i < menutitles.length; i++) {
//            RowItem item = new RowItem(menutitles[i], menuIcons.getResourceId(i, -1));
            RowItem item = new RowItem(menutitles[i], icons[i]);
            rowItems.add(item);
        }

//        menuIcons.recycle();
        adapter = new CustomAdapter(getApplicationContext(), rowItems, R.layout.list_item);

        mDrawerList.setAdapter(adapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i) {
                    case 0: // Opciones
                        Toast.makeText(MainActivity.this, "En Construcción", Toast.LENGTH_SHORT).show();
                        break;
                    case 1: // Favoritos
                        showFavoritePopup();
                        break;
                    case 2: // Legal
                        showLegalPopUp();
                        break;
                }
            }
        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open_drawer, R.string.closed_drawer) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                drawerView.bringToFront();
                mDrawerLayout.requestLayout();
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);


        //! IMPORTANT MAP STUFF !!
        mPrefs = getSharedPreferences(KMLPREF, MODE_PRIVATE);
        latestKMLPrefs = new LatestLocalFiles();
        kmlTask = HttpTask.getKMLPath(this, kmlURL, kmlListener);
        stationTask = HttpTask.getStationsMarkers(this, stationListener);

        tracker = new GPSTracker();
        boolean shouldRequest = tracker.checkPermissions(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        View searchView = autocompleteFragment.getView();
        searchView.setBackgroundColor(Color.WHITE);
        searchView.getBackground().setAlpha(200);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                search(place);
//                Log.i(LOG_TAG, "Place: " + place.getName().toString());
            }

            @Override
            public void onError(Status status) {
//                Log.i(LOG_TAG, "An error occurred: " + status);
            }
        });

        if (shouldRequest && Build.VERSION.SDK_INT >= 23) { // if version is less than 23, the app will automatically ask for permission

            Snackbar.make(mContentView, "Tu ubicación es necesaria para poder mostrar información veráz", Snackbar.LENGTH_INDEFINITE).setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, GPS_PERMISSION);
                }
            }).show();
            return;
        } else {
            tracker.initLocationService(this);
            mapFragment.getMapAsync(this);
        }
    }


    protected void search(Place place) {
        LatLng latLng = place.getLatLng();
        moveCurrentMarker(latLng, true);

    }

    private void moveCurrentMarker(LatLng latlng, boolean moveCam){
        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latlng);
        String name ="Mi posición";
        Geocoder geocoder = new Geocoder(MainActivity.this);
        try {
            List<Address> addressList = geocoder.getFromLocation(latlng.latitude,latlng.longitude,2);
            Address address = addressList.get(0);
            name = getAdressName(address);
        } catch (IOException e) {
            e.printStackTrace();
        }
        markerOptions.title(name);

        if (mMap != null) {
            myMarker.remove();
            myMarker = mMap.addMarker(markerOptions);
            if(moveCam) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
            if (currentKMLLayer == null) {
                try {
                    getKMLFileFromPath(latestKMLPath);
                } catch (Exception exc) {
//                    Log.w(LOG_TAG, "Error parsing KML Layer");
                }
            }
        }
    }

    private void getKMLPreferences() {
//        Log.i(LOG_TAG, "Getting latesKML preferences from system");
        shouldLoadKML = false;
        shouldLoadStations = false;
        latestKMLPath = "";

        String jstring = mPrefs.getString(KMLPREFJSON, null);

        if (jstring == null) {
//            Log.w(LOG_TAG, "Preferences not found");
            shouldLoadKML = true;
            return;
        }
        latestKMLPrefs = LatestLocalFiles.fromJSONString(jstring);
        latestSelected = mPrefs.getString(KMLPREFSEL, "IMECA");
        long lastDate = latestKMLPrefs.getLatestDate();
        long now = System.currentTimeMillis();
        long diff = now - lastDate;
        long diffMinutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        String url = getURLForType(latestKMLPrefs, latestSelected);
        kmlTask.setmURLoc(url);
        if (diffMinutes >= 60) {
            shouldLoadKML = true;
//            Log.i(LOG_TAG, "kml is older than 60 min");
        } else {
            latestKMLPath = latestKMLPrefs.getLatestSelected(latestSelected);
            if (latestKMLPath == null) {
                shouldLoadKML = true;
//                Log.i(LOG_TAG, "no path recorded for selected type");
            } else {
                shouldLoadKML = false;
//                Log.i(LOG_TAG, "path to previous kml \n" + latestKMLPath);
            }
        }
        latestStationPath = latestKMLPrefs.getLatestStations();
        if(latestStationPath == null || latestStationPath == ""){
            shouldLoadStations = true;
        }

        String favArrayString = mPrefs.getString(FAVPREFARRAY, "[]");
        try {
            favArray = new JSONArray(favArrayString);
        }catch (JSONException ex){
            favArray = new JSONArray();
        }
    }

    private String getURLForType(LatestLocalFiles kmlFiles, String selected) {
        switch (selected) {
            case LatestLocalFiles.IMECA:
                return kmlURL;
            case LatestLocalFiles.CONCE:
                return kmlURL;
            case LatestLocalFiles.RESPI:
                return kmlURL;
            default:
                return kmlURL;
        }
    }


    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
//                    Log.i(LOG_TAG, "Executing task from click kmlListener!");
                    kmlTask.execute(latestSelected);
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    // Que hacer?
                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        getKMLPreferences();
        if (shouldLoadKML) {
            checkWifiStateAndDownloadKML();
        } else {
            // wait for map to init
            waitForMapToLoadKML = true;
        }
        if (shouldLoadStations) {
            checkWifiStateAndDownloadStations();
        }else{
            waitForMapToLoadStations = true;
        }

    }

    private void checkWifiStateAndDownloadKML(){
        if(isWifiEnabled()){
            kmlTask.execute(latestSelected);
        }else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Alerta");
            builder.setMessage("No se ha detectado una conexión WiFi, deseas descargar la información utilizando tu plan de datos?");
            builder.setPositiveButton("Si", dialogClickListener);
            builder.setNegativeButton("No", dialogClickListener);

            builder.create().show();
        }
    }

    private void checkWifiStateAndDownloadStations(){
        if(!isWifiEnabled()){
            Toast.makeText(MainActivity.this, "Descargando estaciones de monitoreo", Toast.LENGTH_SHORT).show();
        }
        stationTask.execute(LatestLocalFiles.STATIONS);
    }

    private boolean isWifiEnabled() {
        SupplicantState supState;
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supState = wifiInfo.getSupplicantState();
        if (supState.equals(SupplicantState.ASSOCIATED) || supState.equals(SupplicantState.COMPLETED)) {
//                Log.i(LOG_TAG, "Executing task because WIFI enabled!");
            return true;
        } else {
            return false;
        }
    }

    private HttpTask.ResultListener stationListener = new HttpTask.ResultListener() {
        @Override
        public void gotHttpResult(String path, boolean error) {
            if(!error){
                SharedPreferences.Editor editor = mPrefs.edit();
                latestKMLPrefs.setLatestStations(path);
                JSONObject jsonObj = LatestLocalFiles.toJSON(latestKMLPrefs);
                editor.putString(KMLPREFJSON, jsonObj.toString());
                editor.commit();
                if(mMap != null) {
                    try {
                        stationLayer = getKMLFileFromPath(path);
                        stationLayer.setMap(mMap);
                        stationLayer.addLayerToMap();
                    } catch (Exception e) {

                    }
                } else{
                    waitForMapToLoadStations = true;
                }
            }else{
                Toast.makeText(MainActivity.this, "Las estaciones de monitoreo no se pueden mostrar", Toast.LENGTH_SHORT).show();
            }
        }
    };
    
    private HttpTask.ResultListener kmlListener = new HttpTask.ResultListener() {
        @Override
        public void gotHttpResult(String path, boolean error) {
            if (!error) {
//                Log.i(LOG_TAG, "Writing new latesKML preferences");
                saveLocalKMLPreferences(path, true);
                if(mMap != null) {
                    try {
                        currentKMLLayer = getKMLFileFromPath(path);
                        currentKMLLayer.setMap(mMap);
                        currentKMLLayer.addLayerToMap();
                    } catch (Exception e) {

                    }
                }else{
                    waitForMapToLoadKML = true;
                }
            }
        }
    };

    private void saveLocalKMLPreferences(String path, boolean saveDate) {
        SharedPreferences.Editor editor = mPrefs.edit();
        latestKMLPrefs.setLatestPath(path, latestSelected);
        if(saveDate)
            latestKMLPrefs.setLatestDate(System.currentTimeMillis());
        JSONObject jsonObj = LatestLocalFiles.toJSON(latestKMLPrefs);
        editor.putString(KMLPREFJSON, jsonObj.toString());
        editor.commit();
    }

    private void saveLocalFavPreferences(){
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(FAVPREFARRAY, favArray.toString());
        editor.commit();
    }

    private KmlLayer getKMLFileFromPath(String path) {
//        Log.i(LOG_TAG, "Getting stored KML");
        File kmlFile = new File(path);
        FileInputStream fis = null;
        KmlLayer layer = null;
        if (mMap == null) {
//            Log.w(LOG_TAG, "Map not initialized");
            return layer;
        }
        try {
            fis = new FileInputStream(kmlFile);
            layer = new KmlLayer(mMap, fis, getApplicationContext());
//            Log.i(LOG_TAG, "KML Layer added to map!");
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e) {
//            e.printStackTrace();
        } finally {
//            mDialog.dismiss();
            return layer;
        }
    }

    private String getAdressName(Address add){
        String name = "";
        if(add.getThoroughfare() != null && !add.getThoroughfare().equals("null")){
            name = add.getThoroughfare();
        }
        if(add.getSubThoroughfare() != null && !add.getSubThoroughfare().equals("null")){
            name += " " + add.getSubThoroughfare();
        }
        return name;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the MainActivity.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.setMyLocationEnabled(true);
            // add padding to move the location button a bit down
            mMap.setPadding(0, 150, 0, 0); // 20 px is the height of the search bar
        } catch (SecurityException ex) {
//            Log.w(LOG_TAG, "Could not enable My Location");
        }

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                moveCurrentMarker(latLng, false);
            }
        });

        Location current = tracker.getLocation();
        LatLng latLng = null;

        Geocoder geocoder = new Geocoder(this);
        if (current == null) {
            // get "Guadalajara" location
            List<Address> addressList = null;
            try {
                addressList = geocoder.getFromLocationName("Guadalajara", 1);
                Address address = addressList.get(0);
                latLng = new LatLng(address.getLatitude(), address.getLongitude());
                myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Guadalajara"));
//                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            latLng = new LatLng(current.getLatitude(), current.getLongitude());
            String name ="Mi posición";
            try {
                List<Address> addressList = geocoder.getFromLocation(current.getLatitude(), current.getLongitude(),2);
                Address address = addressList.get(0);
                name = getAdressName(address);
            } catch (IOException e) {
                e.printStackTrace();
            }
            myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title(name));
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (myMarker != null && marker.equals(myMarker)) {
//                    createCustomDialog(marker.getTitle());
                    String completeURL;
                    if (myMarker.getPosition() != null) {
                        completeURL = dataURL + "lat=" + myMarker.getPosition().latitude + "&lon=" + myMarker.getPosition().longitude;
                    } else {
                        completeURL = dataURL + "lat=20.660291&lon=-103.396454"; // DUMMY
                    }
                    HttpTask dataTask = HttpTask.getStatusData(MainActivity.this, completeURL, statusListener);
                    dataTask.execute("");

                }
                return true;
            }
        });

        if (latLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            if (waitForMapToLoadKML) {
//                Log.i(LOG_TAG, "Waited for map");
                if(latestKMLPath != null) {
                    try {
                        currentKMLLayer = getKMLFileFromPath(latestKMLPath);
                        currentKMLLayer.setMap(mMap);
                        currentKMLLayer.addLayerToMap();
                    } catch (Exception e) {

                    }
                }
                else
                    checkWifiStateAndDownloadKML();
            }
            if(waitForMapToLoadStations){
                if(latestStationPath != null){
                    try {
//                        stationLayer = new KmlLayer(mMap, R.raw.estacionesdemonitoreo, MainActivity.this);
                        stationLayer = getKMLFileFromPath(latestStationPath);
                        stationLayer.setMap(mMap);
                        stationLayer.addLayerToMap();
                    } catch (Exception e) {
                        Log.w(LOG_TAG, e);
                    }
                }else{
                    checkWifiStateAndDownloadStations();
                }
            }
        }
//        } else
//            Toast.makeText(this, "No hay posicion?", Toast.LENGTH_SHORT).show();

    }


    private String[] names = {"PM10","SOX","CO","O3","NOX"};
    private HttpTask.ResultListener statusListener = new HttpTask.ResultListener() {
        @Override
        public void gotHttpResult(String resultString, boolean error) {
            if(error){
                Toast.makeText(MainActivity.this, "Error de comunicación con el servidor", Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<TreeMap> allContaminants = new ArrayList<>();

            Calendar c = Calendar.getInstance();
            int hourofday = c.get(Calendar.HOUR_OF_DAY);
            try {
                JSONArray bigarray = new JSONArray(resultString);
                JSONObject allJObj = bigarray.getJSONObject(0);
                JSONArray allConts = allJObj.getJSONArray("All");
                for (int contaminante = 0; contaminante < allConts.length() ; contaminante++){
                    JSONObject jcontaminant = allConts.getJSONObject(contaminante);
                    JSONArray jarray = jcontaminant.getJSONArray(names[contaminante]);
                    TreeMap<Integer, JSONObject> values = new TreeMap<>();
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject obj = jarray.getJSONObject(i);
                        int time = obj.optInt("tiempo");
                        String qlty = obj.optString("calidad");
                        double conc = obj.optDouble("conc");
                        double imeca = obj.optDouble("imeca");

                        int futurehour = hourofday + time;
                        JSONObject data = new JSONObject();
                        data.put("conc", conc);
                        data.put("imeca", imeca);
                        data.put("calidad", qlty);

                        values.put(futurehour, data);
                    }
                    allContaminants.add(values);
                }

            } catch (JSONException e) {
                Log.i(LOG_TAG, "error obteniendo valores del server");
            }
            createCustomDialog(allContaminants, hourofday);
        }
    };

    private void showLegalPopUp() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.legal_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

    private void showFavoritePopup() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.favorite_list_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        TextView nofav = (TextView)dialog.findViewById(R.id.fav_nofavTV);
        final ListView favlist = (ListView)dialog.findViewById(R.id.fav_listView);
        if(favArray.length() == 0){
            favlist.setVisibility(View.INVISIBLE);
            nofav.setVisibility(View.VISIBLE);
        }else{
            favlist.setVisibility(View.VISIBLE);
            nofav.setVisibility(View.INVISIBLE);
            ArrayList<RowItem> favRowItems = new ArrayList<>();
            try {
                for (int i = 0; i < favArray.length(); i++) {
                    String name = FavPlace.fromJSON(favArray.getJSONObject(i)).getName();
                    RowItem item = new RowItem(name,0); // pass 0 to leave default icon
                    favRowItems.add(item);
                }
            }catch (JSONException ex){

            }
            final CustomAdapter adapter = new CustomAdapter(getApplicationContext(), favRowItems, R.layout.list_item_fav);
            favlist.setAdapter(adapter);
            favlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    try {
                        FavPlace favp = FavPlace.fromJSON(favArray.getJSONObject(i));
                        LatLng latlong = new LatLng(favp.getLat(), favp.getLon());
                        moveCurrentMarker(latlong, true);
                        dialog.dismiss();
                        mDrawerLayout.closeDrawers();
                    }catch (JSONException ex){

                    }
                }
            });
            favlist.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l) {
                    favArray.remove(position);
                    saveLocalFavPreferences();
                    adapter.notifyDataSetChanged();
                    favlist.invalidate();
                    dialog.dismiss();
                    return true;
                }
            });
        }
        dialog.show();
    }

    private void createCustomDialog(ArrayList<TreeMap> allvalues, int key) {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.data_popup);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                LinearLayout container = (LinearLayout) dialog.findViewById(R.id.popuptextContainer);
                int maxWidth = container.getWidth();
            }
        });

        TextView tvDataNow = (TextView) dialog.findViewById(R.id.tvDataPopUpNow);
        TextView tvDataPlus1 = (TextView) dialog.findViewById(R.id.tvDataPopUpPlus1);
        TextView tvDataPlus2 = (TextView) dialog.findViewById(R.id.tvDataPopUpPlus2);
        ArrayList<TextView> tvs = new ArrayList<>();
        tvs.add(tvDataNow);
        tvs.add(tvDataPlus1);
        tvs.add(tvDataPlus2);

        TreeMap<Integer, JSONObject> pm10Values = allvalues.get(0);
        for (TextView tv : tvs){
            JSONObject data = pm10Values.get(key);
            String hour = ""+key;
            if(key >= 24)
                hour = ""+(key-24);
            tv.setText(hour + ":00 hrs\n" + data.optString("calidad"));
            key++;
        }

        ArrayList<TreeMap> passableArrayExtra = new ArrayList<>();
        for(TreeMap<Integer, JSONObject> map : allvalues) {
            TreeMap<Integer, String> passableExtra = new TreeMap<>();
            for (Map.Entry<Integer, JSONObject> entri : map.entrySet()) {
                passableExtra.put(entri.getKey(), entri.getValue().toString());
            }
            passableArrayExtra.add(passableExtra);
        }

        final ArrayList<TreeMap> finalPassableArrayExtra = passableArrayExtra;

        RelativeLayout container = (RelativeLayout) dialog.findViewById(R.id.popupContainer);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra("extra_data",finalPassableArrayExtra);
                startActivity(i);
            }
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_app, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.menu_info:
                final Dialog dialogInfo = new Dialog(MainActivity.this);
                dialogInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
                Drawable back;
                if (Build.VERSION.SDK_INT >= 21) {
                    back = getResources().getDrawable(R.mipmap.infobackground, null);
                }else{
                    back = getResources().getDrawable(R.mipmap.infobackground);
                }
                dialogInfo.getWindow().setBackgroundDrawable(back);
                dialogInfo.setContentView(R.layout.info_popup);
                dialogInfo.show();
                return true;
            case R.id.menu_fav:
                final Dialog dialogFav = new Dialog(MainActivity.this);
                dialogFav.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogFav.setContentView(R.layout.favorite_save_popup);
                final TextView favTV = (TextView)dialogFav.findViewById(R.id.fav_editText);
                favTV.requestFocus();
                if(myMarker != null){
                    favTV.setText(myMarker.getTitle());
                }

                Button fav_save = (Button)dialogFav.findViewById(R.id.fav_buttonSave);
                fav_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String placeName = favTV.getText().toString();
                        FavPlace place = new FavPlace();
                        place.setName(placeName);
                        place.setLat(myMarker.getPosition().latitude);
                        place.setLon(myMarker.getPosition().longitude);
                        JSONObject placeobj = place.getFavObj();
                        favArray.put(placeobj);
                        saveLocalFavPreferences();
                        dialogFav.dismiss();
                    }
                });
                Button fav_cancel = (Button)dialogFav.findViewById(R.id.fav_buttonCancel);
                fav_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogFav.dismiss();
                    }
                });
                dialogFav.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogFav.show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // if nav drawer is opened, hide the action items
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
//        menu.findItem(R.id.action_settings).setVisible(true);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == GPS_PERMISSION) {
            if (mapFragment != null)
                mapFragment.getMapAsync(this);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
