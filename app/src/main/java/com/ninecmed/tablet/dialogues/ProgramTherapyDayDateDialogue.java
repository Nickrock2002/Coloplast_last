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
import com.ninecmed.tablet.databinding.DialogSetStartDayTherapyBinding;

import java.util.Calendar;
import java.util.Objects;

public class ProgramTherapyDayDateDialogue extends Dialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    private long timeDiff;

    public ProgramTherapyDayDateDialogue(Context context, long timeDifferenceMillis) {
        super(context);
        this.timeDiff = timeDifferenceMillis;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogSetStartDayTherapyBinding binding = DialogSetStartDayTherapyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);

        // Set max date to 31 days in the future, accounting for time difference
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 31);
        maxDate.setTimeInMillis(maxDate.getTimeInMillis() + timeDiff);
        binding.datePicker.setMaxDate(maxDate.getTimeInMillis());

        // Set min date to today (disable past dates), accounting for time difference
        Calendar minDate = Calendar.getInstance();
        minDate.setTimeInMillis(minDate.getTimeInMillis() + timeDiff);
        binding.datePicker.setMinDate(minDate.getTimeInMillis());
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
