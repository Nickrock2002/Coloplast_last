package com.ninecmed.tablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.FragmentItnsBinding;
import com.ninecmed.tablet.dialogs.ItnsResetDialog;
import com.ninecmed.tablet.dialogs.LeadRSurgeryDialog;
import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class ItnsFragment extends Fragment {
    public static final String CLASS_NAME = "ITNSF";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 0;
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    FragmentItnsBinding binding;
    private boolean isFromBackstack = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (binding == null)
            binding = FragmentItnsBinding.inflate(inflater, container, false);
        else
            isFromBackstack = true;

        initializeStimulationButton();
        initializeInterrogateButton();
        initializeAmpControls();
        initializeLeadRWarnButton();

        mMainActivity = (MainActivity) getActivity();
        return binding.getRoot();
    }

    private void initializeLeadRWarnButton() {
        binding.btnLeadRWarn.setOnClickListener(v -> {
            showLeadRWarningIfFound();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton() {
        binding.btItnsInterrogate.setOnClickListener(view -> {
            mMainActivity.wandComm.interrogate(WandComm.frags.ITNS);
            binding.ibItnsPlus.setClickable(false);
            binding.ibItnsMinus.setClickable(false);
            binding.btItnsStartStim.setOnTouchListener(null);
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeStimulationButton() {
        binding.btItnsStartStim.setOnTouchListener((view, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (mNow + 500 < System.currentTimeMillis()) {
                        binding.btItnsStartStim.setPressed(true);
                        mMainActivity.wandComm.setStimulation(true);

                        binding.btItnsStartStim.setText(R.string.stimulation_active);
                        WandData.invalidateStimLeadI();

                        mNow = System.currentTimeMillis();
                        mStimEnabled = true;
                    }
                    binding.ibItnsPlus.setClickable(false);
                    binding.ibItnsMinus.setClickable(false);
                    binding.btItnsInterrogate.setClickable(false);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    if (mStimEnabled) {
                        binding.btItnsStartStim.setPressed(false);
                        binding.btItnsStartStim.setText(R.string.hold_to_deliver_neurostimulation);

                        // Set delay to 1500 to be the same delay as ExternalFragment
                        if (mNow + 1500 < System.currentTimeMillis()) {
                            mMainActivity.wandComm.setStimulation(false);
                            mStimEnabled = false;
                        } else {
                            mHandler.postDelayed(HoldStimulation, mNow + 1500 - System.currentTimeMillis());
                        }
                    }
                    break;
            }
            return true;
        });
    }

    private final Runnable HoldStimulation = () -> {
        mMainActivity.wandComm.setStimulation(false);
        //MakeTone(ToneGenerator.TONE_PROP_NACK);
        mStimEnabled = false;
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getFrag() == WandComm.frags.ITNS) {
            updateItnsUI(event.isUiUpdateSuccess());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ItnsUpdateAmpEvent event) {
        updateAmplitude();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeAmpControls() {
        if (!isFromBackstack) {
            binding.ibItnsPlus.setEnabled(false);
            binding.ibItnsMinus.setEnabled(false);
        }

        // Use OnTouchListener rather than onClickListener so that we register the change
        // on the action down, rather that on the action up!
        binding.ibItnsPlus.setOnClickListener(view -> {
            if (mAmplitudePos < 42) {
                mAmplitudePos += 1;
            }

            WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
            binding.tvItnsAmplitude.setText(String.format(Locale.ENGLISH, "%.2f V",
                    WandData.getAmpFromPos(mAmplitudePos)));

            if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
            } else {
                mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
            }

            setPlusMinusButtonColors(false);
        });

        binding.ibItnsMinus.setOnClickListener(view -> {
            if (mAmplitudePos > 0) {
                mAmplitudePos -= 1;
            }

            WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
            binding.tvItnsAmplitude.setText(String.format(Locale.ENGLISH, "%.2f V",
                    WandData.getAmpFromPos(mAmplitudePos)));

            if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
            } else {
                mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
            }
            setPlusMinusButtonColors(false);
        });
    }

    void setPlusMinusButtonColors(boolean isDefault) {
        if (mAmplitudePos == 42) {
            binding.ibItnsPlus.setBackgroundResource(R.drawable.button_circular_grey_three_hundred);
            binding.ibItnsPlus.setClickable(false);
        } else {
            binding.ibItnsPlus.setClickable(true);
            if (isDefault) {
                binding.ibItnsPlus.setBackgroundResource(R.drawable.button_circular_primary);
            } else {
                binding.ibItnsPlus.setBackgroundResource(R.drawable.button_circular_deep_blue);
            }
        }
        if (mAmplitudePos == 0) {
            binding.ibItnsMinus.setBackgroundResource(R.drawable.button_circular_grey_three_hundred);
            binding.ibItnsMinus.setClickable(false);
        } else {
            binding.ibItnsMinus.setClickable(true);
            if (isDefault) {
                binding.ibItnsMinus.setBackgroundResource(R.drawable.button_circular_primary);
            } else {
                binding.ibItnsMinus.setBackgroundResource(R.drawable.button_circular_deep_blue);
            }
        }
    }

    public void updateItnsUI(boolean success) {
        View view = getView();

        binding.ibItnsPlus.setClickable(true);
        binding.ibItnsMinus.setClickable(true);

        if (success) {
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                showLeadRWarningIfFound();
                binding.btItnsInterrogate.setClickable(true);
                binding.btItnsStartStim.setPressed(false);
                binding.btItnsStartStim.setText(R.string.hold_to_deliver_neurostimulation);
            } else {
                binding.tvItnsModelNumber.setText((WandData.getModelNumber(view.getContext())));
                binding.tvItnsSN.setText(WandData.getSerialNumber());

                binding.ibItnsPlus.setEnabled(true);
                binding.ibItnsPlus.setImageResource(R.drawable.ic_plus_white);

                binding.ibItnsMinus.setEnabled(true);
                binding.ibItnsMinus.setImageResource(R.drawable.ic_minus_white);

                binding.btItnsStartStim.setEnabled(true);
                binding.btItnsStartStim.setTextColor(getResources().getColor(R.color.colorWhite));

                binding.tvItnsAmplitude.setTextColor(getResources().getColor(R.color.cardview_dark_background));

                showLeadRWarningIfFound();
                checkForReset();
                setInitialAmplitude();
                initializeStimulationButton();
            }
        }
        // Here's what happens on fail
        else {
            mMainActivity.showWandITNSCommunicationIssueDialog();
        }
        setPlusMinusButtonColors(true);
    }

    private void setInitialAmplitude() {
        binding.tvItnsAmplitude.setText(WandData.getAmplitude());
        mAmplitudePos = WandData.getAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || leadRValue < 250;
        if (isWarningFound) {
            binding.btnLeadRWarn.setVisibility(View.VISIBLE);
            LeadRSurgeryDialog dialog = new LeadRSurgeryDialog(requireContext());

            dialog.setConfirmButtonListener(v -> dialog.dismiss());
            dialog.show();
        } else {
            binding.tvLeadRVal.setText(R.string.ok);
            binding.btnLeadRWarn.setVisibility(View.GONE);
        }
    }

    private void checkForReset() {
        int resets = WandData.getResets();
        if (resets > 0) {
            showItnsResetDialog();
        }
    }

    public void showItnsResetDialog() {
        ItnsResetDialog dialog = new ItnsResetDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            dialog.dismiss();
            mMainActivity.wandComm.clearResetCounter();
        });
        dialog.show();
    }

    public void updateAmplitude() {
        binding.tvItnsAmplitude.setText(WandData.getAmplitude());
        binding.tvItnsAmplitude.setTextColor(Color.BLACK);
        mAmplitudePos = WandData.getAmplitudePos();
    }
}