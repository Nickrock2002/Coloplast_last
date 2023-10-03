package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.dialogues.AmplitudeDialogue;
import com.ninecmed.tablet.dialogues.BatteryReplaceRRTDialogue;
import com.ninecmed.tablet.dialogues.FrequencyDialogue;
import com.ninecmed.tablet.dialogues.LeadRDialogue;
import com.ninecmed.tablet.dialogues.ProgramTherapyDayDateDialogue;
import com.ninecmed.tablet.dialogues.ProgramTherapyTimeOfDayDialogue;
import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
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
    private static final String TAG = "ItnsFragment";
    private MainActivity mMainActivity = null;
    private int mAmplitudePos = 0;
    private boolean bTouch = false;
    private int checkedRadioButtonId = -1;
    private String lastCheckedText = "";
    Button btnImplantBatteryStatus;
    Button btnLeadRWarn;
    TextView tvLeadRVal;
    Button btnAmplitudeVal;
    Button btnInterrogate;
    Button btnFrequencyVal;
    Button btnDayDateVal;
    Button btnTimeOfDayVal;
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.fragment_program_therapy, container, false);

        initializeInterrogateButton(view);
        InitializeProgramButton(view);

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
        float leadRValue = WandData.GetLeadR();
        float leadIValue = WandData.GetLeadI();
        final LeadRDialogue dialogue = new LeadRDialogue(getActivity());
        dialogue.setLeadRValue(leadRValue);
        dialogue.setLeadIValue(leadIValue);
        dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
        dialogue.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setUpAmplitudeButtonClick(View rootView) {
        btnAmplitudeVal = rootView.findViewById(R.id.btn_amplitude_val);
        btnAmplitudeVal.setOnClickListener(amplitudeButton -> {
            String amplitudeVal = String.valueOf(WandData.GetAmpFromPos(mAmplitudePos));
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
                    TextView amp = dialogue.findViewById(R.id.tv_itns_amplitude);
                    amp.setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));
                    if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                        mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.BLACK);
                    } else {
                        mMainActivity.wandComm.AddProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.RED);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    minusButton.setPressed(false);
                    Drawable drawable = (Drawable) minusButton.getBackground().mutate();
                    drawable.setTint(getResources().getColor(R.color.colorBaseDeepBlue));
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
                    TextView amp = dialogue.findViewById(R.id.tv_itns_amplitude);
                    WandData.amplitude[WandData.FUTURE] = (byte) mAmplitudePos;
                    amp.setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));
                    ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));

                    if (WandData.amplitude[WandData.CURRENT] == WandData.amplitude[WandData.FUTURE]) {
                        mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.BLACK);
                    } else {
                        mMainActivity.wandComm.AddProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.RED);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    plusButton.setPressed(false);
                    Drawable drawable = (Drawable) plusButton.getBackground().mutate();
                    drawable.setTint(getResources().getColor(R.color.colorBaseDeepBlue));
                }
                return true;
            });
            dialogue.setStimulationButtonListener((stimulationButton, motionEvent) -> {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mNow + 500 < System.currentTimeMillis()) {
                            stimulationButton.setPressed(true);
                            mMainActivity.wandComm.SetStimulation(true);
                            //MakeTone(ToneGenerator.TONE_PROP_BEEP);
                            ((Button) stimulationButton).setText(R.string.stimulation_active);
                            WandData.InvalidateStimLeadI();

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
                                mMainActivity.wandComm.SetStimulation(false);

//                                MakeTone(ToneGenerator.TONE_PROP_NACK);
                                mStimEnabled = false;
                            } else {
                                mHandler.postDelayed(HoldStimulation, mNow + 1500 - System.currentTimeMillis());
                            }
                        }
                        break;
                }
                return true;
            });
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
            dialogue.setConfirmButtonListener(confirmView -> {
                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));
                Drawable drawable = (Drawable) amplitudeButton.getBackground().mutate();
                drawable.setTint(getResources().getColor(R.color.colorBaseDeepBlue));
                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private final Runnable HoldStimulation = () -> {
        mMainActivity.wandComm.SetStimulation(false);
        //MakeTone(ToneGenerator.TONE_PROP_NACK);
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
                WandData.therapy[WandData.FUTURE] = checkedRadioButtonId;
                if (!lastCheckedText.isEmpty()) btnFrequencyVal.setText(lastCheckedText);

                if (WandData.therapy[WandData.CURRENT] == WandData.therapy[WandData.FUTURE]) {
                    mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.THERAPY);

                    // Clear date and time...
                    WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];
                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(WandData.dateandtime[WandData.CURRENT]);

                    Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                    date.setText(WandData.GetDate());
                    date.setEnabled(true);

                    // Enable control if therapy is weekly for model 1
                    if (WandData.GetModelNumber() == 1)
                        date.setClickable(WandData.therapy[WandData.CURRENT] == R.id.radio_weekly);
                        // else enable control if therapy daily, weekly, etc. for model 2
                    else
                        date.setClickable(WandData.therapy[WandData.FUTURE] == R.id.radio_daily || WandData.therapy[WandData.FUTURE] == R.id.radio_weekly || WandData.therapy[WandData.FUTURE] == R.id.radio_fort_nightly || WandData.therapy[WandData.FUTURE] == R.id.radio_monthly || WandData.therapy[WandData.FUTURE] == R.id.radio_auto);

                    Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                    time.setText(WandData.GetTime());
                    time.setEnabled(true);

                    // Enable control if therapy is enabled
                    time.setClickable(WandData.therapy[WandData.CURRENT] != R.id.radio_off);
                } else {
                    mMainActivity.wandComm.AddProgramChanges(WandComm.changes.THERAPY);

                    Calendar c = Calendar.getInstance();
                    long timeDifferenceMillis = mMainActivity.getTimeDifferenceMillis();
                    c.setTimeInMillis(c.getTimeInMillis() + timeDifferenceMillis);

                    int modelNumber = WandData.GetModelNumber();

                    // If therapy set to daily for the model 1...
                    if (WandData.therapy[WandData.FUTURE] == R.id.radio_daily && modelNumber == 1) {

                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        //date.setBackgroundResource(R.color.colorControlNoChange);
                        date.setText(R.string._3_dash);
                        date.setEnabled(true);
                        date.setClickable(false);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        time.setText(R.string._3_dash);
                        time.setEnabled(true);
                        time.setClickable(true);
                    }
                    // Else, therapy set to weekly for Model 1
                    else if (WandData.therapy[WandData.FUTURE] == R.id.radio_weekly && modelNumber == 1) {

                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        date.setText(R.string._3_dash);
                        date.setEnabled(true);
                        date.setClickable(true);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        time.setText(R.string._3_dash);
                        time.setEnabled(true);
                        time.setClickable(true);
                    }
                    // Else, if therapy is set for weekly, fortnightly or monthly for Model 2
                    else if ((WandData.therapy[WandData.FUTURE] == R.id.radio_weekly || WandData.therapy[WandData.FUTURE] == R.id.radio_fort_nightly || WandData.therapy[WandData.FUTURE] == R.id.radio_monthly || WandData.therapy[WandData.FUTURE] == R.id.radio_auto) && modelNumber == 2) {

                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        date.setText(R.string._3_dash);
                        date.setEnabled(true);
                        date.setClickable(true);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        time.setText(R.string._3_dash);
                        time.setEnabled(true);
                        time.setClickable(true);
                    }
                    // Else, therapy is off
                    else {
                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        if (WandData.therapy[WandData.CURRENT] != R.id.radio_daily) {
                            //date.setTextColor(Color.RED);
                            //date.setBackgroundResource(R.color.colorControlChange);
                        }
                        date.setText(R.string._3_dash);
                        date.setEnabled(true);
                        date.setClickable(false);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        //time.setTextColor(Color.RED);
                        //time.setBackgroundResource(R.color.colorControlChange);
                        time.setText(R.string._3_dash);
                        time.setEnabled(true);
                        time.setClickable(false);
                    }
                }
                dialogue.dismiss();
            });
            dialogue.setCheckedChangeListener((group, checkedId) -> {
                RadioButton checkedRadioButton = (RadioButton) dialogue.findViewById(checkedId);
                checkedRadioButtonId = checkedId;
                lastCheckedText = checkedRadioButton.getText().toString();
            });
            dialogue.show();
            RadioButton rb;
            if (checkedRadioButtonId != -1) {
                rb = (RadioButton) dialogue.findViewById(checkedRadioButtonId);
            } else {
                rb = (RadioButton) dialogue.findViewById(R.id.radio_off);
            }
            rb.setChecked(true);
        });
    }

    private void setUpDateButtonClick(View rootView) {
        btnDayDateVal = rootView.findViewById(R.id.btn_start_day);

        btnDayDateVal.setOnClickListener(dayDateButton -> {
            final ProgramTherapyDayDateDialogue dialogue = new ProgramTherapyDayDateDialogue(getActivity());
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
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
                    mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.DATE);
                } else {
                    mMainActivity.wandComm.AddProgramChanges(WandComm.changes.DATE);
                }
                btnDayDateVal.setText(formattedDate);
                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private void setUpTimeButtonClick(View rootView) {
        btnTimeOfDayVal = rootView.findViewById(R.id.btn_time_of_day);

        btnTimeOfDayVal.setOnClickListener(timeOfDayButton -> {
            final ProgramTherapyTimeOfDayDialogue dialogue = new ProgramTherapyTimeOfDayDialogue(getActivity());
            dialogue.setCancelButtonListener(cancelView -> dialogue.dismiss());
            dialogue.setConfirmButtonListener(confirmView -> {
                TimePicker timePicker = dialogue.findViewById(R.id.timePicker);

                // Get the selected hour and minute from the TimePicker
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();

                // Determine if it's AM or PM
                String amPm;
                if (hour < 12) {
                    amPm = "AM";
                } else {
                    amPm = "PM";
                    if (hour > 12) {
                        hour -= 12;
                    }
                }

                // Update the button text with the formatted time
                String formattedTime = String.format("%02d:%02d %s", hour, minute, amPm);
                btnTimeOfDayVal.setText(formattedTime);

                Calendar futuretime = Calendar.getInstance();
                futuretime.setTimeInMillis(WandData.dateandtime[WandData.FUTURE]);
                futuretime.set(Calendar.MINUTE, minute);
                futuretime.set(Calendar.HOUR_OF_DAY, hour);
                WandData.dateandtime[WandData.FUTURE] = futuretime.getTimeInMillis();

                if (WandData.dateandtime[WandData.CURRENT] == WandData.dateandtime[WandData.FUTURE]) {
                    mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.TIME);
                } else {
                    mMainActivity.wandComm.AddProgramChanges(WandComm.changes.TIME);
                }

                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton(View view) {
        btnInterrogate = view.findViewById(R.id.btn_interrogate);
        btnInterrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                btnInterrogate.setPressed(true);
                mMainActivity.wandComm.Interrogate();
                btnInterrogate.setClickable(false);
//                MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                btnInterrogate.setPressed(false);
            }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeProgramButton(View view) {
        Button program = view.findViewById(R.id.btn_program);
        program.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && !bTouch) {
                bTouch = true;
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

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            long future = WandData.dateandtime[WandData.FUTURE];
            long now = c.getTimeInMillis() + mMainActivity.getTimeDifferenceMillis();

            // Check date range for weekly, fortnightly and monthly therapy for Model 2
            if (WandData.therapy[WandData.FUTURE] >= 1 && WandData.GetModelNumber() == 2) {
                if (future < (now + 1000L * 3600L)) {
                    // Don't allow therapy to be set within 1 hour of now because only a
                    // magnet could stop therapy, telemetry can't interrupt therapy for
                    // the model 2.
                    ShowDateTimeMsgDialog(getString(R.string.itns_time_before_now_msg));
                    return;
                } else if (future > (now + 1000L * 3600L * 24L * 31L)) {
                    ShowDateTimeMsgDialog(getString(R.string.itns_time_after_31days_msg));
                    return;
                }
            }
            // Only check date range of one week for Model 1
            else if (WandData.therapy[WandData.FUTURE] == 2 && WandData.GetModelNumber() == 1) {
                if (future < now) {
                    ShowDateTimeMsgDialog(getString(R.string.itns_time_before_now_msg));
                    return;
                } else if (future > (now + 1000 * 3600 * 24 * 7)) {
                    ShowDateTimeMsgDialog(getString(R.string.itns_time_after_7days_msg));
                    return;
                }
            }

            if (mMainActivity.wandComm.AnyAmplitudeChanges()) {
                WandData.InvalidateStimLeadI();
            }
            mMainActivity.wandComm.Program();
        });

        TextView tvAmpVal = (TextView) dialog.findViewById(R.id.tv_amp_val);
        tvAmpVal.setText(WandData.GetAmplitude());

        TextView tvFreqVal = (TextView) dialog.findViewById(R.id.tv_freq_val);
        tvFreqVal.setText(WandData.GetTherapy(requireContext()));

        TextView tvDayVal = (TextView) dialog.findViewById(R.id.tv_start_day_date_val);
        if (rootView != null) {
            Button dateBtn = (Button) rootView.findViewById(R.id.btn_start_day);
            tvDayVal.setText(dateBtn.getText().toString());
        }

        TextView tvTimeVal = dialog.findViewById(R.id.tv_time_val);
        if (rootView != null) {
            Button timeBtn = (Button) rootView.findViewById(R.id.btn_time_of_day);
            tvTimeVal.setText(timeBtn.getText().toString());
        }

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void ShowDateTimeMsgDialog(String string) {
        View view = getView();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

        alertDialog.setTitle(string);
        alertDialog.setMessage(R.string.itns_time_correct_msg);

        alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                bTouch = false;
            }
        });
        AlertDialog dialog = alertDialog.create();
        dialog.setCancelable(false);
        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            UIUpdate(event.isUiUpdateSuccess());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ItnsUpdateAmpEvent event) {
        UpdateAmplitude();
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

    // This method is called when the fragment is hidden
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (!isVisibleToUser && getView() != null) {
            OnHidden();
        }
    }

    private void OnHidden() {
        // Always make sure buttons are enabled when leaving window
        requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void UIUpdate(boolean success) {
        View view = getView();
        if (success & view != null) {
            if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.SETSTIM) {
                // Re-enable changed parameters (and the test stim button) only when
                // UIUpdate is called - meaning that the state machine has finished its tasks
                mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.AMPLITUDE);
                showLeadRWarningIfFound();
                btnFrequencyVal.setEnabled(true);
                btnFrequencyVal.setClickable(true);
            } else if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.PROGRAM) {
                ResetChangedParameters();
                // TODO reset all the values here
            } else { /* This is interrogate callback */
//                MakeTone(ToneGenerator.TONE_CDMA_PIP);
                btnInterrogate.setClickable(true);
                TextView mn = Objects.requireNonNull(view).findViewById(R.id.tv_itns_model_number);
                mn.setText((WandData.GetModelNumber(view.getContext())));

                TextView sn = view.findViewById(R.id.tv_itns_serial_val);
                sn.setText(WandData.GetSerialNumber());

                showBatteryWarningIfLow(view);
                showLeadRWarningIfFound();

                btnAmplitudeVal.setEnabled(true);
                btnAmplitudeVal.setClickable(true);

                setInitialAmplitudeAndEnableAmplitudeButton();

                String toolFrequency = WandData.GetTherapy(requireContext());
                btnFrequencyVal.setText(toolFrequency);

                if(toolFrequency!= null && !toolFrequency.isEmpty()) {
                    btnFrequencyVal.setEnabled(true);
                    btnFrequencyVal.setClickable(true);
                }

                String date = WandData.GetDate();
                String time = WandData.GetTime();

                if (!date.isEmpty())
                    btnDayDateVal.setText(date);
                if (!time.isEmpty())
                    btnTimeOfDayVal.setText(time);

                ResetChangedParameters();
                checkForReset();
            }
        } else {
            // Here's what happens on fail
            AlertDialog mAlertDialog;
            if (mMainActivity.wandComm.GetCurrentJob() != WandComm.jobs.INTERROGATE) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_newitns_title_msg));
                alertDialog.setMessage(getString(R.string.itns_newitns_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                });
                mAlertDialog = alertDialog.create();
                mAlertDialog.setCancelable(false);
                mAlertDialog.show();
                return;
            }
            if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.SETSTIM) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    // TODO enable interrogate & program button
                });
                mAlertDialog = alertDialog.create();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_retry), (dialogInterface, i) -> {
                    if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.INTERROGATE)
                        mMainActivity.wandComm.Interrogate();
                    else if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.PROGRAM)
                        mMainActivity.wandComm.Program();
                });
                alertDialog.setNegativeButton(getString(R.string.all_cancel), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                    // TODO enable interrogate & program button
                });
                mAlertDialog = alertDialog.create();
            }
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }
    }

    private void showBatteryWarningIfLow(View view) {
        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
        String rrt_result = WandData.GetRRT(view.getContext());
        if (rrt_result != null && rrt_result.equals(getString(R.string.all_yes))) {
            btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
            cellv.setVisibility(View.VISIBLE);
            cellv.setText(R.string.ok);
        } else {
            btnImplantBatteryStatus.setVisibility(View.VISIBLE);
            cellv.setVisibility(View.INVISIBLE);
        }
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.GetLeadR();
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || leadRValue < 250;
        if (isWarningFound) {
            btnLeadRWarn.setVisibility(View.VISIBLE);
            tvLeadRVal.setVisibility(View.GONE);
            displayLeadRDialogue();
        } else {
            tvLeadRVal.setText(R.string.ok);
            btnLeadRWarn.setVisibility(View.GONE);
        }
    }

    private void setInitialAmplitudeAndEnableAmplitudeButton() {
        btnAmplitudeVal.setText(WandData.GetAmplitude());
        mAmplitudePos = WandData.GetAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];
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
            mMainActivity.wandComm.ClearResetCounter();
        });

        TextView tvCount = (TextView) dialog.findViewById(R.id.tv_reset_counter);
        tvCount.setText("Implant Reset Counter: " + count);

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    private void ResetChangedParameters() {

        WandData.therapy[WandData.FUTURE] = WandData.therapy[WandData.CURRENT];

        WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];

        mAmplitudePos = WandData.GetAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];

        mMainActivity.wandComm.RemoveAllProgramChanges();
    }

    private void MakeTone(int sound) {
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(sound, 150);
        long now = System.currentTimeMillis();
        while ((System.currentTimeMillis() - now) < 150) ;
        tone.release();
    }

    public void UpdateAmplitude() {
        btnAmplitudeVal.setText(WandData.GetAmplitude());
        mAmplitudePos = WandData.GetAmplitudePos();
    }
}