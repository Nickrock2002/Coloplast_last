package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogResetCounterCvBinding;

public class ItnsResetCVDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public ItnsResetCVDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogResetCounterCvBinding binding = DialogResetCounterCvBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvResetCounter.setText(getContext().getString(R.string.implant_reset_counter)
                .concat(String.valueOf(WandData.getResets())));

        binding.btnResetCounterConfirm.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
