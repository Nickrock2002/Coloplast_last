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
import com.ninecmed.tablet.events.InsideOutsideEntryEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FeatureSelectionFragment extends Fragment {
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
        EventBus.getDefault().register(this);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(InsideOutsideEntryEvent event) {
        binding.btSurgery.setClickable(!event.isInside());
        binding.btClinicVisit.setClickable(!event.isInside());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.btClinicVisit.setOnClickListener(view12 -> mainActivity.showWandConnectionDialogue(true));
        binding.btSurgery.setOnClickListener(view1 -> mainActivity.showWandConnectionDialogue(false));
    }
}
