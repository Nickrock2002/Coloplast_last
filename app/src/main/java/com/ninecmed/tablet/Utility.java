package com.ninecmed.tablet;

import static android.content.Context.MODE_PRIVATE;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utility {
    static SimpleDateFormat dateFormatForClinicVisit = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    static SimpleDateFormat dateFormatForHeaderDisplay = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
    static SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public static Pair<Integer, Integer> getDimensionsForDialogue(Context context) {
        Pair<Integer, Integer> dimensions;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dimensions = new Pair<>((int) (width * 0.95), (int) (height * 0.7));

        return dimensions;
    }

    public static void setTheSystemButtonsHidden(Dialog dialog) {
        // Hide the system navigation bar
        View decorView = dialog.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public static Pair<String, String> getTimeAndDateForFirstTimeHam(long timeInMilis) {
        Pair<String, String> dateTimePair;
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeMillis = currentCalendar.getTimeInMillis() + timeInMilis;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        dateTimePair = new Pair<>(dateFormat.format(currentTimeMillis), timeFormat.format(currentTimeMillis));
        return dateTimePair;
    }

    public static String getFormattedDate(Date date) {
        return dateFormatForClinicVisit.format(date);
    }

    public static String getFormattedDateForHeader(Date date) {
        return dateFormatForHeaderDisplay.format(date);
    }

    public static Date getDateFromFormat(String formattedDate) {
        Date date;
        try {
            date = dateFormatForClinicVisit.parse(formattedDate);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public static String getFormattedTime(Date date) {
        // Format the time in 12-hour format with AM/PM
        return timeFormat.format(date);
    }

    public static Date getTimeFromFormat(String formattedTime) {
        Date date;
        try {
            date = timeFormat.parse(formattedTime);
        } catch (ParseException e) {
            date = new Date();
        }
        return date;
    }

    public static long getTimeDifferenceInSharedPref(Context context) {
        SharedPreferences sh = context.getSharedPreferences("MySharedPref", MODE_PRIVATE);
        return sh.getLong("time_diff", 0);
    }

    public static void setTimeDifferenceInSharedPref(Context context, long timeDiff) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MySharedPref", MODE_PRIVATE);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putLong("time_diff", timeDiff);
        myEdit.apply();
    }
}
