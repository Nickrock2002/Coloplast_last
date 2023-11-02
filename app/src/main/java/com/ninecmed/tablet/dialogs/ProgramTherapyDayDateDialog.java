package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogSetStartDayTherapyBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProgramTherapyDayDateDialog extends BaseDialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;
    private final long timeDiff;
    private final String dateStr;
    private final boolean isFrequencyAuto;

    public ProgramTherapyDayDateDialog(Context context, long timeDifferenceMillis, String dateStr, boolean isFreqAuto) {
        super(context);
        this.timeDiff = timeDifferenceMillis;
        this.dateStr = dateStr;
        this.isFrequencyAuto = isFreqAuto;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogSetStartDayTherapyBinding binding = DialogSetStartDayTherapyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MMM-yyyy", Locale.US);
        try {
            Date date = dateFormat.parse(dateStr);
            if (date != null) {
                Calendar selectedDate = Calendar.getInstance();
                selectedDate.setTimeInMillis(date.getTime());
                if (selectedDate.getTimeInMillis() >= Calendar.getInstance().getTimeInMillis()) {
                    binding.datePicker.updateDate(selectedDate.get(Calendar.YEAR),
                            selectedDate.get(Calendar.MONTH),
                            selectedDate.get(Calendar.DAY_OF_MONTH));
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Set max date to 31 days in the future, accounting for time difference
        Calendar maxDate = Calendar.getInstance();
        if (isFrequencyAuto) {
            maxDate.add(Calendar.DAY_OF_MONTH, 32);
            maxDate.setTimeInMillis(maxDate.getTimeInMillis() + timeDiff);
        } else {
            maxDate.add(Calendar.DAY_OF_MONTH, 31);
        }
        binding.datePicker.setMaxDate(maxDate.getTimeInMillis());

        // Set min date to today (disable past dates), accounting for time difference
        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(minDate.getTimeInMillis() + timeDiff);
        if (isFrequencyAuto) {
            minDate.add(Calendar.DAY_OF_MONTH, 16);
        }
        binding.datePicker.setMinDate(minDate.getTimeInMillis());
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
