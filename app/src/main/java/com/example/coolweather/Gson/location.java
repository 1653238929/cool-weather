package com.example.coolweather.Gson;

import com.google.gson.annotations.SerializedName;

public class location {
    @SerializedName("name")
    public String name;
    @SerializedName("id")
    public  String id;
    @SerializedName("lat")
    public String lat;
    @SerializedName("lon")
    public String lon;
    @SerializedName("adm2")
    public String adm2;
    @SerializedName("adm1")
    public String adm1;
    @SerializedName("country")
    public String country;
    @SerializedName("tz")
    public String tz;
    public String rank;
}
