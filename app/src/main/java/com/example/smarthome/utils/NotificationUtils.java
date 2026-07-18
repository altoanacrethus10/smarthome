package com.example.smarthome.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.smarthome.R;
import com.example.smarthome.activities.MainActivity;

public class NotificationUtils {
    private static final String CHANNEL_ID = "smarthome_channel";
    private static final String CHANNEL_NAME = "SmartHome Notifications";
    private static final String CHANNEL_DESC = "Notifications from SmartHome app";
    private static final int NOTIFICATION_ID = 1001;

    /**
     * Create notification channel (required for Android O and above)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show a notification
     */
    public static void showNotification(Context context, String title, String message) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        // Create intent to open app when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message));

        manager.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Show new listing notification
     */
    public static void showNewListingNotification(Context context, String houseTitle, String location) {
        String title = "New House Available!";
        String message = houseTitle + " is now available in " + location;
        showNotification(context, title, message);
    }

    /**
     * Show booking confirmation notification
     */
    public static void showBookingConfirmedNotification(Context context, String houseTitle) {
        String title = "Booking Confirmed!";
        String message = "Your booking for " + houseTitle + " has been confirmed.";
        showNotification(context, title, message);
    }

    /**
     * Show booking request notification (for owner)
     */
    public static void showBookingRequestNotification(Context context, String tenantName, String houseTitle) {
        String title = "New Booking Request!";
        String message = tenantName + " wants to book " + houseTitle;
        showNotification(context, title, message);
    }

    /**
     * Show message notification
     */
    public static void showMessageNotification(Context context, String senderName, String message) {
        String title = "New Message from " + senderName;
        showNotification(context, title, message);
    }

    /**
     * Show reminder notification
     */
    public static void showReminderNotification(Context context, String title, String message) {
        showNotification(context, title, message);
    }

    /**
     * Cancel all notifications
     */
    public static void cancelAllNotifications(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.cancelAll();
        }
    }
}
