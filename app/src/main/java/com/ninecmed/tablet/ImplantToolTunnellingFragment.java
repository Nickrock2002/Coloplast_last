package com.ninecmed.tablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.FragmentImplantTunnelingBinding;
import com.ninecmed.tablet.dialogs.LeadRImplantTunnelingDialog;
import com.ninecmed.tablet.dialogs.StimulationProgressDialog;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class ImplantToolTunnellingFragment extends Fragment {
    public static final String CLASS_NAME = "SF";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 8;                                                                   // Set default position ot 1.5V
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private FragmentImplantTunnelingBinding binding;
    private StimulationProgressDialog stimulationProgressDialog;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getFrag() == WandComm.frags.EXTERNAL) {
            updateImplantTunnellingUI(event.isUiUpdateSuccess());
            showLeadRWarningIfFound();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (binding == null) {
            binding = FragmentImplantTunnelingBinding.inflate(inflater, container, false);
        }
        initializeStimulationButton();
        initializeAmplitudeButton();
        initializeTitle();
        initializeLeadRWarnButton();
        setPlusMinusButtonColors(true);

        mMainActivity = (MainActivity) getActivity();
        return binding.getRoot();
    }

    private void initializeTitle() {
        String text = binding.tvImplantTitle.getText().toString();

        // Create a SpannableString to apply styles
        SpannableString spannableString = new SpannableString(text);

        // Apply bold style to "Implant Tool Tunnelling"
        int startIndex1 = text.indexOf("Implant Tool Tunnelling");
        int endIndex1 = startIndex1 + "Implant Tool Tunnelling".length();
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply bold style to "ITNS Interrogation Tab"
        int startIndex2 = text.indexOf("ITNS Interrogation");
        int endIndex2 = startIndex2 + "ITNS Interrogation".length();
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex2, endIndex2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the modified text in the TextView
        binding.tvImplantTitle.setText(spannableString);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeStimulationButton() {
        binding.btExternalStartStim.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (mNow + 500 < System.currentTimeMillis()) {
                        binding.btExternalStartStim.setPressed(true);
                        mMainActivity.wandComm.setStimulationExt(true, WandComm.frags.EXTERNAL);
                        binding.btExternalStartStim.setText(getString(R.string.stimulation_active));
                        mNow = System.currentTimeMillis();
                        mStimEnabled = true;
                    }
                    binding.ibExternalPlus.setClickable(false);
                    binding.ibExternalMinus.setClickable(false);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_POINTER_DOWN:
                case MotionEvent.ACTION_POINTER_UP:
                    if (mStimEnabled) {
                        binding.btExternalStartStim.setPressed(false);
                        binding.btExternalStartStim.setText(R.string.hold_to_deliver_neurostimulation);
                        if (mNow + 1500 < System.currentTimeMillis()) {
                            mMainActivity.wandComm.setStimulationExt(false, WandComm.frags.EXTERNAL);
                            mStimEnabled = false;
                        } else {
                            mHandler.postDelayed(holdStimulationRunnable, mNow + 1500 - System.currentTimeMillis());
                        }
                        showNeurostimulationProgressDialog();
                    }
                    binding.ibExternalPlus.setClickable(true);
                    binding.ibExternalMinus.setClickable(true);
                    break;
            }
            return true;
        });
    }

    private final Runnable holdStimulationRunnable = () -> {
        mMainActivity.wandComm.setStimulationExt(false, WandComm.frags.EXTERNAL);
        mStimEnabled = false;
    };

    @SuppressLint("ClickableViewAccessibility")
    private void initializeAmplitudeButton() {
        binding.ibExternalPlus.setOnClickListener(view -> {
            if (mAmplitudePos < 22) {
                mAmplitudePos += 1;
            }
            WandData.setStimAmplitude(mAmplitudePos);
            WandData.invalidateStimExtLeadI();
            binding.tvExternalAmplitude.setText(String.format(Locale.ENGLISH, "%.2f V",
                    WandData.getAmpFromPos(mAmplitudePos)));

            setPlusMinusButtonColors(false);
        });

        binding.ibExternalMinus.setOnClickListener(view -> {
            if (mAmplitudePos > 0) {
                mAmplitudePos -= 1;
            }
            WandData.setStimAmplitude(mAmplitudePos);
            WandData.invalidateStimExtLeadI();
            binding.tvExternalAmplitude.setText(String.format(Locale.ENGLISH, "%.2f V",
                    WandData.getAmpFromPos(mAmplitudePos)));
            setPlusMinusButtonColors(false);
        });
    }

    void setPlusMinusButtonColors(boolean isDefault) {
        if (mAmplitudePos == 22) {
            binding.ibExternalPlus.setBackgroundResource(R.drawable.button_circular_grey_three_hundred);
            binding.ibExternalPlus.setClickable(false);
        } else {
            binding.ibExternalPlus.setClickable(true);
            if (isDefault) {
                binding.ibExternalPlus.setBackgroundResource(R.drawable.button_circular_primary);
            } else {
                binding.ibExternalPlus.setBackgroundResource(R.drawable.button_circular_deep_blue);
            }
        }
        if (mAmplitudePos == 0) {
            binding.ibExternalMinus.setBackgroundResource(R.drawable.button_circular_grey_three_hundred);
            binding.ibExternalMinus.setClickable(false);
        } else {
            binding.ibExternalMinus.setClickable(true);
            if (isDefault) {
                binding.ibExternalMinus.setBackgroundResource(R.drawable.button_circular_primary);
            } else {
                binding.ibExternalMinus.setBackgroundResource(R.drawable.button_circular_deep_blue);
            }
        }
    }

    public void showNeurostimulationProgressDialog() {
        if (stimulationProgressDialog == null) {
            stimulationProgressDialog = new StimulationProgressDialog(requireContext());
        }
        if (!stimulationProgressDialog.isShowing()) {
            stimulationProgressDialog.show();
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateImplantTunnellingUI(boolean success) {
        if (stimulationProgressDialog != null && stimulationProgressDialog.isShowing()) {
            stimulationProgressDialog.dismiss();
        }
        if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIMEXT) {
            mMainActivity.isImplantInterrogationDone = true;
            binding.btExternalStartStim.setEnabled(true);
            binding.btExternalStartStim.setPressed(false);
            binding.btExternalStartStim.setText(R.string.hold_to_deliver_neurostimulation);
            setPlusMinusButtonColors(true);
        }

        if (success) {
            binding.tvExternalAmplitude.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
        } else {
            mMainActivity.showWandTabCommunicationIssueDialog();
        }
    }

    private void initializeLeadRWarnButton() {
        binding.btnLeadRWarn.setOnClickListener(v -> {
            showLeadRWarningIfFound();
        });
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getStimLeadR();
        boolean isWarningFound = leadRValue > Utility.maxLeadR;
        if (isWarningFound) {
            binding.btnLeadRWarn.setVisibility(View.VISIBLE);
            LeadRImplantTunnelingDialog dialog = new LeadRImplantTunnelingDialog(requireContext());
            dialog.setConfirmButtonListener(v -> dialog.dismiss());
            dialog.show();
        } else {
            binding.tvLeadRVal.setText(R.string.ok);
            binding.btnLeadRWarn.setVisibility(View.GONE);
        }
    }
}
