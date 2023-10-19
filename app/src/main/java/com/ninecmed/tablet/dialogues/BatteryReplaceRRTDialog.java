package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogItnsRrtBinding;

import java.util.Objects;

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
