package com.example.gassistance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class EmailActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;

    private EditText toEmail;
    private EditText subject;
    private EditText body;
    private EditText attachment;
    private Button attachButton;
    private Button sendButton;
    private Uri attachmentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email);

        toEmail = findViewById(R.id.to_email);
        subject = findViewById(R.id.subject);
        body = findViewById(R.id.body);
        attachment = findViewById(R.id.attachment);
        attachButton = findViewById(R.id.attach_button);
        sendButton = findViewById(R.id.send_button);

        attachButton.setOnClickListener(view -> openFileChooser());
        sendButton.setOnClickListener(view -> sendEmail());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            attachmentUri = data.getData();
            attachment.setText(attachmentUri.getPath());
        }
    }

    private void sendEmail() {
        String to = toEmail.getText().toString();
        String subjectText = subject.getText().toString();
        String bodyText = body.getText().toString();

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + to));
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectText);
        intent.putExtra(Intent.EXTRA_TEXT, bodyText);
        if (attachmentUri != null) {
            intent.putExtra(Intent.EXTRA_STREAM, attachmentUri);
        }

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
