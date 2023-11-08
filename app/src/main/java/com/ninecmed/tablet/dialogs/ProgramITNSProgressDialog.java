package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.ninecmed.tablet.R;
import com.ninecmed.tablet.databinding.DialogProgramItnsProgressBinding;

public class ProgramITNSProgressDialog extends BaseDialog {

    public ProgramITNSProgressDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogProgramItnsProgressBinding binding = DialogProgramItnsProgressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Glide.with(getContext())
                .load(R.raw.coloplast_spinner)
                .into(binding.ivProgress);
    }
}
