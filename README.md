## PinYourAddressOnTheMap is a simple app to help getting started with Huawei Mobile Services

### Application description

When you open the app, the app locates user’s current location and shows
Huawei map. Marks user’s current location on the map with Star and popup user’s
current address when click the Star. (Address description, not the
Geocoding value) Display a pin on the map. User can move and click on the map, the pin
will move to the point as user clicks. Popup the address description when
user pins a point on the map.

### First you need to create an account on https://developer.huawei.com/consumer/en/ so you can use Huawei mobile services 

Step one register here https://developer.huawei.com/consumer/en/ you need an account to start using Huawei mobile serives 
on this app (PinYourAddressOnTheMap) we are going to use mapKit and location Kit

#### 1. when you've registered please follow these steps to create you your first app on in AppGallery Connect
link for the steps : https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#0

Now open your application on android studio 

#### 2. configure maven repository address for the HMS SDK open build.gradle on the project layer
please see the build.gradle from the PinYourAddressOnTheMap project

```
// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        jcenter()
        maven { url 'http://developer.huawei.com/repo/' } // HUAWEI Maven repository
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:4.1.0-alpha06'
        classpath 'com.huawei.agconnect:agcp:1.2.0.300'   //HUAWEI agcp plugin
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'http://developer.huawei.com/repo/' } // HUAWEI Maven repository
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

link to the code : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/build.gradle

#### 3. now configure build.gradle on your app level by adding Compile Dependencies for the map,location and Okhhpt

please see the build.gradle from the PinYourAddressOnTheMap project
```
dependencies {
    implementation 'androidx.appcompat:appcompat:1.1.0'
    implementation 'com.google.android.material:material:1.1.0'
    implementation 'androidx.constraintlayout:constraintlayout:1.1.3'
    testImplementation 'junit:junit:4.13'
    androidTestImplementation 'androidx.test.ext:junit:1.1.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.2.0'
    implementation("com.google.code.gson:gson:2.8.5")

    implementation 'com.huawei.agconnect:agconnect-core:1.2.1.301'
    implementation 'com.huawei.hms:maps:4.0.1.301'
    implementation 'com.huawei.hms:location:4.0.3.301'
    implementation("com.squareup.okhttp3:okhttp:4.5.0")
    
}
```
link to the code : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/app/build.gradle

#### 4. Add a MapFragment in the layout file of an activity, and set the map attributes using the XML

```
<fragment xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:map="http://schemas.android.com/apk/res-auto"
        android:id="@+id/mapfragment_mapfragmentdemo"
        class="com.huawei.hms.maps.MapFragment"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        map:cameraTargetLat="-26.064738"
        map:cameraTargetLng="28.0902398"
        map:cameraZoom="4"/>
```
link to the code : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/app/src/main/res/layout/activity_main.xml

#### 5. add access permission on the AndroidManifest.xml 

```
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
```
please see the code : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/app/src/main/AndroidManifest.xml

#### 6. create an xml under layout this xml will be used for the popup whe you select an marker on the map i've called mine custome_info_wi.xml

```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal">

    <LinearLayout
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:background="@drawable/bg"
        android:orientation="vertical">

        <TextView
            android:id="@+id/txtv_snippett"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="7dp"
            android:layout_marginLeft="15dp"
            android:layout_marginRight="15dp"
            android:layout_marginBottom="35dp"
            android:ellipsize="none"
            android:singleLine="false"
            android:textColor="#000000"
            android:textSize="10sp"/>
    </LinearLayout>
</LinearLayout>
```
please see the code here : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/app/src/main/res/layout/custom_info_wi.xml

#### 7. now open MainActivity and import all required packages and declare all required variable and objects also read the comments they are very detailed

```
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
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.List;


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

    //LOCATION SERVES DECLARATION
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;

    //CREATE AN OBJECT FOR MY CLASS CALLED myMapWorkLoad
    myMapWorkLoad mapworkloard = new myMapWorkLoad();

```
code to mainActivity : https://github.com/Nxele/PinYourAddressOnTheMap/blob/master/app/src/main/java/com/sizwe/PinYourAddressOnTheMap/MainActivity.java

#### 8. now inside onCreate we request location access for the app, create location client and load the map layer, get user current location and display in on the map please read the comments they are very detailed

```
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("PIN YOUR ADDRESS");

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
                                myLocationMarker = myMap.addMarker(new MarkerOptions().position(userCurrentLocation)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.mylocation_star_icon))
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
```

#### 9. now the map fragment is loaded , on load we add a draggable pin, setOnMapClickListener for moving the pin new selected location, setOnMarkerClickListener for displaying the markers current address on the pop up, add CustomInfoWindowAdapter for the popup and setOnMarkerDragListener for update camera view when you drag the pin also read the comments they are very detailed. 
 
```
//THIS IS THE MAP fragment METHOD ALL THE MAP INTERACTION HAPPENS HERE
    @RequiresPermission(allOf = {ACCESS_FINE_LOCATION, ACCESS_WIFI_STATE})
    @Override
    public void onMapReady(HuaweiMap map){
        myMap = map;
        myMap.setMyLocationEnabled(false);// Enable the my-location overlay.
        myMap.getUiSettings().setMyLocationButtonEnabled(true);// Enable the my-location icon.

        // CREATE THE CustomInfoWindowAdapter FOR DISPLAY ADDRESS AS A POPUP
        class CustomInfoWindowAdapter implements HuaweiMap.InfoWindowAdapter {
            private final View mWindow;
            CustomInfoWindowAdapter() {
                mWindow = getLayoutInflater().inflate(R.layout.custom_info_wi, null);
            }
            @Override
            public View getInfoWindow(Marker marker) {
                TextView txtvSnippett;txtvSnippett = mWindow.findViewById(R.id.txtv_snippett);
                txtvSnippett.setText(marker.getSnippet());
                return mWindow;
            }
            @Override
            public View getInfoContents(Marker marker) {
                return null;
            }
        }
        //SET THE COSTUME WINDOW
        myMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        //CREATE MARKER OBJECT AND SET IT TO BE DRAGGABLE
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.draggable(true);

        // SET DEFAULT MARKER ON TECHNOLOGIES IN GUATENG SA
        pinMarker = myMap.addMarker(markerOptions.position(huawei_SA_latLng));

        // GET THE CURRENT ADDRESS OF THE MARKER WHEN IS IT IS CLICKED AND UPDATE THE POPUP
        myMap.setOnMarkerClickListener(new HuaweiMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                    String addressDescption = (mapworkloard.getReverseGeocode("" + marker.getPosition().latitude, "" + marker.getPosition().longitude));
                    marker.setSnippet(addressDescption);
                return false;
            }
        });

        myMap.setOnMapClickListener(new HuaweiMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //WHEN A USER CLICK ON THE MAP MOVE THE MARKER TO A NEW POSITION
                pinMarker.setPosition(latLng);
                //update map camera
                updateMapCamera(latLng);
            }

        });

        //CENTER THE MARKER ON DRAG END AND GET THE NEW ADDRESS
        myMap.setOnMarkerDragListener(new HuaweiMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                Log.i(TAG, "onMarkerDragStart: ");
            }
            @Override
            public void onMarkerDrag(Marker marker) {
                Log.i(TAG, "onMarkerDrag: ");
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {
                updateMapCamera(marker.getPosition());
            }
        });
    }
```

#### 10. use one method for updating the map camera you just call it and pass the new camera view latlng

```
private void updateMapCamera(LatLng latLng){
        CameraPosition build = new CameraPosition.Builder().target(latLng).zoom(12).build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(build);
        myMap.animateCamera(cameraUpdate);
    }
```

#### 11. now create a class myMapWorkLoad this class have two methods which i use on the mainActivity first method is getReverseGeocode this is a method that do a post request to Huawie API reverseGeocoder using Okhttp client this is the method that returns address when you click a pin on the map then i have wrapString this method replace commas with a nextline i use to contract a readable address and you can see on the popup

```
package com.sizwe.PinYourAddressOnTheMap;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class myMapWorkLoad {
    private static final String TAG = "";

    //HUAWEI MOBILE SERVES API VALUES
    private final String API_key = "CV7BndPX5tuyjCq+sl7dHrlNxw9xnEPTv9q6s84nbr4c8Fy+Ka9SSayA2bkSTqnwRwvcXl2TJV53iRK9DH88/dCoh3F9";
    private final String url ="https://siteapi.cloud.huawei.com/mapApi/v1/siteService/reverseGeocode";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    //getReverseGeocode METHOD RECEIVES LATITUDE AND LONGITUDE AS STRINGS AND MAKE AN HTTP POST REQUEST TO HMS reverseGeocode SERVICES
    //THE METHOD RETURNS AN ADDRESS AND A STRING
    public String getReverseGeocode(String latitude,String longitude){
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
            String addressString = result.substring(indexAddress+16);
            int lastIndexOfComma = addressString.indexOf("\"");
            addressDescription = addressString.substring(0,lastIndexOfComma);

            addressDescription = wrapString(addressDescription);
        }
        catch (IOException e){ // CATCH ANY CRASHES FROM THE POST REQUEST
            addressDescription = "address not found!";
            Log.e(TAG,"Exceptoin on reverseGeocode request :"+e.getMessage().toString());

        }
        return addressDescription; //RETURN THE addressDescription
    }
    
    //FUNCTION THAT RECEIVE AN ADDRESS AS A STRING THEN AND ADDS NEXT LINE WHERE THE IS COMMAS AND RETURN THE NEW FORMATTED STRING
    public String wrapString(String address){
        String dataX = address.replaceAll("[,]", "\n");
        return dataX;
    }
}

```

#### 12. then we requestLocationUpdatesWithCallback this method keeps requesting new location or the user then we onRequestPermissionsResult

```
 // THIS FUNCTION LOCATE USER CURRENT LOCATION ALSO CHECK IF LOCATION ACCESS IS ENABLED ON THE DEVICE
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
```

### please look at these examples from Huawei.

#### mapKit documentation
Doc: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/hms-map-
v4-abouttheservice
#### Codelab to also help you get started with mapit
Codelab: https://developer.huawei.com/consumer/en/codelab/HMSMapKit/index.html#0

#### location kit documentation
Doc: https://developer.huawei.com/consumer/en/doc/development/HMS-Guides/location-
#### Codelab to also help you get started with location kit
Codelab: https://developer.huawei.com/consumer/en/codelab/HMSLocationKit/index.html#0


