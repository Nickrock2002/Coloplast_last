package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import com.ninecmed.tablet.BuildConfig;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogAboutBinding;

import java.util.Objects;

public class BaseDialog extends Dialog {

    public BaseDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }
}
