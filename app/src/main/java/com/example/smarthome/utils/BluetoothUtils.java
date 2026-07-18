package com.example.smarthome.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.smarthome.constants.AppConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothUtils {
    private static final String TAG = "BluetoothUtils";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
    private static BluetoothAdapter bluetoothAdapter;
    private static BluetoothSocket bluetoothSocket;
    private static OutputStream outputStream;

    /**
     * Check if device supports Bluetooth
     */
    public static boolean isBluetoothSupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);
    }

    /**
     * Check if Bluetooth is enabled
     */
    public static boolean isBluetoothEnabled() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    /**
     * Enable Bluetooth (requires user action)
     */
    public static void enableBluetooth(Context context) {
        if (!isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                context.startActivity(enableBtIntent);
            } catch (SecurityException e) {
                Toast.makeText(context, "Bluetooth permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Get paired devices list
     */
    public static Set<BluetoothDevice> getPairedDevices() {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        if (bluetoothAdapter != null && isBluetoothEnabled()) {
            try {
                return bluetoothAdapter.getBondedDevices();
            } catch (SecurityException e) {
                Log.e(TAG, "Bluetooth permission missing: " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * Share data via Bluetooth
     */
    public static void shareViaBluetooth(Context context, String data) {
        if (!isBluetoothSupported(context)) {
            Toast.makeText(context, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isBluetoothEnabled()) {
            Toast.makeText(context, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
            enableBluetooth(context);
            return;
        }

        // Create intent to share via Bluetooth
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, data);
        shareIntent.setPackage("com.android.bluetooth");
        
        try {
            context.startActivity(shareIntent);
        } catch (Exception e) {
            // Fallback: regular share intent
            Intent fallbackIntent = new Intent(Intent.ACTION_SEND);
            fallbackIntent.setType("text/plain");
            fallbackIntent.putExtra(Intent.EXTRA_TEXT, data);
            context.startActivity(Intent.createChooser(fallbackIntent, "Share via Bluetooth"));
        }
    }

    /**
     * Share house details via Bluetooth
     */
    public static void shareHouseViaBluetooth(Context context, String houseTitle, String location, String price) {
        String data = "SmartHome - House Details\n" +
                      "========================\n" +
                      "Title: " + houseTitle + "\n" +
                      "Location: " + location + "\n" +
                      "Price: " + price + "\n" +
                      "========================\n" +
                      "Find more on SmartHome app!";
        shareViaBluetooth(context, data);
    }

    /**
     * Share receipt via Bluetooth
     */
    public static void shareReceiptViaBluetooth(Context context, String receiptData) {
        shareViaBluetooth(context, receiptData);
    }

    /**
     * Connect to a Bluetooth device (for printing or data transfer)
     */
    public static boolean connectToDevice(BluetoothDevice device) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "Bluetooth permission missing: " + e.getMessage());
            return false;
        } catch (IOException e) {
            Log.e(TAG, "Failed to connect to device: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send data to connected Bluetooth device
     */
    public static void sendData(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                outputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Failed to send data: " + e.getMessage());
            }
        }
    }

    /**
     * Close Bluetooth connection
     */
    public static void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
                outputStream = null;
            }
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
                bluetoothSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to close connection: " + e.getMessage());
        }
    }

    /**
     * Check if Bluetooth permission is granted
     */
    public static boolean hasBluetoothPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
        } else {
            return context.checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED;
        }
    }
}
