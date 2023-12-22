package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogInvalidModelBinding;

public class InvalidModelDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public InvalidModelDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogInvalidModelBinding binding = DialogInvalidModelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String modelNumber = WandData.getModelNumber(getContext());
        modelNumber = getContext().getString(R.string.model_num_text)
                .concat(" ").concat(modelNumber);

        binding.tvModelNumber.setText(modelNumber);
        binding.btnResetCounterConfirm.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
