package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogSetTimeTherapyBinding;

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
        DialogSetTimeTherapyBinding binding = DialogSetTimeTherapyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);

        binding.timePicker.setIs24HourView(false);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis() + timeDiff);
//        if (hour == 0) {
//            binding.timePicker.setHour(cal.get(Calendar.HOUR_OF_DAY));
//        } else {
//            binding.timePicker.setHour(hour);
//        }
        binding.timePicker.setHour(hour);
//
//        if (min == 0) {
//            binding.timePicker.setMinute(cal.get(Calendar.MINUTE));
//        } else {
//            binding.timePicker.setMinute(min);
//        }
        binding.timePicker.setMinute(min);
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
