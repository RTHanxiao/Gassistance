package com.example.gassistance;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;

public class FeedbackActivity extends AppCompatActivity {

    private EditText feedbackEditText;
    private EditText contactInfoEditText;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        feedbackEditText = findViewById(R.id.feedbackEditText);
        contactInfoEditText = findViewById(R.id.contactInfoEditText);
        submitButton = findViewById(R.id.submitButton);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
            @Override
            public void onClick(View v) {
                String feedback = feedbackEditText.getText().toString().trim();
                String contactInfo = contactInfoEditText.getText().toString().trim();

                if (!feedback.isEmpty()) {
                    Feedback feedbackObj = new Feedback();
                    feedbackObj.setContent(feedback);
                    feedbackObj.setContactInfo(contactInfo);

                    feedbackObj.save(new SaveListener<String>() {
                        @Override
                        public void done(String objectId, BmobException e) {
                            if (e == null) {
                                Toast.makeText(FeedbackActivity.this, "反馈已提交，谢谢！", Toast.LENGTH_SHORT).show();
                                feedbackEditText.setText(""); // 清空反馈内容输入框
                                contactInfoEditText.setText(""); // 清空联系方式输入框
                            } else {
                                Toast.makeText(FeedbackActivity.this, "提交失败，请稍后重试", Toast.LENGTH_SHORT).show();
                                // 这里可以打印出具体的错误信息
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    Toast.makeText(FeedbackActivity.this, "请输入您的反馈", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
