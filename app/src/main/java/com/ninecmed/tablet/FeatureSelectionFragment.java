package com.ninecmed.tablet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.FragmentFeatureSelectionBinding;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FeatureSelectionFragment extends Fragment {

    public static final String CLASS_NAME = "FSF";
    MainActivity mainActivity;
    private FragmentFeatureSelectionBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFeatureSelectionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btClinicVisit.setOnClickListener(view12 -> mainActivity.showWandConnectionDialogue(true));
        binding.btSurgery.setOnClickListener(view1 -> mainActivity.showWandConnectionDialogue(false));
    }
}
