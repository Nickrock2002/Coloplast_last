package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogBackToStartBinding;

public class BackToStartDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;

    public BackToStartDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogBackToStartBinding binding = DialogBackToStartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnYesGoBack.setOnClickListener(confirmButtonListener);
        binding.btnCancel.setOnClickListener(cancelButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }
}
