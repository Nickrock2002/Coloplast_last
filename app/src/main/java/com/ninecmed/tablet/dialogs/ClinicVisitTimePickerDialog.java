package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TimePicker;

import com.ninecmed.tablet.databinding.DialogTimePickerBinding;

import java.util.Calendar;

public class ClinicVisitTimePickerDialog extends BaseDialog {
    private TimePicker.OnTimeChangedListener timeChangedListener = null;
    private View.OnClickListener confirmButtonListener = null;

    public ClinicVisitTimePickerDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogTimePickerBinding binding = DialogTimePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set to true if you want 24-hour format
        binding.timePicker.setIs24HourView(false);

        // Set a default time (optional)
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        binding.timePicker.setHour(hour);
        binding.timePicker.setMinute(minute);

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
