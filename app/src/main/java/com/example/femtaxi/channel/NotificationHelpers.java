package com.example.femtaxi.channel;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.femtaxi.R;

import static android.os.Build.VERSION_CODES.O;

public class NotificationHelpers extends ContextWrapper {

    private String CHANNEL_ID = "com.example.femtaxi";
    private String CHANNEL_NAME = "femtaxi";

    private NotificationManager mNotificationManager;

    public NotificationHelpers(Context base) { //====1
        super(base);
        if (Build.VERSION.SDK_INT >= O)
            createChannel();
    }

    @RequiresApi(api = O)
    private void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.enableLights(true);
        notificationChannel.enableVibration(true);
        notificationChannel.setLightColor(Color.GRAY);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(notificationChannel);
    }

    public NotificationManager getManager() {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

    @RequiresApi(api = O)
    public Notification.Builder getNotification(String title, String body,
                                                PendingIntent pendingIntent,
                                                Uri sound) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_action_notification)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body)
                        .setBigContentTitle(title));

    }

    @RequiresApi(api = O)
    public Notification.Builder getNotificationAction(String title, String body,
                                                      Uri sound,
                                                      Notification.Action acceptAction,
                                                      Notification.Action cancelAction) {
        return new Notification.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_action_notification)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(body)
                        .setBigContentTitle(title));

    }

    public NotificationCompat.Builder getNotificationAllApi(String title, String body,
                                                            PendingIntent pendingIntent,
                                                            Uri sound) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_action_notification)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body)
                        .setBigContentTitle(title));

    }

    public NotificationCompat.Builder getNotificationAllApiAction(String title, String body,
                                                                  Uri sound,
                                                                  NotificationCompat.Action acceptAction,
                                                                  NotificationCompat.Action cancelAction) {
        return new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .addAction(acceptAction)
                .addAction(cancelAction)
                .setSmallIcon(R.drawable.ic_action_notification)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body)
                        .setBigContentTitle(title));

    }
}
