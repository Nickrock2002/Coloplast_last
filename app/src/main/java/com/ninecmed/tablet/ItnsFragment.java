package com.ninecmed.tablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.FragmentItnsBinding;
import com.ninecmed.tablet.dialogs.ItnsResetDialog;
import com.ninecmed.tablet.dialogs.LeadRSurgeryDialog;
import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ItnsFragment extends Fragment {
    private static final String TAG = "ItnsFragment";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 0;
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    FragmentItnsBinding binding;

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
        Log.d(TAG, "OnCreate: starting.");

        binding = FragmentItnsBinding.inflate(inflater, container, false);
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
        binding.btItnsInterrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                binding.btItnsInterrogate.setPressed(true);
                mMainActivity.wandComm.interrogate(WandComm.frags.ITNS);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                binding.btItnsInterrogate.setPressed(false);
            }
            return true;
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
                        //MakeTone(ToneGenerator.TONE_PROP_BEEP);
                        binding.btItnsStartStim.setText(R.string.stimulation_active);
                        WandData.invalidateStimLeadI();

                        // Disable changed parameters during test stim. Only re-enable once
                        // job is completed. Even though controls are disabled, don't change
                        // alpha, meaning don't gray out the controls, otherwise it appears
                        // strange.
                        //SetChangedParametersEnable(false, false);
                        // Also, don't update alpha for the program and interrogate
                        // buttons either, otherwise pressing the test stim button
                        // would cause the program and interrogate button to go grey. Since
                        // this isn't consistent with what we do when the interrogate or
                        // program button is pressed - we decided to disable other telemetry
                        // controls when a telemetry command is in progress, but without changing
                        // the appearance.

                        mNow = System.currentTimeMillis();
                        mStimEnabled = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // Only execute code on up/cancel when mStimEnabled is true,
                    // otherwise this means that the user pressed the down key too
                    // quickly and when he let's go, the motion event causes SetTestStimulation
                    // to be executed again even though it wasn't started. This causes an
                    // unnecessary beep as well.
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

                        Drawable drawable = binding.ibItnsPlus.getBackground().mutate();
                        drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));
                        Drawable drawablePlus = binding.ibItnsMinus.getBackground().mutate();
                        drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));
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
        binding.ibItnsPlus.setEnabled(false);
        binding.ibItnsMinus.setEnabled(false);

        // Use OnTouchListener rather than onClickListener so that we register the change
        // on the action down, rather that on the action up!
        binding.ibItnsPlus.setOnTouchListener((view12, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                binding.ibItnsPlus.setPressed(true);
                if (mAmplitudePos < 42) {
                    mAmplitudePos += 1;
                }

                WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                binding.tvItnsAmplitude.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));

                if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                binding.ibItnsPlus.setPressed(false);
                Drawable drawable = binding.ibItnsPlus.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = binding.ibItnsMinus.getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
            }
            return true;
        });

        binding.ibItnsMinus.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                binding.ibItnsMinus.setPressed(true);
                if (mAmplitudePos > 0) {
                    mAmplitudePos -= 1;
                }

                WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                binding.tvItnsAmplitude.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));

                if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }

            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                binding.ibItnsMinus.setPressed(false);
                Drawable drawable = binding.ibItnsPlus.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = binding.ibItnsMinus.getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
            }
            return true;
        });
    }

    public void updateItnsUI(boolean success) {
        View view = getView();

        if (success) {
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                showLeadRWarningIfFound();
            } else {
                binding.tvItnsModelNumber.setText((WandData.getModelNumber(view.getContext())));
                binding.tvItnsSN.setText(WandData.getSerialNumber());

                binding.ibItnsPlus.setEnabled(true);
                binding.ibItnsPlus.setImageResource(R.drawable.ic_plus_white);
                binding.ibItnsPlus.setBackgroundResource(R.drawable.button_circular);

                binding.ibItnsMinus.setEnabled(true);
                binding.ibItnsMinus.setBackgroundResource(R.drawable.button_circular);
                binding.ibItnsMinus.setImageResource(R.drawable.ic_minus_white);

                binding.btItnsStartStim.setEnabled(true);
                binding.btItnsStartStim.setTextColor(getResources().getColor(R.color.colorWhite));

                binding.tvItnsAmplitude.setTextColor(getResources().getColor(R.color.cardview_dark_background));

                showLeadRWarningIfFound();
                checkForReset();
                setInitialAmplitude();
            }
        }
        // Here's what happens on fail
        else {
            if (WandData.isITNSNew() && mMainActivity.wandComm.getCurrentJob() != WandComm.jobs.INTERROGATE) {
                mMainActivity.showSerialNumberMismatchWarnDialog();
                return;
            }
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                mMainActivity.showWandITNSCommunicationIssueDialog();
            } else {
                mMainActivity.showWandITNSCommunicationIssueDialog();
            }
        }
    }

    private void setInitialAmplitude() {
        binding.tvItnsAmplitude.setText(WandData.getAmplitude());
        mAmplitudePos = WandData.getAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        float leadIValue = WandData.getLeadI();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || (leadRValue < 250 && leadRValue > 0);
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