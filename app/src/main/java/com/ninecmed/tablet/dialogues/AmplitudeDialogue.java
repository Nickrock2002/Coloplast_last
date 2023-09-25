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
import android.widget.ImageButton;
import android.widget.TextView;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;

import java.util.Objects;

public class AmplitudeDialogue extends Dialog {
    private String amplitude;
    private View.OnTouchListener itnsPlusListener = null;
    private View.OnTouchListener itnsMinusListener = null;
    private View.OnTouchListener stimulationButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    public AmplitudeDialogue(Context context) {
        super(context);
    }

    public AmplitudeDialogue(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected AmplitudeDialogue(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_set_amp);

        TextView tvItnsAmplitude = (TextView) findViewById(R.id.tv_itns_amplitude);
        tvItnsAmplitude.setText(amplitude);

        ImageButton ibItnsPlus = (ImageButton) findViewById(R.id.ib_itns_plus);
        ibItnsPlus.setOnTouchListener(itnsPlusListener);

        ImageButton ibItnsMinus = (ImageButton) findViewById(R.id.ib_itns_minus);
        ibItnsMinus.setOnTouchListener(itnsMinusListener);

        Button btItnsStartStim = (Button) findViewById(R.id.bt_itns_start_stim);
        btItnsStartStim.setOnTouchListener(stimulationButtonListener);

        Button btCancel = (Button) findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(cancelButtonListener);

        Button btConfirm = (Button) findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public String getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(String amplitude) {
        this.amplitude = amplitude;
    }

    public void setItnsPlusListener(View.OnTouchListener onTouchListener) {
        this.itnsPlusListener = onTouchListener;
    }

    public void setItnsMinusListener(View.OnTouchListener onTouchListener) {
        this.itnsMinusListener = onTouchListener;
    }

    public void setStimulationButtonListener(View.OnTouchListener onTouchListener) {
        this.stimulationButtonListener = onTouchListener;
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}