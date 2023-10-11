package com.ninecmed.tablet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.events.InsideOutsideEntryEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FeatureSelectionFragment extends Fragment {
    MainActivity mainActivity;
    private AppCompatButton buttonSurgery;
    private AppCompatButton buttonClinicVisit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_feature_selection, container, false);
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
        buttonSurgery.setClickable(!event.isInside());
        buttonClinicVisit.setClickable(!event.isInside());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        buttonSurgery = view.findViewById(R.id.bt_surgery);
        buttonClinicVisit = view.findViewById(R.id.bt_clinic_visit);

        buttonClinicVisit.setOnClickListener(view12 -> mainActivity.showWandConnectionDialogue(true));

        buttonSurgery.setOnClickListener(view1 -> mainActivity.showWandConnectionDialogue(false));
    }
}
