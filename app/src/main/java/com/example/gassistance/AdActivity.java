package com.example.gassistance;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gassistance.ShakeDetector;

public class AdActivity extends Activity {
    private SensorManager sensorManager;
    private ShakeDetector shakeDetector;
    private boolean hasJumpedToMain = false; // 标志变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);

        ImageView adImage = findViewById(R.id.ad_image);
        Button skipButton = findViewById(R.id.skip_button);
        TextView countdownText = findViewById(R.id.countdown_text);

        // 设置广告图片资源
        adImage.setImageResource(R.drawable.wtl_image1);

        // 初始化摇晃检测器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        shakeDetector = new ShakeDetector(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake() {
                //跳转
                Intent intent = new Intent();
                ComponentName cmp = new ComponentName("tv.danmaku.bili","tv.danmaku.bili.MainActivityV2");
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(cmp);
                startActivityForResult(intent, 0);
            }
        });

        // 设置广告图片点击事件
        adImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跳转学习强国
                Intent intent = new Intent();
                ComponentName cmp = new ComponentName("cn.xuexi.android","com.alibaba.android.rimet.biz.SplashActivity");
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(cmp);
                startActivityForResult(intent, 0);
            }
        });

        // 开始倒计时
        new CountDownTimer(5000, 1000) {
            public void onTick(long millisUntilFinished) {
                countdownText.setText("跳过 " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                // 倒计时结束，检查是否已经跳转过
                if (!hasJumpedToMain) {
                    startActivity(new Intent(AdActivity.this, MainActivity.class));
                    finish();
                }
            }
        }.start();

        // 跳过按钮点击事件
        skipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 跳转到 MainActivity 并设置标志
                if (!hasJumpedToMain) {
                    hasJumpedToMain = true;
                    startActivity(new Intent(AdActivity.this, MainActivity.class));
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeDetector,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeDetector);
    }
}