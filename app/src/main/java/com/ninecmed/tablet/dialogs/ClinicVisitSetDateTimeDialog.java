package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogSetDateTimeBinding;

public class ClinicVisitSetDateTimeDialog extends BaseDialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;
    private View.OnClickListener dateButtonListener = null;
    private View.OnClickListener timeButtonListener = null;
    private final String formattedTime;
    private final String formattedDate;

    public ClinicVisitSetDateTimeDialog(Context context, String formattedDate, String formattedTime) {
        super(context);
        this.formattedDate = formattedDate;
        this.formattedTime = formattedTime;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogSetDateTimeBinding binding = DialogSetDateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!formattedTime.isEmpty()) {
            binding.btnTime.setText(formattedTime.toUpperCase());
            binding.btnTime.setPressed(true);
        }
        if (!formattedDate.isEmpty()) {
            binding.btnDate.setText(formattedDate);
            binding.btnDate.setPressed(true);
        }

        if (!formattedDate.isEmpty() && !formattedTime.isEmpty()) {
            binding.btnConfirm.setEnabled(true);
            binding.btnConfirm.setClickable(true);
        }
        binding.btnConfirm.setOnClickListener(confirmButtonListener);
        binding.btnCancel.setOnClickListener(cancelButtonListener);
        binding.btnDate.setOnClickListener(dateButtonListener);
        binding.btnTime.setOnClickListener(timeButtonListener);
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setDateButtonListener(View.OnClickListener onClickListener) {
        this.dateButtonListener = onClickListener;
    }

    public void setTimeButtonListener(View.OnClickListener onClickListener) {
        this.timeButtonListener = onClickListener;
    }
}
