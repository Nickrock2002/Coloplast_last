package com.ninecmed.tablet;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Utility {
    static Pair<Integer, Integer> getDimensionsForDialogue(Context context) {
        Pair<Integer, Integer> dimensions = null;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dimensions = new Pair<>((int) (width * 0.8), (int) (height * 0.7));

        return dimensions;
    }

    public static void setTheSystemButtonsHidden(Dialog dialog){
        // Hide the system navigation bar
        View decorView = dialog.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    static Pair<String, String> getTimeAndDateForFirstTime(long timeInMilis) {
        Pair<String, String> dateTimePair;
        // Get the current date and time from the device
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeMillis = currentCalendar.getTimeInMillis() + timeInMilis;

        // Format the time in "2:00 PM" format
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        // Format the date in "01/10/2023" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());

        dateTimePair = new Pair<>(dateFormat.format(currentTimeMillis), timeFormat.format(currentTimeMillis));
        return dateTimePair;
    }
}
