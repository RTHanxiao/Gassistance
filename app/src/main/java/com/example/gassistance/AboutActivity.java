package com.example.gassistance;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 在这里可以做一些初始化工作，比如设置标题等
        setTitle("关于我们");
    }
}
