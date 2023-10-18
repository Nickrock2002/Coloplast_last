package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import com.ninecmed.tablet.BuildConfig;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogAboutBinding;

import java.util.Objects;

public class AboutDialogue extends Dialog {
    private View.OnClickListener confirmButtonListener = null;

    public AboutDialogue(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogAboutBinding binding = DialogAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setCancelable(false);
        String implantFirmware = WandData.getCellV();
        binding.tvImplantFirmwareVal.setText(implantFirmware == null ? "-" : implantFirmware);

        String wandFirmware = WandData.getCellV();
        binding.tvWandFirmwareVal.setText(wandFirmware == null ? "-" : wandFirmware);

        binding.tvTabApplicationVal.setText(BuildConfig.VERSION_NAME);

        binding.btnOk.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
