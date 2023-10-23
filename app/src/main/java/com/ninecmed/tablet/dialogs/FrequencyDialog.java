package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.databinding.DialogSetFrequencyBinding;

public class FrequencyDialog extends BaseDialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;
    DialogSetFrequencyBinding binding;

    public FrequencyDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DialogSetFrequencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //disabled
                        new int[]{android.R.attr.state_checked} //enabled
                },
                new int[]{
                        Color.BLACK, //disabled
                        ActivityCompat.getColor(getContext(), R.color.colorPrimary) //enabled
                }
        );
        binding.radioOff.setButtonTintList(colorStateList);
        binding.radioDaily.setButtonTintList(colorStateList);
        binding.radioWeekly.setButtonTintList(colorStateList);
        binding.radioFortNightly.setButtonTintList(colorStateList);
        binding.radioMonthly.setButtonTintList(colorStateList);
        binding.radioAuto.setButtonTintList(colorStateList);

        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setOnClickListener(confirmButtonListener);
    }

    public int getCheckedButtonId() {
        return binding.frequencyRadioGroup.getCheckedRadioButtonId();
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
