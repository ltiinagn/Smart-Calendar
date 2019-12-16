package com.java.weekview.sample;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.java.weekview.MonthLoader;
import com.java.weekview.WeekViewEvent;
import com.java.weekview.WeekViewLoader;
import com.java.weekview.WeekViewUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.java.weekview.WeekViewUtil.masterEvents;
import static com.java.weekview.WeekViewUtil.monthMasterEvents;
import static com.java.weekview.sample.BaseActivity.UserType;

public class EventDetailsActivity extends AppCompatActivity implements View.OnClickListener, MonthLoader.MonthLoaderListener{
  WeekViewEvent event;
  boolean isEditMode;
  private EditText title;
  private EditText startDate, startTime;
  private EditText endDate, endTime;
  private EditText location, activity;
  private WeekViewLoader mWeekViewLoader;
  private EditText Collaborator;
  private Calendar startCal;
  private Calendar endCal;
  private DatabaseReference mDataBase;
  private Spinner activity_type;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.event_details_layout);

    Intent intent = getIntent();
    if (intent.getExtras() != null) {
      Bundle bundle = intent.getExtras();
      if (bundle.containsKey("event")) {
        event = (WeekViewEvent) bundle.get("event");
      }

      if (bundle.containsKey("start")) {
        startCal = (Calendar) bundle.get("start");
        endCal = (Calendar) startCal.clone();
        endCal.setTimeInMillis(startCal.getTimeInMillis() + (1000 * 60 * 60 * 1));
      }
    }

    title =  findViewById(R.id.title);
    startDate = findViewById(R.id.start_date);
    endDate = findViewById(R.id.end_date);
    startTime = findViewById(R.id.start_time);
    endTime = findViewById(R.id.end_time);
    activity_type=findViewById(R.id.color_text);
    activity_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        int color=0;
        String type=activity_type.getSelectedItem().toString();
        if (type.equals("Class")) {color=R.color.White;}
        else if (type.equals("Homework")) {color=R.color.Green;}
        else if (type.equals("Project")) {color=R.color.LightOrange;}
        else if (type.equals("Exam")) {color=R.color.Blue;}
        else if (type.equals("Help Session")) {color=R.color.Yellow;}
        else if (type.equals("Consultation")) {color=R.color.LightGreen;}
        else if (type.equals("Fifth Row")) {color=R.color.Orange;}
        else if (type.equals("Others")) {color=R.color.Red;}
        findViewById(R.id.color_icon).setBackgroundColor(getResources().getColor(color));
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    final DatePickerDialog.OnDateSetListener start_date = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int day) {
        startCal.set(Calendar.YEAR, year);
        startCal.set(Calendar.MONTH, month);
        startCal.set(Calendar.DAY_OF_MONTH, day);
        setStartDate(startCal);
      }
    };
    startDate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new DatePickerDialog(EventDetailsActivity.this, R.style.DialogTheme, start_date, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)).show();
      }
    });

    final DatePickerDialog.OnDateSetListener end_date = new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int day) {
        endCal.set(Calendar.YEAR, year);
        endCal.set(Calendar.MONTH, month);
        endCal.set(Calendar.DAY_OF_MONTH, day);
        setEndDate(endCal);
      }
    };
    endDate.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new DatePickerDialog(EventDetailsActivity.this, R.style.DialogTheme, end_date, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)).show();
      }
    });

    startTime.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog TimePicker;
        TimePicker = new TimePickerDialog(EventDetailsActivity.this, R.style.DialogThemeTime, new TimePickerDialog.OnTimeSetListener() {
          @Override
          public void onTimeSet(TimePicker timePicker, int selHour, int selMinute) {
            startCal.set(Calendar.HOUR_OF_DAY, selHour);
            startCal.set(Calendar.MINUTE, selMinute);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            startTime.setText( String.format("%02d", selHour) + ":" + String.format("%02d", selMinute));
          }
        }, hour, minute, true);
        TimePicker.setTitle("Select Time");
        TimePicker.show();
      }
    });

    endTime.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog TimePicker;
        TimePicker = new TimePickerDialog(EventDetailsActivity.this, R.style.DialogThemeTime, new TimePickerDialog.OnTimeSetListener() {
          @Override
          public void onTimeSet(TimePicker timePicker, int selHour, int selMinute) {
            endCal.set(Calendar.HOUR_OF_DAY, selHour);
            endCal.set(Calendar.MINUTE, selMinute);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);
            endTime.setText( String.format("%02d", selHour) + ":" + String.format("%02d", selMinute));
          }
        }, hour, minute, true);
        TimePicker.setTitle("Select Time");
        TimePicker.show();
      }
    });

    Collaborator = findViewById(R.id.collaborators);
    location = findViewById(R.id.location);
    activity = findViewById(R.id.activity);

    if (event != null) { isEditMode = false; }
    else { isEditMode = true; }

    mWeekViewLoader = new MonthLoader(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();

    if (event != null) {
      setData();
      setStartEndTime(event.getStartTime(),event.getEndTime());
      createReadOnlyView();
    } else {
      setStartEndTime(startCal,endCal);
      createEditableView();
    }
    findViewById(R.id.save).setVisibility(View.VISIBLE);
    findViewById(R.id.save).setOnClickListener(this);
  }

  private void setData() {
    findViewById(R.id.title_layout).setBackgroundColor(event.getColor());
    title.setText(event.getName());
    findViewById(R.id.color_icon).setBackgroundColor(event.getColor());
    location.setText(event.getLocation());
    activity.setText(event.getActivity());
  }

  private void setStartEndTime(Calendar start, Calendar end) {
    int hour = start.get(Calendar.HOUR_OF_DAY);
    int minute = start.get(Calendar.MINUTE);
    String startTime=formatTime(start);
    int commaIndex = startTime.lastIndexOf(",");
    if (commaIndex != -1) {
      String date = startTime.substring(0, commaIndex);
      startDate.setText(date);
      this.startTime.setText( String.format("%02d", hour) + ":" + String.format("%02d", minute));
    } else {
      startDate.setText(startTime);
    }
    String endTime = formatTime(end);
    hour = end.get(Calendar.HOUR_OF_DAY);
    minute = end.get(Calendar.MINUTE);
    commaIndex = endTime.lastIndexOf(",");
    if (commaIndex != -1) {
      String date = endTime.substring(0, commaIndex);
      endDate.setText(date);
      this.endTime.setText( String.format("%02d", hour) + ":" + String.format("%02d", minute));
    }
    else { endDate.setText(endTime); }
  }

  private void setStartDate(Calendar start) {
    String startTime = formatTime(start);
    int commaIndex = startTime.lastIndexOf(",");
    String date = startTime.substring(0, commaIndex);
    String time = startTime.substring(commaIndex+1, startTime.length());
    startDate.setText(date);
  }

  private void setEndDate(Calendar end) {
    String endTime = formatTime(end);
    int commaIndex = endTime.lastIndexOf(",");
    String date = endTime.substring(0, commaIndex);
    String time = endTime.substring(commaIndex+1, endTime.length());
    endDate.setText(date);
  }

  private String formatTime(Calendar time) {
    SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy,hh:mm a");
//    Mon, Aug 15, 2016, 06:00 PM06:00 PM
    String msg = sdf.format(time.getTime());
    /*msg += String.format("%02d:%02d", time.get(Calendar.HOUR),
      time.get(Calendar.MINUTE));
    String AM_PM = " AM";
    if (time.get(Calendar.AM_PM) == Calendar.PM) {
      AM_PM = " PM";
    }
    msg += AM_PM;*/
    return msg;
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.save) {
      String tag = (String) v.getTag();
      if (tag != null) {
        if (tag.equals("delete")) {
          Calendar eventCal=event.getStartTime();
          String monthKey=eventCal.get(Calendar.MONTH)+ "-" + eventCal.get(Calendar.YEAR);
          String eventTime="";
          for (Map.Entry<String, WeekViewEvent> entry : WeekViewUtil.masterEvents.entrySet()) {
            Log.i("entryout",entry.getKey());
            Log.i("entryevent",event.getStartTime().toString());
            Log.i("entryevent",entry.getValue().getStartTime().toString());
            if (event.getStartTime().toString().equals(entry.getValue().getStartTime().toString())) {
              eventTime=entry.getKey();
            }
          }
          WeekViewUtil.masterEvents.remove(eventTime);
          if (!UserType.equals("Student")) {
            DatabaseReference dR = FirebaseDatabase.getInstance().getReference("masterEvents").child(eventTime);
            dR.removeValue();
          }

          Log.i("tostring",Long.toString(System.currentTimeMillis()));
          List<WeekViewEvent> eventListByMonth = WeekViewUtil.monthMasterEvents.get(monthKey);
          eventListByMonth.remove(event);
          WeekViewUtil.monthMasterEvents.put(monthKey, eventListByMonth);

          Log.i("masterEventshere",masterEvents.toString());
          Log.i("monthMasterEventshere",monthMasterEvents.toString());

          SharedPreferences prefs = getSharedPreferences("saved", MODE_PRIVATE);
          SharedPreferences prefs2 = getSharedPreferences("saved2", MODE_PRIVATE);
          SharedPreferences.Editor editor=prefs.edit();
          SharedPreferences.Editor editor2=prefs2.edit();
          Gson gson=new Gson();
          Gson gson2=new Gson();
          String json=gson.toJson(WeekViewUtil.masterEvents);
          String json2=gson2.toJson(WeekViewUtil.monthMasterEvents);
          editor.putString("masterEvents", json).apply();
          editor2.putString("monthMasterEvents", json2).apply();
          /*
          if (!UserType.equals("Student")) {
            Gson gson=new Gson();
            String json=gson.toJson(event);
            mDataBase = FirebaseDatabase.getInstance().getReference();
            mDataBase.child("masterEvents").child().removeValue();
          }
          */

          setResult(RESULT_OK);
          finish();
        }
        else if (tag.equals("save")) {
          String title = this.title.getText().toString();
          if (title == null || title.isEmpty()) {
            Toast.makeText(this, "Please enter event title", Toast.LENGTH_SHORT).show();
            return;
          }

          String location = this.location.getText().toString();
          if (location == null || location.isEmpty()) {
            Toast.makeText(this, "Please enter event location", Toast.LENGTH_SHORT).show();
            return;
          }

          Calendar startTime = startCal;
          Calendar endTime = endCal;

          WeekViewEvent createdEvent;

          if (event == null) {
            createdEvent = new WeekViewEvent(WeekViewUtil.eventId++, title, startTime, endTime);
          } else {
            createdEvent = new WeekViewEvent(event.getId(), title, startTime, endTime);
          }

          String type = this.activity_type.getSelectedItem().toString();
          int color=0;
          if (type.equals("Class")) {color=R.color.White;}
          else if (type.equals("Homework")) {color=R.color.Green;}
          else if (type.equals("Project")) {color=R.color.LightOrange;}
          else if (type.equals("Exam")) {color=R.color.Blue;}
          else if (type.equals("Help Session")) {color=R.color.Yellow;}
          else if (type.equals("Consultation")) {color=R.color.LightGreen;}
          else if (type.equals("Fifth Row")) {color=R.color.Orange;}
          else if (type.equals("Others")) {color=R.color.Red;}
          createdEvent.setColor(ContextCompat.getColor(this, color));

          createdEvent.setLocation(location);

          String activity = this.activity.getText().toString();
          if (activity != null && !activity.isEmpty()) {
            createdEvent.setActivity(activity);
          }

          if (event != null && !event.getStartTime().equals(startTime)) {
            int periodToFetch = (int) mWeekViewLoader.toWeekViewPeriodIndex(event.getStartTime());
            int year = periodToFetch / 12;
            int month = periodToFetch % 12 + 1;
            String monthKey = "" + (month - 1) + "-" + year;

            List<WeekViewEvent> eventListByMonth = WeekViewUtil.monthMasterEvents.get(monthKey);

            if (eventListByMonth != null && eventListByMonth.contains(event)) {
              eventListByMonth.remove(event);
            }
            WeekViewUtil.monthMasterEvents.put(monthKey, eventListByMonth);
          }

          WeekViewUtil.masterEvents.put(Long.toString(System.currentTimeMillis()), createdEvent);

          int periodToFetch = (int) mWeekViewLoader.toWeekViewPeriodIndex(startTime);
          int year = periodToFetch / 12;
          int month = periodToFetch % 12 + 1;
          String monthKey = "" + (month - 1) + "-" + year;

          List<WeekViewEvent> eventListByMonth = WeekViewUtil.monthMasterEvents.get(monthKey);
          if (eventListByMonth == null) {
            eventListByMonth = new ArrayList<>();
          }
          eventListByMonth.add(createdEvent);
          WeekViewUtil.monthMasterEvents.put(monthKey, eventListByMonth);
          if (!UserType.equals("Student")) {
            Gson gson=new Gson();
            String json=gson.toJson(createdEvent);
            mDataBase = FirebaseDatabase.getInstance().getReference();
            mDataBase.child("masterEvents").child(Long.toString(System.currentTimeMillis())).setValue(json);
          }
          setResult(RESULT_OK);
          finish();
        }
      }
    } else if (v.getId() == R.id.start_date_time) {

    } else if (v.getId() == R.id.end_date_time) {

    }
  }

  @Override
  public List<WeekViewEvent> onMonthLoad(int newYear, int newMonth) {
    return null;
  }

  private void createReadOnlyView() {
    title.setEnabled(false);
    startDate.setEnabled(false);
    startTime.setEnabled(false);
    endDate.setEnabled(false);
    endTime.setEnabled(false);
    location.setEnabled(false);
    activity.setEnabled(false);
    findViewById(R.id.save).setTag("delete");
    ((Button) findViewById(R.id.save)).setText("Delete");
  }

  private void createEditableView() {
    title.setEnabled(true);
    startDate.setEnabled(true);
    startTime.setEnabled(true);
    endDate.setEnabled(true);
    endTime.setEnabled(true);
    location.setEnabled(true);
    activity.setEnabled(true);
    findViewById(R.id.save).setTag("save");
    ((Button) findViewById(R.id.save)).setText("Save");
    findViewById(R.id.start_date_time).setOnClickListener(this);
    findViewById(R.id.end_date_time).setOnClickListener(this);
  }
}