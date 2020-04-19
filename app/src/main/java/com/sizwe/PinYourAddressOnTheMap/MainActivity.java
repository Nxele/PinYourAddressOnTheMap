package com.sizwe.PinYourAddressOnTheMap;
import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import com.huawei.hms.maps.CameraUpdate;
import com.huawei.hms.maps.CameraUpdateFactory;
import com.huawei.hms.maps.HuaweiMap;
import com.huawei.hms.maps.MapFragment;
import com.huawei.hms.maps.MapView;
import com.huawei.hms.maps.OnMapReadyCallback;
import com.huawei.hms.maps.model.BitmapDescriptorFactory;
import com.huawei.hms.maps.model.CameraPosition;
import com.huawei.hms.maps.model.LatLng;
import com.huawei.hms.maps.model.Marker;
import com.huawei.hms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    //LOG TAG AND ACCESS DECLARATION
    private static final String TAG = "";
    private static final String ACCESS_FINE_LOCATION = "";
    private static final String ACCESS_WIFI_STATE = "";

    //MAP DECLARATION
    private MapFragment mMapFragment;
    private HuaweiMap myMap;
    private Marker pinMarker;
    private LatLng userCurrentLocation;
    private Marker myLocationMarker = null;

    //HUAWEI TECHNOLOGIES SOUTH AFRICA FINAL DEFAULT VALUES
    private final LatLng huawei_SA_latLng = new LatLng(-26.064738,28.0902398);
    private final String huawei_SA_address = "Woodmead Ext 20, Sandton, City of Johannesburg, Gauteng, South Africa";

    //HUAWEI MOBILE SERVES API VALUES
    private final String API_key = "CV7BndPX5tuyjCq+sl7dHrlNxw9xnEPTv9q6s84nbr4c8Fy+Ka9SSayA2bkSTqnwRwvcXl2TJV53iRK9DH88/dCoh3F9";
    private final String url ="https://siteapi.cloud.huawei.com/mapApi/v1/siteService/reverseGeocode";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    //LOCATION SERVES DECLARATION
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("pin your address");

        //PERMIT THE APP TO CREATE THREADS IN THE BACKGROUND
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
         StrictMode.setThreadPolicy(policy);

        //ON CREATE CHECK AND ALLOW PERMISSION ACCESS
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, strings, 1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(this, strings, 2);
            }
        }

        // CREATE fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        // CREATE settingsClient
        settingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();
        // Set the interval for location updates, in milliseconds.
        mLocationRequest.setInterval(1000);
        // set the priority of the request
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        //CALL requestLocationUpdatesWithCallback() A METHOD THAT GET USER CURRENT LOCATION AND PLACE A CLICKABLE STAR MARKER
        requestLocationUpdatesWithCallback();

        //SET THE MAP Fragment
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapfragment_mapfragmentdemo);
        mMapFragment.getMapAsync(this);

        //LOCATION LocationCallback METHOD THAT KEEP GETTING THE LOCATION CURRENT USER AND PLACES THE MARKER ON THE MAP
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    List<Location> locations = locationResult.getLocations();
                    if (!locations.isEmpty()) {
                        for (Location location : locations) {
                            //create LatLng object and store current user location LatLng
                            userCurrentLocation = new LatLng(location.getLatitude(),location.getLongitude());

                            if(myLocationMarker==null){
                                String userAddress = getReverseGeocode(""+location.getLatitude(),""+location.getLongitude());
                                myLocationMarker = myMap.addMarker(new MarkerOptions().position(userCurrentLocation)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation_star_icon))
                                        .title(userAddress)
                                        .clusterable(true));
                                //update map camera
                                updateMapCamera(userCurrentLocation);
                            }else{
                                myLocationMarker.setPosition(userCurrentLocation);
                            }
                            Log.i(TAG,"onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude() + "," + location.getLatitude() + "," + location.getAccuracy());
                        }
                    }
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                if (locationAvailability != null) {
                    boolean flag = locationAvailability.isLocationAvailable();
                    Log.i(TAG, "onLocationAvailability isLocationAvailable:" + flag);
                }
            }
        };

    }

    //getReverseGeocode METHOD RECEIVES LATITUDE AND LONGITUDE AS STRINGS AND MAKE AN HTTP POST REQUEST TO HMS reverseGeocode SERVICES
    //THE METHOD RETURNS AN ADDRESS AND A STRING
    private String getReverseGeocode(String latitude,String longitude){
        OkHttpClient client = new OkHttpClient();
        String addressDescription = "";

        //REQUEST BODY TEMPLATE
        String requestJsonString = "{'location':"
                    + "{'lng':'" + longitude + "',"
                    + "'lat':'" + latitude + "'},"
                    + "'language':'en'}";

        try{
            RequestBody body = RequestBody.create(requestJsonString,JSON);
            Request newRequest = new Request.Builder()
                    .url(url)  // HMS LINK FOR reverseGeocode
                    .header("key",API_key) // ADD API KEY AS A HEADER
                    .post(body)  // BODY WITH LATITUDE AND LONGITUDE
                    .build();

            //RECEIVE RESPONSE STORE IT AS A RESPONSE
            Response response = client.newCall(newRequest).execute();

            //CONVERT THE RESPONSE TO A STRING
            String result = response.body().string();

            //GET formatAddress FROM THE STRING USING SUBSTRING
            int indexAddress = result.indexOf("formatAddress");
            String word1 = result.substring(indexAddress+16);
            int lastIndex = word1.indexOf("\"");
            addressDescription = word1.substring(0,lastIndex);
        }
        catch (IOException e){ // CATCH ANY CRASHES FROM THE POST REQUEST
            addressDescription = "address not found!";
            Log.e(TAG,"Exceptoin on reverseGeocode request :"+e.getMessage().toString());

        }
        return addressDescription; //RETURN THE addressDescription
    }

    //This is the map fragment method all map interation
    @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    @Override
    public void onMapReady(HuaweiMap map){
        myMap = map;
        myMap.setMyLocationEnabled(false);// Enable the my-location overlay.
        myMap.getUiSettings().setMyLocationButtonEnabled(true);// Enable the my-location icon.

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);

        //set dealft marker on the map default locat HUAWEI TECHNOLOGIES IN GUATENG SA
        pinMarker = myMap.addMarker(markerOptions.position(huawei_SA_latLng));
        pinMarker.setTitle(huawei_SA_address);

        myMap.setOnMapClickListener(new HuaweiMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                String addressDescption = (getReverseGeocode(""+latLng.latitude,""+latLng.longitude));

                //remove the current marker add the marker here on map click
                pinMarker.remove();
                markerOptions.title(addressDescption);
                pinMarker = myMap.addMarker(markerOptions.position(latLng));
                //pinMarker.setPosition(latLng);

                //update map camera
                updateMapCamera(latLng);
            }
        });
    }
    //
    private void updateMapCamera(LatLng latLng){
        //This is to move the camera to where the marker is zoom is set to 10
        CameraPosition build = new CameraPosition.Builder().target(latLng).zoom(12).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(build);
        myMap.animateCamera(cameraUpdate);
    }

    //requestion the user current location method it also do checks if the user location is enabled
    private void requestLocationUpdatesWithCallback() {
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();
            // check devices settings before request location updates.
            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG, "check location settings success");
                            //request location updates
                            fusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.getMainLooper()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.i(TAG, "requestLocationUpdatesWithCallback onSuccess");
                                }
                            })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            Log.e(TAG,
                                                    "requestLocationUpdatesWithCallback onFailure:" + e.getMessage());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.e(TAG, "checkLocationSetting onFailure:" + e.getMessage());
                            int statusCode = ((ApiException) e).getStatusCode();
                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult(MainActivity.this, 0);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.e(TAG, "PendingIntent unable to execute request.");
                                    }
                                    break;
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "requestLocationUpdatesWithCallback exception:" + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply LOCATION PERMISSSION  failed");
            }
        }

        if (requestCode == 2) {
            if (grantResults.length > 2 && grantResults[2] == PackageManager.PERMISSION_GRANTED
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_BACKGROUND_LOCATION  failed");
            }
        }
    }

}