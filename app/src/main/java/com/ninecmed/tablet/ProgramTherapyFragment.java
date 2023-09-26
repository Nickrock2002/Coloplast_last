package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.fragment_program_therapy, container, false);

        initializeInterrogateButton(view);

        InitializeProgramButton(view);

//        InitializeTherapySpinner(view);
//        InitializeDate(view);
//        InitializeTime(view);

        setUpRRTButtonClick(view);
        setUpLeadRButtonClick(view);
        setUpAmplitudeButtonClick(view);
        setUpFrequencyButtonClick(view);
        setUpDateButtonClick(view);
        setUpTimeButtonClick(view);
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        mMainActivity = (MainActivity) getActivity();
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
            String amplitudeVal = WandData.GetAmplitude();
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
                        //TODO enable all these 4 lines while testing
//                        mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.BLACK);
                    } else {
//                        mMainActivity.wandComm.AddProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.RED);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    minusButton.setPressed(false);
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
//                        mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.BLACK);
                    } else {
//                        mMainActivity.wandComm.AddProgramChanges(WandComm.changes.AMPLITUDE);
                        amp.setTextColor(Color.RED);
                    }
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    plusButton.setPressed(false);
                }
                return true;
            });
            dialogue.setStimulationButtonListener((stimulationButton, motionEvent) -> {

                return false;
            });
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
            dialogue.setConfirmButtonListener(confirmView -> {
                ((Button) amplitudeButton).setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));
                dialogue.dismiss();
            });
            dialogue.show();
        });
    }

    private void setUpFrequencyButtonClick(View rootView) {
        Button btnFrequencyVal = rootView.findViewById(R.id.btn_frequency_val);

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
                    modelNumber = 1;

                    // If therapy set to daily for the model 1...
                    if (WandData.therapy[WandData.FUTURE] == R.id.radio_daily && modelNumber == 1) {

                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        //date.setBackgroundResource(R.color.colorControlNoChange);
                        date.setText("----");
                        date.setEnabled(true);
                        date.setClickable(false);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        time.setText("----");
                        time.setEnabled(true);
                        time.setClickable(true);
                    }
                    // Else, therapy set to weekly for Model 1
                    else if (WandData.therapy[WandData.FUTURE] == R.id.radio_weekly && modelNumber == 1) {

                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        date.setText("----");
                        date.setEnabled(true);
                        date.setClickable(true);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        time.setText("----");
                        time.setEnabled(true);
                        time.setClickable(true);
                    }
                    // Else, if therapy is set for weekly, fortnightly or monthly for Model 2
                    else if ((WandData.therapy[WandData.FUTURE] == R.id.radio_weekly || WandData.therapy[WandData.FUTURE] == R.id.radio_fort_nightly || WandData.therapy[WandData.FUTURE] == R.id.radio_monthly || WandData.therapy[WandData.FUTURE] == R.id.radio_auto) && modelNumber == 2) {
                        /*c.set(Calendar.HOUR_OF_DAY, 8);
                        c.set(Calendar.MINUTE, 0);
                        c.set(Calendar.SECOND, 0);
                        if (WandData.therapy[WandData.FUTURE] == R.id.radio_auto)
                            c.add(Calendar.HOUR, 24 * 7 * 3);                               // If Auto mode, set default time to 3 weeks from now
                        else
                            c.add(Calendar.HOUR, 24 * 7);                                   // Else set one week ahead

                        WandData.dateandtime[WandData.FUTURE] = c.getTimeInMillis();*/

                        Button date = (Button) rootView.findViewById(R.id.btn_start_day);
                        date.setText("----");
                        date.setEnabled(true);
                        date.setClickable(true);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        time.setText("----");
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
                        date.setText("----");
                        date.setEnabled(true);
                        date.setClickable(false);

                        Button time = (Button) rootView.findViewById(R.id.btn_time_of_day);
                        //time.setTextColor(Color.RED);
                        //time.setBackgroundResource(R.color.colorControlChange);
                        time.setText("----");
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
        Button btnDayDateVal = rootView.findViewById(R.id.btn_start_day);

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
        Button btnTimeOfDayVal = rootView.findViewById(R.id.btn_time_of_day);

        btnTimeOfDayVal.setOnClickListener(timeOfDayButton -> {
            final ProgramTherapyTimeOfDayDialogue dialogue = new ProgramTherapyTimeOfDayDialogue(getActivity());
            dialogue.setCancelButtonListener(cancelView -> {
                dialogue.dismiss();
            });
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
        final Button interrogate = view.findViewById(R.id.btn_interrogate);
        interrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                interrogate.setPressed(true);
                mMainActivity.wandComm.Interrogate();
                MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                interrogate.setPressed(false);
            }
            return true;
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeProgramButton(View view) {
        Button program = view.findViewById(R.id.btn_program);
        program.setEnabled(false);
        program.setAlpha(0.5f);
        program.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN && !bTouch) {
                bTouch = true;
                Calendar c = Calendar.getInstance();
                long future = WandData.dateandtime[WandData.FUTURE];
                long now = c.getTimeInMillis();

                // Check date range for weekly, fortnightly and monthly therapy for Model 2
                if (WandData.therapy[WandData.FUTURE] >= 1 && WandData.GetModelNumber() == 2) {
                    if (future < (now + 1000L * 3600L)) {
                        // Don't allow therapy to be set within 1 hour of now because only a
                        // magnet could stop therapy, telemetry can't interrupt therapy for
                        // the model 2.
                        ShowDateTimeMsgDialog(getString(R.string.itns_time_before_now_msg));
                        return true;
                    } else if (future > (now + 1000L * 3600L * 24L * 31L)) {
                        ShowDateTimeMsgDialog(getString(R.string.itns_time_after_31days_msg));
                        return true;
                    }
                }
                // Only check date range of one week for Model 1
                else if (WandData.therapy[WandData.FUTURE] == 2 && WandData.GetModelNumber() == 1) {
                    if (future < now) {
                        ShowDateTimeMsgDialog(getString(R.string.itns_time_before_now_msg));
                        return true;
                    } else if (future > (now + 1000 * 3600 * 24 * 7)) {
                        ShowDateTimeMsgDialog(getString(R.string.itns_time_after_7days_msg));
                        return true;
                    }
                }

                if (mMainActivity.wandComm.AnyAmplitudeChanges()) {
                    WandData.InvalidateStimLeadI();
                }

                mMainActivity.wandComm.Program();
                MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
//                StartProgressBar();
            }
            return true;
        });
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

    private void InitializeDate(View view) {
        final TextView date = view.findViewById(R.id.tvItnsDate);
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar c = Calendar.getInstance();
                final DatePickerDialog dp = new DatePickerDialog(requireContext(), (view1, year, month, dayOfMonth) -> {
                    date.setText(String.format("%02d/%02d/%4d", month + 1, dayOfMonth, year));

                    // ITNS Model 1 can only be programmed one week ahead.  Model 2 can be programmed 31 days ahead
                    Calendar selected_date = Calendar.getInstance();
                    selected_date.setTimeInMillis(WandData.dateandtime[WandData.FUTURE]);       // Set Calendar object to future time
                    selected_date.set(Calendar.YEAR, year);
                    selected_date.set(Calendar.MONTH, month);
                    selected_date.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    WandData.dateandtime[WandData.FUTURE] = selected_date.getTimeInMillis();

                    if (WandData.dateandtime[WandData.CURRENT] == WandData.dateandtime[WandData.FUTURE]) {
                        mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.DATE);
                        date.setTextColor(Color.BLACK);
                        date.setBackgroundResource(R.color.colorControlNoChange);
                    } else {
                        mMainActivity.wandComm.AddProgramChanges(WandComm.changes.DATE);
                        date.setTextColor(Color.RED);
                        date.setBackgroundResource(R.color.colorControlChange);
                    }

                    EnableProgramButton(true, true);
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                // Model 1 and 2 should start from today, except if Model 1 and the schedule = auto,
                // we should start 15 days ahead.
                if ((WandData.GetModelNumber() == 2) && (WandData.therapy[WandData.FUTURE] == 5))
                    dp.getDatePicker().setMinDate(c.getTimeInMillis() + 1000L * 3600L * 24L * 15L);
                else dp.getDatePicker().setMinDate(c.getTimeInMillis());

                // If Model 1 do this...
                if (WandData.GetModelNumber() == 1)
                    dp.getDatePicker().setMaxDate(c.getTimeInMillis() + 1000L * 3600L * 24L * 7L);      // Set max date 7 days ahead.
                    // If Model 2 do this...
                else
                    dp.getDatePicker().setMaxDate(c.getTimeInMillis() + 1000L * 3600L * 24L * 31L);     // Set max date 31 days ahead.
                dp.show();
            }
        });
    }

    private void InitializeTime(View view) {
        final TextView time = view.findViewById(R.id.tvItnsTime);
        time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Calendar c = Calendar.getInstance();
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int min = c.get(Calendar.MINUTE);

                TimePickerDialog tp = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @SuppressLint("DefaultLocale")
                    @Override
                    public void onTimeSet(TimePicker timePicker, int h, int m) {
                        time.setText(String.format("%02d:%02d", h, m));

                        Calendar futuretime = Calendar.getInstance();
                        futuretime.setTimeInMillis(WandData.dateandtime[WandData.FUTURE]);
                        futuretime.set(Calendar.MINUTE, m);
                        futuretime.set(Calendar.HOUR_OF_DAY, h);
                        WandData.dateandtime[WandData.FUTURE] = futuretime.getTimeInMillis();

                        if (WandData.dateandtime[WandData.CURRENT] == WandData.dateandtime[WandData.FUTURE]) {
                            mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.TIME);
                            time.setTextColor(Color.BLACK);
                            time.setBackgroundResource(R.color.colorControlNoChange);
                        } else {
                            mMainActivity.wandComm.AddProgramChanges(WandComm.changes.TIME);
                            time.setTextColor(Color.RED);
                            time.setBackgroundResource(R.color.colorControlChange);
                        }

                        EnableProgramButton(true, true);
                    }
                }, hour, min, false);
                tp.show();
            }
        });
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

    public void OnConnected() {
        View view = getView();

        TextView tv = Objects.requireNonNull(view).findViewById(R.id.tvItnsBtStatus);
        tv.setText(getString(R.string.link_msg));

        ImageView iv = view.findViewById(R.id.ivItnsLink);
        iv.setImageResource(R.drawable.ic_link);

        EnableInterrogateButton(true, true);
        EnableProgramButton(false, true);

        SetChangedParametersEnable(true, true);
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

                tvLeadRVal.setText(String.valueOf(WandData.GetLeadR()));
                showLeadRWarningIfFound();

            } else {
//            MakeTone(ToneGenerator.TONE_CDMA_PIP);

                TextView mn = Objects.requireNonNull(view).findViewById(R.id.tv_itns_model_number);
                mn.setText((WandData.GetModelNumber(view.getContext())));

                TextView sn = view.findViewById(R.id.tv_itns_serial_val);
                sn.setText(WandData.GetSerialNumber());

                TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
                cellv.setText(WandData.GetCellV());

                String rrt_result = WandData.GetRRT(view.getContext());
                if (rrt_result.equals(getString(R.string.all_yes))) {
                    btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
                    cellv.setVisibility(View.VISIBLE);
                } else {
                    btnImplantBatteryStatus.setVisibility(View.VISIBLE);
                    cellv.setVisibility(View.INVISIBLE);
                }

                showLeadRWarningIfFound();

                tvLeadRVal.setText(String.valueOf(WandData.GetLeadR()));
                ((Button) view.findViewById(R.id.btn_start_day)).setText(WandData.GetDate());
                ((Button) view.findViewById(R.id.btn_time_of_day)).setText(WandData.GetTime());

                ResetChangedParameters();
                checkForReset();
                setInitialAmplitude();
            }
        } else {// Here's what happens on fail
            AlertDialog mAlertDialog;
            if (mMainActivity.wandComm.GetCurrentJob() != WandComm.jobs.INTERROGATE) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_newitns_title_msg));
                alertDialog.setMessage(getString(R.string.itns_newitns_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        SetChangedParametersEnable(true, true);
                        EnableInterrogateButton(true, true);
                        EnableProgramButton(false, true);
                        /*Group gp = Objects.requireNonNull(getView()).findViewById(R.id.ghITNS);
                        gp.setVisibility(View.GONE);*/
                    }
                });
                mAlertDialog = alertDialog.create();
                mAlertDialog.setCancelable(false);
                mAlertDialog.show();
                return;
            }
            if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.SETSTIM) {
                //StopStimProgressBar();
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        SetChangedParametersEnable(true, true);
                        EnableInterrogateButton(true, true);
                        EnableProgramButton(true, true);
                    }
                });
                mAlertDialog = alertDialog.create();
            } else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.INTERROGATE)
                            mMainActivity.wandComm.Interrogate();
                        else if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.PROGRAM)
                            mMainActivity.wandComm.Program();
//                        StartProgressBar();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.all_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        SetChangedParametersEnable(true, true);
                        EnableInterrogateButton(true, true);
                        EnableProgramButton(true, true);
                    }
                });
                mAlertDialog = alertDialog.create();
            }
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
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

    private void setInitialAmplitude() {
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

    private void SetChangedParametersEnable(boolean enable, boolean change_alpha) {
        View view = getView();

        Spinner therapydd = Objects.requireNonNull(view).findViewById(R.id.ddItnsTherapy);
        therapydd.setEnabled(enable);
        if (change_alpha) therapydd.setAlpha(enable ? 1f : 0.5f);

        TextView date = view.findViewById(R.id.tvItnsDate);
        date.setEnabled(enable);
        if (change_alpha) date.setAlpha(enable ? 1f : 0.5f);

        TextView time = view.findViewById(R.id.tvItnsTime);
        time.setEnabled(enable);
        if (change_alpha) time.setAlpha(enable ? 1f : 0.5f);

        ImageButton plus = view.findViewById(R.id.ibItnsPlus);
        plus.setEnabled(enable);
        if (change_alpha) plus.setAlpha(enable ? 1f : 0.5f);

        ImageButton minus = view.findViewById(R.id.ibItnsMinus);
        minus.setEnabled(enable);
        if (change_alpha) minus.setAlpha(enable ? 1f : 0.5f);

        TextView amp = view.findViewById(R.id.tvItnsAmplitude);
        if (change_alpha) amp.setAlpha(enable ? 1f : 0.5f);
    }

    private void ResetChangedParameters() {

        WandData.therapy[WandData.FUTURE] = WandData.therapy[WandData.CURRENT];

        WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];

        mAmplitudePos = WandData.GetAmplitudePos();
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];

        mMainActivity.wandComm.RemoveAllProgramChanges();
    }

   /* private void StartProgressBar() {
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbItns);
        progressBar.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }*/

    /*private void StopProgressBar() {
        bTouch = false;
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbItns);
        progressBar.setVisibility(View.INVISIBLE);
        Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }*/

    private void StartStimProgressBar() {
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbItnsStim);
        progressBar.setVisibility(View.VISIBLE);

        TextView tv = getView().findViewById(R.id.tvItnsStimProgress);
        tv.setVisibility((View.VISIBLE));
    }

    private void StopStimProgressBar() {
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbItnsStim);
        progressBar.setVisibility(View.INVISIBLE);

        TextView tv = getView().findViewById(R.id.tvItnsStimProgress);
        tv.setVisibility((View.INVISIBLE));
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

    private void EnableInterrogateButton(boolean enable, boolean change_alpha) {
        Button interrogate = requireView().findViewById(R.id.btItnsInterrogate);
        interrogate.setEnabled(enable);
        if (change_alpha) interrogate.setAlpha(enable ? 1f : 0.5f);
    }

    private void EnableProgramButton(boolean enable, boolean change_alpha) {
        Button program = requireView().findViewById(R.id.btItnsProgram);

        if (enable && mMainActivity.wandComm.AnyProgramChanges()) {
            program.setEnabled(true);
            if (change_alpha) program.setAlpha(1f);
        } else {
            program.setEnabled(false);
            if (change_alpha) program.setAlpha(0.5f);
        }
    }
}