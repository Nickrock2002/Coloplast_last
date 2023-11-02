package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogBackToStartBinding;
import com.ninecmed.tablet.databinding.DialogResetDateTimeBinding;

public class ResetDateTimeDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;

    public ResetDateTimeDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogResetDateTimeBinding binding = DialogResetDateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btConfirm.setOnClickListener(confirmButtonListener);
        binding.btCancel.setOnClickListener(cancelButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }
}
