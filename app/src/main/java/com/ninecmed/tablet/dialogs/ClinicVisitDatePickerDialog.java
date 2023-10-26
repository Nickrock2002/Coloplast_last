package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;

import com.ninecmed.tablet.databinding.DialogDatePickerBinding;

public class ClinicVisitDatePickerDialog extends BaseDialog {
    private DatePicker.OnDateChangedListener dateChangedListener = null;
    private View.OnClickListener confirmButtonListener = null;

    int year;
    int month;
    int day;

    public ClinicVisitDatePickerDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogDatePickerBinding binding = DialogDatePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        year = binding.datePicker.getYear();
        month = binding.datePicker.getMonth();
        day = binding.datePicker.getDayOfMonth();

        binding.datePicker.init(year, month, day, dateChangedListener);

        binding.btnConfirmDate.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setDateChangeListener(DatePicker.OnDateChangedListener dateChangedListener) {
        this.dateChangedListener = dateChangedListener;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }
}
