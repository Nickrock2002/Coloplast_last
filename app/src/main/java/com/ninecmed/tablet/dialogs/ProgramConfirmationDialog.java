package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogProgramItnsBinding;

public class ProgramConfirmationDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;
    String ampVal, freqVal, dayDateVal, timeOfDayVal;

    public ProgramConfirmationDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogProgramItnsBinding binding = DialogProgramItnsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConfirm.setOnClickListener(confirmButtonListener);
        binding.btnCancel.setOnClickListener(cancelButtonListener);

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

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }
}
