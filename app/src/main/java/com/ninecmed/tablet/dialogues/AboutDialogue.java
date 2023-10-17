package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ninecmed.tablet.BuildConfig;
import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.WandData;

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
        setCancelable(false);
        setContentView(R.layout.dialog_about);

        TextView tvImplantFirmware = findViewById(R.id.tv_implant_firmware_val);
        String implantFirmware = WandData.getCellV();
        tvImplantFirmware.setText(implantFirmware == null ? "-" : implantFirmware);

        TextView tvWandFirmware = findViewById(R.id.tv_wand_firmware_val);
        String wandFirmware = WandData.getCellV();
        tvWandFirmware.setText(wandFirmware == null ? "-" : wandFirmware);

        TextView tvTabApplicationVersion = findViewById(R.id.tv_tab_application_val);
        tvTabApplicationVersion.setText(BuildConfig.VERSION_NAME);

        Button btOk = findViewById(R.id.btn_ok);
        btOk.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
