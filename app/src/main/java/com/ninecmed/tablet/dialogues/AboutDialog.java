package com.ninecmed.tablet.dialogues;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.BuildConfig;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogAboutBinding;

public class AboutDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public AboutDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogAboutBinding binding = DialogAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String implantFirmware = WandData.getCellV();
        binding.tvImplantFirmwareVal.setText(implantFirmware == null ? "-" : implantFirmware);

        String wandFirmware = WandData.getCellV();
        binding.tvWandFirmwareVal.setText(wandFirmware == null ? "-" : wandFirmware);

        binding.tvTabApplicationVal.setText(BuildConfig.VERSION_NAME);

        binding.btnOk.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
