package com.ninecmed.tablet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
}
