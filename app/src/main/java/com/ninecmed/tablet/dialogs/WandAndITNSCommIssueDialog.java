package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.databinding.DialogWandItnsCommIssueBinding;

public class WandAndITNSCommIssueDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public WandAndITNSCommIssueDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogWandItnsCommIssueBinding binding = DialogWandItnsCommIssueBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnConfirmWandComm.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
