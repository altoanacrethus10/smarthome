package com.example.smarthome.constants;

import java.util.Arrays;
import java.util.List;

public class AppConstants {
    // Database
    public static final String DATABASE_NAME = "smarthome_db";
    public static final int DATABASE_VERSION = 3;
    
    // Firebase Collections
    public static final String USERS_COLLECTION = "users";
    public static final String HOUSES_COLLECTION = "houses";
    public static final String BOOKINGS_COLLECTION = "bookings";
    public static final String FEEDBACK_COLLECTION = "feedback";
    public static final String COMPLAINTS_COLLECTION = "complaints";
    
    // User Types
    public static final String USER_TYPE_OWNER = "owner";
    public static final String USER_TYPE_TENANT = "tenant";
    
    // Shared Preferences
    public static final String PREF_NAME = "SmartHomePrefs";
    public static final String KEY_USER_ID = "user_id";
    public static final String KEY_USER_NAME = "user_name";
    public static final String KEY_USER_EMAIL = "user_email";
    public static final String KEY_USER_PHONE = "user_phone";
    public static final String KEY_USER_TYPE = "user_type";
    public static final String KEY_IS_LOGGED_IN = "is_logged_in";
    public static final String KEY_USER_AVATAR = "user_avatar";
    
    // Intent Extras
    public static final String EXTRA_HOUSE_ID = "house_id";
    public static final String EXTRA_BOOKING_ID = "booking_id";
    public static final String EXTRA_USER_ID = "user_id";
    
    // Dar es Salaam Locations
    public static final List<String> DAR_LOCATIONS = Arrays.asList(
        "Kinondoni", "Ilala", "Temeke", "Ubungo", 
        "Kigamboni", "Kariakoo", "Mbezi", "Bunju", 
        "Tabata", "Manzese", "Sinza", "Magomeni",
        "Tandale", "Mwananyamala", "Mikocheni", "Masaki",
        "Oyster Bay", "Msasani", "Mlimani City", "Posta"
    );

    // Property Types
    public static final List<String> PROPERTY_TYPES = Arrays.asList(
        "Apartment", "Whole House", "Single Room", "Studio", "Villa", "Bedsitter"
    );
    
    // House Features
    public static final List<String> HOUSE_FEATURES = Arrays.asList(
        "Water Available", "Electricity", "Security Guard",
        "Parking Space", "Fenced Compound", "Floor Tiles",
        "Furnished", "Kitchen", "Balcony", "Garden"
    );
    
    // Request Codes
    public static final int REQUEST_SMS_PERMISSION = 100;
    public static final int REQUEST_CALL_PERMISSION = 101;
    public static final int REQUEST_BLUETOOTH_PERMISSION = 102;
    public static final int REQUEST_LOCATION_PERMISSION = 103;
    public static final int REQUEST_STORAGE_PERMISSION = 104;
    public static final int REQUEST_CAMERA_PERMISSION = 105;
    public static final int REQUEST_IMAGE_PICK = 200;
    public static final int REQUEST_ALL_PERMISSIONS = 106;
    // Add to AppConstants.java
    public static final String KEY_FCM_TOKEN = "fcm_token";
    public static final String FCM_SERVER_KEY = "YOUR_FCM_SERVER_KEY"; // Get from Firebase Console

    // API Keys (Replace with your actual keys)
    public static final String GOOGLE_MAPS_API_KEY = "YOUR_GOOGLE_MAPS_API_KEY";
    public static final String FIREBASE_WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID";

    // Firebase Cloud Messaging
    public static final String FCM_TOPIC = "smarthome_notifications";

    // Receipt Prefix
    public static final String RECEIPT_PREFIX = "RCP-2026-";

    // Sinch App Keys (Get these from Sinch Dashboard: https://dashboard.sinch.com)
    public static final String SINCH_APP_KEY = "YOUR_SINCH_APP_KEY";
    public static final String SINCH_APP_SECRET = "YOUR_SINCH_APP_SECRET";
    public static final String SINCH_ENVIRONMENT = "ocra.api.sinch.com"; // Change based on your Sinch region
}

