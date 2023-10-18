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

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogSetAmpBinding;

import java.util.Locale;
import java.util.Objects;

public class AmplitudeDialogue extends Dialog {
    DialogSetAmpBinding binding;
    private float amplitude;
    private View.OnClickListener itnsPlusListener = null;
    private View.OnClickListener itnsMinusListener = null;
    private View.OnTouchListener stimulationButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    public AmplitudeDialogue(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogSetAmpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        binding.tvItnsAmplitude.setText(String.format(Locale.ENGLISH, "%.2f V", getAmplitude()));
        binding.ibItnsPlus.setOnClickListener(itnsPlusListener);
        binding.ibItnsMinus.setOnClickListener(itnsMinusListener);
        binding.btItnsStartStim.setOnTouchListener(stimulationButtonListener);
        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public float getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public void setItnsPlusListener(View.OnClickListener onClickListener) {
        this.itnsPlusListener = onClickListener;
    }

    public void setItnsMinusListener(View.OnClickListener onClickListener) {
        this.itnsMinusListener = onClickListener;
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

    public Button getConfirmButtonRef() {
        return findViewById(R.id.bt_confirm);
    }

    public Button getCancelButtonRef() {
        return binding.btCancel;
    }

    public ImageButton getPlusButtonRef() {
        return binding.ibItnsPlus;
    }

    public ImageButton getMinusButtonRef() {
        return binding.ibItnsMinus;
    }
}
