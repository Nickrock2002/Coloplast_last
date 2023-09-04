package com.ninecmed.tablet;

import static com.ninecmed.tablet.R.string.all_ok;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

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

    Dialog wandConnDialog;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // This hides the status bar at the top
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // And this hides the navigation bar at the bottom - along with the code in onResume()
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
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
    }

    public void updateToolbarColor(boolean isInside){
        if (isInside) {
            LinearLayoutCompat toolbar = findViewById(R.id.ll_toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorGreyThreeHundred));

            ImageView intibiaIv = findViewById(R.id.intibia_logo);
            intibiaIv.setVisibility(View.VISIBLE);
        }
    }

    protected void showWandConnectionDialogue(final boolean isClinicVisit) {
        wandConnDialog = new Dialog(this);
        wandConnDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        wandConnDialog.setContentView(R.layout.dialogue_wand_comm);

        AppCompatButton btConfirm = wandConnDialog.findViewById(R.id.bt_confirm);
        btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClinicVisit) {
                    showSetDateTimeDialog();
                } else {
                    launchSurgeryFragment();
                }
                wandConnDialog.dismiss();
            }
        });
        wandConnDialog.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wandConnDialog.dismiss();
            }
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        wandConnDialog.getWindow().setLayout(dimensions.first, dimensions.second);
        wandConnDialog.show();
    }

    private void launchFeatureSelectionFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FeatureSelectionFragment featureSelectionFragment = new FeatureSelectionFragment();
        fragmentTransaction.replace(R.id.fl_fragment, featureSelectionFragment);

        fragmentTransaction.commit();
    }

    private void launchSurgeryFragment(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        DashboardFragment dashboardFragment = new DashboardFragment();
        fragmentTransaction.replace(R.id.fl_fragment, dashboardFragment);

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
                showBluetoothPermissionRequiredDialog();
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

        /*ImageView intibiaLogo = findViewById(R.id.ivBatteryPer);
        intibiaLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().post(new MessageEvent());
            }
        });*/
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

    protected final Runnable Reconnect = new Runnable() {
        @Override
        public void run() {
            mBluetooth.connectToDevice(mBTDevice);
        }
    };

    private final Runnable MinuteTimer = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            updateBatteryStatus();
            mHandler.postDelayed(MinuteTimer, 60000);
        }
    };

    void updateBatteryStatus() {
        BatteryManager bm = (BatteryManager) getApplicationContext().getSystemService(BATTERY_SERVICE);
        boolean batteryCharging = bm.isCharging();
        int batteryPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        if (batteryPct <= 15 && !batteryCharging) {
            if (mLowBatDialog == null)
                BatteryAlert();
        }

        ImageView ivBatteryPer = findViewById(R.id.ivBatteryPer);
        TextView tvBatteryPer = findViewById(R.id.tvBatteryPer);

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

        alertDialog.setPositiveButton(getString(all_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                mLowBatDialog = null;
            }
        });

        mLowBatDialog = alertDialog.create();
        mLowBatDialog.setCancelable(false);
        mLowBatDialog.show();
    }

    public void UpdateUIFragments(final int frag, final boolean success) {
        //TODO add event to dashboard fragment
//        MainActivity.this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (frag == WandComm.frags.EXTERNAL) {
//                    ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.EXT);
//                    external.UIUpdate(success);
//                }
//
//                if (frag == WandComm.frags.ITNS) {
//                    ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.ITNS);
//                    itns.UIUpdate(success);
//                }
//            }
//        });
    }

    public void UpdateUI(final boolean success) {
        //TODO add event to dashboard fragment
//        if (success) {
//            if (wandComm.GetCurrentJob() == WandComm.jobs.INITWAND) {
//                ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.EXT);
//                external.OnConnected();
//
//                ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.ITNS);
//                itns.OnConnected();
//            }
//        } else {
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
//
//            alertDialog.setTitle(R.string.main_wand_error_title);
//            alertDialog.setMessage(R.string.main_wand_error_msg);
//
//            alertDialog.setPositiveButton(getString(all_ok), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            });
//            alertDialog.show();
//
//            ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.EXT);
//            external.OnDisconnected();
//
//            ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(DashboardFragment.tabs.ITNS);
//            itns.OnDisconnected();
//        }
    }

    public void UpdateItnsAmplitude() {
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
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
                dialog.dismiss();
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
                dialog.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: save delta time
                dialog.dismiss();
            }
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
        btnConfirmTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetDateTimeDialog();
                dialog.dismiss();
            }
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

        Button btnConfirmDate = (Button) dialog.findViewById(R.id.btn_confirm_date);
        btnConfirmDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetDateTimeDialog();
                dialog.dismiss();
            }
        });

        DatePicker datePicker = dialog.findViewById(R.id.datePicker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePicker.setOnDateChangedListener(new DatePicker.OnDateChangedListener() {
                @Override
                public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                }
            });
        }

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }

    public void showBluetoothPermissionRequiredDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_bluetooth_permission_required);

        Button btnOkCloseApp = (Button) dialog.findViewById(R.id.btn_ok_close_app);
        btnOkCloseApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });

        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
    }
}