package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogItnsRrtBinding;

public class BatteryReplaceRRTDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public BatteryReplaceRRTDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogItnsRrtBinding binding = DialogItnsRrtBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvImplantBatteryVolts.setText(String.format("%s%s",
                getContext().getString(R.string.battery_voltage), WandData.getCellV()));
        binding.btConfirm.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
