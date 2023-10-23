package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogSetTimeTherapyBinding;

import java.util.Calendar;

public class ProgramTherapyTimeOfDayDialog extends BaseDialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;
    private final long timeDiff;
    private final int hour;
    private final int min;

    public ProgramTherapyTimeOfDayDialog(Context context, long timeDifferenceMillis, int lastSetHour, int lastSetMinute) {
        super(context);
        this.timeDiff = timeDifferenceMillis;
        this.hour = lastSetHour;
        this.min = lastSetMinute;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogSetTimeTherapyBinding binding = DialogSetTimeTherapyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);

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
