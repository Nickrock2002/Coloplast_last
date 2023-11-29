package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.databinding.DialogSetAmpBinding;

import java.util.Locale;

public class AmplitudeDialog extends BaseDialog {
    DialogSetAmpBinding binding;
    private float amplitude;
    private View.OnClickListener itnsPlusListener = null;
    private View.OnClickListener itnsMinusListener = null;
    private View.OnTouchListener stimulationButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    public AmplitudeDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DialogSetAmpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvItnsAmplitude.setText(String.format(Locale.ENGLISH, "%.2f V", getAmplitude()));
        binding.ibItnsMinus.setOnClickListener(itnsMinusListener);
        binding.ibItnsPlus.setOnClickListener(itnsPlusListener);
        binding.btItnsStartStim.setOnTouchListener(stimulationButtonListener);
        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);
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

    public Button getStimulationButtonRef() {
        return binding.btItnsStartStim;
    }
}
