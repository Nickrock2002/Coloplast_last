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

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.WandData;

import java.util.Objects;

public class BatteryReplaceRRTDialogue extends Dialog {
    private View.OnClickListener confirmButtonListener = null;

    public BatteryReplaceRRTDialogue(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_itns_rrt);

        TextView tvImplantBatteryVoltage = (TextView) findViewById(R.id.tv_implant_battery_volts);
        tvImplantBatteryVoltage.setText("Battery Voltage: " + WandData.getCellV());

        Button btConfirm = (Button) findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
