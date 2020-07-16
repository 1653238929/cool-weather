package com.example.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.coolweather.uitl.HttpUtil;
import com.example.coolweather.uitl.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AutoUpdateServer extends Service {
    public AutoUpdateServer() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        updateWeather();
        updateBingPic();
        AlarmManager manager =  (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8*60*60*1000;
        long triggerAtTime = SystemClock.elapsedRealtime() +anHour;
        Intent i= new Intent(this,AutoUpdateServer.class);
        PendingIntent pendingIntent =  PendingIntent.getService(this,0,i,0);
        manager.cancel(pendingIntent);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pendingIntent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateBingPic() {
        final String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager
                        .getDefaultSharedPreferences(AutoUpdateServer.this)
                        .edit();
                editor.putString("bing_pic",bingPic);
                editor.apply();
            }
        });
    }

//    private void updateWeather() {
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString = prefs.getString("weather",null);
//        if(weatherString != null)
//        {
//            Weather weather = Utility.handleWeatherResponse(weatherString);
//
//            String weatherId = weather.basic.weatherId;
//            String weatherUrl = "http:/guolin.tech/api/weather?cityid=" + weatherId +"\n" +
//                    "&key=7f75228c2e194db0bfe6b2c0c9d80839";
//            HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
//                @Override
//                public void onFailure(Call call, IOException e) {
//                    e.printStackTrace();
//                }
//
//                @Override
//                public void onResponse(Call call, Response response) throws IOException {
//                    String responseText = response.body().string();
//                    Weather weather = Utility.handleWeatherResponse(responseText);
//                    if(weather !=null&&"ok".equals(weather.status)) {
//                        SharedPreferences.Editor editor = PreferenceManager
//                                .getDefaultSharedPreferences(AutoUpdateServer.this)
//                                .edit();
//                        editor.putString("weather", responseText);
//                        editor.apply();
//                    }
//                }
//            });
//        }
//    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
