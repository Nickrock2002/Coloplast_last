package com.ninecmed.tablet;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.View;
import android.view.Window;

public class Utility {
    static Pair<Integer, Integer> getDimensionsForDialogue(Context context) {
        Pair<Integer, Integer> dimensions = null;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dimensions = new Pair<>((int) (width * 0.7), (int) (height * 0.5));

        return dimensions;
    }
}
