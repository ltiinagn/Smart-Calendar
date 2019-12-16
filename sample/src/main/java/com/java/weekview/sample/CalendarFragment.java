package com.java.weekview.sample;

import android.support.v4.app.Fragment;


public class CalendarFragment extends Fragment {

  protected static String TAG = CalendarFragment.class.getName();

  private static CalendarFragment instance;

  public static CalendarFragment getInstance() {
    if (instance == null) {
      instance = new CalendarFragment();
    }
    return instance;
  }
}
