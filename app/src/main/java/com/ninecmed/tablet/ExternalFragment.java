package com.ninecmed.tablet;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Objects;

public class ExternalFragment extends Fragment {
    private static final String TAG = "ExternalFragment";
//    private MainActivity mDashboardFragment = null;
    private DashboardFragment mDashboardFragment = null;
    private int mAmplitudePos = 8;                                                                   // Set default position ot 1.5V
    private long mNow;
    private final Handler mHandler = new Handler();
    private boolean mStimEnabled = false;
    private AlertDialog mAlertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate: starting.");
        View view = inflater.inflate(R.layout.external_fragment, container, false);

        InitializeStimulationButton(view);
        InitializeAmplitudeButton(view);

        // Must redraw the icon for this fragment and not for the itns fragment.
        // Not sure why since both imageviews are visible.
        ImageView iv = view.findViewById(R.id.ivExternalLink);
        iv.setImageResource(R.drawable.ic_link_off);

        mDashboardFragment = (DashboardFragment) getParentFragment();
        return view;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeStimulationButton(View view) {
        final Button stimulate = view.findViewById(R.id.btExternalStartStim);
        stimulate.setEnabled(false);
        stimulate.setAlpha(0.5f);
        stimulate.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
             public boolean onTouch(View view, MotionEvent motionEvent) {
                 switch (motionEvent.getActionMasked()) {
                     case MotionEvent.ACTION_DOWN:
                         if(mNow + 500 < System.currentTimeMillis()) {
                             mDashboardFragment.wandComm.SetStimulationExt(true);
                             WandData.InvalidateStimExtLeadI();
                             UIUpdate(true);
                             MakeTone(ToneGenerator.TONE_PROP_BEEP);
                             SetAmplitudeParameterEnabled(false, false);
                             mDashboardFragment.EnableTabs(false);
                             StartProgressBar();
                             mNow = System.currentTimeMillis();
                             mStimEnabled = true;
                         }
                         break;

                     case MotionEvent.ACTION_UP:
                     case MotionEvent.ACTION_CANCEL:
                         // Only execute code on up/cancel when mStimEnabled is true,
                         // otherwise this means that the user pressed the down key too
                         // quickly and when he let's go, the motion event causes SetTestStimulation
                         // to be executed again even though it wasn't started. This causes an
                         // unnecessary beep as well.
                         if(mStimEnabled) {
                             stimulate.setEnabled(false);
                             // Set delay to 1500 to ensure state machine completes before trying
                             // to cancel the machine. If this is done too quickly, the stimulation
                             // stop command is ignored.
                             if (mNow + 1500 < System.currentTimeMillis()) {
                                 mDashboardFragment.wandComm.SetStimulationExt(false);
                                 MakeTone(ToneGenerator.TONE_PROP_NACK);
                                 StopProgressBar();
                                 mStimEnabled = false;
                             } else {
                                 mHandler.postDelayed(HoldStimulation, mNow + 1500 - System.currentTimeMillis());
                             }
                         }
                         break;
                 }
                 return true;
             }
        });
    }

    private final Runnable HoldStimulation = new Runnable() {
        @Override
        public void run() {
            mDashboardFragment.wandComm.SetStimulationExt(false);
            MakeTone(ToneGenerator.TONE_PROP_NACK);
            StopProgressBar();
            mStimEnabled = false;
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    private void InitializeAmplitudeButton(View view) {
        final ImageButton plus = view.findViewById(R.id.ibExternalPlus);
        plus.setEnabled(false);
        plus.setAlpha(0.5f);

        final ImageButton minus = view.findViewById(R.id.ibExternalMinus);
        minus.setEnabled(false);
        minus.setAlpha(0.5f);

        plus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if(mAmplitudePos < 42) {
                        mAmplitudePos += 1;
                        MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                    }
                    WandData.SetStimAmplitude(mAmplitudePos);
                    WandData.InvalidateStimExtLeadI();
                    UIUpdate(true);
                }
                return true;
            }
        });

        minus.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN ) {
                    if(mAmplitudePos > 0) {
                        mAmplitudePos -= 1;
                        MakeTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD);
                    }
                    WandData.SetStimAmplitude(mAmplitudePos);
                    WandData.InvalidateStimExtLeadI();
                    UIUpdate(true);
                }
                return true;
            }
        });
    }

    public void OnConnected() {
        TextView tv = Objects.requireNonNull(getView()).findViewById(R.id.tvExternalBtStatus);
        tv.setText(getString(R.string.link_msg));

        ImageView iv = getView().findViewById(R.id.ivExternalLink);
        iv.setImageResource(R.drawable.ic_link);

        // Enable and disable controls here
        Button stim = getView().findViewById(R.id.btExternalStartStim);
        stim.setEnabled(true);
        stim.setAlpha(1f);

        SetAmplitudeParameterEnabled(true, true);
    }

    public void OnDisconnected() {
        if(mAlertDialog != null)
            mAlertDialog.dismiss();

        TextView tv = Objects.requireNonNull(getView()).findViewById(R.id.tvExternalBtStatus);
        tv.setText(getString(R.string.no_link_msg));

        ImageView iv = getView().findViewById(R.id.ivExternalLink);
        iv.setImageResource(R.drawable.ic_link_off);

        Button stim = getView().findViewById(R.id.btExternalStartStim);
        stim.setEnabled(false);
        stim.setAlpha(0.5f);

        SetAmplitudeParameterEnabled(false, true);
        StopProgressBar();
        mDashboardFragment.EnableTabs(true);
    }

    @SuppressLint("DefaultLocale")
    public void UIUpdate(boolean success) {
        mDashboardFragment.EnableTabs(true);

        if(mDashboardFragment.wandComm.GetCurrentJob() == WandComm.jobs.SETSTIMEXT) {
            // Re-enable the test stim button) only when UIUpdate is called -
            // meaning that the state machine has finished its tasks
            SetAmplitudeParameterEnabled(true, false);
            Button stimulate = Objects.requireNonNull(getView()).findViewById(R.id.btExternalStartStim);
            stimulate.setEnabled(true);
        }

        if(success) {
            TextView amp = Objects.requireNonNull(getView()).findViewById(R.id.tvExternalAmplitude);
            amp.setText(String.format("%.2f V", WandData.GetAmpFromPos(mAmplitudePos)));

            TextView leadi = getView().findViewById(R.id.tvExternalLeadI);
            leadi.setText(WandData.GetStimLeadI());

            TextView leadr = getView().findViewById(R.id.tvExternalLeadR);
            leadr.setText(WandData.GetStimLeadR());
        }
        else {
            View view = getView();
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(Objects.requireNonNull(view).getContext());

            alertDialog.setTitle(getString(R.string.external_comm_error_title_msg));
            alertDialog.setMessage(getString(R.string.external_comm_error_msg));
            alertDialog.setPositiveButton(getString(R.string.all_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                }
            });
            mAlertDialog = alertDialog.create();
            mAlertDialog.setCancelable(false);
            mAlertDialog.show();
        }
    }

    private void MakeTone(int sound) {
        ToneGenerator tone = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        tone.startTone(sound,150);
        long now = System.currentTimeMillis();
        while((System.currentTimeMillis() - now) < 150);
        tone.release();
    }

    private void StartProgressBar() {
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbExternalStim);
        progressBar.setVisibility(View.VISIBLE);

        TextView tv= getView().findViewById(R.id.tvExternalStimProgress);
        tv.setVisibility((View.VISIBLE));
    }

    private void StopProgressBar() {
        ProgressBar progressBar = Objects.requireNonNull(getView()).findViewById(R.id.pbExternalStim);
        progressBar.setVisibility(View.INVISIBLE);

        TextView tv= getView().findViewById(R.id.tvExternalStimProgress);
        tv.setVisibility((View.INVISIBLE));
    }

    private void SetAmplitudeParameterEnabled(boolean enable, boolean change_alpha) {
        ImageButton minus = Objects.requireNonNull(getView()).findViewById(R.id.ibExternalMinus);
        minus.setEnabled(enable);
        if(change_alpha) minus.setAlpha(enable ? 1f : 0.5f);

        ImageButton plus = getView().findViewById(R.id.ibExternalPlus);
        plus.setEnabled(enable);
        if(change_alpha) plus.setAlpha(enable ? 1f : 0.5f);

        TextView amp = getView().findViewById(R.id.tvExternalAmplitude);
        amp.setEnabled(enable);
        if(change_alpha) amp.setAlpha(enable ? 1f : 0.5f);
    }
}
