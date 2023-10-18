package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;

import java.util.Calendar;
import java.util.Objects;

public class ProgramTherapyDayDateDialogue extends Dialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    private long timeDiff = 0L;

    public ProgramTherapyDayDateDialogue(Context context, long timeDifferenceMillis) {
        super(context);
        this.timeDiff = timeDifferenceMillis;
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

        Button btCancel = findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(cancelButtonListener);

        Button btConfirm = findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);

        DatePicker datePicker = findViewById(R.id.datePicker);

        // Set max date to 31 days in the future, accounting for time difference
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 31);
        maxDate.setTimeInMillis(maxDate.getTimeInMillis() + timeDiff);
        datePicker.setMaxDate(maxDate.getTimeInMillis());

        // Set min date to today (disable past dates), accounting for time difference
        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(minDate.getTimeInMillis() + timeDiff);
        datePicker.setMinDate(minDate.getTimeInMillis());
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
