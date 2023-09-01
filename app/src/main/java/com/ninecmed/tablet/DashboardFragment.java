package com.ninecmed.tablet;

import static com.ninecmed.tablet.R.string.all_ok;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import me.aflak.bluetooth.interfaces.DeviceCallback;

public class DashboardFragment extends Fragment {
    private SectionsPageAdapter mSectionsPageAdapter;
    private TabLayout mTabLayout;
    public WandComm wandComm = null;
    private MainActivity mainActivity;

    public interface tabs {
        int EXT = 1;
        int ITNS = 0;           // Tab 0 must also be the default fragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mSectionsPageAdapter = new SectionsPageAdapter(getFragmentManager());

        // Setup ViewPager with the sections adapter
        CustomViewPager mViewPager = view.findViewById(R.id.container);
        SetupViewPager(mViewPager);

        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
        assert mainActivity != null;
        wandComm = new WandComm(mainActivity.mBluetooth, this);
        mainActivity.mBluetooth.setDeviceCallback(deviceCallback);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void SetupViewPager(ViewPager viewPager) {
        // The first fragment added is the default fragment that appears
        mSectionsPageAdapter.AddFragment(new ItnsFragment(), getResources().getString(R.string.itns_title));
        mSectionsPageAdapter.AddFragment(new ExternalFragment(), getResources().getString(R.string.external_title));

        viewPager.setAdapter(mSectionsPageAdapter);
        viewPager.setOffscreenPageLimit(2);                                                         // Set page limit to 2 when using two fragments
    }

    public void UpdateUIFragments(final int frag, final boolean success) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (frag == WandComm.frags.EXTERNAL) {
                    ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
                    external.UIUpdate(success);
                }

                if (frag == WandComm.frags.ITNS) {
                    ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                    itns.UIUpdate(success);
                }
            }
        });
    }

    public void UpdateUI(final boolean success) {
        if (success) {
            if (wandComm.GetCurrentJob() == WandComm.jobs.INITWAND) {
                ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
                external.OnConnected();

                ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                itns.OnConnected();
            }
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mainActivity);

            alertDialog.setTitle(R.string.main_wand_error_title);
            alertDialog.setMessage(R.string.main_wand_error_msg);

            alertDialog.setPositiveButton(getString(all_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();

            ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
            external.OnDisconnected();

            ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
            itns.OnDisconnected();
        }
    }

    public void UpdateItnsAmplitude() {
        requireActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                itns.UpdateAmplitude();
            }
        });
    }

    public void EnableTabs(boolean enable) {
        LinearLayout tabStrip = ((LinearLayout) mTabLayout.getChildAt(0));
        tabStrip.setEnabled(false);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(enable);
        }
    }

    private final DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            wandComm.InitWand();
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            wandComm.ResetWandComm();
            // Reconnect
            mainActivity.mBluetooth.connectToDevice(mainActivity.mBTDevice);
        }

        @Override
        public void onMessage(byte[] message) {
            wandComm.ReturnMessage(message);
        }

        @Override
        public void onError(int errorCode) {
        }

        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            if (mainActivity.mRunBT)
                mainActivity.mHandler.postDelayed(mainActivity.Reconnect, 1000);                                    // And if fail, try every second
        }
    };
}
