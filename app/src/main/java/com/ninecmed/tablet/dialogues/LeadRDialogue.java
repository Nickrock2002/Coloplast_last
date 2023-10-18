package com.ninecmed.tablet.dialogues;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.Window;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogLeadSurgeryBinding;

import java.util.Objects;

public class LeadRDialogue extends Dialog {
    private float leadRValue;
    private float leadIValue;
    private View.OnClickListener confirmButtonListener = null;

    public LeadRDialogue(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        DialogLeadSurgeryBinding binding = DialogLeadSurgeryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setCancelable(false);

        if (leadRValue > 2000) {
            binding.tvWarnTitle.setText(R.string.lead_r_is_2000_ohms);
            binding.tvElectrodeTip.setText(R.string.electrode_tip_must_make_contact_with_the_tissue);
            binding.tvLeadRSubtitle.setText(R.string.lead_r_above);
        } else {
            binding.tvWarnTitle.setText(R.string.lead_r_is_250_ohms);
            binding.tvElectrodeTip.setText(R.string.use_a_different_intibia_itns);
            binding.tvLeadRSubtitle.setText(R.string.lead_r_below);
        }

        binding.tvLeadRVal.setText(String.valueOf(leadRValue).concat(" ohms"));
        binding.tvLeadIVal.setText(String.valueOf(leadIValue).concat(" mA"));

        setTheSystemButtonsHidden(this);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(getContext());
        Objects.requireNonNull(getWindow()).setLayout(dimensions.first, dimensions.second);

        binding.btnConfirmLeadR.setOnClickListener(confirmButtonListener);
    }

    public void setLeadRValue(Float leadRValue) {
        this.leadRValue = leadRValue;
    }

    public void setLeadIValue(Float leadIValue) {
        this.leadIValue = leadIValue;
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
