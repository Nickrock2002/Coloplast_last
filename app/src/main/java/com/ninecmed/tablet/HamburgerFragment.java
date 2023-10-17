package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.ninecmed.tablet.dialogues.AboutDialogue;
import com.ninecmed.tablet.dialogues.LeadRDialogue;
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Objects;

public class HamburgerFragment extends Fragment {
    private static final String TAG = "HamburgerFragment";
    private MainActivity mMainActivity = null;
    private TextView tvDateVal;
    private TextView tvTimeVal;
    private TextView tvLanguage;
    private TextView tvLeadRVal;
    private Button btnLeadRWarn;
    private Button btnAbout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.fragment_hamburger, container, false);

        initializeCloseAppButton(view);

        TextView tvSoftwareVersion = view.findViewById(R.id.tv_software_version_val);
        tvSoftwareVersion.setText(BuildConfig.VERSION_NAME);

        tvDateVal = view.findViewById(R.id.tv_date_val);
        tvTimeVal = view.findViewById(R.id.tv_time_val);
        tvLanguage = view.findViewById(R.id.tv_language_val);
        tvLeadRVal = view.findViewById(R.id.tv_lead_r_val);
        btnLeadRWarn = view.findViewById(R.id.btn_lead_r_warn);
        btnAbout = view.findViewById(R.id.btn_about);
        TabLayout mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText("Intibia ITNS Information and Settings"));
        Button setLanguage = view.findViewById(R.id.btn_set_language);
        setLanguage.setOnClickListener(v -> showChangeLanguageDialogue());

        Button resetDateTime = view.findViewById(R.id.btn_set_date_time);
        resetDateTime.setOnClickListener(v -> showResetDateTimeConfirmationDialog());
        btnLeadRWarn.setOnClickListener(v -> displayLeadRDialogue());
//        btnAbout.setOnClickListener(v -> displayAboutDialogue());

        return view;
    }

    public void showResetDateTimeConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_reset_date_time);

        Button btnConfirm = dialog.findViewById(R.id.btn_confirm_to_reset);
        btnConfirm.setOnClickListener(v -> {
            mMainActivity.showSetDateTimeDialog(true);
            dialog.dismiss();
        });

        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireContext());
        Objects.requireNonNull(dialog.getWindow()).setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeInterrogateButton(view);
        setupCurrentData(view);
    }

    private void setupCurrentData(View view) {
        //Date - time
        if (mMainActivity.isDeviceConnected()) {
            Pair<String, String> dateTimePair = Utility.getTimeAndDateForFirstTimeHam(mMainActivity.getTimeDifferenceMillis());
            tvDateVal.setText(dateTimePair.first);
            tvTimeVal.setText(dateTimePair.second);
            tvLanguage.setText(getString(R.string.english));
        }

        // Model Num
        TextView mn = view.findViewById(R.id.tv_itns_model_val);
        String modelNum = WandData.getModelNumber(view.getContext());
        if (modelNum != null) {
            mn.setText(modelNum);
        } else {
            mn.setText("_");
        }

        // Serial Num
        TextView sn = view.findViewById(R.id.tv_itns_serial_val);
        String serialNum = WandData.getSerialNumber();
        if (serialNum != null) {
            sn.setText(serialNum);
        } else {
            sn.setText("_");
        }

        // Cell V
        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
        String cellVoltage = WandData.getCellV();
        if (cellVoltage != null) {
            cellv.setText(cellVoltage);
        } else {
            cellv.setText("_");
        }

        //RRT
        showBatteryWarningIfLow(view);

        // LEAD I
        TextView leadi = view.findViewById(R.id.tv_lead_i_val);
        if (WandData.getLeadI() == 0.0f) {
            leadi.setText("_");
        } else {
            String formattedLeadI = String.format(Locale.ENGLISH, "%.1f mA", WandData.getLeadI());
            leadi.setText(formattedLeadI);
        }

        // LEAD R
        showLeadRWarningIfFound();
    }

    private void showBatteryWarningIfLow(View view) {
        TextView cellv = view.findViewById(R.id.tv_battery_replace_val);
        Button btnImplantBatteryStatus = view.findViewById(R.id.btn_implant_battery_status);
        String rrt_result = WandData.getRRT(view.getContext());
        if (rrt_result != null) {
            if (rrt_result.equals(getString(R.string.all_yes))) {
                btnImplantBatteryStatus.setVisibility(View.VISIBLE);
                cellv.setVisibility(View.INVISIBLE);
            } else {
                btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
                cellv.setVisibility(View.VISIBLE);
                cellv.setText(R.string.ok);
            }
        } else {
            cellv.setText("_");
        }
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        String formattedLeadR = String.format(Locale.ENGLISH, "%.0f ohms", WandData.getLeadR());
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || (leadRValue < 250 && leadRValue > 0);
        if (isWarningFound) {
            btnLeadRWarn.setText(formattedLeadR);
            btnLeadRWarn.setVisibility(View.VISIBLE);
            tvLeadRVal.setVisibility(View.INVISIBLE);
        } else {
            if (leadRValue == 0f) {
                tvLeadRVal.setText("_");
            } else {
                tvLeadRVal.setText(formattedLeadR);
            }
            tvLeadRVal.setVisibility(View.VISIBLE);
            btnLeadRWarn.setVisibility(View.INVISIBLE);
        }
    }

    private void displayLeadRDialogue() {
        float leadRValue = WandData.getLeadR();
        float leadIValue = WandData.getLeadI();
        final LeadRDialogue dialogue = new LeadRDialogue(getActivity());
        dialogue.setLeadRValue(leadRValue);
        dialogue.setLeadIValue(leadIValue);
        dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
        dialogue.show();
    }

    private void displayAboutDialogue() {
        final AboutDialogue dialogue = new AboutDialogue(getActivity());
        dialogue.setConfirmButtonListener(view1 -> dialogue.dismiss());
        dialogue.show();
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
                if (i == 0) {
                    spinnerLanguages.setSelection(1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final Button buttonConfirm = dialog.findViewById(R.id.btn_confirm);
        final Button buttonCancel = dialog.findViewById(R.id.btn_cancel);
        buttonConfirm.setOnClickListener(view -> {
            tvLanguage.setText(getString(R.string.english));
            dialog.dismiss();
        });
        buttonCancel.setOnClickListener(view -> dialog.dismiss());
        setTheSystemButtonsHidden(dialog);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireActivity());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton(View view) {
        Button btnInterrogate = view.findViewById(R.id.btn_interrogate);
        btnInterrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                btnInterrogate.setPressed(true);
                mMainActivity.wandComm.interrogate();
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                btnInterrogate.setPressed(false);
            }
            return true;
        });

        if (mMainActivity.isDeviceConnected()) {
            btnInterrogate.setEnabled(true);
            btnInterrogate.setClickable(true);
        } else {
            btnInterrogate.setEnabled(false);
            btnInterrogate.setClickable(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            updateHamburgerUI(event.isUiUpdateSuccess());
        }
    }

    public void updateHamburgerUI(boolean success) {
        View view = getView();

        if (success) {
            TextView mn = Objects.requireNonNull(view).findViewById(R.id.tv_itns_model_val);
            mn.setText((WandData.getModelNumber(view.getContext())));

            TextView sn = view.findViewById(R.id.tv_itns_serial_val);
            sn.setText(WandData.getSerialNumber());

            showBatteryWarningIfLow(view);

            // LEAD I
            TextView leadi = view.findViewById(R.id.tv_lead_i_val);
            if (WandData.getLeadI() == 0.0f) {
                leadi.setText("_");
            } else {
                String formattedLeadI = String.format(Locale.ENGLISH, "%.1f mA", WandData.getLeadI());
                leadi.setText(formattedLeadI);
            }

            // LEAD R
            showLeadRWarningIfFound();
        }
        // Here's what happens on fail
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
            } else {
                mMainActivity.showWandTabCommunicationIssueDialog();
            }
        }
    }
}