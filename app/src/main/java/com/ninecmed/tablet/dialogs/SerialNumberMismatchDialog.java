package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogSerialMismatchBinding;
import com.ninecmed.tablet.databinding.DialogTabletBatteryLowBinding;

public class SerialNumberMismatchDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public SerialNumberMismatchDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogSerialMismatchBinding binding = DialogSerialMismatchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConfirmMismatch.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
