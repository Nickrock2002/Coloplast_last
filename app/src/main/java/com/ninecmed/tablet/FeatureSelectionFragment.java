package com.ninecmed.tablet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

public class FeatureSelectionFragment extends Fragment {

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_feature_selection, container, false);

        AppCompatButton buttonSurgery = view.findViewById(R.id.bt_surgery);
        AppCompatButton buttonClinicVisit = view.findViewById(R.id.bt_clinic_visit);

        buttonSurgery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showBluetoothConnectionDialogue();
            }
        });

        buttonClinicVisit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mainActivity.showBluetoothConnectionDialogue();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatButton btn = view.findViewById(R.id.bt_surgery);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showSetDateTimeDialog();
            }
        });
    }

    //TODO- call this method once the bluetooth dialog setup flow is done
    public void showSetDateTimeDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_set_date_time);

        Button btnDate = (Button) dialog.findViewById(R.id.btn_date);
        Button btnTime = (Button) dialog.findViewById(R.id.btn_time);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
                dialog.dismiss();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: save delta time
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showTimePickerDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_time_picker);

        Button btnConfirmTime = (Button) dialog.findViewById(R.id.btn_confirm_time);
        btnConfirmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetDateTimeDialog();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void showDatePickerDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_date_picker);

        Button btnConfirmDate = (Button) dialog.findViewById(R.id.btn_confirm_date);
        btnConfirmDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetDateTimeDialog();
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
