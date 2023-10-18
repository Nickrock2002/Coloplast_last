package com.ninecmed.tablet;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.ninecmed.tablet.databinding.FragmentSurgeryBinding;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BaseTabFragment extends Fragment {
    private SectionsPageAdapter mSectionsPageAdapter;
    private MainActivity mainActivity;
    private boolean isClinicVisit = false;
    private TextView tvCurrentTime;
    private TextView tvCurrentDate;
    FragmentSurgeryBinding binding;

    public void setClinicVisit(boolean clinicVisit) {
        isClinicVisit = clinicVisit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSurgeryBinding.inflate(inflater, container, false);
        mSectionsPageAdapter = new SectionsPageAdapter(getChildFragmentManager());

        // Setup ViewPager with the sections adapter
        SetupViewPager(binding.container);
        binding.tabs.setupWithViewPager(binding.container);

        return binding.getRoot();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateCurrentTimeEvent event) {
        if (isClinicVisit) {
            tvCurrentDate.setText(event.getDate());
            tvCurrentTime.setText(event.getTime());
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (isClinicVisit) {
            tvCurrentTime = view.findViewById(R.id.tv_time_program_therepy);
            tvCurrentDate = view.findViewById(R.id.tv_date_program_therepy);
            tvCurrentDate.setVisibility(View.VISIBLE);
            tvCurrentTime.setVisibility(View.VISIBLE);
            Pair<String, String> dateTimePair = Utility.getTimeAndDateForFirstTimeHam(
                    mainActivity.getTimeDifferenceMillis());
            tvCurrentDate.setText(dateTimePair.first);
            tvCurrentTime.setText(dateTimePair.second);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void SetupViewPager(ViewPager viewPager) {
        if (isClinicVisit) {
            mSectionsPageAdapter.AddFragment(new ProgramTherapyFragment(), "Program therapy");
        } else {
            mSectionsPageAdapter.AddFragment(new ImplantToolTunnellingFragment(), getResources().getString(R.string.external_title));
            mSectionsPageAdapter.AddFragment(new ItnsFragment(), getResources().getString(R.string.itns_title));
        }

        viewPager.setAdapter(mSectionsPageAdapter);
        viewPager.setOffscreenPageLimit(2);                                                         // Set page limit to 2 when using two fragments
    }
}
