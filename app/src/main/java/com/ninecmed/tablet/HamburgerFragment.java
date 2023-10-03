package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
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
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

public class HamburgerFragment extends Fragment {
    private static final String TAG = "HamburgerFragment";
    private MainActivity mMainActivity = null;
    private TextView tvDateVal;
    private TextView tvTimeVal;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.fragment_hamburger, container, false);

        initializeInterrogateButton(view);
        initializeCloseAppButton(view);
        initializeAddress(view);

        TextView tvSoftwareVersion = view.findViewById(R.id.tv_software_version_val);
        tvSoftwareVersion.setText(BuildConfig.VERSION_NAME);

        tvDateVal = view.findViewById(R.id.tv_date_val);
        tvTimeVal = view.findViewById(R.id.tv_time_val);
        TabLayout mTabLayout = view.findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText("Intibia ITNS Information and Settings"));
        Button setLanguage = view.findViewById(R.id.btn_set_language);
        setLanguage.setOnClickListener(v -> {
            showChangeLanguageDialogue();
        });

        Button resetDateTime = view.findViewById(R.id.btn_set_date_time);
        resetDateTime.setOnClickListener(v -> {
           showResetDateTimeConfirmationDialog();
        });

        return view;
    }

    private void initializeAddress(View view) {
        TextView textView = view.findViewById(R.id.tv_company_title);

        // Define the text with the link
        String text = "Coloplast Corporation \n+1-800-258-3476 \nIFU link: intibia.com/IFU";

        // Create a SpannableString to apply styles to specific parts of the text
        SpannableString spannableString = new SpannableString(text);

        // Define the start and end indices of the link within the text
        int startIndex = text.indexOf("intibia.com/IFU");
        int endIndex = startIndex + "intibia.com/IFU".length();

        // Define the color you want for the link
        int linkColor = getResources().getColor(R.color.colorPrimary);

        // Apply the color to the link
        spannableString.setSpan(new ForegroundColorSpan(linkColor), startIndex, endIndex, 0);

        // Make the link clickable
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
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
        setupCurrentData(view);
    }

    private void setupCurrentData(View view) {
        //Date - time
        Pair<String, String> dateTimePair = Utility.getTimeAndDateForFirstTime(mMainActivity.getTimeDifferenceMillis());
        tvDateVal.setText(dateTimePair.first);
        tvTimeVal.setText(dateTimePair.second);

        // Model Num
        TextView mn = view.findViewById(R.id.tv_itns_model_val);
        String modelNum = WandData.GetModelNumber(view.getContext());
        if (modelNum != null) {
            mn.setText(modelNum);
        } else {
            mn.setText("_");
        }

        // Serial Num
        TextView sn = view.findViewById(R.id.tv_itns_serial_val);
        String serialNum = WandData.GetSerialNumber();
        if (serialNum != null) {
            sn.setText(serialNum);
        } else {
            sn.setText("_");
        }

        // Cell V
        TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
        String cellVoltage = WandData.GetCellV();
        if (cellVoltage != null) {
            cellv.setText(cellVoltage);
        } else {
            cellv.setText("_");
        }

        //RRT
        TextView rrt = view.findViewById(R.id.tv_battery_replace_val);
        String rrt_result = WandData.GetRRT(view.getContext());

        if (rrt_result != null) {
            if (rrt_result.equals(getString(R.string.all_yes)))
                rrt.setText("NOT OK");
            else
                rrt.setText("OK");
        } else {
            rrt.setText("_");
        }

        // LEAD I
        TextView leadi = view.findViewById(R.id.tv_lead_i_val);
        if (WandData.GetLeadI() == 0.0f) {
            leadi.setText("_");
        } else {
            leadi.setText("" + WandData.GetLeadI() + "mA");
        }

        // LEAD R
        TextView leadr = view.findViewById(R.id.tv_lead_r_val);
        if (WandData.GetLeadR() == 0f) {
            leadr.setText("_");
        } else {
            leadr.setText("" + WandData.GetLeadR() + "Ohms");
        }
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
        spinnerLanguages.setSelection(1);
        spinnerLanguages.setEnabled(false);
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
            dialog.dismiss();
        });
        buttonCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });
        setTheSystemButtonsHidden(dialog);
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireActivity());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton(View view) {
        final Button interrogate = view.findViewById(R.id.btn_interrogate);
        interrogate.setOnTouchListener((view1, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                interrogate.setPressed(true);
                mMainActivity.wandComm.Interrogate();
            } else if (motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL || motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                interrogate.setPressed(false);
            }
            return true;
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getTabEnum() == TabEnum.ITNS) {
            updateUI(event.isUiUpdateSuccess());
        }
    }

    public void updateUI(boolean success) {
        View view = getView();

        if (success) {
            TextView mn = Objects.requireNonNull(view).findViewById(R.id.tv_itns_model_val);
            mn.setText((WandData.GetModelNumber(view.getContext())));

            TextView sn = view.findViewById(R.id.tv_itns_serial_val);
            sn.setText(WandData.GetSerialNumber());

            TextView cellv = view.findViewById(R.id.tv_implant_battery_val);
            cellv.setText(WandData.GetCellV());

            TextView rrt = view.findViewById(R.id.tv_battery_replace_val);
            String rrt_result = WandData.GetRRT(view.getContext());

            if (rrt_result != null) {
                if (rrt_result.equals(getString(R.string.all_yes)))
                    rrt.setText("NOT OK");
                else
                    rrt.setText("OK");
            } else {
                rrt.setText("_");
            }

            TextView leadi = view.findViewById(R.id.tv_lead_i_val);
            leadi.setText("" + WandData.GetLeadI() + "mA");

            TextView leadr = view.findViewById(R.id.tv_lead_r_val);
            leadr.setText("" + WandData.GetLeadR() + "Ohms");
        }
        // Here's what happens on fail
        else {
            AlertDialog mAlertDialog;
            if (WandData.IsITNSNew() && mMainActivity.wandComm.GetCurrentJob() != WandComm.jobs.INTERROGATE) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_newitns_title_msg));
                alertDialog.setMessage(getString(R.string.itns_newitns_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mAlertDialog = alertDialog.create();
                mAlertDialog.show();
                return;
            }
            if (mMainActivity.wandComm.GetCurrentJob() == WandComm.jobs.SETSTIM) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

                alertDialog.setTitle(getString(R.string.itns_telem_fail_msg));
                alertDialog.setMessage(getString(R.string.itns_telem_checkwand_msg));

                alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                mAlertDialog = alertDialog.create();
                mAlertDialog.show();
            } else {
                mMainActivity.showWandTabCommunicationIssueDialog();
            }
        }
    }
}