package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
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

import com.ninecmed.tablet.databinding.FragmentProgramTherapyBinding;
import com.ninecmed.tablet.dialogues.AmplitudeDialog;
import com.ninecmed.tablet.dialogues.BatteryReplaceRRTDialog;
import com.ninecmed.tablet.dialogues.FrequencyDialog;
import com.ninecmed.tablet.dialogues.GetProgramConfirmationDialog;
import com.ninecmed.tablet.dialogues.IncorrectTimeDialog;
import com.ninecmed.tablet.dialogues.LeadRDialog;
import com.ninecmed.tablet.dialogues.ProgramItnsSuccessDialog;
import com.ninecmed.tablet.dialogues.ProgramTherapyDayDateDialog;
import com.ninecmed.tablet.dialogues.ProgramTherapyTimeOfDayDialog;
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
    FragmentProgramTherapyBinding binding;
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private int lastSetHour;
    private int lastSetMinute;
    //For amplitude, frequency, date & time respectively
    private final boolean[] valuesChanged = new boolean[4];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        binding = FragmentProgramTherapyBinding.inflate(inflater, container, false);

        initializeInterrogateButton();
        initializeProgramButton();
        setUpRRTButtonClick();
        setUpLeadRButtonClick();
        setUpAmplitudeButtonClick();
        setUpFrequencyButtonClick();
        setUpDateButtonClick();
        setUpTimeButtonClick();

        return binding.getRoot();
    }

    private void setUpRRTButtonClick() {
        binding.btnImplantBatteryStatus.setOnClickListener(view -> {
            final BatteryReplaceRRTDialog dialogue = new BatteryReplaceRRTDialog(getActivity());
            dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
            dialogue.show();
        });
    }

    private void setUpLeadRButtonClick() {
        binding.btnLeadRWarn.setOnClickListener(view -> {
            displayLeadRDialogue();
        });
    }

    private void displayLeadRDialogue() {
        float leadRValue = WandData.getLeadR();
        float leadIValue = WandData.getLeadI();
        final LeadRDialog dialogue = new LeadRDialog(getActivity());
        dialogue.setLeadRValue(leadRValue);
        dialogue.setLeadIValue(leadIValue);
        dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
        dialogue.show();
    }

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    private void setUpAmplitudeButtonClick() {
        binding.btnAmplitudeVal.setOnClickListener(amplitudeButton -> {
            mAmplitudePos = WandData.getAmplitudePos();
            float amplitudeVal = WandData.getAmpFromPos(mAmplitudePos);
            final AmplitudeDialog dialogue = new AmplitudeDialog(getActivity());
            dialogue.setAmplitude(amplitudeVal);
            dialogue.setItnsMinusListener(minusButton -> {
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

                Drawable drawable = dialogue.getMinusButtonRef().getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = dialogue.getPlusButtonRef().getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                dialogue.getConfirmButtonRef().setEnabled(false);
                dialogue.getCancelButtonRef().setEnabled(true);
            });
            dialogue.setItnsPlusListener(plusButton -> {
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

                Drawable drawable = dialogue.getMinusButtonRef().getBackground().mutate();
                drawable.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                Drawable drawablePlus = dialogue.getPlusButtonRef().getBackground().mutate();
                drawablePlus.setTint(ActivityCompat.getColor(requireContext(), R.color.colorBaseDeepBlue));
                dialogue.getConfirmButtonRef().setEnabled(false);
                dialogue.getCancelButtonRef().setEnabled(true);
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
                            dialogue.getCancelButtonRef().setEnabled(false);
                        }
                        break;
                }
                return true;
            });
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
            dialogue.setConfirmButtonListener(confirmView -> {
                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
                amplitudeButton.setBackgroundResource(R.drawable.rounded_button_dark_always);
                valuesChanged[0] = true;
                dialogue.dismiss();
                if (valuesChanged[1]) {
                    if (binding.btnFrequencyVal.getText().equals(getString(R.string.off))) {
                        enableDisableProgramButton(true);
                    } else {
                        enableDisableProgramButton(valuesChanged[2] && valuesChanged[3]);
                    }
                } else {
                    enableDisableProgramButton(false);
                }
            });
            dialogue.show();
        });
    }

    private final Runnable HoldStimulation = () -> {
        mMainActivity.wandComm.setStimulation(false);
        mStimEnabled = false;
    };

    private void setUpFrequencyButtonClick() {
        binding.btnFrequencyVal.setOnClickListener(frequencyButton -> {
            final FrequencyDialog dialogue = new FrequencyDialog(getActivity());
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
            dialogue.setConfirmButtonListener(confirmView -> {
                int prevFreq = WandData.therapy[WandData.FUTURE];
                checkedRadioButtonId = dialogue.getCheckedButtonId();
                RadioButton checkedRadioButton = dialogue.findViewById(checkedRadioButtonId);
                binding.btnFrequencyVal.setText(checkedRadioButton.getText().toString());
                byte position = Byte.parseByte(checkedRadioButton.getTag().toString());
                boolean freqChanged = position != WandData.therapy[WandData.FUTURE];
                WandData.therapy[WandData.FUTURE] = position;

                if (WandData.therapy[WandData.CURRENT] == WandData.therapy[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.THERAPY);
                    WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.THERAPY);
                }
                if (WandData.therapy[WandData.FUTURE] == 0) {
                    /*
                        Frequency selected as Off
                        When the user changes Frequency to "Off" have the application
                        automatically turn the Amplitude button dark blue .
                    */
                    valuesChanged[0] = true;
                    enableDisableProgramButton(true);
                    binding.btnAmplitudeVal.setBackgroundResource(R.drawable.rounded_button_dark_always);

                    binding.btnStartDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                    binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                    enableDisableDayDateButton(false);
                    enableDisableTimeOfDayButton(false);
                    binding.btnStartDay.setText(R.string._3_dash);
                    binding.btnTimeOfDay.setText(R.string._3_dash);
                } else {
                    /*
                        Frequency selected as other than Off
                        When the user changes frequency from off to other than Off, reset all the values to Coloplast blue
                    */
                    if (prevFreq == 0) {
                        valuesChanged[0] = false;
                        binding.btnAmplitudeVal.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                    }

                    valuesChanged[2] = false;
                    valuesChanged[3] = false;
                    binding.btnStartDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                    binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                    enableDisableDayDateButton(true);
                    enableDisableTimeOfDayButton(true);
                    enableDisableProgramButton(false);

                    if (freqChanged) {
                        binding.btnStartDay.setText(R.string._3_dash);
                        binding.btnTimeOfDay.setText(R.string._3_dash);
                    }
                }

                binding.btnFrequencyVal.setBackgroundResource(R.drawable.rounded_button_dark_always);
                valuesChanged[1] = true;

                dialogue.dismiss();
            });
            dialogue.show();
            RadioButton rb;
            if (checkedRadioButtonId != -1) {
                rb = dialogue.findViewById(checkedRadioButtonId);
            } else {
                rb = dialogue.findViewById(R.id.radio_off);
            }
            if (rb != null) {
                rb.setChecked(true);
            } else {
                rb = dialogue.findViewById(R.id.radio_off);
                rb.setChecked(true);
            }
        });
    }

    private void setUpDateButtonClick() {
        binding.btnStartDay.setOnClickListener(dayDateButton -> {
            final ProgramTherapyDayDateDialog dialogue = new ProgramTherapyDayDateDialog(
                    getActivity(), mMainActivity.getTimeDifferenceMillis(),
                    binding.btnStartDay.getText().toString(),
                    WandData.therapy[WandData.FUTURE] == 5);
            dialogue.setCancelButtonListener(cancelView -> dialogue.dismiss());
            dialogue.setConfirmButtonListener(confirmView -> {
                DatePicker datePicker = dialogue.findViewById(R.id.datePicker);
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MMM-yyyy", Locale.US);
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
                binding.btnStartDay.setText(formattedDate);
                binding.btnStartDay.setBackgroundResource(R.drawable.rounded_button_dark_always);
                valuesChanged[2] = true;

                enableDisableProgramButton(valuesChanged[0] && valuesChanged[1] && valuesChanged[3]);

                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private void setUpTimeButtonClick() {
        binding.btnTimeOfDay.setOnClickListener(timeOfDayButton -> {
            final ProgramTherapyTimeOfDayDialog dialogue = new ProgramTherapyTimeOfDayDialog(
                    getActivity(), mMainActivity.getTimeDifferenceMillis(), lastSetHour, lastSetMinute);
            dialogue.setCancelButtonListener(cancelView -> dialogue.dismiss());
            dialogue.setConfirmButtonListener(confirmView -> {
                TimePicker timePicker = dialogue.findViewById(R.id.timePicker);

                lastSetHour = timePicker.getHour();
                lastSetMinute = timePicker.getMinute();

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

                int hourToDisplay = lastSetHour;
                String amPm;
                if (lastSetHour < 12) {
                    amPm = "AM";
                    if (lastSetHour == 0) {
                        hourToDisplay = 12;
                    }
                } else {
                    amPm = "PM";
                    if (lastSetHour > 12) {
                        hourToDisplay -= 12;
                    }
                }

                String formattedTime = String.format(Locale.ENGLISH, "%02d:%02d %s",
                        hourToDisplay, lastSetMinute, amPm);
                binding.btnTimeOfDay.setText(formattedTime);
                binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_button_dark_always);
                valuesChanged[3] = true;

                enableDisableProgramButton(valuesChanged[0] && valuesChanged[1] && valuesChanged[2]);

                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private void enableDisableProgramButton(boolean enable) {
        binding.btnProgram.setClickable(enable);
        binding.btnProgram.setEnabled(enable);
    }

    private void enableDisableAmplitudeButton(boolean enable) {
        binding.btnAmplitudeVal.setClickable(enable);
        binding.btnAmplitudeVal.setEnabled(enable);
    }

    private void enableDisableFrequencyButton(boolean enable) {
        binding.btnFrequencyVal.setClickable(enable);
        binding.btnFrequencyVal.setEnabled(enable);
    }

    private void enableDisableDayDateButton(boolean enable) {
        binding.btnStartDay.setClickable(enable);
        binding.btnStartDay.setEnabled(enable);
    }

    private void enableDisableTimeOfDayButton(boolean enable) {
        binding.btnTimeOfDay.setClickable(enable);
        binding.btnTimeOfDay.setEnabled(enable);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton() {
        binding.btnInterrogate.setOnClickListener(interrogateButton -> {
            mMainActivity.wandComm.interrogate();
            binding.btnInterrogate.setClickable(false);
            binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_button_dark_always);

            resetAllButtonsWithDefaultBackground();
            disableAllTheButtons();
//                MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeProgramButton() {
        binding.btnProgram.setOnClickListener(programButton -> {
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_button_dark_always);
            Calendar c = Calendar.getInstance();
            long future = WandData.dateandtime[WandData.FUTURE];
            long now = c.getTimeInMillis() + mMainActivity.getTimeDifferenceMillis();
            if (future < (now + 1000L * 3600L)) {
                // Don't allow therapy to be set within 1 hour of now because only a
                // magnet could stop therapy, telemetry can't interrupt therapy for
                // the model 2.
                showIncorrectTimeDialog();
            } else {
                showProgramConfirmationDialog();
            }
        });
    }

    public void showProgramConfirmationDialog() {
        final GetProgramConfirmationDialog dialog = new GetProgramConfirmationDialog(requireContext());
        dialog.setAmpVal(binding.btnAmplitudeVal.getText().toString());
        dialog.setFreqVal(binding.btnFrequencyVal.getText().toString());
        dialog.setDayDateVal(binding.btnStartDay.getText().toString());
        dialog.setTimeOfDayVal(binding.btnTimeOfDay.getText().toString());

        dialog.setCancelButtonListener(v -> {
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            dialog.dismiss();
        });

        dialog.setConfirmButtonListener(v -> {
            if (mMainActivity.wandComm.anyAmplitudeChanges()) {
                WandData.invalidateStimLeadI();
            }
            mMainActivity.wandComm.program();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showIncorrectTimeDialog() {
        final IncorrectTimeDialog dialog = new IncorrectTimeDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            binding.btnTimeOfDay.setText(getString(R.string._3_dash));
            valuesChanged[3] = false;
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            enableDisableProgramButton(false);
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showProgramSuccessDialog() {
        final ProgramItnsSuccessDialog dialog = new ProgramItnsSuccessDialog(requireContext());

        dialog.setConfirmButtonListener(v -> {
            resetAllButtonsWithDefaultBackground();
            enableDisableProgramButton(false);

            dialog.dismiss();
        });

        dialog.setAmpVal(WandData.getAmplitude());
        dialog.setFreqVal(WandData.getTherapy(requireContext()));
        dialog.setDayDateVal(binding.btnStartDay.getText().toString());
        dialog.setTimeOfDayVal(binding.btnTimeOfDay.getText().toString());

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

    /*
     * Set default background to all the buttons
     * */
    private void resetAllButtonsWithDefaultBackground() {
        valuesChanged[0] = false;
        valuesChanged[1] = false;
        valuesChanged[2] = false;
        valuesChanged[3] = false;

        binding.btnAmplitudeVal.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
        binding.btnFrequencyVal.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
        binding.btnStartDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
        binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
        binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
    }

    private void disableAllTheButtons() {
        enableDisableAmplitudeButton(false);
        enableDisableFrequencyButton(false);
        enableDisableDayDateButton(false);
        enableDisableTimeOfDayButton(false);
        enableDisableProgramButton(false);
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
                String implToolFrequency = WandData.getTherapy(requireContext());
                if (implToolFrequency != null && !implToolFrequency.isEmpty()) {
                    enableDisableFrequencyButton(true);
                    if (implToolFrequency.equals(getString(R.string.off))) {
                        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
                        cellv.setText("_");
                    } else {
                        showBatteryWarningIfLow(view);
                    }
                }
            } else { /* This is interrogate callback */
//                MakeTone(ToneGenerator.TONE_CDMA_PIP);
                binding.btnInterrogate.setClickable(true);
                binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                TextView mn = Objects.requireNonNull(view).findViewById(R.id.tv_itns_model_number);
                mn.setText((WandData.getModelNumber(view.getContext())));

                TextView sn = view.findViewById(R.id.tv_itns_serial_val);
                sn.setText(WandData.getSerialNumber());

                showLeadRWarningIfFound();

                enableDisableAmplitudeButton(true);

                setInitialAmplitudeAndEnableAmplitudeButton();

                String implToolFrequency = WandData.getTherapy(requireContext());
                binding.btnFrequencyVal.setText(implToolFrequency);

                if (implToolFrequency != null && !implToolFrequency.isEmpty()) {
                    enableDisableFrequencyButton(true);
                    if (implToolFrequency.equals(getString(R.string.off))) {
                        enableDisableDayDateButton(false);
                        enableDisableTimeOfDayButton(false);
                        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
                        cellv.setText("_");
                    } else {
                        showBatteryWarningIfLow(view);
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
                    binding.btnStartDay.setText(date);
                if (!time.isEmpty())
                    binding.btnTimeOfDay.setText(time);

                resetChangedParameters();
                checkForReset();
            }
        } // Here's what happens on fail
        else {
            if (WandData.isITNSNew() && mMainActivity.wandComm.getCurrentJob() != WandComm.jobs.INTERROGATE) {
                mMainActivity.showSerialNumberMismatchWarnDialog();
                binding.btnInterrogate.setClickable(true);
                binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                return;
            }
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), (dialogInterface, i) -> dialogInterface.dismiss());
                alertDialog.show();
            } else if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.PROGRAM) {
                showProgramUnsuccessfulWarnDialog();
            } else {
                mMainActivity.showWandTabCommunicationIssueDialog();
                if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.INTERROGATE) {
                    binding.btnInterrogate.setClickable(true);
                    binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                }
            }
        }
    }

    private void showProgramUnsuccessfulWarnDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_programming_unsuccessful);

        Button btnCancel = dialog.findViewById(R.id.btn_confirm_prog_unsuccess);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            enableDisableProgramButton(true);
        });

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void showBatteryWarningIfLow(View view) {
        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
        String rrt_result = WandData.getRRT(view.getContext());

        if (rrt_result != null) {
            if (rrt_result.equals(getString(R.string.all_yes))) {
                binding.btnImplantBatteryStatus.setVisibility(View.VISIBLE);
                cellv.setVisibility(View.INVISIBLE);
            } else {
                binding.btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
                cellv.setVisibility(View.VISIBLE);
                cellv.setText(R.string.ok);
            }
        } else {
            cellv.setText(R.string.ok);
        }
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || (leadRValue < 250 && leadRValue > 0);
        if (isWarningFound) {
            binding.btnLeadRWarn.setVisibility(View.VISIBLE);
            binding.tvLeadRVal.setVisibility(View.INVISIBLE);
            displayLeadRDialogue();
        } else {
            binding.tvLeadRVal.setVisibility(View.VISIBLE);
            binding.tvLeadRVal.setText(R.string.ok);
            binding.btnLeadRWarn.setVisibility(View.INVISIBLE);
        }
    }

    private void setInitialAmplitudeAndEnableAmplitudeButton() {
        binding.btnAmplitudeVal.setText(WandData.getAmplitude());
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
        binding.btnAmplitudeVal.setText(WandData.getAmplitude());
        mAmplitudePos = WandData.getAmplitudePos();
    }
}