package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogResetCounterBinding;

public class ItnsResetDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public ItnsResetDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogResetCounterBinding binding = DialogResetCounterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.tvResetCounter.setText(getContext().getString(R.string.implant_reset_counter)
                .concat(String.valueOf(WandData.getResets())));

        binding.btnResetCounterConfirm.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
