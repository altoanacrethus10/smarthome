package com.example.smarthome.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthome.R;
import com.example.smarthome.services.SinchService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;
import java.util.Locale;

public class CallActivity extends AppCompatActivity implements ServiceConnection {

    private static final String TAG = "CallActivity";
    private TextView tvName, tvHouse, tvStatus, tvTimer;
    private FloatingActionButton fabEndCall, fabMute, fabSpeaker, fabAnswer;
    private Call mCall;
    private String mCallId;
    private SinchService.SinchServiceInterface mSinchServiceInterface;
    private boolean mIsIncoming;
    private final Handler mTimerHandler = new Handler();
    private int mSeconds = 0;
    private final Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            mSeconds++;
            int mins = mSeconds / 60;
            int secs = mSeconds % 60;
            tvTimer.setText(String.format(Locale.US, "%02d:%02d", mins, secs));
            mTimerHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        tvName = findViewById(R.id.tv_call_name);
        tvHouse = findViewById(R.id.tv_call_house);
        tvStatus = findViewById(R.id.tv_call_status);
        tvTimer = findViewById(R.id.tv_call_timer);
        fabEndCall = findViewById(R.id.fab_end_call);
        fabMute = findViewById(R.id.fab_mute);
        fabSpeaker = findViewById(R.id.fab_speaker);
        fabAnswer = findViewById(R.id.fab_answer_call);

        mCallId = getIntent().getStringExtra("CALL_ID");
        mIsIncoming = getIntent().getBooleanExtra("IS_INCOMING", false);

        fabEndCall.setOnClickListener(v -> endCall());
        fabAnswer.setOnClickListener(v -> answerCall());
        fabMute.setOnClickListener(v -> toggleMute());
        fabSpeaker.setOnClickListener(v -> toggleSpeaker());

        getApplicationContext().bindService(new Intent(this, SinchService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mSinchServiceInterface = (SinchService.SinchServiceInterface) service;
        if (mSinchServiceInterface != null) {
            mCall = mSinchServiceInterface.getCall(mCallId);
            if (mCall != null) {
                mCall.addCallListener(new SinchCallListener());
                updateUI();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mSinchServiceInterface = null;
    }

    private void updateUI() {
        if (mCall == null) return;
        tvName.setText(String.format("User %s", mCall.getRemoteUserId()));
        
        if (mIsIncoming && mCall.getState() == com.sinch.android.rtc.calling.CallState.INITIATING) {
            tvStatus.setText("Incoming Call...");
            fabAnswer.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setText(mCall.getState().toString());
            fabAnswer.setVisibility(View.GONE);
        }
    }

    private void answerCall() {
        if (mCall != null) {
            mCall.answer();
            fabAnswer.setVisibility(View.GONE);
        }
    }

    private void endCall() {
        if (mCall != null) {
            mCall.hangup();
        }
        finish();
    }

    private void toggleMute() {
        AudioController audioController = mSinchServiceInterface.getAudioController();
        if (audioController != null) {
            // Logic for mute toggle
        }
    }

    private void toggleSpeaker() {
        AudioController audioController = mSinchServiceInterface.getAudioController();
        if (audioController != null) {
            audioController.enableSpeaker();
        }
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallProgressing(Call call) {
            Log.d(TAG, "Call progressing");
            tvStatus.setText("Ringing...");
        }

        @Override
        public void onCallRinging(Call call) {
            Log.d(TAG, "Call ringing");
            tvStatus.setText("Ringing...");
        }

        @Override
        public void onCallAnswered(Call call) {
            Log.d(TAG, "Call answered");
            tvStatus.setText("Answering...");
        }

        @Override
        public void onCallEstablished(Call call) {
            Log.d(TAG, "Call established");
            tvStatus.setText("Connected");
            tvTimer.setVisibility(View.VISIBLE);
            mTimerHandler.postDelayed(mTimerRunnable, 1000);
        }

        @Override
        public void onCallEnded(Call call) {
            Log.d(TAG, "Call ended");
            CallEndCause cause = call.getDetails().getEndCause();
            tvStatus.setText(String.format("Call Ended: %s", cause.toString()));
            mTimerHandler.removeCallbacks(mTimerRunnable);
            mCall = null;
            new Handler().postDelayed(CallActivity.this::finish, 2000);
        }

        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
        }
    }

    @Override
    protected void onDestroy() {
        if (mSinchServiceInterface != null) {
            getApplicationContext().unbindService(this);
        }
        mTimerHandler.removeCallbacks(mTimerRunnable);
        super.onDestroy();
    }
}
