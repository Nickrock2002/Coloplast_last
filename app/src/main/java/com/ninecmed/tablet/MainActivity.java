package com.ninecmed.tablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ninecmed.tablet.databinding.ActivityMainBinding;
import com.ninecmed.tablet.dialogs.BackToStartDialog;
import com.ninecmed.tablet.dialogs.BatteryWarnDialog;
import com.ninecmed.tablet.dialogs.ClinicVisitDatePickerDialog;
import com.ninecmed.tablet.dialogs.ClinicVisitSetDateTimeDialog;
import com.ninecmed.tablet.dialogs.ClinicVisitTimePickerDialog;
import com.ninecmed.tablet.dialogs.CloseAppDialog;
import com.ninecmed.tablet.dialogs.SerialNumberMismatchDialog;
import com.ninecmed.tablet.dialogs.WandAndITNSCommIssueDialog;
import com.ninecmed.tablet.dialogs.WandAndTabletCommIssueDialog;
import com.ninecmed.tablet.events.InsideOutsideEntryEvent;
import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.UIUpdateEvent;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.DeviceCallback;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private boolean isBluetoothPermissionGranted = false;
    protected BluetoothDevice mBTDevice = null;
    private boolean isDeviceConnected;
    protected Bluetooth mBluetooth = null;
    protected final Handler mHandler = new Handler();
    protected boolean mRunBT = false;
    public WandComm wandComm = null;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;
    private String formattedTime = "";
    private String formattedDate = "";
    private Dialog wandConnDialog;
    private long timeDifferenceMillis = 0;
    private int selectedHour = 0;
    private int selectedMinutes = 0;
    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedDay = 0;
    private ActivityMainBinding binding;
    ArrayList<Dialog> dialogs;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialogs = new ArrayList<>();
        // This hides the status bar at the top
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // And this hides the navigation bar at the bottom - along with the code in onResume()
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        });

        launchFeatureSelectionFragment(false);

        mHandler.postDelayed(MinuteTimer, 60000);

        requestBluetoothPermission();
        setUpToolbarClickEvents();
        timeDifferenceMillis = Utility.getTimeDifferenceInSharedPref(MainActivity.this);
        manageFragmentToolbar();
    }

    private void manageFragmentToolbar() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                FragmentManager.BackStackEntry topEntry = getSupportFragmentManager().getBackStackEntryAt(getSupportFragmentManager().getBackStackEntryCount() - 1);
                String fragmentName = topEntry.getName();
                boolean isInside = fragmentName.equals("inside");
                updateToolbar(isInside);
                InsideOutsideEntryEvent insideOutsideEntryEvent = new InsideOutsideEntryEvent();
                insideOutsideEntryEvent.setInside(isInside);
                EventBus.getDefault().post(insideOutsideEntryEvent);
            }
        });
    }

    public boolean isDeviceConnected() {
        return isDeviceConnected;
    }

    private void requestBluetoothPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                            != PackageManager.PERMISSION_GRANTED) {
                // Request both permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSION);
            } else {
                isBluetoothPermissionGranted = true;
            }
        } else {
            isBluetoothPermissionGranted = true;
        }
    }

    private void setUpToolbarClickEvents() {
        binding.ivHamburger.setOnClickListener(view -> {
            launchHamburgerFragment();
            binding.ivHamburger.setVisibility(View.INVISIBLE);
            binding.ivBack.setVisibility(View.VISIBLE);
        });

        binding.ivBack.setOnClickListener(view -> showBackToStartDialog());
    }

    public void updateToolbar(boolean isInside) {
        if (isInside) {
            binding.llToolbar.setBackgroundColor(ActivityCompat.getColor(this,
                    R.color.colorGreyThreeHundred));
            binding.ivIntbiaLogo.setVisibility(View.VISIBLE);
            binding.ivBack.setVisibility(View.VISIBLE);
        } else {
            binding.llToolbar.setBackgroundColor(ActivityCompat.getColor(this,
                    R.color.colorBaseGrayFifty));
            binding.ivIntbiaLogo.setVisibility(View.INVISIBLE);
            binding.ivBack.setVisibility(View.INVISIBLE);
        }
    }

    protected void showWandConnectionDialogue(final boolean isClinicVisit) {
        new Handler(Looper.getMainLooper()).post(() -> {
            wandConnDialog = new Dialog(MainActivity.this);
            wandConnDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            wandConnDialog.setContentView(R.layout.dialogue_wand_comm);

            AppCompatButton btConfirm = wandConnDialog.findViewById(R.id.bt_confirm);
            btConfirm.setOnClickListener(view -> {
                if (isClinicVisit) {
                    showSetDateTimeDialog(false);
                } else {
                    launchBaseTabFragment(false);
                }
                wandConnDialog.dismiss();
            });
            btConfirm.setClickable(false);
            wandConnDialog.findViewById(R.id.bt_cancel).setOnClickListener(view -> wandConnDialog.dismiss());

            setTheSystemButtonsHidden(wandConnDialog);
            Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(MainActivity.this);
            wandConnDialog.getWindow().setLayout(dimensions.first, dimensions.second);
            wandConnDialog.setCancelable(false);
            wandConnDialog.show();
            if (isBluetoothPermissionGranted) {
                if (!isDeviceConnected) {
                    initBluetooth();
                } else {
                    showWandConnectionInActiveMode();
                }
            } else {
                requestBluetoothPermission();
            }
        });
    }

    protected void launchFeatureSelectionFragment(boolean clearHistory) {
        MainActivity.this.runOnUiThread(() -> binding.ivHamburger.setVisibility(View.VISIBLE));

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        FeatureSelectionFragment featureSelectionFragment = new FeatureSelectionFragment();
        if (clearHistory)
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fragmentTransaction.replace(R.id.fl_fragment, featureSelectionFragment);
        fragmentTransaction.addToBackStack("outside");

        fragmentTransaction.commit();
    }

    private void launchBaseTabFragment(boolean isClinicVisit) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        BaseTabFragment baseTabFragment = new BaseTabFragment();
        baseTabFragment.setClinicVisit(isClinicVisit);
        fragmentTransaction.add(R.id.fl_fragment, baseTabFragment);
        fragmentTransaction.addToBackStack("inside");

        fragmentTransaction.commit();
    }

    private void launchHamburgerFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        HamburgerFragment hamburgerFragment = new HamburgerFragment();
        fragmentTransaction.add(R.id.fl_fragment, hamburgerFragment);
        fragmentTransaction.addToBackStack("inside");
        fragmentTransaction.commit();
    }

    /**
     * This function is written for showing updates in wand connection dialogue when wand is connected
     */
    void showWandConnectionInActiveMode() {
        if (wandConnDialog != null) {
            wandConnDialog.findViewById(R.id.ll_header).setVisibility(View.INVISIBLE);
            wandConnDialog.findViewById(R.id.ll_header_active).setVisibility(View.VISIBLE);
            ((TextView) wandConnDialog.findViewById(R.id.tv_connection_status)).setText(R.string.wand_is_comm);
            AppCompatButton btConfirm = wandConnDialog.findViewById(R.id.bt_confirm);
            btConfirm.setClickable(true);
            btConfirm.setBackgroundResource(R.drawable.bt_dialogue_wand_comm_active);
            btConfirm.setTextColor(ActivityCompat.getColor(this, R.color.txt_dialogue_wand_comm_active));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            // Check if all permissions are granted
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            isBluetoothPermissionGranted = allPermissionsGranted;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
        updateBatteryStatus();
    }

    private void initBluetooth() {
        // Permission has been granted, you can proceed with your Bluetooth functionality
        mBluetooth = new Bluetooth(this);
        mBluetooth.setReader(ByteReader.class);
        mBluetooth.setDeviceCallback(deviceCallback);
        wandComm = new WandComm(mBluetooth, this);

        mBluetooth.onStart();

        if (mBluetooth.isEnabled()) {
            Log.d(TAG, "BT was enabled");
        } else {
            mBluetooth.enable();
        }

        List<BluetoothDevice> btDevices = mBluetooth.getPairedDevices();
        for (BluetoothDevice bt : btDevices) {
            mBTDevice = bt;
        }

        if (mBTDevice != null) mBluetooth.connectToDevice(mBTDevice);

        mRunBT = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBluetooth.isConnected()) {
            mHandler.removeCallbacks(MinuteTimer);
            mHandler.removeCallbacks(Reconnect);
            mBluetooth.disconnect();
        }
        mBluetooth.onStop();
        mRunBT = false;
    }

    protected final Runnable Reconnect = () -> mBluetooth.connectToDevice(mBTDevice);

    private final Runnable MinuteTimer = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            updateBatteryStatus();
            updateAppTime();
            mHandler.postDelayed(MinuteTimer, 60000);
        }
    };

    private void updateAppTime() {
        // Get the current date and time from the device
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeMillis = currentCalendar.getTimeInMillis() + timeDifferenceMillis;
        currentCalendar.setTimeInMillis(currentTimeMillis);

        // Format the time in "2:00 PM" format
        String timeToShow = Utility.getFormattedTime(currentCalendar.getTime());

        // Format the date in "01/10/2023" format
        String dateToShow = Utility.getFormattedDateForHeader(currentCalendar.getTime());

        UpdateCurrentTimeEvent updateCurrentTimeEvent = new UpdateCurrentTimeEvent();
        updateCurrentTimeEvent.setTime(timeToShow);
        updateCurrentTimeEvent.setDate(dateToShow);

        EventBus.getDefault().post(updateCurrentTimeEvent);
    }

    void updateBatteryStatus() {
        BatteryManager bm = (BatteryManager) getApplicationContext().getSystemService(BATTERY_SERVICE);
        boolean batteryCharging = bm.isCharging();
        int batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        if (batteryPct <= 15 && !batteryCharging) {
            showBatteryWarnDialog();
        }

        binding.tvBatteryPer.setText(String.valueOf(batteryPct).concat("%"));

        if (batteryPct > 70)
            binding.ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_full);
        else if (batteryPct > 40)
            binding.ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_level_1);
        else if (batteryPct > 10)
            binding.ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_level_2);
        else
            binding.ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_empty);
    }

    public void updateUIFragments(final int frag, final boolean success) {
        MainActivity.this.runOnUiThread(() -> {
            UIUpdateEvent uiUpdateEvent = new UIUpdateEvent();
            uiUpdateEvent.setFrag(frag);
            uiUpdateEvent.setUiUpdateSuccess(success);
            EventBus.getDefault().post(uiUpdateEvent);
        });
    }

    public void updateItnsAmplitude() {
        ItnsUpdateAmpEvent itnsUpdateAmpEvent = new ItnsUpdateAmpEvent();
        EventBus.getDefault().post(itnsUpdateAmpEvent);
    }

    private final DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            runOnUiThread(() -> {
                isDeviceConnected = true;
                new Handler().postDelayed(() -> {
                    wandComm.initWand();
                    showWandConnectionInActiveMode();
                }, 3000);
            });
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            isDeviceConnected = false;
            wandComm.resetWandComm();
            launchFeatureSelectionFragment(true);
            dismissAllDialogs();
            MainActivity.this.runOnUiThread(() -> showWandTabCommunicationIssueDialog());
        }

        @Override
        public void onMessage(byte[] message) {
            wandComm.returnMessage(message);
        }

        @Override
        public void onError(int errorCode) {
        }

        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            if (mRunBT)
                mHandler.postDelayed(Reconnect, 1000); // And if fail, try every second
        }
    };

    private void dismissAllDialogs() {
        if (wandConnDialog != null && wandConnDialog.isShowing()) {
            wandConnDialog.dismiss();
        }
        for (Dialog dialog : dialogs) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                dialogs.remove(dialog);
            }
        }
    }

    public void showSetDateTimeDialog(boolean isFromHamburger) {
        ClinicVisitSetDateTimeDialog dialog = new ClinicVisitSetDateTimeDialog(
                this, formattedDate, formattedTime);

        dialog.setDateButtonListener(v -> {
            showDatePickerDialog(isFromHamburger);
            dialog.dismiss();
        });

        dialog.setTimeButtonListener(v -> {
            showTimePickerDialog(isFromHamburger);
            dialog.dismiss();
        });

        dialog.setCancelButtonListener(v -> {
            formattedTime = "";
            formattedDate = "";
            dialog.dismiss();
        });

        dialog.setConfirmButtonListener(v -> {
            formattedTime = "";
            formattedDate = "";
            calculateTimeDifference(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinutes);
            dialog.dismiss();

            if (!isFromHamburger) {
                launchBaseTabFragment(true);
            } else {
                //go back to home screen
                launchFeatureSelectionFragment(true);
            }
        });

        dialog.show();
        dialogs.add(dialog);
    }

    public void showDatePickerDialog(boolean isFromHamburger) {
        if (formattedDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + timeDifferenceMillis);
            formattedDate = Utility.getFormattedDate(calendar.getTime());

            selectedYear = calendar.get(Calendar.YEAR);
            selectedMonth = calendar.get(Calendar.MONTH);
            selectedDay = calendar.get(Calendar.DAY_OF_MONTH);
        }

        ClinicVisitDatePickerDialog dialog = new ClinicVisitDatePickerDialog(this,
                formattedDate);

        dialog.setConfirmButtonListener(v -> {
            showSetDateTimeDialog(isFromHamburger);
            dialog.dismiss();
        });
        dialog.setDateChangeListener(
                (view, year, month, day) -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(year, month, day);

                    formattedDate = Utility.getFormattedDate(calendar.getTime());

                    selectedYear = year;
                    selectedMonth = month;
                    selectedDay = day;
                }
        );

        dialog.show();
        dialogs.add(dialog);
    }

    public void showTimePickerDialog(boolean isFromHamburger) {
        if (formattedTime.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(calendar.getTimeInMillis() + timeDifferenceMillis);
            formattedTime = Utility.getFormattedTime(calendar.getTime());

            selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
            selectedMinutes = calendar.get(Calendar.MINUTE);

        }

        ClinicVisitTimePickerDialog dialog = new ClinicVisitTimePickerDialog(this, formattedTime);
        dialog.setTimeChangeListener((view, hourOfDay, minute12) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute12);

            formattedTime = Utility.getFormattedTime(calendar.getTime());

            selectedHour = hourOfDay;
            selectedMinutes = minute12;
        });

        dialog.setConfirmButtonListener(v -> {
            showSetDateTimeDialog(isFromHamburger);
            dialog.dismiss();
        });

        dialog.show();
        dialogs.add(dialog);
    }

    public void showBackToStartDialog() {
        BackToStartDialog dialog = new BackToStartDialog(this);
        dialog.setCancelButtonListener(v -> dialog.dismiss());
        dialog.setConfirmButtonListener(v -> {
            dialog.dismiss();
            getSupportFragmentManager().popBackStack();
            binding.ivHamburger.setVisibility(View.VISIBLE);
            binding.ivBack.setVisibility(View.GONE);
        });
        dialog.show();
    }

    public void showCloseAppDialog() {
        CloseAppDialog dialog = new CloseAppDialog(this);
        dialog.setCancelButtonListener(v -> dialog.dismiss());
        dialog.setConfirmButtonListener(v -> finish());
        dialog.show();
    }

    public void showBatteryWarnDialog() {
        BatteryWarnDialog dialog = new BatteryWarnDialog(this);
        dialog.setConfirmButtonListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showSerialNumberMismatchWarnDialog() {
        SerialNumberMismatchDialog dialog = new SerialNumberMismatchDialog(this);
        dialog.setConfirmButtonListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showWandTabCommunicationIssueDialog() {
        WandAndTabletCommIssueDialog dialog = new WandAndTabletCommIssueDialog(this);
        dialog.setConfirmButtonListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void showWandITNSCommunicationIssueDialog() {
        WandAndITNSCommIssueDialog dialog = new WandAndITNSCommIssueDialog(this);
        dialog.setConfirmButtonListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void calculateTimeDifference(int year, int month, int day, int hour, int minute) {

        // Create a Calendar object for the user-selected date and time
        Calendar userSelectedCalendar = Calendar.getInstance();
        userSelectedCalendar.set(year, month, day, hour, minute);

        // Get the current date and time from the device
        Calendar currentCalendar = Calendar.getInstance();

        // Calculate the time difference in milliseconds
        timeDifferenceMillis = userSelectedCalendar.getTimeInMillis() - currentCalendar.getTimeInMillis();

        Utility.setTimeDifferenceInSharedPref(MainActivity.this, timeDifferenceMillis);
    }

    private void setTheSystemButtonsHidden(Dialog dialog) {
        // Hide the system navigation bar
        View decorView = dialog.getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    public long getTimeDifferenceMillis() {
        return timeDifferenceMillis;
    }
}