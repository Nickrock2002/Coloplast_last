package com.ninecmed.tablet;

import static com.ninecmed.tablet.Utility.setTheSystemButtonsHidden;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.ninecmed.tablet.databinding.DialogChangeLanguageBinding;
import com.ninecmed.tablet.databinding.DialogResetDateTimeBinding;
import com.ninecmed.tablet.databinding.FragmentHamburgerBinding;
import com.ninecmed.tablet.dialogs.AboutDialog;
import com.ninecmed.tablet.dialogs.LeadRDialog;
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
    FragmentHamburgerBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        binding = FragmentHamburgerBinding.inflate(inflater, container, false);

        binding.btCloseApp.setOnClickListener(v -> showCloseAppDialog());
        binding.tvSoftwareVersionVal.setText(BuildConfig.VERSION_NAME);

        binding.tabs.addTab(binding.tabs.newTab().setText("Intibia ITNS Information and Settings"));
        binding.btnSetLanguage.setOnClickListener(v -> showChangeLanguageDialogue());

        binding.btnSetDateTime.setOnClickListener(v -> showResetDateTimeConfirmationDialog());
        binding.btnLeadRWarn.setOnClickListener(v -> displayLeadRDialogue());
        binding.btnAbout.setOnClickListener(v -> displayAboutDialogue());

        return binding.getRoot();
    }

    public void showResetDateTimeConfirmationDialog() {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        DialogResetDateTimeBinding resetDateTimeBinding = DialogResetDateTimeBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(resetDateTimeBinding.getRoot());

        resetDateTimeBinding.btnConfirmToReset.setOnClickListener(v -> {
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
        initializeInterrogateButton();
        setupInitialData();
    }

    private void showBatteryWarningIfLow() {
        String rrt_result = WandData.getRRT(getContext());
        if (rrt_result != null) {
            if (rrt_result.equals(getString(R.string.all_yes))) {
                binding.btnImplantBatteryStatus.setVisibility(View.VISIBLE);
                binding.tvBatteryReplaceVal.setVisibility(View.INVISIBLE);
            } else {
                binding.btnImplantBatteryStatus.setVisibility(View.INVISIBLE);
                binding.tvBatteryReplaceVal.setVisibility(View.VISIBLE);
                binding.tvBatteryReplaceVal.setText(R.string.ok);
            }
        } else {
            binding.tvBatteryReplaceVal.setText("_");
        }
    }

    private void showLeadRWarningIfFound() {
        float leadRValue = WandData.getLeadR();
        String formattedLeadR = String.format(Locale.ENGLISH, "%.0f ohms", WandData.getLeadR());
        boolean isWarningFound;
        isWarningFound = leadRValue > 2000 || (leadRValue < 250 && leadRValue > 0);
        if (isWarningFound) {
            binding.btnLeadRWarn.setText(formattedLeadR);
            binding.btnLeadRWarn.setVisibility(View.VISIBLE);
            binding.tvLeadRVal.setVisibility(View.INVISIBLE);
        } else {
            if (leadRValue == 0f) {
                binding.tvLeadRVal.setText("_");
            } else {
                binding.tvLeadRVal.setText(formattedLeadR);
            }
            binding.tvLeadRVal.setVisibility(View.VISIBLE);
            binding.btnLeadRWarn.setVisibility(View.INVISIBLE);
        }
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

    private void displayAboutDialogue() {
        final AboutDialog dialogue = new AboutDialog(getActivity());
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
        if (mMainActivity.isDeviceConnected()) {
            binding.tvDateVal.setText(event.getDate());
            binding.tvTimeVal.setText(event.getTime());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        EventBus.getDefault().unregister(this);
    }

    private void showCloseAppDialog() {
        mMainActivity.showCloseAppDialog();
    }

    private void showChangeLanguageDialogue() {
        final Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);

        DialogChangeLanguageBinding changeLanguageBinding = DialogChangeLanguageBinding.inflate(LayoutInflater.from(requireContext()));
        dialog.setContentView(changeLanguageBinding.getRoot());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireActivity(), R.array.languages, R.layout.change_language_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        changeLanguageBinding.spinnerLanguages.setAdapter(adapter);
        changeLanguageBinding.spinnerLanguages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    changeLanguageBinding.spinnerLanguages.setSelection(1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        changeLanguageBinding.btnConfirm.setOnClickListener(view -> {
            binding.tvLanguageVal.setText(getString(R.string.english));
            dialog.dismiss();
        });
        changeLanguageBinding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        setTheSystemButtonsHidden(dialog);

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(requireActivity());
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeInterrogateButton() {
        binding.btnInterrogate.setOnClickListener(view -> {
            binding.btnInterrogate.setPressed(true);
            mMainActivity.wandComm.interrogate(WandComm.frags.HAMBURGER);
            binding.btnInterrogate.setPressed(false);
        });

        if (mMainActivity.isDeviceConnected()) {
            binding.btnInterrogate.setEnabled(true);
            binding.btnInterrogate.setClickable(true);
        } else {
            binding.btnInterrogate.setEnabled(false);
            binding.btnInterrogate.setClickable(false);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UIUpdateEvent event) {
        if (event.getFrag() == WandComm.frags.HAMBURGER) {
            updateHamburgerUI(event.isUiUpdateSuccess());
        }
    }

    private void setupInitialData() {
        //Date - time
        if (mMainActivity.isDeviceConnected()) {
            Pair<String, String> dateTimePair = Utility.getTimeAndDateForFirstTimeHam(mMainActivity.getTimeDifferenceMillis());
            binding.tvDateVal.setText(dateTimePair.first);
            binding.tvTimeVal.setText(dateTimePair.second);
            binding.tvLanguageVal.setText(getString(R.string.english));
        }

        setupWandData();
    }

    private void setupWandData() {
        // Model Num
        String modelNum = WandData.getModelNumber(getContext());
        if (modelNum != null) {
            binding.tvItnsModelVal.setText(modelNum);
        } else {
            binding.tvItnsModelVal.setText("_");
        }

        // Serial Num
        String serialNum = WandData.getSerialNumber();
        if (serialNum != null) {
            binding.tvItnsSerialVal.setText(serialNum);
        } else {
            binding.tvItnsSerialVal.setText("_");
        }

        // Cell V
        String cellVoltage = WandData.getCellV();
        if (cellVoltage != null) {
            binding.tvImplantBatteryVal.setText(cellVoltage);
        } else {
            binding.tvImplantBatteryVal.setText("_");
        }

        //RRT
        showBatteryWarningIfLow();

        // LEAD I
        if (WandData.getLeadI() == 0.0f) {
            binding.tvLeadIVal.setText("_");
        } else {
            String formattedLeadI = String.format(Locale.ENGLISH, "%.1f mA", WandData.getLeadI());
            binding.tvLeadIVal.setText(formattedLeadI);
        }

        // LEAD R
        showLeadRWarningIfFound();
    }

    public void updateHamburgerUI(boolean success) {
        if (success) {
            setupWandData();
        } else {
            // Here's what happens on fail
            if (this.isResumed()) {
                if (WandData.isITNSNew() && mMainActivity.wandComm.getCurrentJob() != WandComm.jobs.INTERROGATE) {
                    mMainActivity.showSerialNumberMismatchWarnDialog();
                } else {
                    mMainActivity.showWandITNSCommunicationIssueDialog();
                }
            }
        }
    }
}