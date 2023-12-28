package com.ninecmed.tablet.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogLeadrClinicalBinding;

import java.util.Locale;

public class LeadRClinicalDialog extends BaseDialog {
    private float leadRValue;
    private float leadIValue;
    private View.OnClickListener confirmButtonListener = null;

    public LeadRClinicalDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogLeadrClinicalBinding binding = DialogLeadrClinicalBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (leadRValue > Utility.maxLeadR) {
            binding.tvWarnTitle.setText(R.string.lead_r_is_2000_ohms);
            binding.tvResetCounter.setText(R.string.lead_r_above);
        } else {
            binding.tvWarnTitle.setText(R.string.lead_r_is_250_ohms);
            binding.tvResetCounter.setText(R.string.lead_r_below);
        }
        String formattedLeadR = String.format(Locale.ENGLISH, "%.0f Ω", leadRValue);
        binding.tvLeadRVal.setText(formattedLeadR);
        String formattedLeadI = String.format(Locale.ENGLISH, "%.1f mA", leadIValue);
        binding.tvLeadIVal.setText(formattedLeadI);
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
