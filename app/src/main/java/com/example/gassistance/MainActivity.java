package com.example.gassistance;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.gassistance.SmartAssistantActivity;




import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.util.IOUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static final String DB_NAME = "offline_answers";
    private static final int DB_VERSION = 1;
    private EditText questionInput;
    private Button submitButton;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private List<Integer> imageList;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;
    private boolean isOfflineMode = false;
    private SQLiteDatabase db;
    private static final int threshold = 300000;
    private DataBaseHelper databaseHelper;
    private ImageButton voiceInputBtn;

    private File audioFile;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder;
    private MediaPlayer mediaPlayer;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bmob.initialize(this, "b0fd2bc3a45eacbd510454be5147c0e6");
        databaseHelper = new DataBaseHelper(this);

        setContentView(R.layout.activity_main);
        // 初始化界面元素
        // 初始化图片资源ID列表
        imageList = new ArrayList<>();
        imageList.add(R.drawable.wtl_image1);
        imageList.add(R.drawable.wtl_image2);
        imageList.add(R.drawable.chx_image1);
        imageList.add(R.drawable.chx_image2);
        initViews();
        // 初始化数据库
        initDatabase();

        // 添加猜你想问按钮的点击事件
        Button suggestQuestionButton = findViewById(R.id.suggest_question_button);
        suggestQuestionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                suggestQuestion();
            }
        });

    }

    private void suggestQuestion() {
        if (db != null) {
            String suggestedQuestion = getSuggestedQuestionFromDatabase();
            if (!suggestedQuestion.isEmpty()) {
                questionInput.setText(suggestedQuestion);
            } else {
                showAlertDialog("没有找到推荐的问题。");
            }
        } else {
            showAlertDialog("数据库不可用，无法生成推荐问题。");
        }
    }

    private String getSuggestedQuestionFromDatabase() {
        String suggestedQuestion = "";
        Cursor cursor = db.rawQuery("SELECT Question FROM Answers ORDER BY _id DESC LIMIT 10", null);
        if (cursor.moveToFirst()) {
            List<String> recentQuestions = new ArrayList<>();
            do {
                String question = cursor.getString(cursor.getColumnIndex("Question"));
                recentQuestions.add(question);
            } while (cursor.moveToNext());

            suggestedQuestion = generateSuggestedQuestion(recentQuestions);
        }
        cursor.close();
        return suggestedQuestion;
    }

    private String generateSuggestedQuestion(List<String> recentQuestions) {
        // 停用词列表
        String[] stopWords = {"the", "is", "in", "at", "of", "and", "a", "what", "how", "why", "who"};
        Set<String> stopWordsSet = new HashSet<>(Arrays.asList(stopWords));

        Map<String, Integer> wordFrequency = new HashMap<>();
        for (String question : recentQuestions) {
            String[] words = question.split("\\s+");
            for (String word : words) {
                word = word.toLowerCase(); // 忽略大小写
                if (!stopWordsSet.contains(word)) {
                    wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
                }
            }
        }

        // 获取出现频率最高的单词
        String mostFrequentWord = "";
        int maxFrequency = 0;
        for (Map.Entry<String, Integer> entry : wordFrequency.entrySet()) {
            if (entry.getValue() > maxFrequency) {
                mostFrequentWord = entry.getKey();
                maxFrequency = entry.getValue();
            }
        }

        // 生成推荐问题
        return "关于 " + mostFrequentWord + " 的问题";
    }


    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void initViews() {
        questionInput = findViewById(R.id.question_input);
        submitButton = findViewById(R.id.submit_button);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        voiceInputBtn = findViewById(R.id.voice_input_btn);

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        chatRecyclerView.setAdapter(chatAdapter);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        questionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    submitButton.setBackgroundColor(getResources().getColor(R.color.black));
                } else {
                    submitButton.setBackgroundColor(getResources().getColor(R.color.gray));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // 检查录音权限
        ActivityResultLauncher<String[]> permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    Boolean recordPermission = result.getOrDefault(Manifest.permission.RECORD_AUDIO, false);
                    Boolean storagePermission = result.getOrDefault(Manifest.permission.WRITE_EXTERNAL_STORAGE, false);
                    if (!recordPermission || !storagePermission) {
                        Toast.makeText(this, "需要录音和存储权限", Toast.LENGTH_SHORT).show();
                    }
                });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
        }

        voiceInputBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startRecording();
                        return true;
                    case MotionEvent.ACTION_UP:
                        stopRecording();
                        uploadAudio();
                        return true;
                }
                return false;
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String question = questionInput.getText().toString();
                if (!question.isEmpty()) {
                    if (question.trim().equalsIgnoreCase("计算器")
                            || question.trim().equalsIgnoreCase("calculator")) {
                        openCalculatorApp();
                    } else if (question.trim().equalsIgnoreCase("日历")
                            || question.trim().equalsIgnoreCase("calendar")) {
                        openCalendarApp();
                    } else if (question.trim().equalsIgnoreCase("便签")
                            || question.trim().equalsIgnoreCase("note")) {
                        openNoteApp();
                    } else if (question.trim().equalsIgnoreCase("设置")
                            || question.trim().equalsIgnoreCase("setting")) {
                        openSettingApp();
                    } else if (question.trim().equalsIgnoreCase("地图")
                            || question.trim().equalsIgnoreCase("map")) {
                        openMap();
                    } else {
                        sendMessage(question, true);
                        questionInput.setText(""); // 清空输入框
                        submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
                        if (isOfflineMode) {
                            String answer = getOfflineAnswer(question);
                            sendMessage(answer, false);
                            databaseHelper.insertAnswer(question, answer);
                        } else {
                            getAnswerFromAPI(question);
                            Log.d("MainActivity", "Question: " + question);
                        }
                    }
                } else {
                    showAlertDialog("输入不能为空");
                }
            }
        });


        FloatingActionButton fabDraggable = findViewById(R.id.fab_draggable);
        fabDraggable.setOnTouchListener(new View.OnTouchListener() {//如果移动幅度很小，当作click处理，不然会使click功能失效
            private int xDelta;
            private int yDelta;
            private float downX;
            private float downY;
            private final int TOUCH_SLOP = ViewConfiguration.get(getApplicationContext()).getScaledTouchSlop();

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getRawX();
                        downY = event.getRawY();
                        RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                        xDelta = x - lParams.leftMargin;
                        yDelta = y - lParams.topMargin;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - downX) > TOUCH_SLOP || Math.abs(event.getRawY() - downY) > TOUCH_SLOP) {
                            // 超过了触摸阈值，认为是拖动事件
                            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
                            layoutParams.leftMargin = x - xDelta;
                            layoutParams.topMargin = y - yDelta;
                            view.setLayoutParams(layoutParams);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // 超过了触摸阈值，认为是点击事件
                        if (Math.abs(event.getRawX() - downX) < TOUCH_SLOP && Math.abs(event.getRawY() - downY) < TOUCH_SLOP) {
                            // 启动 SmartAssistantActivity
                            Log.d("MainActivity", "FloatingActionButton clicked!");
                            startActivity(new Intent(MainActivity.this, SmartAssistantActivity.class));
                        }
                        break;
                }
                view.invalidate();
                return true;
            }
        });

        // 为可拖动按钮添加点击事件监听器
        fabDraggable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 启动 SmartAssistantActivity
                Log.d("MainActivity", "FloatingActionButton clicked!"); // 添加这行日志输出
                startActivity(new Intent(MainActivity.this, SmartAssistantActivity.class));
            }
        });

    }

    private void initDatabase() {
        SQLiteOpenHelper dbHelper = new DataBaseHelper(this) ;
//        {
//            @Override
//            public void onCreate(SQLiteDatabase db) {
//                Log.e("InsertData", "22222");
//                db.execSQL("CREATE TABLE Answers (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
//                        + "Question TEXT, "
//                        + "Answer TEXT);");
//
//            }
//
//            @Override
//            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//            }
//            @Override
//            public void onOpen(SQLiteDatabase db) {
//                super.onOpen(db);
//                SampleDataInsertion.insertSampleData(db);
//            }
//        };


        try {
            db = dbHelper.getReadableDatabase();
        } catch (SQLiteException e) {
            Log.e("Database", "SQLiteException: " + e.getMessage());
            showAlertDialog("无法访问本地数据库，离线模式不可用");
            isOfflineMode = false;
        }
    }

    public void generateRandomQuestion(View view) {
        if (isOfflineMode && db != null) {
            String randomQuestion = getRandomQuestionFromDatabase();
            questionInput.setText(randomQuestion);
        } else {
            // 在线模式或离线模式但数据库不可用时，直接调用API或显示警告
            if (isOfflineMode) {
                showAlertDialog("无法获取随机问题，因为数据库不可用。");
            } else {
                showAlertDialog("网络连接正常，试试随意向我提问吧！");
            }
        }
    }


    private String getRandomQuestionFromDatabase() {
        Cursor cursor = db.rawQuery("SELECT Question FROM Answers ORDER BY RANDOM() LIMIT 1", null);
        String randomQuestion = "";
        if (cursor.moveToFirst()) {
            randomQuestion = cursor.getString(0);
        }
        cursor.close();
        return randomQuestion;
    }


    @SuppressLint("Range")
    private String getOfflineAnswer(String question) {
        String answer = null;
        Cursor cursor = db.query("Answers",
                new String[]{"Question", "Answer"},
                "Question LIKE ?",
                new String[]{"%" + question + "%"},
                null, null, null);

        if (cursor.moveToFirst()) {
            // 如果找到了包含用户输入问题的问题，则直接返回对应的答案
            answer = cursor.getString(cursor.getColumnIndex("Answer"));
        } else {
            // 如果没有找到包含用户输入问题的问题，则寻找最相似的问题
            cursor = db.query("Answers",
                    new String[]{"Question", "Answer"},
                    null,
                    null,
                    null, null, null);
            String mostSimilarQuestion = "";
            int minDistance = Integer.MAX_VALUE;

            if (cursor.moveToFirst()) {
                do {
                    String storedQuestion = cursor.getString(cursor.getColumnIndex("Question"));
                    int distance = calculateLevenshteinDistance(question, storedQuestion);
                    if (distance < minDistance) {
                        minDistance = distance;
                        mostSimilarQuestion = storedQuestion;
                        answer = cursor.getString(cursor.getColumnIndex("Answer"));
                    }
                } while (cursor.moveToNext());
            }

            // 如果找到了最相似的问题，则返回其对应的答案
            if (minDistance < threshold) {
                answer = (answer != null) ? answer : "抱歉，找不到答案。最相似的问题是：" + mostSimilarQuestion;
            } else {
                answer = "抱歉，找不到答案。可以试着就用一个词来描述你的问题。";
            }
        }
        cursor.close();

        return answer;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private int min(int x, int y, int z) {
        return Math.min(Math.min(x, y), z);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void sendMessage(String message, boolean sentByUser) {
//        if (message.trim().equalsIgnoreCase("计算器")) {
//            openCalculatorApp();
//            return;
//        }

        Message newMessage = new Message(message, sentByUser);
        messageList.add(newMessage);
        chatAdapter.notifyDataSetChanged();
        chatRecyclerView.smoothScrollToPosition(messageList.size() - 1);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(questionInput.getWindowToken(), 0);
        submitButton.setBackgroundColor(getResources().getColor(R.color.blue));
    }

    private void getAnswerFromAPI(String question) {
        if (isNetworkAvailable()) {
            progressDialog = ProgressDialog.show(this, "请稍候", "我正在思考...", true);
            new AsyncTask<String, Void, String>() {
                @Override
                protected String doInBackground(String... strings) {
                    return callAPI(strings[0]);
                }

                @Override
                protected void onPostExecute(String answer) {
                    progressDialog.dismiss();
                    if (answer != null) {
                        sendMessage(answer, false);
                        // 插入到数据库
                        databaseHelper.insertAnswer(question, answer);
                        submitButton.setBackgroundColor(getResources().getColor(R.color.gray));
                    } else {
                        showAlertDialog("获取答案失败，请稍后重试");
                    }
                }
            }.execute(question);
        } else {
            isOfflineMode = true;
            showAlertDialog("没有网络连接，已切换到离线模式，试试点击随机问题按钮向我提问吧！");
            Log.d("MainActivity", "Switching to offline mode...");
            String answer = getOfflineAnswer(question);
            sendMessage(answer, false);
            // 将问题和答案插入数据库
            databaseHelper.insertAnswer(question, answer);
        }
    }

    private String callAPI(String question) {
        try {
            URL url = new URL("http://192.168.99.108:8000/ask");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject jsonParam = new JSONObject();
            jsonParam.put("question", question);

            OutputStream os = conn.getOutputStream();
            os.write(jsonParam.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                Log.d("MainActivity", "API调用成功，返回答案：" + jsonResponse.getString("answer"));
                return jsonResponse.getString("answer");
            } else {
                Log.e("API_RESPONSE", "HTTP错误代码: " + responseCode);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("API_REQUEST", "请求异常: " + e.getMessage());
            return null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("确定", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_feedback:
                startActivity(new Intent(this, FeedbackActivity.class));
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.action_smart:
                startActivity(new Intent(this, SmartAssistantActivity.class));
                return true;
            case R.id.action_calendar:
                openCalendarApp();
                return true;
            case R.id.action_map:
                Uri uri = Uri.parse("geo:36.899533,66.036476");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
                return true;

            case R.id.action_email:
//                Uri uri = Uri.parse("mailto:123456@qq.com");
//                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
//                startActivity(intent);//todo：1.跳转页面，自行输入邮箱和内容，再跳转第三方应用 2.用户输入“计算器”时，自动打开计算器应用
                startActivity(new Intent(this, EmailActivity.class));
                return true;
            case R.id.action_reward_author:
                showImageViewerDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showImageViewerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_image_viewer, null);
        ViewPager viewPager = view.findViewById(R.id.viewPager);
        ImageView btnClose = view.findViewById(R.id.btnClose);

        ImageViewPagerAdapter adapter = new ImageViewPagerAdapter(imageList, this);
        viewPager.setAdapter(adapter);

        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        builder.setView(view);
        dialog = builder.create();
        dialog.show();
    }
    private void openCalculatorApp() {
        String calculatorPackage = null;
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo applicationInfo : appInfos) {
            String packageName = applicationInfo.packageName;
            if (packageName.contains("calculator")) {
                calculatorPackage = packageName;
                break;
            }
        }
        if (calculatorPackage != null) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(calculatorPackage);
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "无法启动计算器应用", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "找不到计算器应用", Toast.LENGTH_SHORT).show();
        }
    }
    private void openNoteApp() {
        String notePackage = null;
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo applicationInfo : appInfos) {
            String packageName = applicationInfo.packageName;
            if (packageName.contains("note")) {
                notePackage = packageName;
                break;
            }
        }
        if (notePackage != null) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(notePackage);
            if (intent != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "无法启动笔记应用", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "找不到笔记应用", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCalendarApp() {
        // 打开日历应用
        Intent calendarIntent = getPackageManager().getLaunchIntentForPackage("com.bbk.calendar");
        if (calendarIntent != null) {
            startActivity(calendarIntent);
        } else {
            Toast.makeText(this, "找不到日历应用", Toast.LENGTH_SHORT).show();
        }

    }
    private void openSettingApp() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        startActivity(intent);
    }
    private void openMap() {
        Uri uri = Uri.parse("geo:36.899533,66.036476");
        Intent intent = new Intent(Intent.ACTION_VIEW,uri);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD_MR1)
    private void startRecording() {
        try {
            audioFile = new File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recorded_audio.mp4");
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioChannels(1);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioEncodingBitRate(192000);
            recorder.setOutputFile(audioFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            Toast.makeText(this, "开始录音", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "录音失败", Toast.LENGTH_SHORT).show();
        }
    }



    private void stopRecording() {
        if (recorder != null) {
            recorder.stop();
            recorder.release();
            recorder = null;
            Toast.makeText(this, "录音结束", Toast.LENGTH_SHORT).show();
        }
    }

    public void downloadAndPlayAudio(final String path, final String fileName) {
        new DownloadAudioTask().execute(path, fileName);
    }

    private class DownloadAudioTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            String path = params[0];
            String fileName = params[1];

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3000, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(3000, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(3000, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            try {
                URL url = new URL(path);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setReadTimeout(5000);
                con.setConnectTimeout(5000);
                con.setRequestProperty("Charset", "UTF-8");
                con.setRequestMethod("GET");

                if (con.getResponseCode() == 200) {
                    InputStream is = con.getInputStream();
                    FileOutputStream fileOutputStream = null;
                    File audioFile = null;

                    if (is != null) {
                        audioFile = FileLoadUtils.createFile(fileName);
                        fileOutputStream = new FileOutputStream(audioFile);

                        byte[] buf = new byte[1024];
                        int ch;
                        while ((ch = is.read(buf)) != -1) {
                            fileOutputStream.write(buf, 0, ch);
                        }
                    }

                    if (fileOutputStream != null) {
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    }

                    return audioFile != null ? audioFile.getPath() : null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(String filePath) {
            if (filePath != null) {
                Toast.makeText(getApplicationContext(), "下载成功", Toast.LENGTH_SHORT).show();
                playAudio(filePath);
            } else {
                Toast.makeText(getApplicationContext(), "下载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadAudio() {
        if (audioFile == null || !audioFile.exists() || !audioFile.isFile()) {
            System.out.println("Invalid audio file. Please select a valid file.");
            return;
        }

        new UploadAudioTask().execute(audioFile);
    }

    private class UploadAudioTask extends AsyncTask<File, Void, String> {
        @Override
        protected String doInBackground(File... files) {
            File audioFile = files[0];

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30000, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            RequestBody fileBody = RequestBody.create(MediaType.parse("audio/*"), audioFile);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", audioFile.getName(), fileBody)
                    .build();

            Request request = new Request.Builder()
                    .url("http://192.168.99.108:8000/audio")
                    .post(requestBody)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                return responseBody;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String responseBody) {
            if (responseBody != null) {
                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String question = jsonResponse.getString("transcription");
                    String answer = jsonResponse.getString("answer");

                    // 插入到数据库
                    databaseHelper.insertAnswer(question, answer);

                    // 在主线程中运行
                    downloadAndPlayAudio("http://192.168.99.108:8000/download_audio", "res_audio.mp3");
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("MainActivity", "请求异常: " + e.getMessage());
                }
            } else {
                Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void playAudio(String filePath) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(MediaPlayer::start);
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(getApplicationContext(), "播放音频出错: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
            return true;
        });
        mediaPlayer.setOnCompletionListener(mp -> Toast.makeText(getApplicationContext(), "音频播放完成", Toast.LENGTH_SHORT).show());
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepareAsync();  // 使用异步准备以避免阻塞UI线程
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}



