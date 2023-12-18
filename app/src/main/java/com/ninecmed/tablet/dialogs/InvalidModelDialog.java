package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import com.ninecmed.tablet.R;
import com.ninecmed.tablet.WandData;
import com.ninecmed.tablet.databinding.DialogWrongModelBinding;

public class InvalidModelDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;

    public InvalidModelDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogWrongModelBinding binding = DialogWrongModelBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String modelNumber = WandData.getModelNumber(getContext());
        if (modelNumber == null) {
            modelNumber = "";
        }
        binding.tvImplantModel.setText(getContext().getString(R.string.implant_wrong_model_msg)
                .concat(" ").concat(modelNumber));

        binding.btnResetCounterConfirm.setOnClickListener(confirmButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }
}
