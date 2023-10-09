package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.dialogues.AmplitudeDialogue;
import com.ninecmed.tablet.dialogues.BatteryReplaceRRTDialogue;
import com.ninecmed.tablet.dialogues.FrequencyDialogue;
import com.ninecmed.tablet.dialogues.LeadRDialogue;
import com.ninecmed.tablet.dialogues.ProgramTherapyDayDateDialogue;
import com.ninecmed.tablet.dialogues.ProgramTherapyTimeOfDayDialogue;
import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.ProgramSuccessEvent;
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class ProgramTherapyFragment extends Fragment {
    private static final String TAG = "ProgramTherapyFragment";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 0;
    private int checkedRadioButtonId = -1;
    Button btnImplantBatteryStatus;
    Button btnLeadRWarn;
    TextView tvLeadRVal;
    Button btnAmplitudeVal;
    Button btnInterrogate;
    Button btnFrequencyVal;
    Button btnDayDateVal;
    Button btnTimeOfDayVal;
    Button btnProgram;
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private int lastSetHour;
    private int lastSetMinute;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.fragment_program_therapy, container, false);

        initializeInterrogateButton(view);
        initializeProgramButton(view);

        setUpRRTButtonClick(view);
        setUpLeadRButtonClick(view);
        setUpAmplitudeButtonClick(view);
        setUpFrequencyButtonClick(view);
        setUpDateButtonClick(view);
        setUpTimeButtonClick(view);
        return view;
    }

    private void setUpRRTButtonClick(View rootView) {
        btnImplantBatteryStatus = rootView.findViewById(R.id.btn_implant_battery_status);

        btnImplantBatteryStatus.setOnClickListener(view -> {
            final BatteryReplaceRRTDialogue dialogue = new BatteryReplaceRRTDialogue(getActivity());
            dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
            dialogue.show();
        });
    }

    private void setUpLeadRButtonClick(View rootView) {
        btnLeadRWarn = rootView.findViewById(R.id.btn_lead_r_warn);
        tvLeadRVal = rootView.findViewById(R.id.tv_lead_r_val);

        btnLeadRWarn.setOnClickListener(view -> {
            displayLeadRDialogue();
        });
    }

    void displayLeadRDialogue() {
        float leadRValue = WandData.getLeadR();
        float leadIValue = WandData.getLeadI();
        final LeadRDialogue dialogue = new LeadRDialogue(getActivity());
        dialogue.setLeadRValue(leadRValue);
        dialogue.setLeadIValue(leadIValue);
        dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
        dialogue.show();
    }

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    private void setUpAmplitudeButtonClick(View rootView) {
        btnAmplitudeVal = rootView.findViewById(R.id.btn_amplitude_val);
        btnAmplitudeVal.setOnClickListener(amplitudeButton -> {
            mAmplitudePos = WandData.getAmplitudePos();
            float amplitudeVal = WandData.getAmpFromPos(mAmplitudePos);
            final AmplitudeDialogue dialogue = new AmplitudeDialogue(getActivity());
            dialogue.setAmplitude(amplitudeVal);
            dialogue.setItnsMinusListener((minusButton, motionEvent) -> {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    minusButton.setPressed(true);
                    if (mAmplitudePos < 42) {
                        mAmplitudePos += 1;
                        //MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                    }
                    WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                    if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                        mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                    } else {
                        mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                    }

                    TextView amp = dialogue.findViewById(R.id.tv_itns_amplitude);
                    amp.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));

                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    minusButton.setPressed(false);
                    Drawable drawable = dialogue.getMinusButtonRef().getBackground().mutate();
                    drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                    Drawable drawablePlus = dialogue.getPlusButtonRef().getBackground().mutate();
                    drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                }
                return true;
            });
            dialogue.setItnsPlusListener((plusButton, motionEvent) -> {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    plusButton.setPressed(true);
                    if (mAmplitudePos > 0) {
                        mAmplitudePos -= 1;
                        //MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                    }
                    WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                    TextView amp = dialogue.findViewById(R.id.tv_itns_amplitude);
                    amp.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));

                    if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                        mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                    } else {
                        mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    plusButton.setPressed(false);
                    Drawable drawable = dialogue.getMinusButtonRef().getBackground().mutate();
                    drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                    Drawable drawablePlus = dialogue.getPlusButtonRef().getBackground().mutate();
                    drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                }
                return true;
            });
            dialogue.setStimulationButtonListener((stimulationButton, motionEvent) -> {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mNow + 500 < System.currentTimeMillis()) {
                            stimulationButton.setPressed(true);
                            mMainActivity.wandComm.setStimulation(true);
                            //MakeTone(ToneGenerator.TONE_PROP_BEEP);
                            ((Button) stimulationButton).setText(R.string.stimulation_active);
                            WandData.invalidateStimLeadI();

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
                            stimulationButton.setPressed(false);
                            ((Button) stimulationButton).setText(R.string.hold_to_deliver_neurostimulation);
                            //stimulate.setEnabled(false);
                            //stimulate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            // Set delay to 1500 to be the same delay as ExternalFragment
                            if (mNow + 1500 < System.currentTimeMillis()) {
                                mMainActivity.wandComm.setStimulation(false);

//                                MakeTone(ToneGenerator.TONE_PROP_NACK);
                                mStimEnabled = false;
                            } else {
                                mHandler.postDelayed(HoldStimulation, mNow + 1500 - System.currentTimeMillis());
                            }
                            Drawable drawablePlus = dialogue.getPlusButtonRef().getBackground().mutate();
                            drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));
                            Drawable drawableMinus = dialogue.getMinusButtonRef().getBackground().mutate();
                            drawableMinus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));

                            dialogue.getConfirmButtonRef().setClickable(true);
                            dialogue.getConfirmButtonRef().setEnabled(true);
                        }
                        break;
                }
                return true;
            });
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
            dialogue.setConfirmButtonListener(confirmView -> {

                /*if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }*/

                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
                Drawable drawable = amplitudeButton.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private final Runnable HoldStimulation = () -> {
        mMainActivity.wandComm.setStimulation(false);
        mStimEnabled = false;
    };

    private void setUpFrequencyButtonClick(View rootView) {
        btnFrequencyVal = rootView.findViewById(R.id.btn_frequency_val);

        btnFrequencyVal.setOnClickListener(frequencyButton -> {
            final FrequencyDialogue dialogue = new FrequencyDialogue(getActivity());
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
            dialogue.setConfirmButtonListener(confirmView -> {
                checkedRadioButtonId = dialogue.getCheckedButtonId();
                RadioButton checkedRadioButton = dialogue.findViewById(checkedRadioButtonId);
                btnFrequencyVal.setText(checkedRadioButton.getText().toString());

                byte position = 0;
                position = Byte.parseByte(checkedRadioButton.getTag().toString());
                WandData.therapy[WandData.FUTURE] = position;

                if (WandData.therapy[WandData.CURRENT] == WandData.therapy[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.THERAPY);

                    // Clear date and time...
                    WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];
//                    Calendar c = Calendar.getInstance();
//                    c.setTimeInMillis(WandData.dateandtime[WandData.CURRENT]);

//                    btnDayDateVal.setText(WandData.GetDate());
//                    btnTimeOfDayVal.setText(WandData.GetTime());

                    /*
                    This is old code which was running as per wand's model number.
                    But this has been commented for future reference as currently only model 2 will be used

                    btnDayDateVal.setEnabled(true);
                    // Enable control if therapy is weekly for model 1
                    if (WandData.GetModelNumber() == 1)
                        btnDayDateVal.setClickable(WandData.therapy[WandData.CURRENT] == R.id.radio_weekly);
                        // else enable control if therapy daily, weekly, etc. for model 2
                    else
                        btnDayDateVal.setClickable(WandData.therapy[WandData.FUTURE] == R.id.radio_daily || WandData.therapy[WandData.FUTURE] == R.id.radio_weekly || WandData.therapy[WandData.FUTURE] == R.id.radio_fort_nightly || WandData.therapy[WandData.FUTURE] == R.id.radio_monthly || WandData.therapy[WandData.FUTURE] == R.id.radio_auto);

                    btnTimeOfDayVal.setEnabled(true);
//
//                    // Enable control if therapy is enabled
                    btnTimeOfDayVal.setClickable(WandData.therapy[WandData.CURRENT] != R.id.radio_off);*/
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.THERAPY);

//                    Calendar c = Calendar.getInstance();
//                    long timeDifferenceMillis = mMainActivity.getTimeDifferenceMillis();
//                    c.setTimeInMillis(c.getTimeInMillis() + timeDifferenceMillis);

                    /*
                    This is old code which was running as per wand's model number.
                    But this has been commented for future reference as currently only model 2 will be used

                    // If therapy set to daily for the model 1...
                    if (WandData.therapy[WandData.FUTURE] == R.id.radio_daily && WandData.GetModelNumber() == 1) {
                        btnDayDateVal.setText(R.string._3_dash);
                        btnDayDateVal.setEnabled(true);
                        btnDayDateVal.setClickable(false);

                        btnTimeOfDayVal.setText(R.string._3_dash);
                        btnTimeOfDayVal.setEnabled(true);
                        btnTimeOfDayVal.setClickable(true);
                    }
                    // Else, therapy set to weekly for Model 1
                    else if (WandData.therapy[WandData.FUTURE] == R.id.radio_weekly && WandData.GetModelNumber() == 1) {
                        btnDayDateVal.setText(R.string._3_dash);
                        btnDayDateVal.setEnabled(true);
                        btnDayDateVal.setClickable(true);

                        btnTimeOfDayVal.setText(R.string._3_dash);
                        btnTimeOfDayVal.setEnabled(true);
                        btnTimeOfDayVal.setClickable(true);
                    }
                    // Else, if therapy is set for weekly, fortnightly or monthly for Model 2
                    else if ((WandData.therapy[WandData.FUTURE] == R.id.radio_weekly || WandData.therapy[WandData.FUTURE] == R.id.radio_fort_nightly || WandData.therapy[WandData.FUTURE] == R.id.radio_monthly || WandData.therapy[WandData.FUTURE] == R.id.radio_auto) && WandData.GetModelNumber() == 2) {

                        btnDayDateVal.setText(R.string._3_dash);
                        btnDayDateVal.setEnabled(true);
                        btnDayDateVal.setClickable(true);

                        btnTimeOfDayVal.setText(R.string._3_dash);
                        btnTimeOfDayVal.setEnabled(true);
                        btnTimeOfDayVal.setClickable(true);
                    }
                    // Else, therapy is off
                    else {
                        btnDayDateVal.setText(R.string._3_dash);
                        btnDayDateVal.setEnabled(true);
                        btnDayDateVal.setClickable(false);
                        btnTimeOfDayVal.setText(R.string._3_dash);
                        btnTimeOfDayVal.setEnabled(true);
                        btnTimeOfDayVal.setClickable(false);
                    }*/
                }
                if (WandData.therapy[WandData.CURRENT] == 0) { //Off Case
                    enableDisableDayDateButton(false);
                    enableDisableTimeOfDayButton(false);
                    enableDisableProgramButton(true);
                } else { // Other than Off
                    enableDisableDayDateButton(true);
                    enableDisableTimeOfDayButton(true);
                    enableDisableProgramButton(false);
                }
                btnDayDateVal.setText(R.string._3_dash);
                btnTimeOfDayVal.setText(R.string._3_dash);
                Drawable drawable = btnFrequencyVal.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));

                dialogue.dismiss();
            });
            dialogue.show();
            RadioButton rb;
            if (checkedRadioButtonId != -1) {
                rb = dialogue.findViewById(checkedRadioButtonId);
            } else {
                rb = dialogue.findViewById(R.id.radio_off);
            }
            rb.setChecked(true);
        });
    }

    private void setUpDateButtonClick(View rootView) {
        btnDayDateVal = rootView.findViewById(R.id.btn_start_day);

        btnDayDateVal.setOnClickListener(dayDateButton -> {
            final ProgramTherapyDayDateDialogue dialogue = new ProgramTherapyDayDateDialogue(getActivity(), mMainActivity.getTimeDifferenceMillis());
            dialogue.setCancelButtonListener(cancelView -> dialogue.dismiss());
            dialogue.setConfirmButtonListener(confirmView -> {
                DatePicker datePicker = dialogue.findViewById(R.id.datePicker);
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM / dd / yyyy", Locale.US);
                String formattedDate = dateFormat.format(calendar.getTime());

                calendar.setTimeInMillis(WandData.dateandtime[WandData.FUTURE]);       // Set Calendar object to future time
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                WandData.dateandtime[WandData.FUTURE] = calendar.getTimeInMillis();

                if (WandData.dateandtime[WandData.CURRENT] == WandData.dateandtime[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.DATE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.DATE);
                }
                btnDayDateVal.setText(formattedDate);

                if (!btnDayDateVal.getText().toString().equals(getString(R.string._3_dash)) && !btnTimeOfDayVal.getText().toString().equals(getString(R.string._3_dash))) {
                    enableDisableProgramButton(true);
                }

                Drawable drawable = btnDayDateVal.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private void setUpTimeButtonClick(View rootView) {
        btnTimeOfDayVal = rootView.findViewById(R.id.btn_time_of_day);

        btnTimeOfDayVal.setOnClickListener(timeOfDayButton -> {
            final ProgramTherapyTimeOfDayDialogue dialogue = new ProgramTherapyTimeOfDayDialogue(getActivity(), mMainActivity.getTimeDifferenceMillis(), lastSetHour, lastSetMinute);
            dialogue.setCancelButtonListener(cancelView -> dialogue.dismiss());
            dialogue.setConfirmButtonListener(confirmView -> {
                TimePicker timePicker = dialogue.findViewById(R.id.timePicker);

                // Get the selected lastSetHour and lasSetMinute from the TimePicker
                lastSetHour = timePicker.getHour();
                lastSetMinute = timePicker.getMinute();

                // Determine if it's AM or PM
                String amPm;
                if (lastSetHour < 12) {
                    amPm = "AM";
                    String formattedTime = String.format("%02d:%02d %s", lastSetHour, lastSetMinute, amPm);
                    btnTimeOfDayVal.setText(formattedTime);
                } else {
                    amPm = "PM";
                    int hrToShow = lastSetHour - 12;
                    String formattedTime = String.format("%02d:%02d %s", hrToShow, lastSetMinute, amPm);
                    btnTimeOfDayVal.setText(formattedTime);
                }

                // Update the button text with the formatted time


                Calendar futureTime = Calendar.getInstance();
                futureTime.setTimeInMillis(WandData.dateandtime[WandData.FUTURE]);
                futureTime.set(Calendar.MINUTE, lastSetMinute);
                futureTime.set(Calendar.HOUR_OF_DAY, lastSetHour);
                WandData.dateandtime[WandData.FUTURE] = futureTime.getTimeInMillis();

                if (WandData.dateandtime[WandData.CURRENT] == WandData.dateandtime[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.TIME);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.TIME);
                }

                if (!btnDayDateVal.getText().toString().equals(getString(R.string._3_dash)) && !btnTimeOfDayVal.getText().toString().equals(getString(R.string._3_dash))) {
                    enableDisableProgramButton(true);
                }

                Drawable drawable = btnTimeOfDayVal.getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private void enableDisableProgramButton(boolean enable) {
        btnProgram.setClickable(enable);
        btnProgram.setEnabled(enable);
    }

    private void enableDisableAmplitudeButton(boolean enable) {
        btnAmplitudeVal.setClickable(enable);
        btnAmplitudeVal.setEnabled(enable);
    }

    private void enableDisableFrequencyButton(boolean enable) {
        btnFrequencyVal.setClickable(enable);
        btnFrequencyVal.setEnabled(enable);
    }

    private void enableDisableDayDateButton(boolean enable) {
        btnDayDateVal.setClickable(enable);
        btnDayDateVal.setEnabled(enable);
    }

    private void enableDisableTimeOfDayButton(boolean enable) {
        btnTimeOfDayVal.setClickable(enable);
        btnTimeOfDayVal.setEnabled(enable);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton(View view) {
        btnInterrogate = view.findViewById(R.id.btn_interrogate);
        btnInterrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                btnInterrogate.setPressed(true);
                mMainActivity.wandComm.interrogate();
                btnInterrogate.setClickable(false);
//                MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                btnInterrogate.setPressed(false);
            }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeProgramButton(View view) {
        btnProgram = view.findViewById(R.id.btn_program);
        btnProgram.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                showProgramConfirmationDialog();
            }
            return true;
        });
    }

    public void showProgramConfirmationDialog() {
        View rootView = getView();

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_program_itns);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            long future = WandData.dateandtime[WandData.FUTURE];
            long now = c.getTimeInMillis() + mMainActivity.getTimeDifferenceMillis();
            if (WandData.therapy[WandData.FUTURE] >= 1) {
                if (future < (now + 1000L * 3600L)) {
                    // Don't allow therapy to be set within 1 hour of now because only a
                    // magnet could stop therapy, telemetry can't interrupt therapy for
                    // the model 2.
                    //showDateTimeMsgDialog(getString(R.string.itns_time_before_now_msg));
                    showIncorrectTimeDialog();
                    dialog.dismiss();
                    return;
                }
            }

            if (mMainActivity.wandComm.anyAmplitudeChanges()) {
                WandData.invalidateStimLeadI();
            }
            mMainActivity.wandComm.program();
            dialog.dismiss();
        });

        TextView tvAmpVal = dialog.findViewById(R.id.tv_amp_val);
        tvAmpVal.setText(WandData.getAmplitude());

        TextView tvFreqVal = dialog.findViewById(R.id.tv_freq_val);
        tvFreqVal.setText(WandData.getTherapy(requireContext()));

        TextView tvDayVal = dialog.findViewById(R.id.tv_start_day_date_val);
        if (rootView != null) {
            Button dateBtn = rootView.findViewById(R.id.btn_start_day);
            tvDayVal.setText(dateBtn.getText().toString());
        }

        TextView tvTimeVal = dialog.findViewById(R.id.tv_time_val);
        if (rootView != null) {
            Button timeBtn = rootView.findViewById(R.id.btn_time_of_day);
            tvTimeVal.setText(timeBtn.getText().toString());
        }

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void showIncorrectTimeDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_incorrect_date_time);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_confirm_incorrect_time);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void showProgramSuccessDialog() {
        View rootView = getView();
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_program_itns_success);

        Button btnOk = dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            enableDisableProgramButton(false);

            Drawable drawableAmplBtn = btnAmplitudeVal.getBackground().mutate();
            drawableAmplBtn.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));

            Drawable drawableFrqBtn = btnFrequencyVal.getBackground().mutate();
            drawableFrqBtn.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));

            Drawable drawableDateBtn = btnDayDateVal.getBackground().mutate();
            drawableDateBtn.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));

            Drawable drawableTimeBtn = btnTimeOfDayVal.getBackground().mutate();
            drawableTimeBtn.setTint(ActivityCompat.getColor(requireContext(), R.color.colorPrimary));

            dialog.dismiss();
        });

        TextView tvAmpVal = dialog.findViewById(R.id.tv_amp_val);
        tvAmpVal.setText(WandData.getAmplitude());

        TextView tvFreqVal = dialog.findViewById(R.id.tv_freq_val);
        tvFreqVal.setText(WandData.getTherapy(requireContext()));

        TextView tvDayVal = dialog.findViewById(R.id.tv_start_day_date_val);
        if (rootView != null) {
            Button dateBtn = rootView.findViewById(R.id.btn_start_day);
            tvDayVal.setText(dateBtn.getText().toString());
        }

        TextView tvTimeVal = dialog.findViewById(R.id.tv_time_val);
        if (rootView != null) {
            Button timeBtn = rootView.findViewById(R.id.btn_time_of_day);
            tvTimeVal.setText(timeBtn.getText().toString());
        }
        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void showDateTimeMsgDialog(String string) {
        View view = getView();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

        alertDialog.setTitle(string);
        alertDialog.setMessage(R.string.itns_time_correct_msg);

        alertDialog.setPositiveButton(getString(R.string.all_ok), (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });
        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            updateUI(event.isUiUpdateSuccess());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ItnsUpdateAmpEvent event) {
        updateAmplitude();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProgramSuccessEvent event) {
        showProgramSuccessDialog();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    public void updateUI(boolean success) {
        View view = getView();
        if (success & view != null) {
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                // Re-enable changed parameters (and the test stim button) only when
                // UIUpdate is called - meaning that the state machine has finished its tasks
                mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                showLeadRWarningIfFound();
                enableDisableFrequencyButton(true);
            } else if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.PROGRAM) {
                resetChangedParameters();
                // TODO reset all the values here
            } else { /* This is interrogate callback */
//                MakeTone(ToneGenerator.TONE_CDMA_PIP);
                btnInterrogate.setClickable(true);
                TextView mn = Objects.requireNonNull(view).findViewById(R.id.tv_itns_model_number);
                mn.setText((WandData.getModelNumber(view.getContext())));

                TextView sn = view.findViewById(R.id.tv_itns_serial_val);
                sn.setText(WandData.getSerialNumber());

                showBatteryWarningIfLow(view);
                showLeadRWarningIfFound();

                enableDisableAmplitudeButton(true);

                setInitialAmplitudeAndEnableAmplitudeButton();

                String implToolFrequency = WandData.getTherapy(requireContext());
                btnFrequencyVal.setText(implToolFrequency);

                if (implToolFrequency != null && !implToolFrequency.isEmpty()) {
                    enableDisableFrequencyButton(true);
                    if (implToolFrequency.equals(getString(R.string.off))) {
                        enableDisableDayDateButton(false);
                        enableDisableTimeOfDayButton(false);
                    } else {
                        enableDisableDayDateButton(true);
                        enableDisableTimeOfDayButton(true);
                    }
                } else {
                    enableDisableFrequencyButton(false);
                }

                String date = WandData.getDate();
                String time = WandData.getTime();

                lastSetMinute = WandData.getProgramMinute();
                lastSetHour = WandData.getProgramHour();

                if (!date.isEmpty())
                    btnDayDateVal.setText(date);
                if (!time.isEmpty())
                    btnTimeOfDayVal.setText(time);

                resetChangedParameters();
                checkForReset();
            }
        } // Here's what happens on fail
        else {
            if (WandData.isITNSNew() && mMainActivity.wandComm.getCurrentJob() != WandComm.jobs.INTERROGATE) {
                mMainActivity.showSerialNumberMismatchWarnDialog();
                return;
            }
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
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
            } else if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.PROGRAM) {
                showProgramUnsuccessfulWarnDialog();
            } else {
                mMainActivity.showWandTabCommunicationIssueDialog();
            }
        }
    }

    private void showProgramUnsuccessfulWarnDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_programming_unsuccessful);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_confirm_prog_unsuccess);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void showBatteryWarningIfLow(View view) {
        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
        String rrt_result = WandData.getRRT(view.getContext());
        if (rrt_result != null && rrt_result.equals(getString(R.string.all_yes))) {
            btnImplantBatteryStatus.setVisibility(View.VISIBLE);
            cellv.setVisibility(View.INVISIBLE);
        } else {
            btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
            cellv.setVisibility(View.VISIBLE);
            cellv.setText(R.string.ok);
        }
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || leadRValue < 250;
        if (isWarningFound) {
            btnLeadRWarn.setVisibility(View.VISIBLE);
            tvLeadRVal.setVisibility(View.INVISIBLE);
            displayLeadRDialogue();
        } else {
            tvLeadRVal.setText(R.string.ok);
            btnLeadRWarn.setVisibility(View.INVISIBLE);
        }
    }

    private void setInitialAmplitudeAndEnableAmplitudeButton() {
        btnAmplitudeVal.setText(WandData.getAmplitude());
        mAmplitudePos = WandData.getAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];
    }

    private void checkForReset() {
        int resets = WandData.getResets();
        if (resets > 0) {
            showItnsResetDialog(resets);
        }
    }

    public void showItnsResetDialog(int count) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_reset_counter);

        Button btnResetCounter = dialog.findViewById(R.id.btn_reset_counter_confirm);
        btnResetCounter.setOnClickListener(v -> mMainActivity.wandComm.clearResetCounter());

        TextView tvCount = dialog.findViewById(R.id.tv_reset_counter);
        tvCount.setText(getString(R.string.implant_reset_counter).concat(String.valueOf(count)));

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        Objects.requireNonNull(dialog.getWindow()).setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void resetChangedParameters() {

        WandData.therapy[WandData.FUTURE] = WandData.therapy[WandData.CURRENT];

        WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];

        mAmplitudePos = WandData.getAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];

        //set radio button id after getting on the response.
        int position = WandData.getTherapyPos();
        if (position == 0) {
            checkedRadioButtonId = R.id.off;
        } else if (position == 1) {
            checkedRadioButtonId = R.id.radio_daily;
        } else if (position == 2) {
            checkedRadioButtonId = R.id.radio_weekly;
        } else if (position == 3) {
            checkedRadioButtonId = R.id.radio_fort_nightly;
        } else if (position == 4) {
            checkedRadioButtonId = R.id.radio_monthly;
        } else if (position == 5) {
            checkedRadioButtonId = R.id.radio_auto;
        }
        mMainActivity.wandComm.removeAllProgramChanges();
    }

    private void makeTone(int sound) {
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(sound, 150);
        long now = System.currentTimeMillis();
        while ((System.currentTimeMillis() - now) < 150) ;
        tone.release();
    }

    public void updateAmplitude() {
        btnAmplitudeVal.setText(WandData.getAmplitude());
        mAmplitudePos = WandData.getAmplitudePos();
    }
}