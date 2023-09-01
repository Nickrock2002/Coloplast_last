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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        FeatureSelectionFragment featureSelectionFragment = new FeatureSelectionFragment();
        fragmentTransaction.replace(R.id.fl_fragment, featureSelectionFragment);

        fragmentTransaction.commit();

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

    protected void showBluetoothConnectionDialogue(final boolean isClinicVisit) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialogue_wand_comm);
        dialog.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isClinicVisit) {
                    showSetDateTimeDialog();
                }
                dialog.dismiss();
            }
        });
//        dialog.getWindow().setLayout((int) (width * 0.7), (int) (height * 0.5));
        Pair<Integer, Integer> dimensions = Utility.getDimensionsForDialogue(this);
        dialog.getWindow().setLayout(dimensions.first, dimensions.second);
        dialog.show();
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
        dialog.show();
    }
}