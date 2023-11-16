package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogDatePickerBinding;

import java.util.Calendar;
import java.util.Date;

public class ClinicVisitDatePickerDialog extends BaseDialog {
    private DatePicker.OnDateChangedListener dateChangedListener = null;
    private View.OnClickListener confirmButtonListener = null;
    int year;
    int month;
    int day;
    String formattedDate;

    public ClinicVisitDatePickerDialog(Context context, String formattedDate) {
        super(context);
        this.formattedDate = formattedDate;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogDatePickerBinding binding = DialogDatePickerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Date date = Utility.getDateFromFormat(formattedDate);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        binding.datePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
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
