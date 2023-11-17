package com.ninecmed.tablet;

import android.os.Handler;
import android.util.Log;

import com.ninecmed.tablet.events.ProgramSuccessEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.Calendar;

import me.aflak.bluetooth.Bluetooth;

class WandComm {
    private final String TAG = "WandComm";

    public interface tasks {
        int SETUSERNAME = 0;
        int SETPASSWORD = 1;
        int GETCABLE = 2;
        int WRTSTIMEXTAMP = 3;
        int RDSTIMILOW = 4;
        int RDSTIMIHIGH = 5;
        int GETSTATE = 6;
        int GETID = 7;
        int GETAMPLITUDE = 8;
        int GETCONFIG = 9;
        int GETLEADI = 10;
        int GETCELLV = 11;
        int GETCLOCK = 12;
        int GETSCHEDULE = 13;
        int SETSCHEDULE = 14;
        int SETTHERAPY = 15;
        int SETAMPLITUDE = 16;
        int CLRRESETS = 17;
        int SENDTESTBURST = 18;
        int SENDTESTBURSTEXT = 19;
        int GETWANDFIRMWARE = 20;
        int LASTTASK = 21;
    }

    public interface jobs {
        int NULL = 0;
        int INITWAND = 1;
        int SETSTIM = 2;
        int SETSTIMEXT = 3;
        int INTERROGATE = 4;
        int PROGRAM = 5;
        int SETRESETCOUNTER = 6;
        int CHECKCABLE = 7;
    }

    public interface changes {
        int THERAPY = 0;
        int DATE = 1;
        int TIME = 2;
        int AMPLITUDE = 3;
    }

    public interface frags {
        int EXTERNAL = 0;
        int ITNS = 1;
        int PROGRAM = 2;
        int HAMBURGER = 3;
    }

    private final boolean[] task_list = new boolean[tasks.LASTTASK];
    private final boolean[] change_list = new boolean[4];

    private int mState;
    private final Bluetooth mBluetooth;
    private int mCurrentTask;
    private int mCurrentJob;
    private int mCallingFragment;
    private boolean mContinue = false;
    private final Handler mHandler = new Handler();

    private final MainActivity mainActivity;
    private int mRetries;
    private boolean mEnableStim = false;
    private boolean mJobCancelled = false;

    private WandComm(Bluetooth bt, MainActivity mainActivity) {
        mBluetooth = bt;
        this.mainActivity = mainActivity;
    }

    static WandComm wandComm;

    public static WandComm getInstance(Bluetooth bt, MainActivity mainActivity) {
        wandComm = new WandComm(bt, mainActivity);
        return wandComm;
    }

    int getCurrentJob() {
        return mCurrentJob;
    }

    void initWand() {
        task_list[tasks.SETUSERNAME] = true;
        task_list[tasks.SETPASSWORD] = true;

        mCurrentJob = jobs.INITWAND;
        mState = 0;
        processStateMachine();
    }

    void setStimulation(boolean enable) {
        mCurrentJob = jobs.SETSTIM;
        mEnableStim = enable;
        mHandler.removeCallbacks(restartTestBurst);
        mHandler.removeCallbacks(timeOut);
        mHandler.removeCallbacks(checkForAcknowledgement);
        if (enable) {
            task_list[tasks.GETID] = true;
            if (anyAmplitudeChanges()) {
                task_list[tasks.SETAMPLITUDE] = true;
            }
            task_list[tasks.SENDTESTBURST] = true;
            mState = 0;
            mRetries = 3;
            mJobCancelled = false;
            processStateMachine();
            Log.d(TAG, "Stim Start.");
        } else {
            if (mJobCancelled) {
                Log.d(TAG, "Stim Cancelled.");
                return;
            }
            task_list[tasks.GETID] = false;
            task_list[tasks.SETAMPLITUDE] = false;
            task_list[tasks.SENDTESTBURST] = false;
            task_list[tasks.GETLEADI] = true;
            mState = 0;
            mRetries = 3;
            mHandler.postDelayed(timeOut, 2500);                                          // Wait until test burst is completed
            Log.d(TAG, "Stim Stop.");
        }
    }

    void setStimulationExt(boolean enable, int frag) {
        mCallingFragment = frag;
        mCurrentJob = jobs.SETSTIMEXT;
        mEnableStim = enable;
        mHandler.removeCallbacks(restartTestBurstExt);
        mHandler.removeCallbacks(timeOut);
        mHandler.removeCallbacks(checkForAcknowledgement);
        // When enabled, set the amplitude. When disabled, read lead I
        if (enable) {
            task_list[tasks.WRTSTIMEXTAMP] = true;
            task_list[tasks.SENDTESTBURSTEXT] = true;
            mState = 0;
            mRetries = 3;
            mJobCancelled = false;
            processStateMachine();
        } else {
            if (mJobCancelled) {
                Log.d(TAG, "Test Stim Cancelled.");
            } else {
                task_list[tasks.WRTSTIMEXTAMP] = false;
                task_list[tasks.SENDTESTBURSTEXT] = false;
                task_list[tasks.RDSTIMILOW] = true;
                task_list[tasks.RDSTIMIHIGH] = true;
                mState = 0;
                mRetries = 3;
                mHandler.postDelayed(timeOut, 1000);
                Log.d(TAG, "Stim Stop.");
            }
        }
    }

    void interrogate(int fragment) {
        task_list[tasks.GETSTATE] = true;
        task_list[tasks.GETID] = true;
        task_list[tasks.GETCONFIG] = true;
        task_list[tasks.GETAMPLITUDE] = true;
        task_list[tasks.GETLEADI] = true;
        task_list[tasks.GETCELLV] = true;
        task_list[tasks.GETCLOCK] = true;
        task_list[tasks.GETSCHEDULE] = true;
        task_list[tasks.GETWANDFIRMWARE] = true;

        mCurrentJob = jobs.INTERROGATE;
        mCallingFragment = fragment;
        mState = 0;
        mRetries = 3;
        processStateMachine();
    }

    void program() {
        mCurrentJob = jobs.PROGRAM;
        mState = 0;
        mRetries = 3;
        task_list[tasks.GETID] = true;
//        if (change_list[changes.THERAPY]) {
        task_list[tasks.SETTHERAPY] = true;
        task_list[tasks.SETSCHEDULE] = true;
//        }
//        if (change_list[changes.DATE]) {
//            task_list[tasks.SETSCHEDULE] = true;
//        }
//        if (change_list[changes.TIME]) {
//            task_list[tasks.SETSCHEDULE] = true;
//        }
//        if (change_list[changes.AMPLITUDE]) {
        task_list[tasks.SETAMPLITUDE] = true;
//        }
        processStateMachine();
    }

    void checkCable() {
        task_list[tasks.GETCABLE] = true;

        mCurrentJob = jobs.CHECKCABLE;
        mState = 0;
        processStateMachine();
    }

    void addProgramChanges(int change) {
        change_list[change] = true;
    }

    void removeProgramChanges(int change) {
        change_list[change] = false;
    }

    boolean AnyProgramChanges() {
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < change_list.length; i++) {
            if (change_list[i])
                return true;
        }
        return false;
    }

    void removeAllProgramChanges() {
        Arrays.fill(change_list, false);
    }

    boolean anyAmplitudeChanges() {
        return change_list[changes.AMPLITUDE];
    }

    boolean anyProgramChangesOtherThanAmplitude() {
        if (change_list[changes.TIME])
            return true;
        else if (change_list[changes.THERAPY])
            return true;
        else if (change_list[changes.DATE])
            return true;
        else
            return false;
    }

    void clearResetCounter() {
        task_list[tasks.CLRRESETS] = true;
        mCurrentJob = jobs.SETRESETCOUNTER;
        mState = 0;
        mRetries = 3;
        processStateMachine();
    }

    // The order of the states in this machine are important, so don't rearrange them
    // unless you understand what you're doing!
    private void processStateMachine() {
        switch (mState) {
            case tasks.SETUSERNAME:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    setUsername();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.SETPASSWORD:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    setPassword();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETCABLE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getCable();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.WRTSTIMEXTAMP:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    write_I2C_Stim(1, WandData.getStimAmplitude());
                } else {
                    mContinue = true;
                }
                break;

            case tasks.RDSTIMILOW:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    read_I2C_Stim(2);
                } else {
                    mContinue = true;
                }
                break;

            case tasks.RDSTIMIHIGH:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    read_I2C_Stim(3);
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETSTATE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getState();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETID:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getID();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETAMPLITUDE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getAmplitude();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETCONFIG:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getConfig();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETLEADI:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getLeadI();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETCELLV:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getCellV();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETCLOCK:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getClock();
                } else {
                    mContinue = true;
                }
                break;
            case tasks.GETSCHEDULE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getSchedule();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.SETSCHEDULE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    setSchedule();
                } else {
                    mContinue = true;
                }
                break;

            // SetTherapy must be after SetSchedule since executing SetSchedule
            // enables therapy.
            case tasks.SETTHERAPY:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    setTherapy();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.SETAMPLITUDE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    setAmplitude();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.CLRRESETS:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    clearResets();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.SENDTESTBURST:
                if (task_list[mState] && mEnableStim) {
                    mCurrentTask = mState;
                    Log.d(TAG, "Send Test Burst.");
                    sendTestBurst();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.SENDTESTBURSTEXT:
                if (task_list[mState] && mEnableStim) {
                    mCurrentTask = mState;
                    Log.d(TAG, "Send Test Burst Ext.");
                    sendTestBurstExt();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.GETWANDFIRMWARE:
                if (task_list[mState]) {
                    mCurrentTask = mState;
                    getWandFirmware();
                } else {
                    mContinue = true;
                }
                break;

            case tasks.LASTTASK:
                if (mCurrentJob == jobs.INTERROGATE) {
                    WandData.interrogateSuccessful();
                } else if (mCurrentJob == jobs.PROGRAM) {
                    WandData.programSuccessful();
                    ProgramSuccessEvent programSuccessEvent = new ProgramSuccessEvent();
                    EventBus.getDefault().post(programSuccessEvent);
                } else if (mCurrentJob == jobs.SETRESETCOUNTER) {
                    WandData.resetSuccessful();
                } else if (mCurrentJob == jobs.SETSTIM) {
                    WandData.stimSuccessfull();
                }
                updateUIFragments(true);
                break;
        }

        mState += 1;
        if (mContinue) {
            mContinue = false;
            mHandler.postDelayed(timeOut, 0);
        }
    }

    private final Runnable timeOut = this::processStateMachine;

    /// This timer only gets called if a message to the wand is not acknowledged
    private final Runnable checkForAcknowledgement = new Runnable() {
        @Override
        public void run() {
            if (mRetries > 0) {
                mRetries--;
                mState -= 1;
                processStateMachine();
            } else {
                updateUIFragments(false);
            }
        }
    };

    private void write_I2C_Stim(int address, int data) {
        byte[] msg = {'W', 'W', (byte) address, (byte) data, 0, 0};
        sendMessage(msg);
    }

    private void read_I2C_Stim(int address) {
        byte[] msg = {'W', 'R', (byte) address, 0, 0};
        sendMessage(msg);
    }

    private void setPassword() {
        byte[] msg = {'W', 'p', 'U', 'c', 'w', '1', '5', 't', 'h', 'U', 0, 0};
        sendMessage(msg);
    }

    private void setUsername() {
        byte[] msg = {'W', 'u', 'N', 'i', 'n', 'e', 'C', 'M', 'e', 'd', 0, 0};
        sendMessage(msg);
    }

    private void getCable() {
        byte[] msg = {'W', 'c', 0, 0};
        sendMessage(msg);
    }

    private void getState() {
        byte[] msg = {'W', '1', 0, 0};
        sendMessage(msg);
    }

    //implant firmware
    private void getID() {
        byte[] msg = {'I', '0', 0, 0};
        sendMessage(msg);
    }

    private void getAmplitude() {
        byte[] msg = {'I', '1', 0, 0};
        sendMessage(msg);
    }

    private void getConfig() {
        byte[] msg = {'I', '2', 0, 0, 0};
        sendMessage(msg);
    }

    private void getLeadI() {
        byte[] msg = {'I', '3', 0, 0};
        sendMessage(msg);
    }

    private void getCellV() {
        byte[] msg = {'I', '4', 0, 0};
        sendMessage(msg);
    }

    private void getWandFirmware() {
        byte[] msg = {'W', 'f', 0, 0};
        sendMessage(msg);
    }

    private void getClock() {
        byte[] msg = {'I', '9', 0, 0};
        sendMessage(msg);
    }

    private void getSchedule() {
        byte[] msg = {'I', 'a', 0, 0};
        sendMessage(msg);
    }

    private void setSchedule() {
        long now_in_ms = Calendar.getInstance().getTimeInMillis() + mainActivity.getTimeDifferenceMillis();
        long next_therapy_date_in_ms = WandData.dateandtime[WandData.FUTURE];
        long deltat = (next_therapy_date_in_ms - now_in_ms) / 1000;                                 // Calculate delta t in seconds

        byte days = (byte) (deltat / 86400);
        byte hours = (byte) ((deltat - days * 86400) / 3600);
        byte mins = (byte) ((deltat - days * 86400 - hours * 3600) / 60);

        byte[] msg = {'I', '7', mins, hours, days, 0, 0};
        sendMessage(msg);
    }

    private void setTherapy() {
        byte config = (byte) WandData.getConfig();

        // Do this for Model 1
        if (WandData.getModelNumber() == 1) {
            switch (WandData.therapy[WandData.FUTURE]) {
                case 0:                         // Therapy off
                    config &= ~0x01;            // Clear the first bit
                    break;

                case 1:                         // Daily therapy
                    config |= 0x01;             // Enable therapy
                    config |= 0x02;             // Set daily therapy
                    break;

                case 2:                         // Weekly therapy
                    config |= 0x01;             // Enable therapy
                    config &= ~0x02;            // Clear daily therapy
                    break;
            }
        }
        // Do this for Model 2
        else {
            switch (WandData.therapy[WandData.FUTURE]) {
                case 0:                         // Therapy off
                    config &= ~0x01;            // Clear the first bit
                    break;

                case 1:                         // Daily therapy
                    config |= 0x01;             // Enable therapy
                    config &= ~0x0e;            // Clear therapy bits
                    break;

                case 2:                         // Weekly therapy
                    config |= 0x01;             // Enable therapy
                    config &= ~0x0e;            // Clear therapy bits
                    config |= 0x02;             // Set weekly therapy
                    break;

                case 3:                         // Fortnightly therapy
                    config |= 0x01;             // Enable therapy
                    config &= ~0x0e;            // Clear therapy bits
                    config |= 0x04;             // Set fortnightly therapy
                    break;

                case 4:                         // Monthly therapy
                    config |= 0x01;             // Enable therapy
                    config &= ~0x0e;            // Clear therapy bits
                    config |= 0x06;             // Set monthly therapy
                    break;

                case 5:                         // Auto jump-start therapy
                    config |= 0x01;             // Enable therapy
                    config &= ~0x0e;            // Clear therapy bits
                    config |= 0x08;             // Set auto therapy
                    break;

                default:
                    config &= ~0xf;            // Clear all the schedule bits and bit 0
                    break;
            }
        }

        config |= 0x80;                     // Set upper bit to change config

        byte[] msg = {'I', '2', config, 0, 0};
        sendMessage(msg);
    }

    private void setAmplitude() {
        float amplitude = WandData.getAmpFromPos(WandData.amplitude[WandData.FUTURE]);
        byte[] msg = {'I', '6', (byte) (amplitude / 0.05f), 0, 0};
        sendMessage(msg);
    }

    private void clearResets() {
        byte config = (byte) WandData.getConfig();

        // Do this for Model 1
        if (WandData.getModelNumber() == 1)
            config |= 0x88;                     // Set upper bit to change config and bit 3 to clear bReset
            // Do this for Model 2
        else
            config |= 0xa0;                     // Set upper bit to change config and bit 5 to clear bReset

        byte[] msg = {'I', '2', config, 0, 0};
        sendMessage(msg);
    }

    private void sendTestBurst() {
        byte[] msg = {'I', '8', 0, 0};
        sendMessage(msg);
    }

    private void sendTestBurstExt() {
        write_I2C_Stim(0x00, 0x01);
    }

    private void sendMessage(byte[] msg) {
        int crc = CRC.Crc16(msg, msg.length - 2);
        msg[msg.length - 2] = (byte) (crc >> 8);
        msg[msg.length - 1] = (byte) (crc & 0xff);

        byte[] txBuffer;
        txBuffer = CobsUtils.Encode(msg);
        if (mBluetooth != null)
            mBluetooth.send(txBuffer);

        // Call timer to verify that every message to the wand is acknowledged
        // 1200 seems to work well when the wand battery is low. Shorter durations
        // seem to fail on the setschedule command
        mHandler.postDelayed(checkForAcknowledgement, 1200);
    }

    void returnMessage(byte[] message) {
        // Kill the timer that checks for a message acknowledgement
        mHandler.removeCallbacks(checkForAcknowledgement);

        byte[] rxBuffer;

        rxBuffer = CobsUtils.Decode(message);

        switch (mCurrentTask) {
            case tasks.SETUSERNAME:
            case tasks.SETPASSWORD:
            case tasks.WRTSTIMEXTAMP:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct.");
                    mRetries = 3;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect.");
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    Log.d(TAG, "CRC incorrect, stop trying.");
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETCABLE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETSTATE.");
                    WandData.setCable(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect.");
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    Log.d(TAG, "CRC incorrect, stop trying.");
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.RDSTIMILOW:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "RDSTIMLOW correct.");
                    WandData.setStimI(rxBuffer, WandData.LOW);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect.");
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    Log.d(TAG, "CRC incorrect, stop trying.");
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.RDSTIMIHIGH:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "RDSTIMHIGH correct.");
                    WandData.setStimI(rxBuffer, WandData.HIGH);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect.");
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    Log.d(TAG, "CRC incorrect, stop trying.");
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETSTATE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETSTATE.");
                    int state = rxBuffer[2];
                    if (state != 6) {
                        updateUIFragments(false);
                        return;
                    }
                    mRetries = 3;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect.");
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    Log.d(TAG, "CRC incorrect, stop trying.");
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETID:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETID.");
                    WandData.setIDInformation(rxBuffer);
                    mRetries = 3;
                    if (mCurrentJob == jobs.PROGRAM || mCurrentJob == jobs.SETSTIM) {
                        if (WandData.isITNSNew()) {
                            updateUIFragments(false);
                            return;
                        }
                    }
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETWANDFIRMWARE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETID.");
                    WandData.setWandFirmwareInfo(rxBuffer);
                    mRetries = 3;
                    if (mCurrentJob == jobs.PROGRAM || mCurrentJob == jobs.SETSTIM) {
                        if (WandData.isITNSNew()) {
                            updateUIFragments(false);
                            return;
                        }
                    }
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETAMPLITUDE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETAMPLITUDE.");
                    WandData.setAmplitude(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETCONFIG:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETCONFIG.");
                    WandData.setConfig(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETLEADI:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETLEADI.");
                    WandData.setLeadI(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETCELLV:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETCELLV.");
                    WandData.setCellV(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETCLOCK:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETCLOCK.");
                    WandData.setClock(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.GETSCHEDULE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for GETSCHEDULE.");
                    WandData.setSchedule(rxBuffer);
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.SETTHERAPY:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for SETTHERAPY.");

                    // Update CURRENT and TEMPORARY values in case the "program" command fails
                    // the CURRENT value will still reflect that the SetTherapy was successful
                    // and if the "program" command succeeds, the TEMPORARY value will copies to
                    // the CURRENT value.
                    WandData.therapy[WandData.TEMPORARY] = WandData.therapy[WandData.CURRENT] = WandData.therapy[WandData.FUTURE];
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.SETSCHEDULE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for SETSCHEDULE.");

                    // Update CURRENT and TEMPORARY values in case the "program" command fails
                    // the CURRENT value will still reflect that the SetSchedule was successful
                    // and if the "program" command succeeds, the TEMPORARY value will copies to
                    // the CURRENT value.
                    WandData.dateandtime[WandData.TEMPORARY] =
                            WandData.dateandtime[WandData.CURRENT] =
                                    WandData.dateandtime[WandData.FUTURE];
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.SETAMPLITUDE:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for SETAMPLITUDE.");
                    // Update CURRENT and TEMPORARY values in case the "program" command fails
                    // the CURRENT value will still reflect that the SetAmplitude was successful
                    // and if the "program" command succeeds, the TEMPORARY value will copies to
                    // the CURRENT value.
                    WandData.amplitude[WandData.TEMPORARY] = WandData.amplitude[WandData.CURRENT] = WandData.amplitude[WandData.FUTURE];
                    mainActivity.updateItnsAmplitude();
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.CLRRESETS:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for CLRRESETS.");
                    mRetries = 3;
                } else if (mRetries > 0) {
                    mRetries--;
                    mState -= 1;            // Backup state machine and try again
                } else {
                    updateUIFragments(false);
                    return;
                }
                break;

            case tasks.SENDTESTBURST:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for TESTBURST.");
                    mRetries = 1;
                    // A delay of 2450 from the receipt of the burst to the start of the
                    // next command results in a cycle of 3 seconds!
                    mHandler.postDelayed(restartTestBurst, 2450);
                    return;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect, retry.");
                    mRetries--;
                    mState -= 1;            // Keep repeating SENDTESTBURST
                    break;
                } else {
                    Log.d(TAG, "CRC incorrect, don't retry.");
                    updateUIFragments(false);
                    return;
                }

            case tasks.SENDTESTBURSTEXT:
                if (CRC.Crc16(rxBuffer, message.length - 2) == 0) {
                    Log.d(TAG, "CRC correct for TESTBURSTEXT.");
                    mRetries = 3;
                    // A delay of 2920 from the receipt of the burst to the start of the
                    // next command results in a cycle of approx 3 seconds!  Note that the
                    // Wand only listens for tablet commands approximately every 300 ms, so
                    // the burst will actually occur only when the wand processes the command.
                    mHandler.postDelayed(restartTestBurstExt, 2920);
                    return;
                } else if (mRetries > 0) {
                    Log.d(TAG, "CRC incorrect, retry.");
                    mRetries--;
                    mState -= 1;            // Keep repeating SENDTESTBURST
                    break;
                } else {
                    Log.d(TAG, "CRC incorrect, don't retry.");
                    updateUIFragments(false);
                    return;
                }
        }

        processStateMachine();
    }

    private final Runnable restartTestBurst = new Runnable() {
        @Override
        public void run() {
            if (mEnableStim) {
                mState = tasks.SENDTESTBURST;
                processStateMachine();
            }
        }
    };

    private final Runnable restartTestBurstExt = new Runnable() {
        @Override
        public void run() {
            if (mEnableStim) {
                mState = tasks.SENDTESTBURSTEXT;
                processStateMachine();
            }
        }
    };

    private void resetTaskList() {
        Arrays.fill(task_list, false);
    }

    private void resetChangeList() {
        Arrays.fill(change_list, false);
    }

    private void updateUIFragments(boolean success) {
        resetTaskList();
        mainActivity.updateUIFragments(mCallingFragment, success);

        if (success) {
            // Only clear the amplitude control if the job is test stimulation.
            // We wouldn't want to cancel any other pending changes.
            if (mCurrentJob == jobs.SETSTIM) {
                change_list[changes.AMPLITUDE] = false;
            }
        }
        resetWandComm();
    }

    public void resetWandComm() {
        // Don't reset change list or current job as these
        // are required when WandComm completes in case of retry

        mHandler.removeCallbacks(restartTestBurst);
        mHandler.removeCallbacks(restartTestBurstExt);
        mHandler.removeCallbacks(checkForAcknowledgement);
        mEnableStim = false;
        mRetries = 0;
        mCurrentTask = 0;
        mJobCancelled = true;
        resetTaskList();
    }
}
