package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogProgramItnsSuccessBinding;

public class ProgramItnsSuccessDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;
    String ampVal, freqVal, dayDateVal, timeOfDayVal;

    public ProgramItnsSuccessDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogProgramItnsSuccessBinding binding = DialogProgramItnsSuccessBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnOk.setOnClickListener(confirmButtonListener);

        binding.tvAmpVal.setText(ampVal);
        binding.tvFreqVal.setText(freqVal);
        binding.tvStartDayDateVal.setText(dayDateVal);
        binding.tvTimeVal.setText(timeOfDayVal);
    }

    public void setAmpVal(String ampVal) {
        this.ampVal = ampVal;
    }

    public void setFreqVal(String freqVal) {
        this.freqVal = freqVal;
    }

    public void setDayDateVal(String dayDateVal) {
        this.dayDateVal = dayDateVal;
    }

    public void setTimeOfDayVal(String timeOfDayVal) {
        this.timeOfDayVal = timeOfDayVal;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
