package com.java.weekview.sample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.java.weekview.DateTimeInterpreter;
import com.java.weekview.Day;
import com.java.weekview.EventRect;
import com.java.weekview.ExtendedCalendarView;
import com.java.weekview.ExtendedCalendarView.OnDayClickListener;
import com.java.weekview.MonthLoader;
import com.java.weekview.WeekView;
import com.java.weekview.WeekViewEvent;
import com.java.weekview.WeekViewUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static com.java.weekview.WeekViewUtil.masterEvents;
import static com.java.weekview.WeekViewUtil.monthMasterEvents;

public abstract class BaseActivity extends AppCompatActivity implements WeekView.EventClickListener, MonthLoader.MonthLoaderListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener,
OnDayClickListener, View.OnClickListener, WeekView.EmptyViewClickListener, ExtendedCalendarView.OnMonthChangeListener {
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_WEEK_VIEW = 3;
    private static final int TYPE_MONTH_VIEW = 4;
    private int mWeekViewType = TYPE_MONTH_VIEW;
    private WeekView mWeekView;
    private ExtendedCalendarView calendar;
    private EventAdapter eventAdapter;
    private AppBarLayout mAppBarLayout;
    private Day DaySel;
    private DatabaseReference mDataBase;
    private DatabaseReference mFireBaseUser;
    public static String UserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        String currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser().getUid().toString() ;
        mDataBase = FirebaseDatabase.getInstance().getReference();
        mFireBaseUser=mDataBase.child(currentFirebaseUser);
        mFireBaseUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserType = (String) dataSnapshot.child("auth").getValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mAppBarLayout = findViewById(R.id.app_bar_layout);
        mWeekView = findViewById(R.id.weekView);
        calendar = findViewById(R.id.calendar);
        calendar.setOnDayClickListener(this);
        calendar.setMonthLoaderListener(this);
        calendar.setOnMonthChangeListener(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

        SharedPreferences prefs = getSharedPreferences("saved", MODE_PRIVATE);
        SharedPreferences prefs2 = getSharedPreferences("saved2", MODE_PRIVATE);

        Gson gson=new Gson();
        Gson gson2=new Gson();
        String json = prefs.getString("masterEvents", "");
        String json2 = prefs2.getString("monthMasterEvents", "");

        if (!json.isEmpty() && !json2.isEmpty()){
            java.lang.reflect.Type type = new TypeToken<HashMap<String, WeekViewEvent>>(){}.getType();
            java.lang.reflect.Type type2 = new TypeToken<HashMap<String, List<WeekViewEvent>>>(){}.getType();
            masterEvents=gson.fromJson(json, type);
            monthMasterEvents=gson2.fromJson(json2, type2);
        }

        mDataBase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot s: dataSnapshot.child("masterEvents").getChildren()) {
                    java.lang.reflect.Type type = new TypeToken<WeekViewEvent>(){}.getType();
                    WeekViewEvent week=new Gson().fromJson(s.getValue(String.class), type);
                    String time =s.getKey();
                    if (!masterEvents.containsKey(time)) {
                        masterEvents.put(s.getKey(), week);
                        String month = week.getStartTime().get(Calendar.MONTH) + "-" + week.getStartTime().get(Calendar.YEAR);
                        List<WeekViewEvent> a = monthMasterEvents.get(month);
                        if (a == null) {
                            a = new ArrayList<WeekViewEvent>();
                        }
                        a.add(week);
                        monthMasterEvents.put(month, a);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        FloatingActionButton addEvent=findViewById(R.id.fab);
        addEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                if (DaySel!=null) {
                    cal.set(DaySel.getYear(), DaySel.getMonth(), DaySel.getDay());
                }
                else {
                    cal.setTimeInMillis(new Date().getTime());
                }
                showEventDetailsScreen(null, cal);
            }
        });

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthLoaderListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);
        mWeekView.setEmptyViewClickListener(this);

        mWeekViewType = TYPE_MONTH_VIEW;
        mWeekView.setVisibility(View.GONE);
        mAppBarLayout.setVisibility(View.VISIBLE);
        updateView();

        setupDateTimeInterpreter(false);
        mWeekView.setVisibility(View.VISIBLE);
        mWeekViewType = TYPE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(1);
        // Lets change some dimensions to best fit the view.
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
    }

    @Override
    protected void onPause(){
        super.onPause();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {
        if(view!=null) {
            view.setBackgroundResource(R.drawable.normal_day);
        }
        DaySel=day;
        buildEventList(day);
    }

    private void buildEventList(Day day) {
        List<WeekViewEvent> events = day.events;
        if (eventAdapter == null) {
            eventAdapter = new EventAdapter(this, events, this);
        } else {
            eventAdapter.setData(events);
        }
    }

    @Override
    public void onMonthChange(Calendar cal, List<EventRect> mEventRects) {
        if (calendar.mAdapter != null) {
            if (calendar.mAdapter.currentDay != null) {
                buildEventList(calendar.mAdapter.currentDay);
            }
        }
        String name = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())+" "+cal.get(Calendar.YEAR);
        SpannableString s = new SpannableString(name);
        s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setTitle(s);
    }

    public void updateView() {
        calendar.setGesture(ExtendedCalendarView.LEFT_RIGHT_GESTURE);
        calendar.getEvents();
        calendar.refreshCalendar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    TextView monthView, weekView;
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem actionViewItem = menu.findItem(R.id.calendar_action);

        // Retrieve the action-view from menu
        View v = MenuItemCompat.getActionView(actionViewItem);

        // Find the button within action-view
        monthView = (TextView) v.findViewById(R.id.month_view);
        monthView.setOnClickListener(this);
        weekView = (TextView) v.findViewById(R.id.week_view);
        weekView.setOnClickListener(this);

        monthView.setBackground(ContextCompat.getDrawable(this, R.drawable.today));
        weekView.setBackground(ContextCompat.getDrawable(this, R.drawable.normal_day));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
//        setupDateTimeInterpreter(id == R.id.action_week_view);
        switch (id){
            /*case R.id.action_today:
                if (mWeekView.getVisibility() == View.VISIBLE) {
                    mWeekView.goToToday();
                }
                return true;*/
           /* case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    mWeekView.setVisibility(View.VISIBLE);
                    calendar.setVisibility(View.GONE);
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;*/
            /*case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    mWeekView.setVisibility(View.VISIBLE);
                    calendar.setVisibility(View.GONE);
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;*/
            /*case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    mWeekView.setVisibility(View.VISIBLE);
                    calendar.setVisibility(View.GONE);
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_month_view:
                if (mWeekViewType != TYPE_MONTH_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_MONTH_VIEW;
                    mWeekView.setVisibility(View.GONE);
                    calendar.setVisibility(View.VISIBLE);

                    updateView();
                }
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    protected String getEventTitle(Calendar time, Calendar endTime) {
        return String.format("Event of %02d:%02d %s/%d :: %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH),
          endTime.get(Calendar.HOUR_OF_DAY), endTime.get(Calendar.MINUTE), endTime.get(Calendar.MONTH)+1, endTime.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
        showEventDetailsScreen(event, null);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time, time), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        Toast.makeText(this, "Empty view clicked: " + getEventTitle(time, time), Toast.LENGTH_SHORT).show();
        showEventDetailsScreen(null, time);
    }

    public WeekView getWeekView() {
        return mWeekView;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.month_view) {
            if (mWeekViewType != TYPE_MONTH_VIEW) {
                mWeekViewType = TYPE_MONTH_VIEW;
                mWeekView.setVisibility(View.GONE);
                mAppBarLayout.setVisibility(View.VISIBLE);
                updateView();
                monthView.setBackground(ContextCompat.getDrawable(this, R.drawable.today));
                weekView.setBackground(ContextCompat.getDrawable(this, R.drawable.normal_day));

                setupDateTimeInterpreter(false);
                mWeekView.setVisibility(View.VISIBLE);
                mWeekViewType = TYPE_DAY_VIEW;
                mWeekView.setNumberOfVisibleDays(1);
                // Lets change some dimensions to best fit the view.
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
            }
        }
        else if (v.getId() == R.id.week_view) {
            if (mWeekViewType != TYPE_WEEK_VIEW) {
                setupDateTimeInterpreter(true);
                mWeekView.setVisibility(View.VISIBLE);
               // calendarViewLayout.setVisibility(View.GONE);
                mAppBarLayout.setVisibility(View.GONE);
//                calendar.setVisibility(View.GONE);
                mWeekViewType = TYPE_WEEK_VIEW;
                mWeekView.setNumberOfVisibleDays(3);

                //getSupportActionBar().setTitle(getTitle());
                // Lets change some dimensions to best fit the view.
                mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));

                monthView.setBackground(ContextCompat.getDrawable(this, R.drawable.normal_day));
                weekView.setBackground(ContextCompat.getDrawable(this, R.drawable.today));
            }
        }
        else if (v.getId() == android.R.id.text1) {
            WeekViewEvent event = (WeekViewEvent) v.getTag();
            showEventDetailsScreen(event, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (mWeekViewType == TYPE_MONTH_VIEW) {
                updateView();
                mWeekView.setRefreshEvents(true);
            } else if (mWeekViewType == TYPE_WEEK_VIEW) {
                mWeekView.notifyDatasetChanged();
            } else if (mWeekViewType == TYPE_DAY_VIEW) {
                mWeekView.notifyDatasetChanged();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showEventDetailsScreen(WeekViewEvent event, Calendar startTime) {
        Intent intent = new Intent(this, EventDetailsActivity.class);
        Bundle bundle = new Bundle();
        if (event != null) { bundle.putSerializable("event",event); }
        else if (startTime != null) { bundle.putSerializable("start",startTime); }
        intent.putExtras(bundle);
        startActivityForResult(intent, 1);
    }
}
