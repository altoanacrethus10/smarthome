package com.example.smarthome.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smarthome.constants.AppConstants;

public class SharedPrefManager {
    private static SharedPrefManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS = "notifications_enabled";
    private static final String KEY_NOTIFICATION_PREVIEW = "notification_preview";
    private static final String KEY_NEW_LISTINGS = "notif_new_listings";
    private static final String KEY_BOOKING_CONFIRMS = "notif_booking_confirms";
    private static final String KEY_PAYMENT_REMINDERS = "notif_payment_reminders";
    private static final String KEY_PROMOTIONS = "notif_promotions";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_BIOMETRIC = "biometric_enabled";
    private static final String KEY_PREFERRED_AREA = "preferred_area";
    private static final String KEY_MEMBER_SINCE = "member_since";
    
    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }
    
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context);
        }
        return instance;
    }

    public void setDarkMode(boolean isEnabled) {
        editor.putBoolean(KEY_DARK_MODE, isEnabled);
        editor.apply();
    }

    public boolean isDarkMode() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setNotificationsEnabled(boolean isEnabled) {
        editor.putBoolean(KEY_NOTIFICATIONS, isEnabled);
        editor.apply();
    }

    public boolean areNotificationsEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATIONS, true);
    }

    public void setNotificationPreviewEnabled(boolean enabled) {
        editor.putBoolean(KEY_NOTIFICATION_PREVIEW, enabled);
        editor.apply();
    }

    public boolean isNotificationPreviewEnabled() {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_PREVIEW, true);
    }

    public void setNewListingsAlertsEnabled(boolean enabled) {
        editor.putBoolean(KEY_NEW_LISTINGS, enabled);
        editor.apply();
    }

    public boolean isNewListingsAlertsEnabled() {
        return sharedPreferences.getBoolean(KEY_NEW_LISTINGS, true);
    }

    public void setBookingConfirmationsEnabled(boolean enabled) {
        editor.putBoolean(KEY_BOOKING_CONFIRMS, enabled);
        editor.apply();
    }

    public boolean isBookingConfirmationsEnabled() {
        return sharedPreferences.getBoolean(KEY_BOOKING_CONFIRMS, true);
    }

    public void setPaymentRemindersEnabled(boolean enabled) {
        editor.putBoolean(KEY_PAYMENT_REMINDERS, enabled);
        editor.apply();
    }

    public boolean isPaymentRemindersEnabled() {
        return sharedPreferences.getBoolean(KEY_PAYMENT_REMINDERS, true);
    }

    public void setPromotionalMessagesEnabled(boolean enabled) {
        editor.putBoolean(KEY_PROMOTIONS, enabled);
        editor.apply();
    }

    public boolean isPromotionalMessagesEnabled() {
        return sharedPreferences.getBoolean(KEY_PROMOTIONS, false);
    }

    public void setLanguage(String language) {
        editor.putString(KEY_LANGUAGE, language);
        editor.apply();
    }

    public String getLanguage() {
        return sharedPreferences.getString(KEY_LANGUAGE, "English");
    }

    public void setCurrency(String currency) {
        editor.putString(KEY_CURRENCY, currency);
        editor.apply();
    }

    public String getCurrency() {
        return sharedPreferences.getString(KEY_CURRENCY, "TZS");
    }

    public void setBiometricEnabled(boolean enabled) {
        editor.putBoolean(KEY_BIOMETRIC, enabled);
        editor.apply();
    }

    public boolean isBiometricEnabled() {
        return sharedPreferences.getBoolean(KEY_BIOMETRIC, false);
    }

    public void setPreferredArea(String area) {
        editor.putString(KEY_PREFERRED_AREA, area);
        editor.apply();
    }

    public String getPreferredArea() {
        return sharedPreferences.getString(KEY_PREFERRED_AREA, "Dar es Salaam");
    }
    
    // Save user login
    public void saveUser(String userId, String name, String email, String phone, String userType) {
        editor.putString(AppConstants.KEY_USER_ID, userId);
        editor.putString(AppConstants.KEY_USER_NAME, name);
        editor.putString(AppConstants.KEY_USER_EMAIL, email);
        editor.putString(AppConstants.KEY_USER_PHONE, phone);
        editor.putString(AppConstants.KEY_USER_TYPE, userType);
        if (!sharedPreferences.contains(KEY_MEMBER_SINCE)) {
            editor.putLong(KEY_MEMBER_SINCE, System.currentTimeMillis());
        }
        editor.putBoolean(AppConstants.KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public long getMemberSince() {
        return sharedPreferences.getLong(KEY_MEMBER_SINCE, System.currentTimeMillis());
    }
    
    // Get user data
    public String getUserId() {
        return sharedPreferences.getString(AppConstants.KEY_USER_ID, null);
    }
    
    public String getUserName() {
        return sharedPreferences.getString(AppConstants.KEY_USER_NAME, null);
    }
    
    public String getUserEmail() {
        return sharedPreferences.getString(AppConstants.KEY_USER_EMAIL, null);
    }
    
    public String getUserPhone() {
        return sharedPreferences.getString(AppConstants.KEY_USER_PHONE, null);
    }
    
    public String getUserType() {
        return sharedPreferences.getString(AppConstants.KEY_USER_TYPE, null);
    }
    
    public String getUserAvatar() {
        return sharedPreferences.getString(AppConstants.KEY_USER_AVATAR, null);
    }
    
    public void setUserAvatar(String avatarUri) {
        editor.putString(AppConstants.KEY_USER_AVATAR, avatarUri);
        editor.apply();
    }

    public void saveFcmToken(String token) {
        editor.putString(AppConstants.KEY_FCM_TOKEN, token);
        editor.apply();
    }

    public String getFcmToken() {
        return sharedPreferences.getString(AppConstants.KEY_FCM_TOKEN, null);
    }
    
    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false);
    }
    
    public boolean isOwner() {
        return AppConstants.USER_TYPE_OWNER.equals(getUserType());
    }
    
    public boolean isTenant() {
        return AppConstants.USER_TYPE_TENANT.equals(getUserType());
    }
    
    // Update user details
    public void updateUser(String name, String phone) {
        editor.putString(AppConstants.KEY_USER_NAME, name);
        editor.putString(AppConstants.KEY_USER_PHONE, phone);
        editor.apply();
    }
    
    // Logout user
    public void logout() {
        boolean darkMode = isDarkMode();
        boolean notificationsEnabled = areNotificationsEnabled();
        String language = getLanguage();
        String preferredArea = getPreferredArea();
        editor.clear();
        editor.putBoolean(KEY_DARK_MODE, darkMode);
        editor.putBoolean(KEY_NOTIFICATIONS, notificationsEnabled);
        editor.putString(KEY_LANGUAGE, language);
        editor.putString(KEY_PREFERRED_AREA, preferredArea);
        editor.apply();
    }
}
