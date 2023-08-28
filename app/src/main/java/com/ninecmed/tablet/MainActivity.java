package com.ninecmed.tablet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import me.aflak.bluetooth.Bluetooth;
import me.aflak.bluetooth.interfaces.DeviceCallback;

import static com.ninecmed.tablet.R.string.all_ok;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private AlertDialog mLowBatDialog = null;

    private BluetoothDevice mBTDevice = null;
    private Bluetooth mBluetooth = null;

    private SectionsPageAdapter mSectionsPageAdapter;
    private final Handler mHandler = new Handler();

    public WandComm wandComm = null;
    private TabLayout mTabLayout;
    private boolean mRunBT = false;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1;

    public interface tabs {
            int EXT = 1;
            int ITNS = 0;           // Tab 0 must also be the default fragment
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_old);

        // This hides the status bar at the top
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // And this hides the navigation bar at the bottom - along with the code in onResume()
        final View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener (new View.OnSystemUiVisibilityChangeListener() {
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

        Log.d(TAG, "onCreate: Starting.");

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_icon_app);
        setSupportActionBar(toolbar);
        mHandler.postDelayed(MinuteTimer, 60000);

        mSectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        // Setup ViewPager with the sections adapter
        CustomViewPager mViewPager = findViewById(R.id.container);
        SetupViewPager(mViewPager);

        mTabLayout = findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

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
    public boolean onPrepareOptionsMenu(Menu menu) {
        UpdateTitle();
        UpdateSubTitle();

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        final View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_bat:
                return true;

            case R.id.menu_date_and_time:
                startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                return true;

            case R.id.menu_about:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                alertDialog.setTitle(getString(R.string.about_title));
                alertDialog.setMessage(getString(R.string.about_version));

                alertDialog.setPositiveButton(getString(all_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();
                return true;

            case R.id.menu_close:
                this.finishAffinity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initBluetooth() {
        // Permission has been granted, you can proceed with your Bluetooth functionality
        mBluetooth = new Bluetooth(this);
        mBluetooth.setReader(ByteReader.class);
        mBluetooth.setDeviceCallback(deviceCallback);

        wandComm = new WandComm(mBluetooth, this);

        mBluetooth.onStart();

        List<BluetoothDevice> btDevices = mBluetooth.getPairedDevices();
        for(BluetoothDevice bt : btDevices) {
            mBTDevice = bt;
        }

        //mBluetooth.connectToDevice(mBTDevice);

        if(mBluetooth.isEnabled()) {
            Log.d(TAG, "BT was enabled");
        }
        else {
            mBluetooth.enable();
        }

        mRunBT = true;

        // Update subtitle - required after returning from activity to set date and time
        UpdateSubTitle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mBluetooth.isConnected()) {
            mHandler.removeCallbacks(MinuteTimer);
            mHandler.removeCallbacks(Reconnect);
            mBluetooth.disconnect();
        }
        mBluetooth.onStop();
        mRunBT = false;
    }

    private final DeviceCallback deviceCallback = new DeviceCallback() {
        @Override
        public void onDeviceConnected(BluetoothDevice device) {
            wandComm.InitWand();
        }

        @Override
        public void onDeviceDisconnected(BluetoothDevice device, String message) {
            wandComm.ResetWandComm();
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
                    external.OnDisconnected();

                    ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                    itns.OnDisconnected();
                }
            });

            // Reconnect
            mBluetooth.connectToDevice(mBTDevice);
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
           if(mRunBT)
                mHandler.postDelayed(Reconnect, 1000);                                    // And if fail, try every second
        }
    };

    private void SetupViewPager(ViewPager viewPager) {
        // The first fragment added is the default fragment that appears
        mSectionsPageAdapter.AddFragment(new ItnsFragment(), getResources().getString(R.string.itns_title));
        mSectionsPageAdapter.AddFragment(new ExternalFragment(), getResources().getString(R.string.external_title));

        viewPager.setAdapter(mSectionsPageAdapter);
        viewPager.setOffscreenPageLimit(2);                                                         // Set page limit to 2 when using two fragments
    }

    private void msg (String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private final Runnable Reconnect = new Runnable() {
        @Override
        public void run() {
            mBluetooth.connectToDevice(mBTDevice);
        }
    };

    private final Runnable MinuteTimer = new Runnable() {
        @SuppressLint("DefaultLocale")
        @Override
        public void run() {
            UpdateTitle();
            UpdateSubTitle();
            mHandler.postDelayed(MinuteTimer, 60000);
        }
    };

    private void UpdateTitle() {
        BatteryManager bm = (BatteryManager) getApplicationContext().getSystemService(BATTERY_SERVICE);
        boolean batteryCharging = bm.isCharging();
        int batteryPct =  bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        if(batteryPct <= 15 && !batteryCharging) {
            if(mLowBatDialog == null)
                BatteryAlert();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        MenuItem item = toolbar.getMenu().findItem(R.id.menu_bat);
//        item.setEnabled(true);
        if(item != null) {
            item.setTitle(batteryPct + "%");

            if(batteryPct > 95)
                item.setIcon(R.drawable.battery);
            else if(batteryPct > 85)
                item.setIcon(R.drawable.battery_90);
            else if(batteryPct > 75)
                item.setIcon(R.drawable.battery_80);
            else if(batteryPct > 65)
                item.setIcon(R.drawable.battery_70);
            else if(batteryPct > 55)
                item.setIcon(R.drawable.battery_60);
            else if(batteryPct > 45)
                item.setIcon(R.drawable.battery_50);
            else if(batteryPct > 35)
                item.setIcon(R.drawable.battery_40);
            else if(batteryPct > 25)
                item.setIcon(R.drawable.battery_30);
            else if(batteryPct > 15)
                item.setIcon(R.drawable.battery_alert);
            else
                item.setIcon(R.drawable.battery_alert);
        }
    }

    private void UpdateSubTitle() {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String timeanddate = sdf.format(Calendar.getInstance().getTime());
        Objects.requireNonNull(getSupportActionBar()).setSubtitle(timeanddate);
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
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(frag == WandComm.frags.EXTERNAL) {
                    ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
                    external.UIUpdate(success);
                }

                if(frag == WandComm.frags.ITNS) {
                    ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                    itns.UIUpdate(success);
                }
            }
        });
    }

    public void UpdateUI(final boolean success) {
        if(success) {
            if(wandComm.GetCurrentJob() == WandComm.jobs.INITWAND) {
                ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
                external.OnConnected();

                ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                itns.OnConnected();
            }
        }
        else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

            alertDialog.setTitle(R.string.main_wand_error_title);
            alertDialog.setMessage(R.string.main_wand_error_msg);

            alertDialog.setPositiveButton(getString(all_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            alertDialog.show();

            ExternalFragment external = (ExternalFragment) mSectionsPageAdapter.getItem(tabs.EXT);
            external.OnDisconnected();

            ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
            itns.OnDisconnected();
        }
    }

    public void UpdateItnsAmplitude() {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ItnsFragment itns = (ItnsFragment) mSectionsPageAdapter.getItem(tabs.ITNS);
                itns.UpdateAmplitude();
            }
        });
    }

    public void EnableTabs(boolean enable) {
        LinearLayout tabStrip = ((LinearLayout)mTabLayout.getChildAt(0));
        tabStrip.setEnabled(false);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setClickable(enable);
        }
    }
}