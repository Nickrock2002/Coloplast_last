package com.ninecmed.tablet;

import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class WandData {
    static private final String TAG = "WandData";

    // All implant parameters are stored in a 3 element array.  The first element is for TEMPORARY
    // parameters, the second is for CURRENT parameters, and the third is for FUTURE parameters.
    // For parameters that are only interrogated, FUTURE values are not used.
    // When interrogating the implant, all parameters are first read into TEMPORARY
    // values in case only a partial interrogation occurs.  If this were to happen, there could
    // be inconsistencies for parameters such as the clock and the schedule data. For this reason
    // we only copy the TEMPORARY set of values over to the CURRENT set of parameters once the
    // interrogation is completed successfully.  Likewise, when we program parameters, we are able
    // to update the CURRENT values when we're confident that the commands were successful, even
    // if it means that the entire "program" set might be unsuccessful.

    // Programmable implant parameters
    static byte[] therapy = new byte[3];                                                            // 0 for Off, 1 for Daily, 2 for Weekly, 3 for Fortnightly, 4 for Monthly, 5 for Auto
    static long[] dateandtime = new long[3];                                                        // Date and time of next therapy in milliseconds
    static byte[] amplitude = new byte[3];                                                          // 0 for 0.1 V, 42 for 10.0.

    // Programmable external stimulation parameters
    static private byte mStimAmplitude = 30;                                                        // Set default amplitude to 1.5 V (50 mV resolution)
    static private int mStimLeadI;                                                                  // Start with StimLeadI hidden

    // Interrogatable implant parameters
    static private final int[] mSerialNumber = new int[3];
    static private final int[] mWandFirmwareVersion = new int[3];
    static private final int[] mImplantFirmwareVersion = new int[3];
    static private final int[] mModelNumber = new int[3];
    static private final int[] mResets = new int[3];
    static private final int[] mConfig = new int[3];
    static private final int[] mLeadI = new int[3];
    static private final int[] mCellV = new int[3];
    static private final int[] mRRT = new int[3];

    // Combined model/serial number to detect unique ITNSs
    static private final long[] mModelSerial = new long[3];

    static private class mClock {
        static private final int[] mHours = new int[3];
        static private final int[] mMins = new int[3];
    }

    static private class mSchedule {
        static private final int[] mAlarms = new int[3];
        static private final int[] mHours = new int[3];
        static private final int[] mMins = new int[3];
    }

    static private boolean mInterrogateSuccessful = false;
    static private boolean mCable;

    static int TEMPORARY = 0;
    static int CURRENT = 1;
    static int FUTURE = 2;

    static int LOW = 0;
    static int HIGH = 1;

    static void setCable(byte[] msg) {
        mCable = (msg[2] & 0xff) > 0;
    }

    static boolean GetCable() {
        return mCable;
    }

    static int getStimAmplitude() {
        return mStimAmplitude;
    }

    // Call InvalidateStimLeadI whenever the amplitude is changed.  After the initial
    // interrogation, LeadI should be invalided by any change to amplitude and then
    // display only following test stimulation.
    static void invalidateStimLeadI() {
        mLeadI[TEMPORARY] = -1;
        mLeadI[CURRENT] = -1;
    }

    static void invalidateStimExtLeadI() {
        mStimLeadI = -1;
    }

    static void setStimAmplitude(int position) {
        float f = getAmpFromPos(position);
        mStimAmplitude = (byte) (f / .05f);
    }

    static void interrogateSuccessful() {
        copyTemporaryValuesToCurrentValues();
        mModelSerial[CURRENT] = (mModelNumber[CURRENT] << 16) + mSerialNumber[CURRENT];
        mInterrogateSuccessful = true;
    }

    static void programSuccessful() {
        copyTemporaryValuesToCurrentValues();
    }

    static void resetSuccessful() {
        // When reset is successful, clear mResets[TEMPORARY] otherwise resets
        // will be detected again if the user happens to Program something
        // rather than perform an Interrogate.
        mResets[CURRENT] = mResets[TEMPORARY] = 0;
    }

    static private void copyTemporaryValuesToCurrentValues() {
        therapy[CURRENT] = therapy[TEMPORARY];
        dateandtime[CURRENT] = dateandtime[TEMPORARY];
        amplitude[CURRENT] = amplitude[TEMPORARY];

        mSerialNumber[CURRENT] = mSerialNumber[TEMPORARY];
        mModelNumber[CURRENT] = mModelNumber[TEMPORARY];
        mResets[CURRENT] = mResets[TEMPORARY];
        mConfig[CURRENT] = mConfig[TEMPORARY];

        // Don't update these parameter following the first successful interrogation for model 1
        if (mModelNumber[TEMPORARY] == 1) {
            if (!mInterrogateSuccessful) {
                mLeadI[CURRENT] = mLeadI[TEMPORARY];
                mCellV[CURRENT] = mCellV[TEMPORARY];
                mRRT[CURRENT] = mRRT[TEMPORARY];
            }
        }
        // Do this for model 2
        else {
            if (!mInterrogateSuccessful) {
                mLeadI[CURRENT] = mLeadI[TEMPORARY];
            }
            mCellV[CURRENT] = mCellV[TEMPORARY];
            mRRT[CURRENT] = mRRT[TEMPORARY];
        }

        mClock.mHours[CURRENT] = mClock.mHours[TEMPORARY];
        mClock.mMins[CURRENT] = mClock.mMins[TEMPORARY];
        mSchedule.mHours[CURRENT] = mSchedule.mHours[TEMPORARY];
        mSchedule.mMins[CURRENT] = mSchedule.mMins[TEMPORARY];
        mSchedule.mAlarms[CURRENT] = mSchedule.mAlarms[TEMPORARY];
    }

    static void stimSuccessfull() {
        amplitude[CURRENT] = amplitude[TEMPORARY];
        mLeadI[CURRENT] = mLeadI[TEMPORARY];
    }

    static void setIDInformation(byte[] msg) {
        mSerialNumber[TEMPORARY] = (msg[2] & 0xff) * 256 + (msg[3] & 0xff);
        mModelNumber[TEMPORARY] = (msg[4] & 0xf0) >> 4;
        mResets[TEMPORARY] = (msg[6] & 0xff);
        mImplantFirmwareVersion[TEMPORARY] = msg[4] & 0x0f;
        mModelSerial[TEMPORARY] = (mModelNumber[TEMPORARY] << 16) + mSerialNumber[TEMPORARY];

        if (isITNSNew()) {
            mInterrogateSuccessful = false;
        }
    }

    static void setWandFirmwareInfo(byte[] msg) {
        mWandFirmwareVersion[TEMPORARY] = (msg[2] & 0xff);
    }

    static int getResets() {
        return mResets[CURRENT];
    }

    @SuppressLint("DefaultLocale")
    static String getSerialNumber() {
        if (mSerialNumber[CURRENT] != 0) {
            return String.format("%05d", mSerialNumber[CURRENT]);
        } else {
            return null;
        }
    }

    public static String getWandFirmware() {
        return String.valueOf(mWandFirmwareVersion[TEMPORARY]);
    }

    public static String getImplantFirmware() {
        return String.valueOf(mImplantFirmwareVersion[TEMPORARY]);
    }

    static String getModelNumber(Context context) {
        if (mModelNumber[CURRENT] == 1)
            return context.getString(R.string.all_model_number_one);
        else if (mModelNumber[CURRENT] == 2)
            return context.getString(R.string.all_model_number_two);
        else
            return null;
    }

    static int getModelNumber() {
        return mModelNumber[CURRENT];
    }

    static void setStimI(byte[] msg, int low_or_high) {
        if (low_or_high == LOW)
            mStimLeadI = (msg[2] & 0xff);
        else
            mStimLeadI += (msg[2] & 0xff) << 8;
    }

    @SuppressLint("DefaultLocale")
    static String getStimLeadI() {
        if (mStimLeadI == -1)
            return null;
        else {
            float amp_setting = (mStimAmplitude & 0xff) * 0.05f;
            float amp = max(amp_setting, 2.25f);

            float vtank, leadr, imeas, leadi;
            // From UD-00197
            imeas = mStimLeadI * 0.00003922f;
            vtank = amp * (608.9f / 500.0f);

            // Determine the load resistance
            leadr = (7150.0f * 7150.0f * imeas) / (imeas * (7218.9f) - vtank) - 40.0f - 7150.0f;
            if (leadr <= 0.0f)
                leadr = 10000.0f;

            // Then calculate lead current
            leadi = 1000.0f * (7150.0f * imeas) / (7190.0f + leadr);

            // If lead current is greater than 2000 ohms, then display 0 mA
            if (leadr > 2000.0f)
                leadi = 0.0f;

            // Adjust lead current based on voltage setting for voltages < 2.25 V
            if (amp_setting < 2.25f)
                leadi = leadi * amp_setting / 2.25f;

            if (leadi < 0.1f)
                return String.format("%.3f mA", leadi);
            else if (leadi < 1.0f)
                return String.format("%.2f mA", leadi);
            else
                return String.format("%.1f mA", leadi);
        }
    }

    static void setAmplitude(byte[] msg) {
        if ((msg[2] & 0xff) <= 10) {
            amplitude[TEMPORARY] = (byte) ((msg[2] & 0xff) / 2 - 1);
        } else
            amplitude[TEMPORARY] = (byte) ((msg[2] & 0xff) / 5 + 2);
    }

    static int getAmplitudePos() {
        return amplitude[CURRENT];
    }

    static float getAmpFromPos(int pos) {
        if (pos <= 4)
            return (pos + 1) * 0.1f;
        else
            return (pos + 1) * 0.25f - 0.75f;
    }

    @SuppressLint("DefaultLocale")
    static String getAmplitude() {
        float amp = getAmpFromPos(amplitude[CURRENT]);
        return String.format("%.2f V", amp);
    }

    static void setConfig(byte[] msg) {
        mConfig[TEMPORARY] = msg[2] & 0xff;

        // For each model, check to see if therapy is active. If so, then update therapy and
        // update mRRT
        if (mModelNumber[TEMPORARY] == 1) {
            if ((mConfig[TEMPORARY] & 0x01) > 0) {
                if ((mConfig[TEMPORARY] & 0x02) > 0)
                    therapy[TEMPORARY] = 1;                                                         // Daily
                else
                    therapy[TEMPORARY] = 2;                                                         // Weekly
            } else
                therapy[TEMPORARY] = 0;                                                             // Off

            if ((mConfig[TEMPORARY] & 0x01) > 0) {
                if ((mConfig[TEMPORARY] & 0x04) > 0)                                                // Report RRT flag
                    mRRT[TEMPORARY] = 1;
                else
                    mRRT[TEMPORARY] = 0;
            } else
                mRRT[TEMPORARY] = -1;                                                               // Otherwise hide value
        } else {
            if ((mConfig[TEMPORARY] & 0x01) > 0) {
                byte config = (byte) (((mConfig[TEMPORARY] & 0x0e) >> 1) + 1);
                therapy[TEMPORARY] = config;                                                        // Set therapy
            } else
                therapy[TEMPORARY] = 0;                                                             // Off

            if ((mConfig[TEMPORARY] & 0x10) > 0)                                                    // Report RRT flag
                mRRT[TEMPORARY] = 1;
            else
                mRRT[TEMPORARY] = 0;
        }
    }

    static int getConfig() {
        return mConfig[CURRENT];
    }

    public static int getTherapyPos() {
        return therapy[CURRENT];
    }

    public static String getTherapy(Context context) {
        // Do this for Model 1
        if (mModelNumber[CURRENT] == 1) {
            String[] therapy_array = context.getResources().getStringArray(R.array.itns_therapy_schedule_array_model_one);
            if (therapy[CURRENT] >= 0 && therapy[CURRENT] <= 2)
                return therapy_array[therapy[CURRENT]];
            else
                return "error";
        }
        // Do this for Model 2
        else {
            String[] therapy_array = context.getResources().getStringArray(R.array.itns_therapy_schedule_array_model_two);
            if (therapy[CURRENT] >= 0 && therapy[CURRENT] <= 5)
                return therapy_array[therapy[CURRENT]];
            else
                return "error";
        }
    }

    @SuppressLint("DefaultLocale")
    static float getLeadI() {
        if (mLeadI[CURRENT] == -1)
            return 0.0f;
        else {
            float amp = getAmpFromPos(amplitude[CURRENT]);

            float leadi;
            if (amp < 2.25f) {
                leadi = mLeadI[CURRENT] * 0.03922f * amp / 2.25f;                                   // Adjust current based on strength duration curve
            } else {
                leadi = mLeadI[CURRENT] * 0.03922f;
            }

            // Calculate load resistance
            float amp_setting = max(amp, 2.25f);
            float leadr = amp_setting / (mLeadI[CURRENT] * 0.00003922f);

            // And set lead current to 0 mA if load resistance > 2000 ohms.
            if (leadr > 2000.0f)
                leadi = 0.0f;

            return leadi;

            /*if(leadi < 0.1f)
                return String.format("%.3f mA", leadi);
            else if(leadi < 1.0f)
                return String.format("%.2f mA", leadi);
            else
                return String.format("%.1f mA", leadi);*/
        }
    }

    static void setLeadI(byte[] msg) {
        if ((mConfig[TEMPORARY] & 0x01) > 0 || mInterrogateSuccessful)                              // Show lead I on first interrogate if therapy is set, or if after a successful interrogation
            mLeadI[TEMPORARY] = (msg[2] & 0xff) * 256 + (msg[3] & 0xff);
        else
            mLeadI[TEMPORARY] = -1;                                                                 // Otherwise hide value
    }

    @SuppressLint("DefaultLocale")
    public static String getCellV() {

        // For model 1, hide Cell V if -1
        if (mModelNumber[CURRENT] == 1) {
            if (mCellV[CURRENT] == -1)
                return null;
            else {
                float cellv = mCellV[CURRENT] * .025f;
                return String.format("%.2f V", cellv);
            }
        }
        // Else for model 2, display Cell V if therapy is active
        else {
            if (therapy[CURRENT] > 0 && therapy[CURRENT] <= 5) {
                float cellv = mCellV[CURRENT] * .025f;
                return String.format("%.2f V", cellv);
            } else
                return null;
        }
    }

    static void setCellV(byte[] msg) {
        if (((mConfig[TEMPORARY] & 0x01) > 0) || (mModelNumber[TEMPORARY] == 2))                    // Show cell V on first interrogate if therapy is set or if Model 2
            mCellV[TEMPORARY] = msg[2] & 0xff;
        else
            mCellV[TEMPORARY] = -1;                                                                 // Otherwise hide value
    }

    static String getRRT(Context context) {

        if (mModelNumber[CURRENT] == 1) {
            if (mRRT[CURRENT] == 1)
                return context.getString(R.string.all_yes);
            else if (mRRT[CURRENT] == 0)
                return context.getString(R.string.all_no);
            else
                return null;
        }
        // For model 2, if therapy is active, display RRT
        else {
            if (therapy[CURRENT] > 0 && therapy[CURRENT] <= 5) {
                if (mRRT[CURRENT] == 1)
                    return context.getString(R.string.all_yes);
                else if (mRRT[CURRENT] == 0)
                    return context.getString(R.string.all_no);
            }
            return null;
        }
    }

    @SuppressLint("DefaultLocale")
    static float getStimLeadR() {
        if (mStimLeadI == -1)
            return 0f;
        else {
            if (mStimLeadI == 0)
                return 0f;

            // Don't allow amplitude values less than 2.25
            float amp = (mStimAmplitude & 0xff) * 0.05f;
            amp = max(amp, 2.25f);

            float vtank, leadr, imeas;
            // From UD-00197
            imeas = mStimLeadI * 0.00003922f;
            vtank = amp * (608.9f / 500.0f);

            // Determine the load resistance
            leadr = (7150.0f * 7150.0f * imeas) / (imeas * (7218.9f) - vtank) - 40.0f - 7150.0f;
            if (leadr <= 0.0f)
                leadr = 10000.0f;

            return leadr;
        }
    }

    @SuppressLint("DefaultLocale")
    static float getLeadR() {

        if (mLeadI[CURRENT] == -1)
            return 0f;
        else {
            if (mLeadI[CURRENT] == 0)
                return 0f;

            // Don't allow amplitude values less than 2.25
            float amp = getAmpFromPos(amplitude[CURRENT]);
            amp = max(amp, 2.25f);
            float leadr = amp / (mLeadI[CURRENT] * 0.00003922f);                                    // leadi = (n*FVR)/(1024*51ohms)
            /*if(leadr <= 2000.0)
                return String.format("%.0f ohms", leadr);
            else
                return "> 2000 ohms";*/
            return leadr;
        }
    }

    @SuppressLint("DefaultLocale")
    static String getDate() {
        if (therapy[CURRENT] == 0 || (therapy[CURRENT] == 1 && mModelNumber[CURRENT] == 1)) {        // Don't show date if therapy is disabled or if daily therapy
            // is chosen for the model 1
            return "---";
        } else {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dateandtime[CURRENT]);

            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE dd-MMM-yyyy", Locale.US);

            return dateFormat.format(c.getTime());
        }
    }

    static void setClock(byte[] msg) {
        mClock.mMins[TEMPORARY] = ((msg[2] & 0xf0) >> 4) * 10 + (msg[2] & 0x0f);                   // Convert from BCD
        mClock.mHours[TEMPORARY] = ((msg[3] & 0xf0) >> 4) * 10 + (msg[3] & 0x0f);                   // Convert from BCD
    }

    static void setSchedule(byte[] msg) {
        mSchedule.mMins[TEMPORARY] = ((msg[2] & 0xf0) >> 4) * 10 + (msg[2] & 0x0f);               // Convert from BCD
        mSchedule.mHours[TEMPORARY] = ((msg[3] & 0xf0) >> 4) * 10 + (msg[3] & 0x0f);               // Convert from BCD
        mSchedule.mAlarms[TEMPORARY] = msg[4];

        // No need to calculate anything since everything is 0xff if therapy is off
        if (therapy[TEMPORARY] == 0)
            return;

        // Calculate the difference between the schedule and clock. If
        // this difference is positive, an alarm will occur today in the FUTURE,
        // if the difference is negative, an alarm has already occurred today,
        // and we'll have to wait.  Know the sign tell us how to handle
        // the remaining alarms.
        int minutes_to_alarm = (mSchedule.mMins[TEMPORARY] & 0xff) +
                (mSchedule.mHours[TEMPORARY] & 0xff) * 60 -
                (mClock.mMins[TEMPORARY] & 0xff) -
                (mClock.mHours[TEMPORARY] & 0xff) * 60;

        if (minutes_to_alarm >= 0)
            minutes_to_alarm += ((mSchedule.mAlarms[TEMPORARY] & 0xff) - 1) * 1440;
        else
            minutes_to_alarm += (mSchedule.mAlarms[TEMPORARY] & 0xff) * 1440;

        Calendar now = Calendar.getInstance();
        now.add(Calendar.MINUTE, minutes_to_alarm);
        dateandtime[TEMPORARY] = now.getTimeInMillis();
    }

    static int getProgramHour() {
        Calendar c = Calendar.getInstance();
        if (therapy[CURRENT] != 0) {
            c.setTimeInMillis(dateandtime[CURRENT]);
            return c.get(Calendar.HOUR_OF_DAY);
        } else {
            return c.get(Calendar.HOUR_OF_DAY);
        }
    }

    static int getProgramMinute() {
        Calendar c = Calendar.getInstance();
        if (therapy[CURRENT] != 0) {
            c.setTimeInMillis(dateandtime[CURRENT]);
            return c.get(Calendar.MINUTE);
        } else {
            return c.get(Calendar.MINUTE);
        }
    }

    @SuppressLint("DefaultLocale")
    static String getTime() {
        if (therapy[CURRENT] != 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(dateandtime[CURRENT]);

            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            String amPm;
            if (hour < 12) {
                amPm = "AM";
                if (hour == 0) {
                    hour = 12;
                }
            } else {
                amPm = "PM";
                if (hour > 12) {
                    hour -= 12;
                }
            }

            return String.format("%02d:%02d %s", hour, minute, amPm);
        } else
            return "---";
    }

    static boolean isITNSNew() {
        if (mModelSerial[CURRENT] != mModelSerial[TEMPORARY])
            return true;
        else
            return false;
    }
}