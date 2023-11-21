package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogSetStartDayTherapyBinding;

import java.util.Calendar;
import java.util.Date;

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

        // Set max date to 31 days in the future, accounting for time difference
        Calendar maxDate = Calendar.getInstance();
        maxDate.setTimeInMillis(maxDate.getTimeInMillis() + timeDiff);
        if (isFrequencyAuto) {
            maxDate.add(Calendar.DAY_OF_MONTH, 31);
        } else {
            maxDate.add(Calendar.DAY_OF_MONTH, 31);
        }
        binding.datePicker.setMaxDate(maxDate.getTimeInMillis());

        // Set min date to today (disable past dates), accounting for time difference
        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(minDate.getTimeInMillis() + timeDiff);
        if (isFrequencyAuto) {
            minDate.add(Calendar.DAY_OF_MONTH, 15);
        }
        binding.datePicker.setMinDate(minDate.getTimeInMillis());

        // Set selection on calender
        Date date = Utility.parseDateFromFormatForProgramTherapy(dateStr);
        if (date != null) {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.setTimeInMillis(date.getTime());

            if (selectedDate.getTimeInMillis() <= maxDate.getTimeInMillis() && selectedDate.getTimeInMillis() >= minDate.getTimeInMillis()) {
                binding.datePicker.updateDate(selectedDate.get(Calendar.YEAR),
                        selectedDate.get(Calendar.MONTH),
                        selectedDate.get(Calendar.DAY_OF_MONTH));
            } else {
                binding.datePicker.updateDate(minDate.get(Calendar.YEAR),
                        minDate.get(Calendar.MONTH),
                        minDate.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            binding.datePicker.updateDate(minDate.get(Calendar.YEAR),
                    minDate.get(Calendar.MONTH),
                    minDate.get(Calendar.DAY_OF_MONTH));
        }
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
