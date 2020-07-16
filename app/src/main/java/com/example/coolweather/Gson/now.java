package com.example.coolweather.Gson;

import com.google.gson.annotations.SerializedName;

public class now {
    @SerializedName("obsTime")
    public String onsTime;
    public String temp;
    @SerializedName("feelsLike")
    public String feelslike;
    @SerializedName("icon")
    public String icon;
    @SerializedName("text")
    public String text;
    @SerializedName("wind360")
    public String wind360;
    @SerializedName("windDir")
    public String windDir;
    @SerializedName("windScale")
    public String windScal;
    @SerializedName("windSpeed")
    public  String windspeed;
    public String humidity;
    public String precip;
    public String pressure;
    public String vis;
    public String cloud;
    public String dew;

}
