package com.example.smarthome.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.example.smarthome.utils.NotificationUtils;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getDisplayMessageBody();

                        Log.d(TAG, "SMS from: " + sender + " - " + messageBody);

                        // Handle incoming SMS
                        handleIncomingSms(context, sender, messageBody);
                    }
                }
            }
        }
    }

    private void handleIncomingSms(Context context, String sender, String message) {
        // Check if it's from SmartHome
        if (message.contains("SmartHome") || sender.contains("SMARTHOME")) {
            // Process SmartHome SMS
            processSmartHomeSms(context, sender, message);
        }
    }

    private void processSmartHomeSms(Context context, String sender, String message) {
        // Show notification for incoming SMS
        NotificationUtils.showNotification(
            context,
            "New SMS from SmartHome",
            message.length() > 50 ? message.substring(0, 50) + "..." : message
        );

        // Parse SMS content
        if (message.contains("booking") || message.contains("Booking")) {
            // Booking confirmation SMS
            String[] parts = message.split("\n");
            for (String part : parts) {
                if (part.contains("House") || part.contains("Location")) {
                    // Extract house info
                    String houseInfo = part.replace("House:", "").trim();
                    NotificationUtils.showBookingConfirmedNotification(context, houseInfo);
                    break;
                }
            }
        } else if (message.contains("password reset") || message.contains("reset code")) {
            // Password reset SMS
            // Extract code if present
            String code = extractCode(message);
            if (code != null) {
                // Store code for verification
                // In a real app, you might want to save this for verification
                Log.d(TAG, "Reset code: " + code);
            }
        } else if (message.contains("welcome") || message.contains("Welcome")) {
            // Welcome SMS
            Toast.makeText(context, "Welcome to SmartHome!", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractCode(String message) {
        // Look for 6-digit code
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\b\\d{6}\\b");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    /**
     * Send SMS response (for auto-reply)
     */
    private void sendSmsResponse(Context context, String phoneNumber, String message) {
        // In a real app, you might want to auto-reply
        // com.example.smarthome.utils.SmsUtils.sendSms(context, phoneNumber, message);
    }
}
