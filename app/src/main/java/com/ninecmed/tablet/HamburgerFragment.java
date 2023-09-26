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
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.Objects;

public class HamburgerFragment extends Fragment {
    private static final String TAG = "ItnsFragment";
    private MainActivity mMainActivity = null;
    private AlertDialog mAlertDialog;
    private boolean bTouch = false;
    private TabLayout mTabLayout;
    private Button setLanguage;
    private Button resetDateTime;
    private TextView tvDateVal;
    private TextView tvTimeVal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.fragment_hamburger, container, false);

//        InitializeStimulationButton(view);
        InitializeInterrogateButton(view);
//        InitializeAmpControls(view);

        initializeCloseAppButton(view);

        tvDateVal = view.findViewById(R.id.tv_date_val);
        tvTimeVal = view.findViewById(R.id.tv_time_val);
        mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText("Intibia ITNS Information and Settings"));
        setLanguage = view.findViewById(R.id.btn_set_language);
        setLanguage.setOnClickListener(v -> {
            showChangeLanguageDialogue();
        });

        resetDateTime = view.findViewById(R.id.btn_set_date_time);
        resetDateTime.setOnClickListener(v -> {
           showResetDateTimeConfirmationDialog();
        });

        return view;
    }

    public void showResetDateTimeConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_reset_date_time);

        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm_to_reset);
        btnConfirm.setOnClickListener(v -> {
            mMainActivity.showSetDateTimeDialog(true);
            dialog.dismiss();
        });

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Pair<String, String> dateTimePair = Utility.getTimeAndDateForFirstTime(mMainActivity.getTimeDifferenceMillis());
        tvDateVal.setText(dateTimePair.first);
        tvTimeVal.setText(dateTimePair.second);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mMainActivity = (MainActivity) getActivity();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateCurrentTimeEvent event) {
        tvDateVal.setText(event.getDate());
        tvTimeVal.setText(event.getTime());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    private void initializeCloseAppButton(View view) {
        Button closeAppBtn = view.findViewById(R.id.bt_close_app);
        closeAppBtn.setOnClickListener(v -> showCloseAppDialog());
    }

    private void showCloseAppDialog() {
        mMainActivity.showCloseAppDialog();
    }

    private void showChangeLanguageDialogue() {
        final Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_change_language);

        final Spinner spinnerLanguages = dialog.findViewById(R.id.spinner_languages);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(),
                R.array.languages, R.layout.change_language_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguages.setAdapter(adapter);
        spinnerLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final Button buttonConfirm = dialog.findViewById(R.id.btn_confirm);
        final Button buttonCancel = dialog.findViewById(R.id.btn_cancel);
        buttonConfirm.setOnClickListener(view -> {
            dialog.dismiss();
        });
        buttonCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireActivity());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeInterrogateButton(View view) {
        final Button interrogate = view.findViewById(R.id.btn_interrogate);

        //TODO: remove comment after BT
        /*interrogate.setEnabled(false);
        interrogate.setAlpha(0.5f);*/
        interrogate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Even though the Interrogate button, and all other buttons are disabled
                // in the StartProgressBar method by setting
                // WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, several
                // ACTION_DOWN events could occur before the window is deactivated.
                // In order to prevent this, we'll add a flag, bTouch, that's set
                // on the first touch and only cleared in the EndProgressBar method.
                // The same steps are required for the Program button.
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN /*&& !bTouch*/) {
                    interrogate.setPressed(true);
                    //bTouch = true;

                    //TODO: remove comment after BT
                    //mMainActivity.wandComm.Interrogate();
                    //MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                    //StartProgressBar();
                } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    interrogate.setPressed(false);
                }
                return true;
            }
        });

    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeProgramButton(View view) {
        Button program = view.findViewById(R.id.btItnsProgram);
        program.setEnabled(false);
        program.setAlpha(0.5f);
        program.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
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
                    StartProgressBar();
                }
                return true;
            }
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

    @SuppressLint("ClickableViewAccessibility")
    /*private void InitializeStimulationButton(View view) {
        final Button stimulate = view.findViewById(R.id.btItnsStartStim);
        //TODO:IMP remove comment after BT
        //stimulate.setEnabled(false);
        //stimulate.setAlpha(0.5f);
        stimulate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        if(mNow + 500 < System.currentTimeMillis()) {
                            stimulate.setPressed(true);
                            //TODO: IMP remove comment after BT
                            //mMainActivity.wandComm.SetStimulation(true);
                            MakeTone(ToneGenerator.TONE_PROP_BEEP);
                            stimulate.setText("Stimulation Active");
                            WandData.InvalidateStimLeadI();

                            *//*TextView leadi = Objects.requireNonNull(getView()).findViewById(R.id.tvItnsLeadI);
                            leadi.setText(WandData.GetLeadI());

                            TextView leadr = getView().findViewById(R.id.tvItnsLeadR);
                            leadr.setText(WandData.GetLeadR());*//*
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
                            mMainActivity.EnableTabs(false);
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
                        if(mStimEnabled) {
                            stimulate.setPressed(false);
                            stimulate.setText("Hold to deliver neurostimulation");
                            //stimulate.setEnabled(false);
                            //stimulate.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                            // Set delay to 1500 to be the same delay as ExternalFragment
                            if (mNow + 1500 < System.currentTimeMillis()) {
                                //TODO:IMP remove comment after BT
                                //mMainActivity.wandComm.SetStimulation(false);

                                //StopStimProgressBar();
                                MakeTone(ToneGenerator.TONE_PROP_NACK);
                                mStimEnabled = false;
                            } else {
                                //TODO:IMP remove comment after BT
                                //mHandler.postDelayed(HoldStimulation, mNow + 1500 - System.currentTimeMillis());
                            }
                        }
                        break;
                }
                return true;
            }
        });
    }*/

    private final Runnable HoldStimulation = new Runnable() {
        @Override
        public void run() {
            mMainActivity.wandComm.SetStimulation(false);
            MakeTone(ToneGenerator.TONE_PROP_NACK);
        }
    };

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
        Objects.requireNonNull(getActivity()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    public void UIUpdate(boolean success) {
        View view = getView();

        if (success) {
            if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.SETSTIM) {
                // Re-enable changed parameters (and the test stim button) only when
                // UIUpdate is called - meaning that the state machine has finished its tasks
                SetChangedParametersEnable(true, true);
                mMainActivity.wandComm.RemoveProgramChanges(WandComm.changes.AMPLITUDE);
                EnableInterrogateButton(true, true);

                /*EnableProgramButton(true, true);
                EnableStimTestButton(true);*/

                /*TextView leadi = Objects.requireNonNull(view).findViewById(R.id.tvItnsLeadI);
                leadi.setText(WandData.GetLeadI());

                TextView leadr = view.findViewById(R.id.tvItnsLeadR);
                leadr.setText(WandData.GetLeadR());*/
            } else {
                MakeTone(ToneGenerator.TONE_CDMA_PIP);

                /*if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.INTERROGATE) {
                    Group gp = Objects.requireNonNull(view).findViewById(R.id.ghITNS);
                    gp.setVisibility(View.VISIBLE);
                }

                if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.PROGRAM) {
                    EnableProgramButton(false, true);
                }*/

                TextView mn = Objects.requireNonNull(view).findViewById(R.id.tvItnsModelNumber);
                mn.setText((WandData.GetModelNumber(view.getContext())));

                TextView sn = view.findViewById(R.id.tvItnsSN);
                sn.setText(WandData.GetSerialNumber());

                /*TextView cellv = view.findViewById(R.id.tvItnsCellV);
                cellv.setText(WandData.GetCellV());

                TextView rrt = view.findViewById(R.id.tvItnsRRT);
                String rrt_result = WandData.GetRRT(view.getContext());
                rrt.setText(rrt_result);
                if(rrt_result == getString(R.string.all_yes))
                    rrt.setTextColor(Color.RED);
                else
                    rrt.setTextColor(Color.BLACK);

                TextView leadi = view.findViewById(R.id.tvItnsLeadI);
                leadi.setText(WandData.GetLeadI());*/

                //TODO:IMP use below for Lead R warnings and OK
                //TextView leadr = view.findViewById(R.id.tvItnsLeadR);
                //leadr.setText(WandData.GetLeadR());


                //TODO IMP: Shift below logic to Program Therapy
                /*Spinner therapySpinner = view.findViewById(R.id.ddItnsTherapy);
                ArrayAdapter<CharSequence> adapter;
                if(WandData.GetModelNumber() == 2)
                     adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getActivity()).getBaseContext(),
                             R.array.itns_therapy_schedule_array_model_two, R.layout.custom_spinner);
                else
                    adapter = ArrayAdapter.createFromResource(Objects.requireNonNull(getActivity()).getBaseContext(),
                            R.array.itns_therapy_schedule_array_model_one, R.layout.custom_spinner);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                therapySpinner.setAdapter(adapter);

                TextView therapy = view.findViewById(R.id.tvItnsTherapy);
                therapy.setText(WandData.GetTherapy(view.getContext()));

                TextView dateRO = view.findViewById(R.id.tvItnsDateRO);
                dateRO.setText(WandData.GetDate());

                TextView timeRO = view.findViewById(R.id.tvItnsTimeRO);
                timeRO.setText(WandData.GetTime());

                TextView ampRO = view.findViewById(R.id.tvItnsAmplitudeRO);
                ampRO.setText(WandData.GetAmplitude());

                ResetChangedParameters();
                CheckForReset();*/
            }
        }
        // Here's what happens on fail
        else {
            if (WandData.IsITNSNew() && mMainActivity.wandComm.GetCurrentJob() != WandComm.jobs.INTERROGATE) {
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
                        EnableStimTestButton(true);
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
                        EnableStimTestButton(true);
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
                        StartProgressBar();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.all_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        SetChangedParametersEnable(true, true);
                        EnableInterrogateButton(true, true);
                        EnableProgramButton(true, true);
                        EnableStimTestButton(true);
                    }
                });
                mAlertDialog = alertDialog.create();
            }
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }
    }

    private void CheckForReset() {
        int resets = WandData.GetResets();
        if (resets > 0) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(getView()).getContext());

            alertDialog.setTitle(String.format(getString(R.string.itns_resets_detected_msg), resets));
            alertDialog.setMessage(getString(R.string.itns_resets_continue_msg));

            alertDialog.setPositiveButton(getString(R.string.all_clear), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    StartProgressBar();
                    mMainActivity.wandComm.ClearResetCounter();
                }
            });
            alertDialog.setNegativeButton(getString(R.string.all_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();
        }
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
        View view = getView();

        Spinner therapydd = Objects.requireNonNull(view).findViewById(R.id.ddItnsTherapy);
        therapydd.setSelection(WandData.GetTherapyPos());
        // ((TextView) therapydd.getChildAt(0)).setTextColor(Color.BLACK);
        // Normally, therapydd.getChildAt() would work fine. However, the spinner contents depend
        // on the model number, so the adapter is changed just before getChildAt is called.
        // This results in a null exception when calling getChildAt().
        // See, https://stackoverflow.com/questions/31206674/listview-getchildat-return-null
        // for the fix.
        ((TextView) therapydd.getAdapter().getView(0, null, therapydd)).setTextColor(Color.BLACK);
        therapydd.getAdapter().getView(0, null, therapydd).setBackgroundResource(R.color.colorControlNoChange);
        WandData.therapy[WandData.FUTURE] = WandData.therapy[WandData.CURRENT];

        TextView date = view.findViewById(R.id.tvItnsDate);
        date.setText(WandData.GetDate());
        date.setTextColor(Color.BLACK);
        date.setBackgroundResource(R.color.colorControlNoChange);

        TextView time = view.findViewById(R.id.tvItnsTime);
        time.setText(WandData.GetTime());
        time.setTextColor(Color.BLACK);
        time.setBackgroundResource(R.color.colorControlNoChange);
        WandData.dateandtime[WandData.FUTURE] = WandData.dateandtime[WandData.CURRENT];

        TextView amp = view.findViewById(R.id.tvItnsAmplitude);
        amp.setText(WandData.GetAmplitude());
        amp.setTextColor(Color.BLACK);
        WandData.amplitude[WandData.FUTURE] = WandData.amplitude[WandData.CURRENT];

        ImageButton plus = view.findViewById(R.id.ibItnsPlus);
        plus.setBackgroundResource(R.color.colorControlNoChange);

        ImageButton minus = view.findViewById(R.id.ibItnsMinus);
        minus.setBackgroundResource(R.color.colorControlNoChange);

        mMainActivity.wandComm.RemoveAllProgramChanges();
    }

    private void StartProgressBar() {
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbItns);
        progressBar.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getActivity()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void MakeTone(int sound) {
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(sound, 150);
        long now = System.currentTimeMillis();
        while ((System.currentTimeMillis() - now) < 150) ;
        tone.release();
    }

    private void EnableInterrogateButton(boolean enable, boolean change_alpha) {
        Button interrogate = Objects.requireNonNull(getView()).findViewById(R.id.btItnsInterrogate);
        interrogate.setEnabled(enable);
        if (change_alpha) interrogate.setAlpha(enable ? 1f : 0.5f);
    }

    private void EnableProgramButton(boolean enable, boolean change_alpha) {
        Button program = Objects.requireNonNull(getView()).findViewById(R.id.btItnsProgram);

        if (enable && mMainActivity.wandComm.AnyProgramChanges()) {
            program.setEnabled(true);
            if (change_alpha) program.setAlpha(1f);
        } else {
            program.setEnabled(false);
            if (change_alpha) program.setAlpha(0.5f);
        }
    }

    private void EnableStimTestButton(boolean enable) {
        Button test = Objects.requireNonNull(getView()).findViewById(R.id.btItnsStartStim);
        test.setEnabled(enable);
        test.setAlpha(enable ? 1f : 0.5f);
    }
}