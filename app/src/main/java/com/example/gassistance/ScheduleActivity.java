package com.example.gassistance;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity implements EventAdapter.OnItemLongClickListener {
    private DatePicker datePicker;
    private List<String> eventList;
    private EventAdapter eventAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        datePicker = findViewById(R.id.datePicker);
        RecyclerView eventRecyclerView = findViewById(R.id.eventRecyclerView);
        EditText eventEditText = findViewById(R.id.eventEditText);
        TimePicker timePicker = findViewById(R.id.timePicker);
        Button addEventButton = findViewById(R.id.addEventButton);

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList);
        eventAdapter.setOnItemLongClickListener(this); // 设置长按监听器
        eventRecyclerView.setAdapter(eventAdapter);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 添加日期选择监听器
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                updateSchedule(year, monthOfYear, dayOfMonth); // 当日期改变时更新日程表
            }
        });

        addEventButton.setOnClickListener(v -> {
            String eventTitle = eventEditText.getText().toString().trim();
            int hour = timePicker.getCurrentHour();
            int minute = timePicker.getCurrentMinute();
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int dayOfMonth = datePicker.getDayOfMonth();

            Calendar startTimeCalendar = Calendar.getInstance();
            startTimeCalendar.set(year, month, dayOfMonth, hour, minute);

            Calendar endTimeCalendar = (Calendar) startTimeCalendar.clone();
            endTimeCalendar.add(Calendar.HOUR_OF_DAY, 1);

            addEventToCalendar(eventTitle, startTimeCalendar.getTimeInMillis(), endTimeCalendar.getTimeInMillis());

            eventEditText.setText(""); // 清空输入框
        });

        // 初始时显示当前日期的日程
        updateSchedule(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
    }


    private void addEventToCalendar(String title, long startTime, long endTime) {
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DTSTART, startTime);
        values.put(CalendarContract.Events.DTEND, endTime);
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, Calendar.getInstance().getTimeZone().getID());
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        if (uri != null) {
            Toast.makeText(this, "事件已成功添加到日历", Toast.LENGTH_SHORT).show();
            updateSchedule(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()); // 添加这行
        } else {
            Toast.makeText(this, "添加事件到日历失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void updateSchedule(int year, int month, int dayOfMonth) {
        String selectedDate = getDate(year, month, dayOfMonth);
        eventList.clear();
        eventList.addAll(getSchedule(selectedDate));
        eventAdapter.notifyDataSetChanged();
    }

    private String getDate(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private List<String> getSchedule(String date) {
        List<String> scheduleList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        long startOfDay = getStartOfDay(date);
        long endOfDay = getEndOfDay(date);

        Cursor cursor = contentResolver.query(uri,
                new String[]{CalendarContract.Events.TITLE},
                CalendarContract.Events.DTSTART + " >= ? AND " + CalendarContract.Events.DTEND + " <= ?",
                new String[]{String.valueOf(startOfDay), String.valueOf(endOfDay)},
                null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
                scheduleList.add(title);
            }
            cursor.close();
        }
        return scheduleList;
    }

    private long getStartOfDay(String date) {
        Calendar calendar = Calendar.getInstance();
        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int day = Integer.parseInt(dateParts[2]);
        calendar.set(year, month, day, 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    private long getEndOfDay(String date) {
        Calendar calendar = Calendar.getInstance();
        String[] dateParts = date.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1;
        int day = Integer.parseInt(dateParts[2]);
        calendar.set(year, month, day, 23, 59, 59);
        return calendar.getTimeInMillis();
    }

    @Override
    public void onItemLongClick(int position) {
        // 当日程项长按时触发，删除对应的日程
        removeEvent(position);
    }

    private void removeEvent(int position) {
        // 从日程列表中移除对应位置的日程
        String removedEvent = eventList.remove(position);
        eventAdapter.notifyItemRemoved(position);

        // 从系统日历中删除对应的日程
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(CalendarContract.Events.CONTENT_URI,
                new String[]{CalendarContract.Events._ID},
                CalendarContract.Events.TITLE + "=?",
                new String[]{removedEvent},
                null);

        if (cursor != null && cursor.moveToFirst()) {
            long eventId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
            Uri deleteUri = Uri.withAppendedPath(CalendarContract.Events.CONTENT_URI, String.valueOf(eventId));
            int rows = contentResolver.delete(deleteUri, null, null);
            if (rows > 0) {
                Toast.makeText(this, "已删除日程：" + removedEvent, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "删除日程失败：" + removedEvent, Toast.LENGTH_SHORT).show();
            }
            cursor.close();
        }
    }
}
