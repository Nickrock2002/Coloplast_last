package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import androidx.core.app.ActivityCompat;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogSetFrequencyBinding;

import java.util.Objects;

public class FrequencyDialogue extends Dialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;
    DialogSetFrequencyBinding binding;

    public FrequencyDialogue(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        binding = DialogSetFrequencyBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

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

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
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
