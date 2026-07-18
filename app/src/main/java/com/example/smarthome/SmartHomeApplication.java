package com.example.smarthome;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.FirebaseApp;
import com.example.smarthome.utils.SharedPrefManager;

public class SmartHomeApplication extends Application {
    private static final String TAG = "SmartHomeApplication";
    private static SmartHomeApplication instance;
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Initialize Firebase
        try {
            FirebaseApp.initializeApp(this);
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed. Check google-services.json");
        }

        // Apply theme early
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        if (sharedPrefManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    public static SmartHomeApplication getInstance() {
        return instance;
    }
    
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }
}
