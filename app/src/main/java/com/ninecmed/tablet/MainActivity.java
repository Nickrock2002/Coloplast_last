package com.ninecmed.tablet;

import static com.ninecmed.tablet.R.string.all_ok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.ninecmed.tablet.events.ItnsUpdateAmpEvent;
import com.ninecmed.tablet.events.OnConnectedUIEvent;
import com.ninecmed.tablet.events.OnDisconnectedUIEvent;
import com.ninecmed.tablet.events.TabEnum;
import com.ninecmed.tablet.events.UIUpdateEvent;
import com.ninecmed.tablet.events.UpdateCurrentTimeEvent;

import org.greenrobot.eventbus.EventBus;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.DeviceCallback;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AlertDialog mLowBatDialog = null;
    protected BluetoothDevice mBTDevice = null;
    protected Bluetooth mBluetooth = null;
    protected final Handler mHandler = new Handler();
    protected boolean mRunBT = false;
    public WandComm wandComm = null;
    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    private String formattedTime = "";

    private String formattedDate = "";

    Dialog wandConnDialog;

    private long timeDifferenceMillis = 0;

    private int selectedHour = 0;
    private int selectedMinutes = 0;
    private int selectedYear = 0;
    private int selectedMonth = 0;
    private int selectedDay = 0;

    private ImageView ivHamburger;
    private ImageView ivBack;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivHamburger = findViewById(R.id.iv_hamburger);
        ivBack = findViewById(R.id.iv_back);

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

        launchFeatureSelectionFragment();

        mHandler.postDelayed(MinuteTimer, 60000);
        // Check for both BLUETOOTH_CONNECT and BLUETOOTH_SCAN permissions
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
                // Both permissions are granted, proceed with Bluetooth functionality
                initBluetooth();
            }
        } else {
            // For devices below Android 12, the permission is granted at install time
            // You can proceed with your Bluetooth functionality
            initBluetooth();
        }

        setUpToolbarClickEvents();
    }

    private void setUpToolbarClickEvents() {
        ivHamburger.setOnClickListener(view -> {
            launchHamburgerFragment();
            updateToolbarColor(true);
            ivHamburger.setVisibility(View.INVISIBLE);
            ivBack.setVisibility(View.VISIBLE);
        });

        ivBack.setOnClickListener(view -> {
            launchFeatureSelectionFragment();
            updateToolbarColor(false);
            ivHamburger.setVisibility(View.VISIBLE);
            ivBack.setVisibility(View.INVISIBLE);
        });
    }

    public void updateToolbarColor(boolean isInside) {
        ConstraintLayout toolbar = findViewById(R.id.ll_toolbar);
        ImageView intibiaIv = findViewById(R.id.intibia_logo);
        if (isInside) {
            toolbar.setBackgroundColor(ActivityCompat.getColor(this,
                    R.color.colorGreyThreeHundred));
            intibiaIv.setVisibility(View.VISIBLE);
        } else {
            toolbar.setBackgroundColor(ActivityCompat.getColor(this,
                    R.color.colorBaseGrayFifty));
            intibiaIv.setVisibility(View.INVISIBLE);
        }
    }

    protected void showWandConnectionDialogue(final boolean isClinicVisit) {
        wandConnDialog = new Dialog(this);
        wandConnDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        wandConnDialog.setContentView(R.layout.dialogue_wand_comm);

        AppCompatButton btConfirm = wandConnDialog.findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(view -> {
            if (isClinicVisit) {
                showSetDateTimeDialog();
            } else {
                launchDashboardFragment(false);
            }
            wandConnDialog.dismiss();
        });
        wandConnDialog.findViewById(R.id.bt_cancel).setOnClickListener(view -> wandConnDialog.dismiss());

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        wandConnDialog.getWindow().setLayout(dimensions.first, dimensions.second);
        wandConnDialog.show();
    }

    private void launchFeatureSelectionFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FeatureSelectionFragment featureSelectionFragment = new FeatureSelectionFragment();
        fragmentTransaction.replace(R.id.fl_fragment, featureSelectionFragment);

        fragmentTransaction.commit();
    }

    private void launchDashboardFragment(boolean isClinicVisit) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        DashboardFragment dashboardFragment = new DashboardFragment();
        dashboardFragment.setClinicVisit(isClinicVisit);
        fragmentTransaction.replace(R.id.fl_fragment, dashboardFragment);

        fragmentTransaction.commit();
    }

    private void launchHamburgerFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        HamburgerFragment hamburgerFragment = new HamburgerFragment();
        fragmentTransaction.replace(R.id.fl_fragment, hamburgerFragment);

        fragmentTransaction.commit();
    }

    /**
     * This function is written for showing updates in wand connection dialogue when wand is connected
     */
    void showWandConnectionInActiveMode() {
        if (wandConnDialog != null) {
            wandConnDialog.findViewById(R.id.ll_header).setBackgroundColor(
                    ActivityCompat.getColor(this, R.color.colorBaseDeepBlue));
            wandConnDialog.findViewById(R.id.iv_header_image).setBackgroundResource(R.drawable.connection_full_element_white);
            ((TextView) wandConnDialog.findViewById(R.id.tv_connection_status)).setText(R.string.wand_is_comm);
            AppCompatButton btConfirm = wandConnDialog.findViewById(R.id.bt_confirm);
            btConfirm.setEnabled(true);
            btConfirm.setAlpha(1);
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

            if (allPermissionsGranted) {
                // Both permissions are granted, proceed with Bluetooth functionality
                initBluetooth();
            } else {
                // Permission denied, handle this scenario (e.g., show a message, disable Bluetooth functionality)
                //showBackToStartDialog();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
        updateBatteryStatus();

        //just for event testing purpose
        ImageView intibiaLogo = findViewById(R.id.intibia_logo);
        intibiaLogo.setOnClickListener(v -> {
            UIUpdateEvent uiUpdateEvent = new UIUpdateEvent();
            uiUpdateEvent.setTabEnum(TabEnum.ITNS);
            EventBus.getDefault().post(uiUpdateEvent);
        });
    }

    private void initBluetooth() {
        // Permission has been granted, you can proceed with your Bluetooth functionality
        mBluetooth = new Bluetooth(this);
        mBluetooth.setReader(ByteReader.class);
        mBluetooth.setDeviceCallback(deviceCallback);
        wandComm = new WandComm(mBluetooth, this);

        mBluetooth.onStart();

        List<BluetoothDevice> btDevices = mBluetooth.getPairedDevices();
        for (BluetoothDevice bt : btDevices) {
            mBTDevice = bt;
        }

        //TODO remove this when publishing
        //mBluetooth.connectToDevice(mBTDevice);

        if (mBluetooth.isEnabled()) {
            Log.d(TAG, "BT was enabled");
        } else {
            mBluetooth.enable();
        }

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
        //TextView tvAppTime = findViewById(R.id.tv_app_time);

        // Get the current date and time from the device
        Calendar currentCalendar = Calendar.getInstance();
        long currentTimeMillis = currentCalendar.getTimeInMillis() + timeDifferenceMillis;

        // Format the time in "2:00 PM" format
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String timeToShow = timeFormat.format(currentTimeMillis);

        // Format the date in "01/10/2023" format
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        String dateToShow = dateFormat.format(currentTimeMillis);

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
            if (mLowBatDialog == null)
                BatteryAlert();
        }

        ImageView ivBatteryPer = findViewById(R.id.iv_battery_per);
        TextView tvBatteryPer = findViewById(R.id.tv_battery_per);

        tvBatteryPer.setText(String.valueOf(batteryPct).concat("%"));

        if (batteryPct > 70)
            ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_full);
        else if (batteryPct > 40)
            ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_level_1);
        else if (batteryPct > 10)
            ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_level_2);
        else
            ivBatteryPer.setBackgroundResource(R.drawable.cp_battery_empty);
    }

    private void BatteryAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        alertDialog.setTitle(R.string.main_lowbat_title);
        alertDialog.setMessage(R.string.main_lowbat_msg);

        alertDialog.setPositiveButton(getString(all_ok), (dialogInterface, i) -> {
            dialogInterface.dismiss();
            mLowBatDialog = null;
        });

        mLowBatDialog = alertDialog.create();
        mLowBatDialog.setCancelable(false);
        mLowBatDialog.show();
    }

    public void UpdateUIFragments(final int frag, final boolean success) {
        MainActivity.this.runOnUiThread(() -> {
            if (frag == WandComm.frags.EXTERNAL) {
                UIUpdateEvent uiUpdateEvent = new UIUpdateEvent();
                uiUpdateEvent.setTabEnum(TabEnum.EXTERNAL);
                uiUpdateEvent.setUiUpdateSuccess(success);
                EventBus.getDefault().post(uiUpdateEvent);
            }

            if (frag == WandComm.frags.ITNS) {
                UIUpdateEvent uiUpdateEvent = new UIUpdateEvent();
                uiUpdateEvent.setTabEnum(TabEnum.ITNS);
                uiUpdateEvent.setUiUpdateSuccess(success);
                EventBus.getDefault().post(uiUpdateEvent);
            }
        });
    }

    public void UpdateUI(final boolean success) {
        if (success) {
            if (wandComm.GetCurrentJob() == WandComm.jobs.INITWAND) {
                OnConnectedUIEvent externalOnConnectedUIEvent = new OnConnectedUIEvent();
                externalOnConnectedUIEvent.setTabEnum(TabEnum.EXTERNAL);
                EventBus.getDefault().post(externalOnConnectedUIEvent);

                OnConnectedUIEvent itnsOnConnectedUIEvent = new OnConnectedUIEvent();
                itnsOnConnectedUIEvent.setTabEnum(TabEnum.ITNS);
                EventBus.getDefault().post(itnsOnConnectedUIEvent);
            }
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

            alertDialog.setTitle(R.string.main_wand_error_title);
            alertDialog.setMessage(R.string.main_wand_error_msg);

            alertDialog.setPositiveButton(getString(all_ok), (dialogInterface, i) -> dialogInterface.dismiss());
            alertDialog.show();

            OnDisconnectedUIEvent externalOnDisconnectedUIEvent = new OnDisconnectedUIEvent();
            externalOnDisconnectedUIEvent.setTabEnum(TabEnum.EXTERNAL);
            EventBus.getDefault().post(externalOnDisconnectedUIEvent);

            OnDisconnectedUIEvent itnsOnDisconnectedUIEvent = new OnDisconnectedUIEvent();
            itnsOnDisconnectedUIEvent.setTabEnum(TabEnum.ITNS);
            EventBus.getDefault().post(itnsOnDisconnectedUIEvent);
        }
    }

    public void UpdateItnsAmplitude() {
        ItnsUpdateAmpEvent itnsUpdateAmpEvent = new ItnsUpdateAmpEvent();
        EventBus.getDefault().post(itnsUpdateAmpEvent);
        //TODO add event to dashboard fragment
//        MainActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.ITNS);
//                itns.UpdateAmplitude();
//            }
//        });
    }

    public void EnableTabs(boolean enable) {
//        LinearLayout tabStrip = ((LinearLayout) mTabLayout.getChildAt(0));
//        tabStrip.setEnabled(false);
//        for (int i = 0; i < tabStrip.getChildCount(); i++) {
//            tabStrip.getChildAt(i).setClickable(enable);
//        }
    }

    private final DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            wandComm.InitWand();
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            wandComm.ResetWandComm();
            // Reconnect
            mBluetooth.connectToDevice(MainActivity.this.mBTDevice);
        }

        @Override
        public void onMessage(byte[] message) {
            wandComm.ReturnMessage(message);
        }

        @Override
        public void onError(int errorCode) {
        }

        @Override
        public void onConnectError(final BluetoothDevice device, String message) {
            if (mRunBT)
                mHandler.postDelayed(Reconnect, 1000);                                    // And if fail, try every second
        }
    };

    public void showResetCounterDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_time_picker);

        /*TextView text = (TextView) dialog.findViewById(R.id.tv_reset_counter);
        //text.setText("msg");

        Button dialogButton = (Button) dialog.findViewById(R.id.btn_reset_counter);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });*/

        dialog.show();
    }

    //TODO- call this method once the bluetooth dialog setup flow is done
    public void showSetDateTimeDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_set_date_time);

        Button btnDate = (Button) dialog.findViewById(R.id.btn_date);
        Button btnTime = (Button) dialog.findViewById(R.id.btn_time);
        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
        Button btnConfirmDisabled = (Button) dialog.findViewById(R.id.btn_confirm_disabled);

        if (!formattedTime.isEmpty()) {
            btnTime.setText(formattedTime.toUpperCase());
            btnTime.setPressed(true);
            btnConfirmDisabled.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.VISIBLE);
            btnConfirm.setClickable(true);
        }
        if (!formattedDate.isEmpty()){
            btnDate.setText(formattedDate);
            btnDate.setPressed(true);
            btnConfirmDisabled.setVisibility(View.GONE);
            btnConfirm.setVisibility(View.VISIBLE);
            btnConfirm.setClickable(true);
        }

        btnDate.setOnClickListener(v -> {
            showDatePickerDialog();
            dialog.dismiss();
        });

        btnTime.setOnClickListener(v -> {
            showTimePickerDialog();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> {
            formattedTime = "";
            formattedDate = "";
            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {
            formattedTime = "";
            formattedDate = "";
            calculateTimeDifference(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinutes);
            dialog.dismiss();
            launchDashboardFragment(true);
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    public void showTimePickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_time_picker);

        Button btnConfirmTime = (Button) dialog.findViewById(R.id.btn_confirm_time);

        TimePicker timePicker = (TimePicker) dialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(false); // Set to true if you want 24-hour format

        // Set a default time (optional)
        Calendar currentTime = Calendar.getInstance();
        int hour = currentTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentTime.get(Calendar.MINUTE);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);

        // Set a listener to the time picker
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute12) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute12);

            // Format the time in 12-hour format with AM/PM
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
            formattedTime = timeFormat.format(calendar.getTime());

            selectedHour = hourOfDay;
            selectedMinutes = minute12;
        });

        btnConfirmTime.setOnClickListener(v -> {
            if (formattedTime.isEmpty()){
                Calendar currentTime1 = Calendar.getInstance();
                int hour1 = currentTime1.get(Calendar.HOUR_OF_DAY);
                int minute1 = currentTime1.get(Calendar.MINUTE);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour1);
                calendar.set(Calendar.MINUTE, minute1);

                // Format the time in 12-hour format with AM/PM
                DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getApplicationContext());
                formattedTime = timeFormat.format(calendar.getTime());

                selectedHour = hour1;
                selectedMinutes = minute1;
            }
            showSetDateTimeDialog();
            dialog.dismiss();
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    public void showDatePickerDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_date_picker);

        final DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        Button btnConfirmDate = (Button) dialog.findViewById(R.id.btn_confirm_date);
        btnConfirmDate.setOnClickListener(v -> {
            if (formattedDate.isEmpty()) {
                int year = datePicker.getYear();
                int month = datePicker.getMonth();
                int day = datePicker.getDayOfMonth();
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day);

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM / dd / yyyy", Locale.US);
                formattedDate = dateFormat.format(calendar.getTime());

                selectedYear = year;
                selectedMonth = month;
                selectedDay = day;
            }
            showSetDateTimeDialog();
            dialog.dismiss();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Set a listener to the date picker
            datePicker.init(
                    datePicker.getYear(),
                    datePicker.getMonth(),
                    datePicker.getDayOfMonth(),
                    (view, year, month, day) -> {
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day);

                        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM / dd / yyyy", Locale.US);
                        formattedDate = dateFormat.format(calendar.getTime());

                        selectedYear = year;
                        selectedMonth = month;
                        selectedDay = day;
                    }
            );
        }

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to set Day/Date from Program therapy.
    public void showSetDayForTherapyDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_set_start_day_therapy);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to set Day/Date from Program therapy.
    public void showSetTimeForTherapyDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_set_time_therapy);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to set frequency from Program therapy.
    public void showSetFrequencyDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_set_frequency);

        RadioButton radioButton1 = (RadioButton) dialog.findViewById(R.id.radio1);
        RadioButton radioButton2 = (RadioButton) dialog.findViewById(R.id.radio2);
        RadioButton radioButton3 = (RadioButton) dialog.findViewById(R.id.radio3);
        RadioButton radioButton4 = (RadioButton) dialog.findViewById(R.id.radio4);
        RadioButton radioButton5 = (RadioButton) dialog.findViewById(R.id.radio5);
        RadioButton radioButton6 = (RadioButton) dialog.findViewById(R.id.radio6);

        RadioGroupPlus radioGroupPlus = (RadioGroupPlus) dialog.findViewById(R.id.frequencyRadioGroup);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //disabled
                        new int[]{android.R.attr.state_checked} //enabled
                },
                new int[]{
                        Color.BLACK, //disabled
                        getResources().getColor(R.color.colorPrimary) //enabled
                }
        );

        radioButton1.setButtonTintList(colorStateList);
        radioButton2.setButtonTintList(colorStateList);
        radioButton3.setButtonTintList(colorStateList);
        radioButton4.setButtonTintList(colorStateList);
        radioButton5.setButtonTintList(colorStateList);
        radioButton6.setButtonTintList(colorStateList);

        radioGroupPlus.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton rb = (RadioButton) dialog.findViewById(checkedId);
            Toast.makeText(getApplicationContext(), rb.getText(), Toast.LENGTH_SHORT).show();
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to set Program ITNS from Program therapy.
    public void showProgramItnsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_program_itns);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        Button btnConfirm = (Button) dialog.findViewById(R.id.btn_confirm);
        btnConfirm.setOnClickListener(v -> {
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to set Program ITNS from Program therapy.
    public void showProgramItnsSuccessDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_program_itns_success);

        Button btnOk = (Button) dialog.findViewById(R.id.btn_ok);
        btnOk.setOnClickListener(v -> {
            dialog.dismiss();
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to show Back to Start dialog.
    public void showBackToStartDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_back_to_start);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    //TODO: Imp call this when we want to show Back to Start dialog.
    public void showCloseAppDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_close_app);

        Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        Button btnYesClose = (Button) dialog.findViewById(R.id.btn_yes_close);
        btnYesClose.setOnClickListener(v -> {
            finish();
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
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
    }

    //TODO : Use this time in during program
    public long getTimeDifferenceMillis() {
        return timeDifferenceMillis;
    }
}