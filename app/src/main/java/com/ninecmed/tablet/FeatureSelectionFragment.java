package com.ninecmed.tablet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

public class FeatureSelectionFragment extends Fragment {
    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_feature_selection, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppCompatButton buttonSurgery = view.findViewById(R.id.bt_surgery);
        AppCompatButton buttonClinicVisit = view.findViewById(R.id.bt_clinic_visit);

        buttonClinicVisit.setOnClickListener(view12 -> mainActivity.showWandConnectionDialogue(true));

        buttonSurgery.setOnClickListener(view1 -> mainActivity.showWandConnectionDialogue(false));
    }
}
