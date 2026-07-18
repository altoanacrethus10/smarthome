package com.example.smarthome.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.utils.SharedPrefManager;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallController;
import com.sinch.android.rtc.calling.CallControllerListener;
import com.sinch.android.rtc.calling.MediaConstraints;

import java.io.IOException;

public class SinchService extends Service {

    private static final String TAG = "SinchService";
    private final SinchServiceInterface mSinchServiceInterface = new SinchServiceInterface();
    private SinchClient mSinchClient;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPrefManager prefManager = SharedPrefManager.getInstance(getApplicationContext());
        if (prefManager.isLoggedIn()) {
            startSinchClient(prefManager.getUserId());
        }
    }

    private void startSinchClient(String userId) {
        try {
            mSinchClient = SinchClient.builder()
                    .context(getApplicationContext())
                    .userId(userId)
                    .applicationKey(AppConstants.SINCH_APP_KEY)
                    .environmentHost(AppConstants.SINCH_ENVIRONMENT)
                    .build();

            mSinchClient.addSinchClientListener(new SinchClientListener() {
                @Override
                public void onClientStarted(SinchClient client) {
                    Log.d(TAG, "SinchClient started");
                }

                @Override
                public void onClientFailed(SinchClient client, SinchError error) {
                    Log.e(TAG, "SinchClient failed: " + error.toString());
                }

                @Override
                public void onCredentialsRequired(ClientRegistration registration) {
                    // registration.register(JWT);
                }

                @Override
                public void onUserRegistered() {
                    Log.d(TAG, "User registered");
                }

                @Override
                public void onUserRegistrationFailed(SinchError error) {
                    Log.e(TAG, "User registration failed: " + error.toString());
                }

                @Override
                public void onPushTokenRegistered() {
                    Log.d(TAG, "Push token registered");
                }

                @Override
                public void onPushTokenRegistrationFailed(SinchError error) {
                    Log.e(TAG, "Push token registration failed: " + error.toString());
                }

                @Override
                public void onPushTokenUnregistered() {
                    Log.d(TAG, "Push token unregistered");
                }

                @Override
                public void onPushTokenUnregistrationFailed(SinchError error) {
                    Log.e(TAG, "Push token unregistration failed: " + error.toString());
                }

                @Override
                public void onLogMessage(int level, String area, String message) {
                    Log.d(area, message);
                }
            });

            mSinchClient.getCallController().addCallControllerListener(new CallControllerListener() {
                @Override
                public void onIncomingCall(CallController callController, Call call) {
                    Log.d(TAG, "Incoming call: " + call.getCallId());
                    Intent intent = new Intent(SinchService.this, com.example.smarthome.activities.CallActivity.class);
                    intent.putExtra("CALL_ID", call.getCallId());
                    intent.putExtra("IS_INCOMING", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

            mSinchClient.start();
        } catch (IOException e) {
            Log.e(TAG, "Failed to build SinchClient: " + e.getMessage());
        }
    }

    private void stopSinchClient() {
        if (mSinchClient != null) {
            mSinchClient.terminateGracefully();
            mSinchClient = null;
        }
    }

    @Override
    public void onDestroy() {
        stopSinchClient();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mSinchServiceInterface;
    }

    public class SinchServiceInterface extends Binder {
        public Call callUser(String userId) {
            if (mSinchClient != null) {
                return mSinchClient.getCallController().callUser(userId, new MediaConstraints(false));
            }
            return null;
        }

        public Call getCall(String callId) {
            return mSinchClient != null ? mSinchClient.getCallController().getCall(callId) : null;
        }

        public com.sinch.android.rtc.AudioController getAudioController() {
            return mSinchClient != null ? mSinchClient.getAudioController() : null;
        }

        public boolean isStarted() {
            return (mSinchClient != null);
        }

        public void startClient(String userId) {
            startSinchClient(userId);
        }

        public void stopClient() {
            stopSinchClient();
        }
    }
}
