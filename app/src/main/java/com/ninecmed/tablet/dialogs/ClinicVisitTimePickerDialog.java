package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogTimePickerBinding;

import java.util.Calendar;
import java.util.Date;

public class ClinicVisitTimePickerDialog extends BaseDialog {
    private TimePicker.OnTimeChangedListener timeChangedListener = null;
    private View.OnClickListener confirmButtonListener = null;

    private final String formattedTime;

    public ClinicVisitTimePickerDialog(Context context, String formattedTime) {
        super(context);
        this.formattedTime = formattedTime;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogTimePickerBinding binding = DialogTimePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Calendar currentTime = Calendar.getInstance();
        Date date = Utility.getTimeFromFormat(formattedTime);
        currentTime.setTimeInMillis(date.getTime());

        binding.timePicker.setHour(currentTime.get(Calendar.HOUR_OF_DAY));
        binding.timePicker.setMinute(currentTime.get(Calendar.MINUTE));
        binding.timePicker.setIs24HourView(false);
        binding.timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);

        binding.timePicker.setOnTimeChangedListener(timeChangedListener);
        binding.btnConfirmTime.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setTimeChangeListener(TimePicker.OnTimeChangedListener timeChangeListener) {
        this.timeChangedListener = timeChangeListener;
    }
}
