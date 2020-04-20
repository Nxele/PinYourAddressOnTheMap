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
