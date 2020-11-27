package com.example.femtaxi.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.example.femtaxi.R;
import com.example.femtaxi.broadcast.AcceptReceiver;
import com.example.femtaxi.broadcast.CancelReceiver;
import com.example.femtaxi.channel.NotificationHelpers;
import com.example.femtaxi.helpers.Constans;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import static android.os.Build.VERSION_CODES.O;

public class MyFirebaseMessagingUser extends FirebaseMessagingService {

    int NOTIFICATION_CODE = 100;

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        String title = data.get("title");
        String body = data.get("body");
        String idClient = data.get("idClient");
        if (title != null) {
            if (Build.VERSION.SDK_INT >= O) {
                if (title.contains("SOLICITUD DE SERVICIO"))
                    showNotificacionApiOreoAction(title, body, idClient);
                else
                    showNotificacionApiOreo(title, body);
            } else {
                if (title.contains("SOLICITUD DE SERVICIO"))
                    showNotificationAllApiAction(title, body, idClient);
                else
                    showNotificationAllApi(title, body);
            }
        }
    }

    @RequiresApi(api = O)
    private void showNotificacionApiOreo(String title, String body) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),
                0,
                new Intent(),
                PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelpers notificationHelpers = new NotificationHelpers(getBaseContext());
        Notification.Builder builder = notificationHelpers.getNotification(
                title, body, pendingIntent, sound
        );
        notificationHelpers.getManager().notify(1, builder.build());
    }

    @RequiresApi(api = O)
    private void showNotificacionApiOreoAction(String title, String body, String idClient) {
        //Aceptar
        Intent accept = new Intent(this, AcceptReceiver.class);
        accept.putExtra(Constans.Extras.EXTRA_CLIENT_ID, idClient);
        PendingIntent pendingIntentAccept = PendingIntent.getBroadcast(this,
                NOTIFICATION_CODE,
                accept,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action acceptAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                pendingIntentAccept
        ).build();

        //cancelar
        Intent cancel = new Intent(this, AcceptReceiver.class);
        accept.putExtra(Constans.Extras.EXTRA_CLIENT_ID, idClient);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this,
                NOTIFICATION_CODE,
                cancel,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action cancelAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                pendingIntentCancel
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelpers notificationHelpers = new NotificationHelpers(getBaseContext());
        Notification.Builder builder = notificationHelpers.getNotificationAction(
                title, body, sound, acceptAction, cancelAction
        );
        notificationHelpers.getManager().notify(2, builder.build());
    }


    private void showNotificationAllApi(String title, String body) {
        PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(),
                0,
                new Intent(),
                PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelpers notificationHelpers = new NotificationHelpers(getBaseContext());
        NotificationCompat.Builder builder = notificationHelpers.getNotificationAllApi(
                title, body, pendingIntent, sound
        );

        notificationHelpers.getManager().notify(1, builder.build());
    }

    private void showNotificationAllApiAction(String title, String body, String idClient) {
        Intent accept = new Intent(this, AcceptReceiver.class);
        accept.putExtra(Constans.Extras.EXTRA_CLIENT_ID, idClient);
        PendingIntent pendingIntentAccept = PendingIntent.getBroadcast(this,
                NOTIFICATION_CODE,
                accept,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action acceptAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Aceptar",
                pendingIntentAccept
        ).build();

        Intent cancel = new Intent(this, CancelReceiver.class);
        accept.putExtra(Constans.Extras.EXTRA_CLIENT_ID, idClient);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this,
                NOTIFICATION_CODE,
                cancel,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                pendingIntentCancel
        ).build();

        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationHelpers notificationHelpers = new NotificationHelpers(getBaseContext());
        NotificationCompat.Builder builder = notificationHelpers.getNotificationAllApiAction(
                title, body, sound, acceptAction, cancelAction
        );

        notificationHelpers.getManager().notify(2, builder.build());
    }
}