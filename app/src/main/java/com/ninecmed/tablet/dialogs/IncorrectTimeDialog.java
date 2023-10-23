package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogIncorrectDateTimeBinding;

public class IncorrectTimeDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public IncorrectTimeDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogIncorrectDateTimeBinding binding = DialogIncorrectDateTimeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConfirmIncorrectTime.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
