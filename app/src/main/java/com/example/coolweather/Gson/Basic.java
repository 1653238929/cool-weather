package com.example.coolweather.Gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Basic {
    @SerializedName("status")
    public String  status;
    @SerializedName("location")
    public List<location> location;
}
