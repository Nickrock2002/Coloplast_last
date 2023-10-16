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
import android.widget.TimePicker;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;

import java.util.Calendar;
import java.util.Objects;

public class ProgramTherapyTimeOfDayDialogue extends Dialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    private final long timeDiff;
    private final int hour;
    private final int min;

    public ProgramTherapyTimeOfDayDialogue(Context context, long timeDifferenceMillis, int lastSetHour, int lastSetMinute) {
        super(context);
        this.timeDiff = timeDifferenceMillis;
        this.hour = lastSetHour;
        this.min = lastSetMinute;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_set_time_therapy);

        Button btCancel = findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(cancelButtonListener);

        Button btConfirm = findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis() + timeDiff);
//        if (hour == 0) {
//            timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
//        } else {
//            timePicker.setHour(hour);
//        }
        timePicker.setHour(hour);
//
//        if (min == 0) {
//            timePicker.setMinute(cal.get(Calendar.MINUTE));
//        } else {
//            timePicker.setMinute(min);
//        }
        timePicker.setMinute(min);
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
