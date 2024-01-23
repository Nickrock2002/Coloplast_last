package com.ninecmed.tablet.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.Utility;
import com.ninecmed.tablet.databinding.DialogLeadrSurgeryBinding;

import java.util.Locale;

public class LeadRSurgeryDialog extends BaseDialog {
    private float leadRValue;
    private float leadIValue;
    private View.OnClickListener confirmButtonListener = null;

    public LeadRSurgeryDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogLeadrSurgeryBinding binding = DialogLeadrSurgeryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if (leadRValue > Utility.maxLeadR) {
            binding.tvWarnTitle.setText(R.string.lead_r_is_2000_ohms);
            binding.tvElectrodeTip.setText(R.string.electrode_tip_must_make_contact_with_the_tissue);
            binding.tvLeadRSubtitle.setText(R.string.lead_r_above);
        } else {
            binding.tvWarnTitle.setText(R.string.lead_r_is_250_ohms);
            binding.tvElectrodeTip.setText(R.string.electrode_tip_must_make_contact_with_the_tissue);
            binding.tvLeadRSubtitle.setText(R.string.lead_r_below);
        }

        String formattedLeadR = String.format(Locale.ENGLISH, "%.0f Ω", leadRValue);
        if (leadRValue == 0) {
            binding.tvLeadRVal.setText(getContext().getString(R.string._1_dash));
        } else {
            binding.tvLeadRVal.setText(formattedLeadR);
        }
        String formattedLeadI = String.format(Locale.ENGLISH, "%.1f mA", leadIValue);
        if (leadIValue == 0) {
            binding.tvLeadIVal.setText(getContext().getString(R.string._1_dash));
        } else {
            binding.tvLeadIVal.setText(formattedLeadI);
        }

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
