package com.ninecmed.tablet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.FragmentProgramTherapyBinding;
import com.ninecmed.tablet.dialogs.AmplitudeDialog;
import com.ninecmed.tablet.dialogs.BatteryReplaceRRTDialog;
import com.ninecmed.tablet.dialogs.FrequencyDialog;
import com.ninecmed.tablet.dialogs.ProgramConfirmationDialog;
import com.ninecmed.tablet.dialogs.IncorrectTimeDialog;
import com.ninecmed.tablet.dialogs.ItnsResetCVDialog;
import com.ninecmed.tablet.dialogs.LeadRClinicalDialog;
import com.ninecmed.tablet.dialogs.ProgramITNSProgressDialog;
import com.ninecmed.tablet.dialogs.ProgramItnsSuccessDialog;
import com.ninecmed.tablet.dialogs.ProgramTherapyDayDateDialog;
import com.ninecmed.tablet.dialogs.ProgramTherapyTimeOfDayDialog;
import com.ninecmed.tablet.dialogs.ProgrammingUnsuccessfulDialog;
import com.ninecmed.tablet.dialogs.ResetDateTimeDialog;
import com.ninecmed.tablet.dialogs.SerialNumberMismatchDialog;
import com.ninecmed.tablet.dialogs.StimulationProgressDialog;
import com.ninecmed.tablet.dialogs.WandAndITNSCommIssueDialog;
import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.ProgramSuccessEvent;
import com.ninecmed.tablet.events.UIUpdateEvent;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;

public class ProgramTherapyFragment extends Fragment {
    public static final String CLASS_NAME = "CVF";
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
    ArrayList<Dialog> dialogs;
    AmplitudeDialog amplitudeDialog;
    FrequencyDialog freqDialog;
    ProgramTherapyDayDateDialog dayDateDialog;
    ProgramTherapyTimeOfDayDialog timeOfDayDialog;
    ProgramITNSProgressDialog dialogProgrammingInProgress;
    StimulationProgressDialog stimulationProgressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (binding == null)
            binding = FragmentProgramTherapyBinding.inflate(inflater, container, false);

        dialogs = new ArrayList<>();
        initializeInterrogateButton();
        initializeProgramButton();
        setUpRRTButtonClick();
        setUpLeadRButtonClick();
        setUpAmplitudeButtonClick();
        setUpFrequencyButtonClick();
        setUpDateButtonClick();
        setUpTimeButtonClick();
        setDateTime();
        String modelNumber = WandData.getModelNumber(getContext());
        if (mMainActivity.isInterrogationDone
                && !getString(R.string.all_model_number_one).equals(modelNumber)) {
            setupWandData(modelNumber);
        }
        return binding.getRoot();
    }

    private void setUpRRTButtonClick() {
        binding.btnImplantBatteryStatus.setOnClickListener(view -> {
            final BatteryReplaceRRTDialog dialog = new BatteryReplaceRRTDialog(requireContext());
            dialog.setConfirmButtonListener(view1 -> {
                dialog.dismiss();
                dialogs.clear();
            });
            dialog.show();
            dialogs.add(dialog);
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
        final LeadRClinicalDialog dialog = new LeadRClinicalDialog(requireContext());
        dialog.setLeadRValue(leadRValue);
        dialog.setLeadIValue(leadIValue);
        dialog.setConfirmButtonListener(view1 -> {
            dialog.dismiss();
            dialogs.clear();
        });
        dialog.show();
        dialogs.add(dialog);
    }

    void setPlusMinusButtonColors(AmplitudeDialog dialog, boolean isDefault) {
        if (mAmplitudePos == 42) {
            dialog.getPlusButtonRef().setBackgroundResource(R.drawable.button_circular_grey_three_hundred);
            dialog.getPlusButtonRef().setClickable(false);
        } else {
            dialog.getPlusButtonRef().setClickable(true);
            if (isDefault) {
                dialog.getPlusButtonRef().setBackgroundResource(R.drawable.button_circular_primary);
            } else {
                dialog.getPlusButtonRef().setBackgroundResource(R.drawable.button_circular_deep_blue);
            }
        }
        if (mAmplitudePos == 0) {
            dialog.getMinusButtonRef().setBackgroundResource(R.drawable.button_circular_grey_three_hundred);
            dialog.getMinusButtonRef().setClickable(false);
        } else {
            dialog.getMinusButtonRef().setClickable(true);
            if (isDefault) {
                dialog.getMinusButtonRef().setBackgroundResource(R.drawable.button_circular_primary);
            } else {
                dialog.getMinusButtonRef().setBackgroundResource(R.drawable.button_circular_deep_blue);
            }
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "DefaultLocale"})
    private void setUpAmplitudeButtonClick() {
        binding.btnAmplitudeVal.setOnClickListener(amplitudeButton -> {
            amplitudeButton.setClickable(false);
            mAmplitudePos = WandData.getAmplitudePos();
            float amplitudeVal = WandData.getAmpFromPos(mAmplitudePos);
            amplitudeDialog = new AmplitudeDialog(requireContext());
            amplitudeDialog.setAmplitude(amplitudeVal);
            amplitudeDialog.setItnsPlusListener(minusButton -> {
                if (mAmplitudePos < 42) {
                    mAmplitudePos += 1;
                }
                WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }

                TextView amp = amplitudeDialog.findViewById(R.id.tv_itns_amplitude);
                amp.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));

                setPlusMinusButtonColors(amplitudeDialog, false);
                amplitudeDialog.getConfirmButtonRef().setEnabled(false);
                amplitudeDialog.getCancelButtonRef().setEnabled(true);
            });
            amplitudeDialog.setItnsMinusListener(plusButton -> {
                if (mAmplitudePos > 0) {
                    mAmplitudePos -= 1;
                }
                WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                TextView amp = amplitudeDialog.findViewById(R.id.tv_itns_amplitude);
                amp.setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));

                if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                    mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                } else {
                    mMainActivity.wandComm.addProgramChanges(WandComm.changes.AMPLITUDE);
                }

                setPlusMinusButtonColors(amplitudeDialog, false);
                amplitudeDialog.getConfirmButtonRef().setEnabled(false);
                amplitudeDialog.getCancelButtonRef().setEnabled(true);
            });
            amplitudeDialog.setStimulationButtonListener((stimulationButton, motionEvent) -> {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mNow + 500 < System.currentTimeMillis()) {
                            stimulationButton.setPressed(true);
                            mMainActivity.wandComm.setStimulation(true);
                            ((Button) stimulationButton).setText(R.string.stimulation_active);
                            WandData.invalidateStimLeadI();
                            mNow = System.currentTimeMillis();
                            mStimEnabled = true;
                        }
                        amplitudeDialog.getPlusButtonRef().setClickable(false);
                        amplitudeDialog.getMinusButtonRef().setClickable(false);
                        amplitudeDialog.getCancelButtonRef().setEnabled(false);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_POINTER_UP:
                        if (mStimEnabled) {
                            stimulationButton.setPressed(false);
                            ((Button) stimulationButton).setText(R.string.hold_to_deliver_neurostimulation);
                            if (mNow + 1500 < System.currentTimeMillis()) {
                                mMainActivity.wandComm.setStimulation(false);
                                mStimEnabled = false;
                            } else {
                                mHandler.postDelayed(HoldStimulation, mNow + 1500 - System.currentTimeMillis());
                            }
                            setPlusMinusButtonColors(amplitudeDialog, true);
                        }
                        showNeurostimulationProgressDialog();
                        amplitudeDialog.getPlusButtonRef().setClickable(true);
                        amplitudeDialog.getMinusButtonRef().setClickable(true);
                        break;
                }
                return true;
            });
            amplitudeDialog.setCancelButtonListener(cancelView -> {
                amplitudeDialog.dismiss();
                amplitudeButton.setClickable(true);
                dialogs.clear();
            });
            amplitudeDialog.setConfirmButtonListener(confirmView -> {
                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.getAmpFromPos(mAmplitudePos)));
                amplitudeButton.setBackgroundResource(R.drawable.rounded_button_dark_always);
                valuesChanged[0] = true;
                amplitudeDialog.dismiss();
                amplitudeButton.setClickable(true);
                dialogs.clear();
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
            amplitudeDialog.show();
            dialogs.add(amplitudeDialog);
            setPlusMinusButtonColors(amplitudeDialog, true);
        });
    }

    private final Runnable HoldStimulation = () -> {
        mMainActivity.wandComm.setStimulation(false);
        mStimEnabled = false;
    };

    private void setUpFrequencyButtonClick() {
        binding.btnFrequencyVal.setOnClickListener(frequencyButton -> {
            frequencyButton.setClickable(false);
            freqDialog = new FrequencyDialog(requireContext());
            freqDialog.setCancelButtonListener(cancelView -> {
                freqDialog.dismiss();
                frequencyButton.setClickable(true);
                dialogs.clear();
            });
            freqDialog.setConfirmButtonListener(confirmView -> {
                checkedRadioButtonId = freqDialog.getCheckedButtonId();
                RadioButton checkedRadioButton = freqDialog.findViewById(checkedRadioButtonId);
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
//                    if (prevFreq == 0) {
//                        valuesChanged[0] = false;
//                        binding.btnAmplitudeVal.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
//                    }

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

                freqDialog.dismiss();
                frequencyButton.setClickable(true);
                dialogs.clear();
            });
            freqDialog.show();
            dialogs.add(freqDialog);

            RadioButton rb;
            if (checkedRadioButtonId != -1) {
                rb = freqDialog.findViewById(checkedRadioButtonId);
            } else {
                rb = freqDialog.findViewById(R.id.radio_off);
            }
            if (rb != null) {
                rb.setChecked(true);
            } else {
                rb = freqDialog.findViewById(R.id.radio_off);
                rb.setChecked(true);
            }
        });
    }

    private void setUpDateButtonClick() {
        binding.btnStartDay.setOnClickListener(dayDateButton -> {
            dayDateButton.setClickable(false);
            dayDateDialog = new ProgramTherapyDayDateDialog(
                    requireContext(), mMainActivity.getTimeDifferenceMillis(),
                    binding.btnStartDay.getText().toString(),
                    WandData.therapy[WandData.FUTURE] == 5);
            dayDateDialog.setCancelButtonListener(cancelView -> {
                dayDateDialog.dismiss();
                dayDateButton.setClickable(true);
            });
            dayDateDialog.setConfirmButtonListener(confirmView -> {
                DatePicker datePicker = dayDateDialog.findViewById(R.id.datePicker);
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                String formattedDate = Utility.getFormattedDateForProgramTherapy(calendar.getTime());

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

                dayDateDialog.dismiss();
                dayDateButton.setClickable(true);
                dialogs.clear();
            });
            dayDateDialog.show();
            dialogs.add(dayDateDialog);
        });
    }

    private void setUpTimeButtonClick() {
        binding.btnTimeOfDay.setOnClickListener(timeOfDayButton -> {
            timeOfDayButton.setClickable(false);
            timeOfDayDialog = new ProgramTherapyTimeOfDayDialog(
                    requireContext(), mMainActivity.getTimeDifferenceMillis(), lastSetHour, lastSetMinute);
            timeOfDayDialog.setCancelButtonListener(cancelView -> {
                timeOfDayDialog.dismiss();
                timeOfDayButton.setClickable(true);
            });
            timeOfDayDialog.setConfirmButtonListener(confirmView -> {
                TimePicker timePicker = timeOfDayDialog.findViewById(R.id.timePicker);

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

                binding.btnTimeOfDay.setText(Utility.getFormattedTime(futureTime.getTime()));
                binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_button_dark_always);
                valuesChanged[3] = true;

                enableDisableProgramButton(valuesChanged[0] && valuesChanged[1] && valuesChanged[2]);

                timeOfDayDialog.dismiss();
                timeOfDayButton.setClickable(true);
                dialogs.clear();
            });
            timeOfDayDialog.show();
            dialogs.add(timeOfDayDialog);
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
            interrogateButton.setClickable(false);
            mMainActivity.wandComm.interrogate(WandComm.frags.PROGRAM);
            binding.btnInterrogate.setClickable(false);
            binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_button_dark_always);

            resetAllButtonsWithDefaultBackground();
            disableAllTheButtons();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeProgramButton() {
        binding.btnProgram.setOnClickListener(programButton -> {
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_button_dark_always);
            Calendar c = Calendar.getInstance();
            long future = WandData.dateandtime[WandData.FUTURE];
            long now = c.getTimeInMillis() + mMainActivity.getTimeDifferenceMillis();
            if (!binding.btnFrequencyVal.getText().toString().equals("Off") && future < (now + 1000L * 3600L)) {
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
        ProgramConfirmationDialog dialog = new ProgramConfirmationDialog(requireContext());
        dialog.setAmpVal(binding.btnAmplitudeVal.getText().toString());
        dialog.setFreqVal(binding.btnFrequencyVal.getText().toString());
        dialog.setDayDateVal(binding.btnStartDay.getText().toString());
        dialog.setTimeOfDayVal(binding.btnTimeOfDay.getText().toString());

        dialog.setCancelButtonListener(v -> {
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            dialog.dismiss();
            dialogs.clear();
        });

        dialog.setConfirmButtonListener(v -> {
            if (mMainActivity.wandComm.anyAmplitudeChanges()) {
                WandData.invalidateStimLeadI();
            }
            mMainActivity.wandComm.program();
            dialog.dismiss();
            dialogs.clear();
            showProgramITNSInProgressDialog();
        });

        dialog.show();
        dialogs.add(dialog);
    }

    private void showIncorrectTimeDialog() {
        IncorrectTimeDialog dialog = new IncorrectTimeDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            binding.btnTimeOfDay.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            binding.btnTimeOfDay.setText(getString(R.string._3_dash));
            valuesChanged[3] = false;
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            enableDisableProgramButton(false);
            dialog.dismiss();
            dialogs.clear();
        });
        dialog.show();
        dialogs.add(dialog);
    }

    private void showProgramSuccessDialog() {
        ProgramItnsSuccessDialog dialog = new ProgramItnsSuccessDialog(requireContext());

        dialog.setConfirmButtonListener(v -> {
            resetAllButtonsWithDefaultBackground();
            enableDisableProgramButton(false);

            dialog.dismiss();
            dialogs.clear();
        });

        dialog.setAmpVal(WandData.getAmplitude());
        dialog.setFreqVal(WandData.getTherapy(requireContext()));
        dialog.setDayDateVal(binding.btnStartDay.getText().toString());
        dialog.setTimeOfDayVal(binding.btnTimeOfDay.getText().toString());
        dialog.show();
        dialogs.add(dialog);
    }

    private void showProgramITNSInProgressDialog() {
        dialogProgrammingInProgress = new ProgramITNSProgressDialog(requireContext());
        dialogProgrammingInProgress.show();
        dialogs.add(dialogProgrammingInProgress);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getFrag() == WandComm.frags.PROGRAM) {
            updateUI(event.isUiUpdateSuccess());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ItnsUpdateAmpEvent event) {
        updateAmplitude();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ProgramSuccessEvent event) {
        if (dialogProgrammingInProgress != null && dialogProgrammingInProgress.isShowing())
            dialogProgrammingInProgress.dismiss();
        showProgramSuccessDialog();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateCurrentTimeEvent event) {
        setDateTime();
        if (event.shouldResetTheDefaultTextsOnTherapy()) {
            resetAllTheTexts();
            disableAllTheButtons();
            resetAllButtonsWithDefaultBackground();
        }
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
        dismissAllDialogs();
    }

    private void dismissAllDialogs() {
        for (Dialog dialog : dialogs) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialogs.remove(dialog);
            }
        }
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

    private void resetAllTheTexts() {
        binding.tvItnsModelNumber.setText(getString(R.string._1_dash));
        binding.tvItnsSerialVal.setText(getString(R.string._1_dash));
        binding.tvImplantBatteryVal.setText(getString(R.string._1_dash));
        binding.tvLeadRVal.setText(getString(R.string._1_dash));
        binding.btnAmplitudeVal.setText(getString(R.string._3_dash));
        binding.btnFrequencyVal.setText(getString(R.string.off));
        binding.btnStartDay.setText(getString(R.string._3_dash));
        binding.btnTimeOfDay.setText(getString(R.string._3_dash));
        binding.btnLeadRWarn.setVisibility(View.INVISIBLE);
        binding.tvLeadRVal.setVisibility(View.VISIBLE);
        binding.btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
        binding.tvImplantBatteryVal.setVisibility(View.VISIBLE);
    }

    public void updateUI(boolean success) {
        if (stimulationProgressDialog != null && stimulationProgressDialog.isShowing()) {
            stimulationProgressDialog.dismiss();
        }
        binding.btnInterrogate.setClickable(true);
        if (success) {
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                // Re-enable changed parameters (and the test stim button) only when
                // UIUpdate is called - meaning that the state machine has finished its tasks
                mMainActivity.wandComm.removeProgramChanges(WandComm.changes.AMPLITUDE);
                showLeadRWarningIfFound();
                enableDisableFrequencyButton(true);
                if (amplitudeDialog != null && amplitudeDialog.isShowing()) {
                    amplitudeDialog.getConfirmButtonRef().setClickable(true);
                    amplitudeDialog.getConfirmButtonRef().setEnabled(true);
                    amplitudeDialog.getStimulationButtonRef().setPressed(false);
                    amplitudeDialog.getStimulationButtonRef().setText(R.string.hold_to_deliver_neurostimulation);
                }

            } else if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.PROGRAM) {
                resetChangedParameters();
                String implToolFrequency = WandData.getTherapy(requireContext());
                if (implToolFrequency != null && !implToolFrequency.isEmpty()) {
                    enableDisableFrequencyButton(true);
                    if (implToolFrequency.equals(getString(R.string.off))) {
                        binding.tvImplantBatteryVal.setText(getString(R.string._1_dash));
                    } else {
                        showBatteryWarningIfLow();
                    }
                }
            } else { /* This is interrogate callback */
                mMainActivity.isInterrogationDone = true;
                setupWandData(WandData.getModelNumber(getContext()));
                checkForReset();
                resetChangedParameters();
            }
        } else {  // Here's what happens on fail
            if (WandData.isITNSNew()) {
                if (dialogProgrammingInProgress != null && dialogProgrammingInProgress.isShowing())
                    dialogProgrammingInProgress.dismiss();
                try {
                    if (amplitudeDialog != null && !amplitudeDialog.isShowing()) {
                        amplitudeDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showSerialNumberMismatchWarnDialog();
                binding.btnInterrogate.setClickable(true);
                binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                disableAllTheButtons();
                resetAllButtonsWithDefaultBackground();
                resetAllTheTexts();
                return;
            }
            if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.SETSTIM) {
                try {
                    if (amplitudeDialog != null && !amplitudeDialog.isShowing()) {
                        amplitudeDialog.getCancelButtonRef().setEnabled(true);
                        amplitudeDialog.getConfirmButtonRef().setEnabled(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                showWandITNSCommunicationIssueDialog();
            } else if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.PROGRAM) {
                if (dialogProgrammingInProgress != null && dialogProgrammingInProgress.isShowing())
                    dialogProgrammingInProgress.dismiss();
                showWandITNSCommunicationIssueDialog();
                binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                enableDisableProgramButton(true);
            } else {
                showWandITNSCommunicationIssueDialog();
                if (mMainActivity.wandComm.getCurrentJob() == WandComm.jobs.INTERROGATE) {
                    binding.btnInterrogate.setClickable(true);
                    binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
                }
            }
        }
    }

    void setupWandData(String modelNumber) {
        binding.btnInterrogate.setClickable(true);
        binding.btnInterrogate.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);

        if (getString(R.string.all_model_number_two).equals(modelNumber) ||
                getString(R.string.all_model_number_three).equals(modelNumber) ||
                getString(R.string.all_model_number_four).equals(modelNumber)) {
            binding.tvItnsModelNumber.setText(modelNumber);
        } else {
            dismissAllDialogs();
            mMainActivity.showInvalidModelNumberDialog();
            disableAllTheButtons();
            resetAllButtonsWithDefaultBackground();
            resetAllTheTexts();
            return;
        }

        binding.tvItnsSerialVal.setText(WandData.getSerialNumber());

        enableDisableAmplitudeButton(true);

        setInitialAmplitudeAndEnableAmplitudeButton();

        String implToolFrequency = WandData.getTherapy(requireContext());
        binding.btnFrequencyVal.setText(implToolFrequency);

        if (implToolFrequency != null && !implToolFrequency.isEmpty()) {
            enableDisableFrequencyButton(true);
            if (implToolFrequency.equals(getString(R.string.off))) {
                enableDisableDayDateButton(false);
                enableDisableTimeOfDayButton(false);
                binding.tvImplantBatteryVal.setText(getString(R.string._1_dash));
            } else {
                showBatteryWarningIfLow();
                enableDisableDayDateButton(true);
                enableDisableTimeOfDayButton(true);
            }
        } else {
            enableDisableFrequencyButton(false);
        }
        showLeadRWarningIfFound();

        String date = WandData.getDate();
        String time = WandData.getTime();

        lastSetMinute = WandData.getProgramMinute();
        lastSetHour = WandData.getProgramHour();

        if (!date.isEmpty())
            binding.btnStartDay.setText(date);
        if (!time.isEmpty())
            binding.btnTimeOfDay.setText(time);
    }

    public void showSerialNumberMismatchWarnDialog() {
        SerialNumberMismatchDialog dialog = new SerialNumberMismatchDialog(requireContext());
        dialog.setConfirmButtonListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showNeurostimulationProgressDialog() {
        if (stimulationProgressDialog == null)
            stimulationProgressDialog = new StimulationProgressDialog(requireContext());
        if (!stimulationProgressDialog.isShowing())
            stimulationProgressDialog.show();
        dialogs.add(stimulationProgressDialog);
    }

    // Client Suggested to hide this & show another dialog
    private void showProgramUnsuccessfulWarnDialog() {
        ProgrammingUnsuccessfulDialog dialog = new ProgrammingUnsuccessfulDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            dialog.dismiss();
            dialogs.clear();
            binding.btnProgram.setBackgroundResource(R.drawable.rounded_corner_button_dynamic);
            enableDisableProgramButton(true);
        });
        dialog.show();
        dialogs.add(dialog);
    }

    public void showWandITNSCommunicationIssueDialog() {
        WandAndITNSCommIssueDialog dialog = new WandAndITNSCommIssueDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            dialog.dismiss();
            dialogs.clear();
        });
        dialog.show();
        dialogs.add(dialog);
    }

    private void showBatteryWarningIfLow() {
        String rrt_result = WandData.getRRT(getContext());

        if (rrt_result != null) {
            if (rrt_result.equals(getString(R.string.all_yes))) {
                binding.btnImplantBatteryStatus.setVisibility(View.VISIBLE);
                binding.tvImplantBatteryVal.setVisibility(View.INVISIBLE);
            } else {
                binding.btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
                binding.tvImplantBatteryVal.setVisibility(View.VISIBLE);
                binding.tvImplantBatteryVal.setText(R.string.ok);
            }
        } else {
            binding.tvImplantBatteryVal.setText(R.string.ok);
        }
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || leadRValue < 250;
        if (isWarningFound) {
            if (leadRValue == 0) {
                binding.tvLeadRVal.setVisibility(View.VISIBLE);
                binding.tvLeadRVal.setText(R.string._1_dash);
                binding.btnLeadRWarn.setVisibility(View.INVISIBLE);
            } else {
                binding.btnLeadRWarn.setVisibility(View.VISIBLE);
                binding.tvLeadRVal.setVisibility(View.INVISIBLE);
                displayLeadRDialogue();
            }
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
            showItnsResetDialog();
        }
    }

    public void showItnsResetDialog() {
        ItnsResetCVDialog dialog = new ItnsResetCVDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            dialog.dismiss();
            dialogs.clear();
            mMainActivity.wandComm.clearResetCounter();
            resetAllTheTexts();
        });
        dialog.show();
        dialogs.add(dialog);
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

    public void updateAmplitude() {
        binding.btnAmplitudeVal.setText(WandData.getAmplitude());
        mAmplitudePos = WandData.getAmplitudePos();
    }

    void setDateTime() {
        Pair<String, String> dateTimePair = Utility.getTimeAndDateForFirstTimeHam(
                mMainActivity.getTimeDifferenceMillis());
        // Format the time in "2:00 PM" format
        binding.tvTime.setText(dateTimePair.second);
        binding.tvTime.setOnClickListener(v -> showResetDateTimeConfirmationDialog());

        // Format the date in "01/10/2023" format
        binding.tvDate.setText(dateTimePair.first);
        binding.tvDate.setOnClickListener(v -> showResetDateTimeConfirmationDialog());
    }

    public void showResetDateTimeConfirmationDialog() {
        ResetDateTimeDialog dialog = new ResetDateTimeDialog(requireContext());
        dialog.setConfirmButtonListener(v -> {
            mMainActivity.showSetDateTimeDialog(true);
            dialog.dismiss();
        });
        dialog.setCancelButtonListener(v -> {
            dialog.dismiss();
        });
        dialog.show();
    }
}