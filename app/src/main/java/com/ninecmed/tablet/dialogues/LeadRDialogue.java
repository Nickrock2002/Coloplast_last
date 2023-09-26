package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

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

import java.util.Objects;

public class LeadRDialogue extends Dialog {
    private float leadRValue;
    private float leadIValue;
    private View.OnClickListener confirmButtonListener = null;

    public LeadRDialogue(Context context) {
        super(context);
    }

    public LeadRDialogue(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected LeadRDialogue(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        setContentView(R.layout.dialog_lead_surgery);

        TextView title = (TextView) findViewById(R.id.tv_warn_title);
        TextView tvElectrodeTip = (TextView)
                findViewById(R.id.tv_electrode_tip);
        if (leadRValue > 2000) {
            title.setText(R.string.lead_r_is_2000_ohms);
            tvElectrodeTip.setText(R.string.electrode_tip_must_make_contact_with_the_tissue);
        } else {
            title.setText(R.string.lead_r_is_250_ohms);
            tvElectrodeTip.setText(R.string.use_a_different_intibia_itns);
        }

        TextView tvLeadRV = (TextView) findViewById(R.id.tv_lead_r_val);
        tvLeadRV.setText(leadRValue + " ohms");

        TextView tvLeadIV = (TextView) findViewById(R.id.tv_lead_i_val);
        tvLeadIV.setText(leadIValue + " mA");

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);


        Button confirmButton = (Button) findViewById(R.id.btn_confirm_lead_r);
        confirmButton.setOnClickListener(confirmButtonListener);

    }

    public Float getLeadR() {
        return leadRValue;
    }

    public void setLeadRValue(Float leadRValue) {
        this.leadRValue = leadRValue;
    }

    public Float getLeadI() {
        return leadIValue;
    }

    public void setLeadIValue(Float leadIValue) {
        this.leadIValue = leadIValue;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
