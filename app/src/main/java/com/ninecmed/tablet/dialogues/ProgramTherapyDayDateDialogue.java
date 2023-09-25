package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.core.app.ActivityCompat;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.RadioGroupPlus;
import com.ninecmed.tablet.Utility;

import java.util.Objects;

public class ProgramTherapyDayDateDialogue extends Dialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    public ProgramTherapyDayDateDialogue(Context context) {
        super(context);
    }

    public ProgramTherapyDayDateDialogue(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ProgramTherapyDayDateDialogue(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_set_start_day_therapy);

        Button btCancel = (Button) findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(cancelButtonListener);

        Button btConfirm = (Button) findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}