package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.BuildConfig;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogAboutBinding;
import com.ninecmed.tablet.databinding.DialogTabletBatteryLowBinding;

public class BatteryWarnDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public BatteryWarnDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogTabletBatteryLowBinding binding = DialogTabletBatteryLowBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConfirmBatt.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
