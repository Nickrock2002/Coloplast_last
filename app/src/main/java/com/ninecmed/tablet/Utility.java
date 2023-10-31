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
    public static Pair<Integer, Integer> getDimensionsForDialogue(Context context) {
        Pair<Integer, Integer> dimensions;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dimensions = new Pair<>((int) (width * 0.95), (int) (height * 0.7));

        return dimensions;
    }

    public static void setTheSystemButtonsHidden(Dialog dialog){
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
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());

        dateTimePair = new Pair<>(dateFormat.format(currentTimeMillis), timeFormat.format(currentTimeMillis));
        return dateTimePair;
    }
}
