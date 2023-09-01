package com.ninecmed.tablet;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Pair;

public class Utility {
    static Pair<Integer, Integer> getDimensionsForDialogue(Context context) {
        Pair<Integer, Integer> dimensions = null;
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        dimensions = new Pair<>((int) (width * 0.8), (int) (height * 0.7));

        return dimensions;
    }
}
