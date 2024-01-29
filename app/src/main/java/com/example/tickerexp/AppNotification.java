package com.example.tickerexp;

import static com.example.tickerexp.CryptoConfig.CHANNEL_ID;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AppNotification extends Application {
    private static NotificationManager notificationManager;
    private static Vibrator vibrator;
    private static AudioManager audioManager;


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Log.d("NOTIF", "Notification services initialized");
    }

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    public static Vibrator getVibrator() {
        return vibrator;
    }

    public static AudioManager getAudioManager() {
        return audioManager;
    }

    public static void updateNotification(int notificationId, String newTitle, String newText) {
        NotificationManager notificationManager = AppNotification.getNotificationManager();
        // Assurez-vous de créer un canal de notification si nécessaire (pour Android O et supérieur)

        Notification updatedNotification = new NotificationCompat.Builder(WatchService.getInstance().getContext(), CryptoConfig.CONST_CHANNEL_ID)
                .setContentTitle(newTitle)
                .setContentText(newText)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        notificationManager.notify(notificationId, updatedNotification);

    }

    public static long sendNotification(String title, String text){
        NotificationManager notificationManager = AppNotification.getNotificationManager();
        Log.d("WatchService", "Vibration: Notification declared");

        // Create a notification channel (if not already created)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Price Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );

            notificationManager.createNotificationChannel(channel);
        }

        // Build and send the notification
        Notification notification = new NotificationCompat.Builder(WatchService.getInstance().getContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        long notificationId = System.currentTimeMillis();
        notificationManager.notify((int) notificationId, notification);
        return notificationId;
    }
    public static void vibrate(){
        // Vibration
        AudioManager audioManager = AppNotification.getAudioManager();

        // Vérifiez si le téléphone est en mode sonnerie ou vibration
        if (audioManager != null && (audioManager.getRingerMode() == AudioManager.RINGER_MODE_VIBRATE || audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL)) {

            Vibrator vibrator = AppNotification.getVibrator();
            if (vibrator != null) {
                long[] pattern = {0, 70, 70, 70}; // Temps de pause, Vibrer, Pause, Vibrer, Pause, Vibrer
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1)); // -1 pour ne pas répéter
                } else {
                    vibrator.vibrate(pattern, -1);
                }
                Log.d("WatchService", "Vibration: Triggered");
            }

        }
    }

    public static void deleteNotification(long notificationId){
        NotificationManager notificationManager = AppNotification.getNotificationManager();
        notificationManager.cancel((int) notificationId);
    }

}
