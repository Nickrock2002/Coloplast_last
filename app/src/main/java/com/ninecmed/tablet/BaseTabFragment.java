package com.ninecmed.tablet;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.ninecmed.tablet.databinding.FragmentSurgeryBinding;

import org.greenrobot.eventbus.EventBus;

import java.util.Objects;

public class BaseTabFragment extends Fragment {
    public static final String CLASS_NAME = "BTF";
    private SectionsPageAdapter mSectionsPageAdapter;
    FragmentSurgeryBinding binding;

    MainActivity mainActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentSurgeryBinding.inflate(inflater, container, false);
        }
        mSectionsPageAdapter = new SectionsPageAdapter(getChildFragmentManager());

        // Setup ViewPager with the sections adapter
        mainActivity.selectedTab = getResources().getString(R.string.external_title);
        SetupViewPager(binding.container);
        binding.tabs.setupWithViewPager(binding.container);
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mainActivity.selectedTab = Objects.requireNonNull(tab.getText()).toString();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    private void SetupViewPager(ViewPager viewPager) {
        mSectionsPageAdapter.AddFragment(new ImplantToolTunnellingFragment(), getResources().getString(R.string.external_title));
        mSectionsPageAdapter.AddFragment(new ItnsFragment(), getResources().getString(R.string.itns_title));

        viewPager.setAdapter(mSectionsPageAdapter);
        viewPager.setOffscreenPageLimit(2);                                                         // Set page limit to 2 when using two fragments
    }
}
