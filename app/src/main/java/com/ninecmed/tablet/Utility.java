package com.ninecmed.tablet;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;

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
}
