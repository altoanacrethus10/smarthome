package com.example.smarthome.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.smarthome.constants.AppConstants;

public class SmsUtils {
    
    /**
     * Send SMS to a phone number
     */
    public static void sendSms(Context context, String phoneNumber, String message) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "SMS permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(context, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Failed to send SMS: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Send welcome SMS to new user
     */
    public static void sendWelcomeSms(Context context, String phoneNumber, String userName) {
        String message = "Welcome to SmartHome, " + userName + "! " +
                         "Your journey to finding the perfect home in Dar es Salaam starts now. " +
                         "Thank you for joining us!";
        sendSms(context, phoneNumber, message);
    }
    
    /**
     * Send password reset SMS
     */
    public static void sendPasswordResetSms(Context context, String phoneNumber, String resetCode) {
        String message = "SmartHome: Your password reset code is: " + resetCode + 
                         ". This code will expire in 10 minutes.";
        sendSms(context, phoneNumber, message);
    }
    
    /**
     * Send booking confirmation SMS
     */
    public static void sendBookingConfirmationSms(Context context, String phoneNumber, 
                                                   String houseLocation, String moveInDate) {
        String message = "SmartHome: Your booking for " + houseLocation + 
                         " has been confirmed! Move-in date: " + moveInDate + 
                         ". Thank you for using SmartHome!";
        sendSms(context, phoneNumber, message);
    }
}
