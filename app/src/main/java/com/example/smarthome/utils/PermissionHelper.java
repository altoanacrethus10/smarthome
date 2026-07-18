package com.example.smarthome.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.smarthome.constants.AppConstants;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    /**
     * Check if all permissions are granted
     */
    public static boolean hasPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request permissions
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    /**
     * Check and request SMS permission
     */
    public static boolean checkSmsPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.SEND_SMS) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.SEND_SMS},
                AppConstants.REQUEST_SMS_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Check and request Call permission
     */
    public static boolean checkCallPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CALL_PHONE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CALL_PHONE},
                AppConstants.REQUEST_CALL_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Check and request Location permission
     */
    public static boolean checkLocationPermission(Activity activity) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            permissions = new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            };
        }

        if (!hasPermissions(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, AppConstants.REQUEST_LOCATION_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Check and request Storage permission
     */
    public static boolean checkStoragePermission(Activity activity) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            };
        } else {
            permissions = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        if (!hasPermissions(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, AppConstants.REQUEST_STORAGE_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Check and request Camera permission
     */
    public static boolean checkCameraPermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                new String[]{Manifest.permission.CAMERA},
                AppConstants.REQUEST_CAMERA_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Check and request Bluetooth permission
     */
    public static boolean checkBluetoothPermission(Activity activity) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions = new String[]{
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            };
        } else {
            permissions = new String[]{
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN
            };
        }

        if (!hasPermissions(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, AppConstants.REQUEST_BLUETOOTH_PERMISSION);
            return false;
        }
        return true;
    }

    /**
     * Get all permissions needed for the app
     */
    public static String[] getAllPermissions() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
        permissions.add(Manifest.permission.SEND_SMS);
        permissions.add(Manifest.permission.CALL_PHONE);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        } else {
            permissions.add(Manifest.permission.BLUETOOTH);
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        return permissions.toArray(new String[0]);
    }
}
