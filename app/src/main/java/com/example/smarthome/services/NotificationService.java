package com.example.smarthome.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.smarthome.R;
import com.example.smarthome.activities.MainActivity;
import com.example.smarthome.constants.AppConstants;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class NotificationService extends FirebaseMessagingService {
    private static final String TAG = "NotificationService";
    private static final String CHANNEL_ID = "smarthome_fcm_channel";
    private static final String CHANNEL_NAME = "SmartHome Push Notifications";
    private static final int NOTIFICATION_ID = 2001;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body, remoteMessage.getData());
        } else if (remoteMessage.getData().size() > 0) {
            // Handle data payload
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String body = data.get("body");
            showNotification(title, body, data);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Send token to your server
        sendRegistrationToServer(token);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Push notifications from SmartHome");
            channel.enableVibration(true);
            channel.enableLights(true);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String title, String body, Map<String, String> data) {
        if (title == null) title = "SmartHome";
        if (body == null) body = "You have a new notification";

        // Create intent to open app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Add data to intent
        if (data != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_splash_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL);

        // Add action buttons if needed
        if (data != null && data.containsKey("action")) {
            String action = data.get("action");
            if ("booking".equals(action)) {
                builder.addAction(
                    R.drawable.ic_check,
                    "View Booking",
                    pendingIntent
                );
            } else if ("message".equals(action)) {
                builder.addAction(
                    R.drawable.ic_message,
                    "Reply",
                    pendingIntent
                );
            }
        }

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void sendRegistrationToServer(String token) {
        // In a real app, send this token to your server
        // For now, save it locally
        SharedPrefManager.getInstance(this).saveFcmToken(token);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up if needed
    }
}
