package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.OnConnectedUIEvent;
import com.ninecmed.tablet.events.OnDisconnectedUIEvent;
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

public class ItnsFragment extends Fragment {
    private static final String TAG = "ItnsFragment";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 0;
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private Button btnLeadRWarn;

    private TextView tvLeadR;
    private ImageButton plus;
    private ImageButton minus;
    private Button stimulate;


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
        View view = inflater.inflate(R.layout.itns_fragment_new, container, false);

        initializeStimulationButton(view);
        initializeInterrogateButton(view);
        initializeAmpControls(view);
        initializeLeadRWarnButton(view);

        mMainActivity = (MainActivity) getActivity();
        return view;
    }

    private void initializeLeadRWarnButton(View view) {
        btnLeadRWarn = view.findViewById(R.id.btn_lead_r_warn);
        tvLeadR = view.findViewById(R.id.tv_lead_r_val);
        btnLeadRWarn.setOnClickListener(v -> {
            showLeadRWarningIfFound();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton(View view) {
        final Button interrogate = view.findViewById(R.id.btItnsInterrogate);
        interrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                interrogate.setPressed(true);
                mMainActivity.wandComm.interrogate();
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                interrogate.setPressed(false);
            }
            return true;
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeStimulationButton(View view) {
        stimulate = view.findViewById(R.id.btItnsStartStim);
        stimulate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mNow + 500 < System.currentTimeMillis()) {
                            stimulate.setPressed(true);
                            mMainActivity.wandComm.setStimulation(true);
                            //MakeTone(ToneGenerator.TONE_PROP_BEEP);
                            stimulate.setText(R.string.stimulation_active);
                            WandData.InvalidateStimLeadI();

                            /*TextView leadi = Objects.requireNonNull(getView()).findViewById(R.id.tvItnsLeadI);
                            leadi.setText(WandData.GetLeadI());

                            TextView leadr = getView().findViewById(R.id.tvItnsLeadR);
                            leadr.setText(WandData.GetLeadR());*/

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
                            //EnableInterrogateButton(false, false);
                            //EnableProgramButton(false, false);
                            //StartStimProgressBar();
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
                            stimulate.setPressed(false);
                            stimulate.setText(R.string.hold_to_deliver_neurostimulation);
                            //stimulate.setEnabled(false);
                            //stimulate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
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
            }
        });
    }

    private final Runnable HoldStimulation = () -> {
        mMainActivity.wandComm.setStimulation(false);
        //MakeTone(ToneGenerator.TONE_PROP_NACK);
        mStimEnabled = false;
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            updateItnsUI(event.isUiUpdateSuccess());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ItnsUpdateAmpEvent event) {
        updateAmplitude();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeAmpControls(View view) {
        plus = view.findViewById(R.id.ibItnsPlus);
        minus = view.findViewById(R.id.ibItnsMinus);

        plus.setEnabled(false);
        minus.setEnabled(false);

        // Use OnTouchListener rather than onClickListener so that we register the change
        // on the action down, rather that on the action up!
        plus.setOnTouchListener((view12, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                plus.setPressed(true);
                if (mAmplitudePos < 42) {
                    mAmplitudePos += 1;
                }

                WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                TextView amp = requireView().findViewById(R.id.tvItnsAmplitude);
                amp.setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));

                if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {

                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                plus.setPressed(false);
            }
            return true;
        });

        minus.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                minus.setPressed(true);
                if (mAmplitudePos > 0) {
                    mAmplitudePos -= 1;
                }

                WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                TextView amp = requireView().findViewById(R.id.tvItnsAmplitude);
                amp.setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));

                if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }

            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                minus.setPressed(false);
            }
            return true;
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnConnectedUIEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            //OnConnected();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(OnDisconnectedUIEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            //OnDisconnected();
        }
    }

    public void updateItnsUI(boolean success) {
        View view = getView();

        if (success) {
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                showLeadRWarningIfFound();
            } else {
                TextView mn = Objects.requireNonNull(view).findViewById(R.id.tvItnsModelNumber);
                mn.setText((WandData.GetModelNumber(view.getContext())));

                TextView sn = view.findViewById(R.id.tvItnsSN);
                sn.setText(WandData.GetSerialNumber());

                plus.setEnabled(true);
                plus.setImageResource(R.drawable.ic_plus_white);
                plus.setBackgroundResource(R.drawable.button_circular);

                minus.setEnabled(true);
                minus.setBackgroundResource(R.drawable.button_circular);
                minus.setImageResource(R.drawable.ic_minus_white);

                stimulate.setEnabled(true);
                stimulate.setTextColor(getResources().getColor(R.color.colorWhite));

                TextView amp = view.findViewById(R.id.tvItnsAmplitude);
                amp.setTextColor(getResources().getColor(R.color.cardview_dark_background));

                showLeadRWarningIfFound();
                checkForReset();
                setInitialAmplitude();
            }
        }
        // Here's what happens on fail
        else {
            if (WandData.IsITNSNew() && mMainActivity.wandComm.getCurrentJob() != WandComm.jobs.INTERROGATE) {
                mMainActivity.showSerialNumberMismatchWarnDialog();
                return;
            }
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                //StopStimProgressBar();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();
            } else {
                mMainActivity.showWandTabCommunicationIssueDialog();
            }
        }
    }

    private void setInitialAmplitude() {
        TextView amp = requireView().findViewById(R.id.tvItnsAmplitude);
        amp.setText(WandData.GetAmplitude());
        mAmplitudePos = WandData.GetAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.GetLeadR();
        float leadIValue = WandData.GetLeadI();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || leadRValue < 250;
        if (isWarningFound) {
            btnLeadRWarn.setVisibility(View.VISIBLE);
            final Dialog dialog = new Dialog(requireContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_lead_surgery);

            Button dialogButton = (Button) dialog.findViewById(R.id.btn_confirm_lead_r);
            dialogButton.setOnClickListener(v -> dialog.dismiss());

            TextView title = (TextView) dialog.findViewById(R.id.tv_warn_title);
            TextView tvElectrodeTip = (TextView) dialog.findViewById(R.id.tv_electrode_tip);
            if (leadRValue > 2000) {
                title.setText(R.string.lead_r_is_2000_ohms);
                tvElectrodeTip.setText(R.string.electrode_tip_must_make_contact_with_the_tissue);
            } else {
                title.setText(R.string.lead_r_is_250_ohms);
                tvElectrodeTip.setText(R.string.use_a_different_intibia_itns);
            }

            TextView tvLeadRV = (TextView) dialog.findViewById(R.id.tv_lead_r_val);
            tvLeadRV.setText(leadRValue + " ohms");

            TextView tvLeadIV = (TextView) dialog.findViewById(R.id.tv_lead_i_val);
            tvLeadIV.setText(leadIValue + " mA");

            setTheSystemButtonsHidden(dialog);
            Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
            dialog.getWindow().setLayout(dimensions.first, dimensions.second);
            dialog.show();
        } else {
            tvLeadR.setText(R.string.ok);
            btnLeadRWarn.setVisibility(View.GONE);
        }
    }

    private void checkForReset() {
        int resets = WandData.GetResets();
        if (resets > 0) {
            showItnsResetDialog(resets);
        }
    }

    public void showItnsResetDialog(int count) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_reset_counter);

        Button btnResetCounter = (Button) dialog.findViewById(R.id.btn_reset_counter_confirm);
        btnResetCounter.setOnClickListener(v -> {
            mMainActivity.wandComm.clearResetCounter();
        });

        TextView tvCount = (TextView) dialog.findViewById(R.id.tv_reset_counter);
        tvCount.setText(getString(R.string.implant_reset_counter) + count);

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    public void updateAmplitude() {
        View view = getView();

        TextView amp = view.findViewById(R.id.tvItnsAmplitude);
        amp.setText(WandData.GetAmplitude());
        amp.setTextColor(Color.BLACK);
        mAmplitudePos = WandData.GetAmplitudePos();
    }
}