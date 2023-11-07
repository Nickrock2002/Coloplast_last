package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import com.bumptech.glide.Glide;
import com.ninecmed.tablet.R;
import com.ninecmed.tablet.databinding.DialogWandCommBinding;

public class WandTabConnDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;
    private DialogWandCommBinding binding;

    public WandTabConnDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DialogWandCommBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Glide.with(getContext())
                .load(R.raw.connect_tablet_with_wand)
                .into(binding.ivHeaderImage);
        binding.btConfirm.setOnClickListener(confirmButtonListener);
        binding.btCancel.setOnClickListener(cancelButtonListener);
        binding.btConfirm.setClickable(false);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public Button getConfirmButtonRef() {
        return binding.btConfirm;
    }

    public LinearLayoutCompat getHeaderRef() {
        return binding.llHeader;
    }

    public LinearLayoutCompat getHeaderActiveRef() {
        return binding.llHeaderActive;
    }

    public TextView getConnectionStatusTvRef() {
        return binding.tvConnectionStatus;
    }
}
