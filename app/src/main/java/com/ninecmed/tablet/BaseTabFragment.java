package com.ninecmed.tablet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.ninecmed.tablet.databinding.FragmentSurgeryBinding;

import org.greenrobot.eventbus.EventBus;

public class BaseTabFragment extends Fragment {
    public static final String CLASS_NAME = "BTF";
    private SectionsPageAdapter mSectionsPageAdapter;
    FragmentSurgeryBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSurgeryBinding.inflate(inflater, container, false);
        mSectionsPageAdapter = new SectionsPageAdapter(getChildFragmentManager());

        // Setup ViewPager with the sections adapter
        SetupViewPager(binding.container);
        binding.tabs.setupWithViewPager(binding.container);

        return binding.getRoot();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    private void SetupViewPager(ViewPager viewPager) {
        mSectionsPageAdapter.AddFragment(new ImplantToolTunnellingFragment(), getResources().getString(R.string.external_title));
        mSectionsPageAdapter.AddFragment(new ItnsFragment(), getResources().getString(R.string.itns_title));

        viewPager.setAdapter(mSectionsPageAdapter);
        viewPager.setOffscreenPageLimit(2);                                                         // Set page limit to 2 when using two fragments
    }
}
