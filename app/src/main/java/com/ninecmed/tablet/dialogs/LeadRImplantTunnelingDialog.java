package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogLeadrImplantTunnelingBinding;
import com.ninecmed.tablet.databinding.DialogProgrammingUnsuccessfulBinding;

public class LeadRImplantTunnelingDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public LeadRImplantTunnelingDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogLeadrImplantTunnelingBinding binding = DialogLeadrImplantTunnelingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConfirmLeadR.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
