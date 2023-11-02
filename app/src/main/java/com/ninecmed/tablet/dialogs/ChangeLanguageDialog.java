package com.ninecmed.tablet.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.ninecmed.tablet.databinding.DialogChangeLanguageBinding;

public class ChangeLanguageDialog extends BaseDialog {
    private View.OnClickListener confirmButtonListener = null;
    private View.OnClickListener cancelButtonListener = null;
    private ArrayAdapter<CharSequence> adapter = null;
    private AdapterView.OnItemSelectedListener itemSelectedListener = null;
    DialogChangeLanguageBinding binding;

    public ChangeLanguageDialog(Context context) {
        super(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DialogChangeLanguageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.spinnerLanguages.setAdapter(adapter);
        binding.spinnerLanguages.setOnItemSelectedListener(itemSelectedListener);
        binding.btnConfirm.setOnClickListener(confirmButtonListener);
        binding.btnCancel.setOnClickListener(cancelButtonListener);
    }

    public void setConfirmButtonListener(View.OnClickListener onClickListener) {
        this.confirmButtonListener = onClickListener;
    }

    public void setCancelButtonListener(View.OnClickListener onClickListener) {
        this.cancelButtonListener = onClickListener;
    }

    public void setItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        this.itemSelectedListener = onItemSelectedListener;
    }

    public void setSpinnerAdapter(ArrayAdapter<CharSequence> adapter) {
        this.adapter = adapter;
    }

    public Spinner getSpinnerRef() {
        return binding.spinnerLanguages;
    }
}
