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
import android.widget.Button;
import android.widget.RadioButton;

import androidx.core.app.ActivityCompat;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;

import java.util.Objects;

public class FrequencyDialogue extends Dialog {
    private View.OnClickListener cancelButtonListener = null;
    private View.OnClickListener confirmButtonListener = null;

    public FrequencyDialogue(Context context) {
        super(context);
    }

    public FrequencyDialogue(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected FrequencyDialogue(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_set_frequency);

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
        ((RadioButton) findViewById(R.id.radio_off)).setButtonTintList(colorStateList);
        ((RadioButton) findViewById(R.id.radio_daily)).setButtonTintList(colorStateList);
        ((RadioButton) findViewById(R.id.radio_weekly)).setButtonTintList(colorStateList);
        ((RadioButton) findViewById(R.id.radio_fort_nightly)).setButtonTintList(colorStateList);
        ((RadioButton) findViewById(R.id.radio_monthly)).setButtonTintList(colorStateList);
        ((RadioButton) findViewById(R.id.radio_auto)).setButtonTintList(colorStateList);

        Button btCancel = (Button) findViewById(R.id.bt_cancel);
        btCancel.setOnClickListener(cancelButtonListener);

        Button btConfirm = (Button) findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(confirmButtonListener);

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
