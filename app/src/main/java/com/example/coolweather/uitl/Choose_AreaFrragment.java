package com.example.coolweather.uitl;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.MainActivity;
import com.example.coolweather.R;
import com.example.coolweather.WeatherActivity;
import com.example.coolweather.db.City;
import com.example.coolweather.db.County;
import com.example.coolweather.db.Province;

import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Choose_AreaFrragment extends Fragment {  //全国各省各城市各县的适配器

    public static final  int LEVEL_PROVINCE = 0;
    public static final  int LEVEL_CITY = 1;
    public static final  int LEVEL_COUNTY = 2;

    private static final String TAG = "Choose_AreaFrragment";

    private ProgressDialog progressDialog;

    private TextView titleText;
    private Button button_return;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provincesList;
    private List<City> cityList;
    private List<County> countyList;
    private  Province selectedProvince;
    private  City selectedCity;
    private int currentLevel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.choose_areafragment,container,false);
        titleText = (TextView) view.findViewById(R.id.text_message);
        button_return = (Button)view.findViewById(R.id.button_return);
        listView = (ListView) view.findViewById(R.id.listView_message);
        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //监听listview点击事件
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(currentLevel ==LEVEL_PROVINCE)//判断当前已经选的层   是省  城市   还是县  进入之后加载相对应的listview
                {
                    selectedProvince = provincesList.get(position);   //确定所选省份
                    queryCities();//加载所选省份的所有城市
                }else if(currentLevel == LEVEL_CITY){
                    selectedCity = cityList.get(position);  //确定所选城市
                    queryCountie();//加载所选城市的所有县
                }else if(currentLevel == LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
//                    String weatherName = countyList.get(position).getCountyName();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra("weather_id", weatherId);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();
                        activity.waveSwipeRefreshLayout.setRefreshing(true);
                        activity.setWeatherId(weatherId);
                        activity.requestWeather(weatherId);
                    }
//                    Intent intent = new Intent(getContext(), WeatherActivity.class);
//                        intent.putExtra("weather_id",weatherId);
//                        startActivity(intent);
//                        getActivity().finish();
                }
//                else if(currentLevel == LEVEL_COUNTY){  //跳转到WeatherActivity并将所选县的weatherid传出
//                    String weatherId = countyList.get(position).getWeatherId();
//                    if(getActivity() instanceof MainActivity)
//                    {
//                        Intent intent = new Intent(getContext(), WeatherActivity.class);
//                        intent.putExtra("weather_id",weatherId);
//                        startActivity(intent);
//                        getActivity().finish();//结束活动
//                    }else if(getActivity() instanceof WeatherActivity){
//                        WeatherActivity activity = (WeatherActivity) getActivity();
//                        activity.drawerLayout.closeDrawers();
//                        activity.swipeRefreshLayout.setRefreshing(true);
//                        activity.setWeatherId(weatherId);
//                        activity.requestWeather(weatherId);
//                    }
//
//                }
            }
        });
        button_return.setOnClickListener(new View.OnClickListener() { //返回事件的监听
            @Override
            public void onClick(View v) {
                if(currentLevel ==LEVEL_COUNTY){
                    queryCities();
                }else if(currentLevel == LEVEL_CITY){
                    queryProvince();
                }
            }
        });
        queryProvince();  //初始化省份列表；
    }

    private void queryProvince(){
        titleText.setText("China");
        button_return.setVisibility(View.GONE);//省份已经是最前的一个页面了 ，所以将button——back 隐藏
        provincesList = DataSupport.findAll(Province.class);
        if(provincesList.size() >0)//如果本地已有省份列表   则直接加载   否则  先网络请求
        {
            dataList.clear();
            for(Province province :provincesList)
            {
                dataList.add(province.getProvienceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }
        else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address,"province");
        }
    }

    private void queryCities(){  //相似于省份的初始化
        titleText.setText(selectedProvince.getProvienceName());
        button_return.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",
                String.valueOf(selectedProvince.getId())).find(City.class);//网络？？？
        if(cityList.size()>0){
            dataList.clear();
            for(City city :cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }
        else {
            int provinceCode = selectedProvince.getProvienceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address,"city");
        }
    }

    private  void queryCountie(){ //相似于省份的初始化
        titleText.setText(selectedCity.getCityName());
        button_return.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?",
                String.valueOf(selectedCity.getId())).find(County.class);
        Log.e(TAG,"city"+countyList.size());
        if(countyList.size() >0) {
            dataList.clear();
            for (County county :countyList)
            {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            int provinceCode = selectedProvince.getProvienceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode+ "/" +cityCode;
            queryFromServer(address,"counties");
        }
        }

    private void  queryFromServer(String address, final String type) {
        /*向网络请求的总函数   总体分为三类：
        1.  省份的请求
        2.  城市的请求
        3.  县的请求

        之后再调用query 函数加载listview
    */
        showProgressDialog();
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(),"ERROR",Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                Log.e(TAG,""+responseText);
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                } else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getId());
                }else if("counties".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getId());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if("province".equals(type)) {
                                queryProvince();
                            }
                            else if("city".equals(type)) {
                                queryCities();
                            }
                           else if("counties".equals(type)){
                                queryCountie();
                            }
                        }
                    });
                }
            }


        });
    }
    private void closeProgressDialog() {  //目前未知。。。
        if(progressDialog != null)
        {
            progressDialog.dismiss();
        }
    }
    private void showProgressDialog() { //+1
        if(progressDialog == null)
        {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("running");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }
}
