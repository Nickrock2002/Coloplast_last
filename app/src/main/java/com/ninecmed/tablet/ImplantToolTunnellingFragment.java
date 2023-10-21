package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.ImplantTunnelingFragmentBinding;
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ImplantToolTunnellingFragment extends Fragment {
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 8;                                                                   // Set default position ot 1.5V
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private ImplantTunnelingFragmentBinding binding;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getTabEnum() == TabEnum.EXTERNAL) {
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
        binding = ImplantTunnelingFragmentBinding.inflate(inflater, container, false);

        initializeStimulationButton();
        initializeAmplitudeButton();
        initializeTitle();
        initializeLeadRWarnButton();

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
        int startIndex2 = text.indexOf("ITNS Interrogation Tab");
        int endIndex2 = startIndex2 + "ITNS Interrogation Tab".length();
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
                        mMainActivity.wandComm.setStimulationExt(true);
                        binding.btExternalStartStim.setText(getString(R.string.stimulation_active));
                        mNow = System.currentTimeMillis();
                        mStimEnabled = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mStimEnabled) {
                        binding.btExternalStartStim.setPressed(false);
                        binding.btExternalStartStim.setText(R.string.hold_to_deliver_neurostimulation);
                        if (mNow + 1500 < System.currentTimeMillis()) {
                            mMainActivity.wandComm.setStimulationExt(false);
                            mStimEnabled = false;
                        } else {
                            mHandler.postDelayed(holdStimulationRunnable, mNow + 1500 - System.currentTimeMillis());
                        }

                        Drawable drawable = binding.ibExternalPlus.getBackground().mutate();
                        drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));
                        Drawable drawablePlus = binding.ibExternalMinus.getBackground().mutate();
                        drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));
                    }
                    break;
            }
            return true;
        });
    }

    private final Runnable holdStimulationRunnable = () -> {
        mMainActivity.wandComm.setStimulationExt(false);
        mStimEnabled = false;
    };

    @SuppressLint("ClickableViewAccessibility")
    private void initializeAmplitudeButton() {

        binding.ibExternalPlus.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                binding.ibExternalPlus.setPressed(true);
                if (mAmplitudePos < 42) {
                    mAmplitudePos += 1;
                }
                WandData.setStimAmplitude(mAmplitudePos);
                WandData.invalidateStimExtLeadI();

                updateImplantTunnellingUI(true);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                binding.ibExternalPlus.setPressed(false);
                Drawable drawable = binding.ibExternalPlus.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = binding.ibExternalMinus.getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
            }
            return true;
        });

        binding.ibExternalMinus.setOnTouchListener((view12, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                binding.ibExternalMinus.setPressed(true);
                if (mAmplitudePos > 0) {
                    mAmplitudePos -= 1;
                }
                WandData.setStimAmplitude(mAmplitudePos);
                WandData.invalidateStimExtLeadI();

                updateImplantTunnellingUI(true);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                binding.ibExternalMinus.setPressed(false);
                Drawable drawable = binding.ibExternalPlus.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = binding.ibExternalMinus.getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
            }

            return true;
        });
    }

    @SuppressLint("DefaultLocale")
    public void updateImplantTunnellingUI(boolean success) {
        if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIMEXT) {
            Button stimulate = requireView().findViewById(R.id.btExternalStartStim);
            stimulate.setEnabled(true);
        }

        if (success) {
            TextView amp = requireView().findViewById(R.id.tvExternalAmplitude);
            amp.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
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
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000;
        if (isWarningFound) {
            binding.btnLeadRWarn.setVisibility(View.VISIBLE);
            final Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_leadr_implant_tunneling);

            Button dialogButton = (Button) dialog.findViewById(R.id.btn_confirm_lead_r);
            dialogButton.setOnClickListener(v -> dialog.dismiss());

            setTheSystemButtonsHidden(dialog);
            Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
            dialog.getWindow().setLayout(dimensions.first, dimensions.second);
            dialog.show();
        } else {
            binding.tvLeadRVal.setText(R.string.ok);
            binding.btnLeadRWarn.setVisibility(View.GONE);
        }
    }
}
