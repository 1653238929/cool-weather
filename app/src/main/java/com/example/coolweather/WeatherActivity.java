package com.example.coolweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.customview.widget.ViewDragHelper;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.coolweather.Gson.Basic;
import com.example.coolweather.Gson.Weather;
import com.example.coolweather.uitl.HttpUtil;
import com.example.coolweather.uitl.Pic;
import com.example.coolweather.uitl.Utility;

import java.io.IOException;
import java.lang.reflect.Field;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;

    public WaveSwipeRefreshLayout waveSwipeRefreshLayout;

    private TextView timeText,loadText,tempText,weatherText,windspeedText,windScalText,wind360Text,windDirText;

    private TextView lat,lon;

    private TextView humidityText,precipText,pressureText,visText,cloudText,dewText;

    private WebView webView;

    private String TAG = "WeatherActivity ";

    private ImageView bingPicImage;

    private ScrollView weatherLayout;

    private String weatherId;

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initview();

        weatherId = getIntent().getStringExtra("weather_id");

        if(weatherId==null)
        {
            weatherId = getIntent().getStringExtra("weather_Id");
        }

        waveSwipeRefreshLayout.setOnRefreshListener(new WaveSwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        String weatherBasic = prefs.getString("Basic",null);

        if (weatherString != null&&weatherBasic != null) {
            // 有缓存时直接解析天气数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            Basic basic = Utility.handleBasicResponse(weatherBasic);
            showBaicInfo(basic);
            showWeatherInfo(weather);
        } else {
            // 无缓存时去服务器查询天气
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        String bingPic = prefs.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bingPicImage);
        } else {
            requestLoadPingPic();
        }
        setDrawerLeftEdgSize(WeatherActivity.this,drawerLayout,0.3f);
    }
//使菜单更容易划出的函数
    private void setDrawerLeftEdgSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage){
        if(activity == null||drawerLayout==null)
            return;
        try {
            Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
            leftDraggerField.setAccessible(true);
            ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
            Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
            edgeSizeField.setAccessible(true);
            int edgeSize = edgeSizeField.getInt(leftDragger);
            DisplayMetrics dm = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
            edgeSizeField.setInt(leftDragger,Math.max(edgeSize,(int)(dm.widthPixels *displayWidthPercentage)));

        } catch (IllegalAccessException e) {
            Log.e("IllegalAccessException",e.getMessage().toString());
        } catch (NoSuchFieldException e) {
            Log.e("NoSuchFieldExcption",e.getMessage().toString());
        }
    }

    private void initview() {
        drawerLayout = findViewById(R.id.drawer_Layout);
        waveSwipeRefreshLayout = findViewById(R.id.waveSwipeRefresh_layout);
        humidityText = findViewById(R.id.humidity_text);
        precipText = findViewById(R.id.preCip_text);
        pressureText = findViewById(R.id.pressure_text);
        visText = findViewById(R.id.vis_text);
        cloudText = findViewById(R.id.cloud_text);
        dewText = findViewById(R.id.dew_text);
        lat =findViewById(R.id.lat_text);
        lon = findViewById(R.id.lot_text);
        weatherLayout = findViewById(R.id.scrool);
        timeText= findViewById(R.id.time);
        weatherText = findViewById(R.id.weather_text);
        loadText = findViewById(R.id.laod);
        tempText = findViewById(R.id.temp);
        wind360Text = findViewById(R.id.wind360_text);
        windDirText = findViewById(R.id.windDir_text);
        windspeedText = findViewById(R.id.windSpeed_text);
        windScalText = findViewById(R.id.windScal_text);
//        webView = findViewById(R.id.web_view);
        bingPicImage= findViewById(R.id.image_back);
    }

    public void requestLoadPingPic()
    {
        String loadPingUrl = "https://api.xygeng.cn/Bing/url/";
        HttpUtil.sendOkHttpRequest(loadPingUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
               // Toast.makeText(WeatherActivity.this,"图片错误",Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                final Pic pic= Utility.handlePicResponse(bingPic);
                final String url = pic.data;
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bingpic",url);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG,"url = "+ url);
                        Glide.with(WeatherActivity.this).load(url).into(bingPicImage);
                    }
                });
            }
        });
    }

    public void requestWeather(final String weatherID){
        String weatherUrl = "https://devapi.heweather.net/v7/weather/now?location=" + weatherID +
                "&key=a88c78fd07ec448dbd76216007818f2e";
        String locadtionUrl = "https://geoapi.heweather.net/v2/city/lookup?location=" +weatherID +
                "&key=a88c78fd07ec448dbd76216007818f2e";
        Log.e(TAG,"id = " +weatherID);
        HttpUtil.sendOkHttpRequest(locadtionUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                Log.e(TAG,"responseText = "+responseText);
                final Basic basic = Utility.handleBasicResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG,"state = "+ basic.status);
                        if(basic!=null&&basic.status.equals("200"))
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("Basic", responseText);
                            editor.apply();
                            showBaicInfo(basic);
                        }
                    }
                });
            }
        });
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e(TAG,"code = "+weather.code);
                        if(weather!=null&&weather.code.equals("200"))
                        {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.putString("weatherid",weatherID);
                            editor.apply();
                            showWeatherInfo(weather);
                        }
                    }
                });
            }
        });
       requestLoadPingPic();
       waveSwipeRefreshLayout.setRefreshing(false);
    }

    private void showBaicInfo(Basic basic) {
        loadText.setText(basic.location.get(0).name);
        lat.setText(basic.location.get(0).lat+"°");
        lon.setText(basic.location.get(0).lon+"°");
    }

    private void showWeatherInfo(Weather weather) {

        Log.e(TAG,"code = "+weather.updateTime);
        Log.e(TAG,"code = "+weather.now.temp);
        Log.e(TAG,"code = "+weather.now.text);
        Log.e(TAG,"code = "+weather.fxLink);

        timeText.setText(weather.updateTime);
        tempText.setText(weather.now.temp+"℃");
        weatherText.setText(weather.now.text);


//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.setBackgroundColor(100);
//        webView.setWebViewClient(new WebViewClient());
//        webView.loadUrl(weather.fxLink);
        windScalText.setText(weather.now.windScal);
        windspeedText.setText(weather.now.windspeed+"km/h");
        wind360Text.setText(weather.now.wind360+"°");
        windDirText.setText(weather.now.windDir);
        humidityText.setText("相对湿度  "+weather.now.humidity+"%");
        precipText.setText("降水量  "+weather.now.precip+"mm");
        pressureText.setText("大气压强  "+weather.now.pressure+"百帕");
        visText.setText("能见度  "+weather.now.vis+"km");
        cloudText.setText("云量  "+weather.now.cloud+"%");
        dewText.setText("露点温度  "+weather.now.dew+"℃");
    }
}
