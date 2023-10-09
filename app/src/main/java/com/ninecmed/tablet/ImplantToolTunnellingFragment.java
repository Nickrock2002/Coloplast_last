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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.events.OnConnectedUIEvent;
import com.ninecmed.tablet.events.OnDisconnectedUIEvent;
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ImplantToolTunnellingFragment extends Fragment {
    private static final String TAG = "ImplantToolTunnellingFragment";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 8;                                                                   // Set default position ot 1.5V
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private Button btnLeadRWarn;
    private TextView tvLeadR;
    ImageButton plus;
    ImageButton minus;

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
        View view = inflater.inflate(R.layout.implant_tunneling_fragment, container, false);

        initializeStimulationButton(view);
        initializeAmplitudeButton(view);
        initializeTitle(view);
        initializeLeadRWarnButton(view);

        mMainActivity = (MainActivity) getActivity();
        return view;
    }

    private void initializeTitle(View view) {
        TextView textView = view.findViewById(R.id.tv_implant_title);
        String text = textView.getText().toString();

        // Create a SpannableString to apply styles
        SpannableString spannableString = new SpannableString(text);

        // Apply bold style to "Implant Tool Tunnelling"
        int startIndex1 = text.indexOf(getString(R.string.external_title));
        int endIndex1 = startIndex1 + getString(R.string.external_title).length();
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex1, endIndex1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Apply bold style to "ITNS Interrogation Tab"
        int startIndex2 = text.indexOf(getString(R.string.itns_interrogation_tab));
        int endIndex2 = startIndex2 + getString(R.string.itns_interrogation_tab).length();
        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex2, endIndex2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the modified text in the TextView
        textView.setText(spannableString);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeStimulationButton(View view) {
        final Button stimulate = view.findViewById(R.id.btExternalStartStim);
        stimulate.setOnTouchListener((view1, motionEvent) -> {
            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    if (mNow + 500 < System.currentTimeMillis()) {
                        stimulate.setPressed(true);
                        mMainActivity.wandComm.setStimulationExt(true);
                        stimulate.setText(getString(R.string.stimulation_active));
                        WandData.invalidateStimLeadI();

                        mNow = System.currentTimeMillis();
                        mStimEnabled = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (mStimEnabled) {
                        stimulate.setPressed(false);
                        stimulate.setText(R.string.hold_to_deliver_neurostimulation);
                        if (mNow + 1500 < System.currentTimeMillis()) {
                            mMainActivity.wandComm.setStimulationExt(false);
                            mStimEnabled = false;
                        } else {
                            mHandler.postDelayed(holdStimulationRunnable, mNow + 1500 - System.currentTimeMillis());
                        }

                        Drawable drawable = plus.getBackground().mutate();
                        drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));
                        Drawable drawablePlus = minus.getBackground().mutate();
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
    private void initializeAmplitudeButton(View view) {
        plus = view.findViewById(R.id.ibExternalPlus);
        minus = view.findViewById(R.id.ibExternalMinus);

        plus.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                plus.setPressed(true);
                if (mAmplitudePos < 42) {
                    mAmplitudePos += 1;
                }
                WandData.setStimAmplitude(mAmplitudePos);
                WandData.invalidateStimExtLeadI();

                updateImplantTunnellingUI(true);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                plus.setPressed(false);
                Drawable drawable = plus.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = minus.getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
            }
            return true;
        });

        minus.setOnTouchListener((view12, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                minus.setPressed(true);
                if (mAmplitudePos > 0) {
                    mAmplitudePos -= 1;
                }
                WandData.setStimAmplitude(mAmplitudePos);
                WandData.invalidateStimExtLeadI();

                updateImplantTunnellingUI(true);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                minus.setPressed(false);
                Drawable drawable = plus.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = minus.getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
            }

            return true;
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnConnectedUIEvent event) {
        if (event.getTabEnum() == TabEnum.EXTERNAL) {
            //TODO : check if really required
            //OnConnected();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnDisconnectedUIEvent event) {
        if (event.getTabEnum() == TabEnum.EXTERNAL) {
            //TODO : check if really required
            //OnDisconnected();
        }
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

    private void initializeLeadRWarnButton(View view) {
        btnLeadRWarn = view.findViewById(R.id.btn_lead_r_warn);
        tvLeadR = view.findViewById(R.id.tv_lead_r_val);
        btnLeadRWarn.setOnClickListener(v -> {
            showLeadRWarningIfFound();
        });
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getStimLeadR();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000;
        if (isWarningFound) {
            btnLeadRWarn.setVisibility(View.VISIBLE);
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
            tvLeadR.setText(R.string.ok);
            btnLeadRWarn.setVisibility(View.GONE);
        }
    }
}
