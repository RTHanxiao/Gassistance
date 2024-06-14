package com.example.gassistance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SmartAssistantActivity extends AppCompatActivity {

    private static final String TAG = "SmartAssistantActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextView weatherTextView;
    private RelativeLayout rootLayout;
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smart_assistant);

        weatherTextView = findViewById(R.id.weatherTextView);
        rootLayout = findViewById(R.id.rootLayout);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        Button getWeatherButton = findViewById(R.id.getWeatherButton);
        getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationPermission();
            }
        });
        Button scheduleButton = findViewById(R.id.scheduleButton);
        scheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SmartAssistantActivity.this, ScheduleActivity.class));
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocationAndFetchWeather();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchWeather();
            } else {
                weatherTextView.setText("没有位置权限，无法获取天气信息");
            }
        }
    }

    private void getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            fetchAreaCodeAndWeatherInfo(latitude, longitude);
                        } else {
                            weatherTextView.setText("无法获取位置信息");
                        }
                    }
                });
    }

    private void fetchAreaCodeAndWeatherInfo(double latitude, double longitude) {//根据当前位置获取地理编码
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://restapi.amap.com/v3/geocode/regeo").newBuilder();
        urlBuilder.addQueryParameter("key", "92de9875895e3dc6056ef8366ff2af32");
        urlBuilder.addQueryParameter("location", longitude + "," + latitude);
        urlBuilder.addQueryParameter("extensions", "base");
        urlBuilder.addQueryParameter("output", "JSON");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String errorMessage = "调用地理编码接口报错：" + e.getMessage();
                runOnUiThread(() -> weatherTextView.setText(errorMessage));
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String resp = response.body().string();
                Log.d(TAG, "Geocode Response JSON: " + resp);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(resp, JsonObject.class);

                if (jsonObject.has("regeocode")) {
                    JsonObject regeocode = jsonObject.getAsJsonObject("regeocode");
                    if (regeocode.has("addressComponent")) {
                        JsonObject addressComponent = regeocode.getAsJsonObject("addressComponent");
                        if (addressComponent.has("adcode")) {
                            String adcode = addressComponent.get("adcode").getAsString();
                            fetchWeatherInfo(adcode);
                        } else {
                            runOnUiThread(() -> weatherTextView.setText("地理编码数据中不存在 'adcode' 字段"));
                        }
                    } else {
                        runOnUiThread(() -> weatherTextView.setText("地理编码数据中不存在 'addressComponent' 字段"));
                    }
                } else {
                    runOnUiThread(() -> weatherTextView.setText("JSON数据中不存在 'regeocode' 字段"));
                }
            }
        });
    }

    private void fetchWeatherInfo(String adcode) {//根据地理编码获取天气信息
        OkHttpClient client = new OkHttpClient();
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://restapi.amap.com/v3/weather/weatherInfo").newBuilder();
        urlBuilder.addQueryParameter("key", "92de9875895e3dc6056ef8366ff2af32");
        urlBuilder.addQueryParameter("city", adcode);
        urlBuilder.addQueryParameter("extensions", "base");
        urlBuilder.addQueryParameter("output", "JSON");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final String errorMessage = "调用天气接口报错：" + e.getMessage();
                runOnUiThread(() -> weatherTextView.setText(errorMessage));
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String resp = response.body().string();
                Log.d(TAG, "Weather Response JSON: " + resp);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(resp, JsonObject.class);

                if (jsonObject.has("lives")) {
                    JsonArray livesArray = jsonObject.getAsJsonArray("lives");
                    if (livesArray.size() > 0) {
                        JsonObject firstObject = livesArray.get(0).getAsJsonObject();

                        String province = firstObject.get("province").getAsString();
                        String city = firstObject.get("city").getAsString();
                        String weather = firstObject.get("weather").getAsString();
                        String temperature = firstObject.get("temperature").getAsString();
                        String windDirection = firstObject.get("winddirection").getAsString();
                        String windPower = firstObject.get("windpower").getAsString();
                        String humidity = firstObject.get("humidity").getAsString();
                        String reportTime = firstObject.get("reporttime").getAsString();

                        String weatherInfo = "省份：" + province + "\n" +
                                "城市：" + city + "\n" +
                                "当前天气：" + weather + "\n" +
                                "当前气温：" + temperature + "℃\n" +
                                "风向：" + windDirection + "\n" +
                                "风力：" + windPower + "\n" +
                                "湿度：" + humidity + "\n" +
                                "报告时间：" + reportTime;

                        runOnUiThread(() -> {
                            weatherTextView.setText(weatherInfo);
                            updateBackground(weather);
                        });
                    } else {
                        runOnUiThread(() -> weatherTextView.setText("无法获取天气信息"));
                    }
                } else {
                    runOnUiThread(() -> weatherTextView.setText("JSON数据中不存在 'lives' 字段"));
                }
            }
        });
    }

    private void updateBackground(String weather) {
        int backgroundResource;

        switch (weather) {
            case "晴":
                backgroundResource = R.drawable.sunny_background;
                break;
            case "雨":
            case "小雨":
            case "中雨":
            case "大雨":
                backgroundResource = R.drawable.rainy_background;
                break;
            case "多云":
            case "阴":
                backgroundResource = R.drawable.cloudy_background;
                break;
            case "雪":
            case "小雪":
            case "中雪":
            case "大雪":
                backgroundResource = R.drawable.snowy_background;
                break;
            default:
                backgroundResource = R.drawable.default_background;
                break;
        }

        rootLayout.setBackgroundResource(backgroundResource);
    }
}
