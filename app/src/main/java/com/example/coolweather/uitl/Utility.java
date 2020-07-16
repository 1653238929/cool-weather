package com.example.coolweather.uitl;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.example.coolweather.Gson.Basic;
import com.example.coolweather.Gson.Weather;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class Utility {
    private static final String TAG = "Unity = ";    //处理网络回应的类
    public static boolean handleProvinceResponse(String response){
        if(!TextUtils.isEmpty(response)){
            try{
               JSONArray allProvinces = new JSONArray(response);
                for (int i =0;i<allProvinces.length();i++)
                {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvienceName(provinceObject.getString("name"));
                    province.setProvienceCode(provinceObject.getInt("id"));
                    province.save();
                }
                return true;
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCityResponse(String response,int provinceId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for (int i =0;i<allCities.length();i++)
                {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("id"));
                    city.setProvinceId(provinceId);
                    city.save();
                }
                return true;
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static boolean handleCountyResponse(String response , int cityId){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties = new JSONArray(response);
                for (int i =0;i<allCounties.length();i++)
                {
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setWeatherId(countyObject.getString("weather_id"));
                    county.setCityId(cityId);
                    county.save();
                }
                return true;
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static Pic handlePicResponse(String response)
    {
        return new Gson().fromJson(response,Pic.class);
    }


    public static Weather handleWeatherResponse(String response){
        return  new Gson().fromJson(response,Weather.class);
    }

    public static Basic handleBasicResponse(String response) {
        return  new Gson().fromJson(response,Basic.class);
    }

//    public static Laction handleLocation(String response) {
//        try{
//            JSONObject jsonObject = new JSONObject(response);
//
////            Laction laction = new Laction();
////            laction.setCode(jsonObject.getString("code"));
////            laction.setLat(jsonObject.getString("location.lat"));
////            laction.setLon(jsonObject.getString("location.lon"));
////            laction.setTz(jsonObject.getString("location.tz"));
////            laction.save();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }
}
