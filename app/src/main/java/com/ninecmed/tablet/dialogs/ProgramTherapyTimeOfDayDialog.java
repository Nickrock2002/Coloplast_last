package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

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

        binding.timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
        binding.timePicker.setIs24HourView(false);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(cal.getTimeInMillis() + timeDiff);
        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(min);
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
