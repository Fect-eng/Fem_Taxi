package pe.com.android.femtaxi.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.broadcast.AcceptReceiver;
import pe.com.android.femtaxi.broadcast.CancelReceiver;
import pe.com.android.femtaxi.channel.NotificationHelpers;
import pe.com.android.femtaxi.ui.driver.NotificationBookingActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.models.ServiceNotification;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static android.os.Build.VERSION_CODES.O;

public class MyFirebaseMessagingUser extends FirebaseMessagingService {
    String TAG = MyFirebaseMessagingUser.class.getSimpleName();

    int NOTIFICATION_CODE = 100;
    MediaPlayer mediaPlayer = null;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "onNewToken token: " + token);
        storeRegIdInPref(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.e(TAG, "initNotificationService remoteMessage: " + remoteMessage);
        Map<String, String> dataMap = remoteMessage.getData();
        Log.e(TAG, "initNotificationService dataMap: " + dataMap);
        String to = dataMap.get("to");
        String priority = dataMap.get("priority");
        String title = dataMap.get("title");
        String body = dataMap.get("body");
        double price = Double.parseDouble(dataMap.get("price"));
        Object object = dataMap.get("data");
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("object", object);
        ServiceNotification serviceNotification = new Gson().fromJson((String) object, ServiceNotification.class);

        Log.d(TAG, "onMessageReceived to: " + to);
        Log.d(TAG, "onMessageReceived priority: " + priority);
        Log.d(TAG, "onMessageReceived title: " + title);
        Log.d(TAG, "onMessageReceived body: " + body);
        Log.d(TAG, "onMessageReceived object: " + object);
        Log.d(TAG, "onMessageReceived objectMap: " + objectMap);
        Log.d(TAG, "onMessageReceived serviceNotification: " + serviceNotification);
        if (title != null) {
            if (Build.VERSION.SDK_INT >= O) {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    playSoundNotify(getApplicationContext());
                    Uri sound = RingtoneManager.getDefaultUri(R.raw.notification);
                    showNotificacionApiOreoAction(title, body, serviceNotification.getIdClient(), sound);
                    showNotificationActivity(
                            serviceNotification.getIdClient(),
                            serviceNotification.getAddressOrigin(),
                            serviceNotification.getAddressDestination(),
                            price,
                            serviceNotification.getMinutes(),
                            serviceNotification.getDistance()
                    );
                } else if (title.contains("VIAJE CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    showNotificacionApiOreo(title, body);
                } else {
                    showNotificacionApiOreo(title, body);
                }
            } else {
                if (title.contains("SOLICITUD DE SERVICIO")) {
                    playSoundNotify(getApplicationContext());
                    Uri sound = RingtoneManager.getDefaultUri(R.raw.notification);
                    showNotificationAllApiAction(title, body, serviceNotification.getIdClient(), sound);
                    showNotificationActivity(
                            serviceNotification.getIdClient(),
                            serviceNotification.getAddressOrigin(),
                            serviceNotification.getAddressDestination(),
                            price,
                            serviceNotification.getMinutes(),
                            serviceNotification.getDistance()
                    );
                } else if (title.contains("VIAJE CANCELADO")) {
                    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(2);
                    showNotificationAllApi(title, body);
                } else {
                    showNotificationAllApi(title, body);
                }
            }
        }
    }

    private void storeRegIdInPref(String token) {
        Log.e(TAG, "storeRegIdInPref token: " + token);
        PreferencesManager preferenceManager = new PreferencesManager(getApplication());
        preferenceManager.setToken(token);
    }

    private void showNotificationActivity(String idClient, String origin, String destino,
                                          double price, String minutos, String distance) {
        Log.d(TAG, "showNotificationActivity ");
        Log.d(TAG, "onMessageReceived idClient: " + idClient);
        PowerManager pm = (PowerManager) getBaseContext().getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = pm.isScreenOn();
        if (!isScreenOn) {
            PowerManager.WakeLock wakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK |
                            PowerManager.ACQUIRE_CAUSES_WAKEUP |
                            PowerManager.ON_AFTER_RELEASE,
                    "AppName:MyLock"
            );
            wakeLock.acquire(10000);
        }
        Intent intent = new Intent(getBaseContext(), NotificationBookingActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
        intent.putExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN, origin);
        intent.putExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO, destino);
        intent.putExtra(Constants.Extras.EXTRA_MINUT, minutos);
        intent.putExtra(Constants.Extras.EXTRA_KM, distance);
        intent.putExtra(Constants.Extras.EXTRA_PRICE, price);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @RequiresApi(api = O)
    private void showNotificacionApiOreo(String title, String body) {
        Log.d(TAG, "showNotificacionApiOreo");
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
    private void showNotificacionApiOreoAction(String title, String body, String idClient,
                                               Uri sound) {
        Log.d(TAG, "showNotificacionApiOreoAction ");
        Log.d(TAG, "onMessageReceived idClient: " + idClient);
        //Aceptar
        Intent accept = new Intent(this, AcceptReceiver.class);
        accept.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
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
        Intent cancel = new Intent(this, CancelReceiver.class);
        cancel.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this,
                NOTIFICATION_CODE,
                cancel,
                PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Action cancelAction = new Notification.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                pendingIntentCancel
        ).build();

        //Uri sound = RingtoneManager.getDefaultUri(R.raw.notification);
        NotificationHelpers notificationHelpers = new NotificationHelpers(getBaseContext());
        Notification.Builder builder = notificationHelpers.getNotificationAction(
                title, body, sound, acceptAction, cancelAction
        );
        notificationHelpers.getManager().notify(2, builder.build());
    }


    private void showNotificationAllApi(String title, String body) {
        Log.d(TAG, "showNotificationAllApi ");
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

    private void showNotificationAllApiAction(String title, String body, String idClient,
                                              Uri sound) {
        Log.d(TAG, "onMessageReceived idClient: " + idClient);
        Log.d(TAG, "showNotificationAllApiAction ");
        Intent accept = new Intent(this, AcceptReceiver.class);
        accept.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
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
        cancel.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
        PendingIntent pendingIntentCancel = PendingIntent.getBroadcast(this,
                NOTIFICATION_CODE,
                cancel,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action cancelAction = new NotificationCompat.Action.Builder(
                R.mipmap.ic_launcher,
                "Cancelar",
                pendingIntentCancel
        ).build();

        //Uri sound = RingtoneManager.getDefaultUri(R.raw.notification);
        NotificationHelpers notificationHelpers = new NotificationHelpers(getBaseContext());
        NotificationCompat.Builder builder = notificationHelpers.getNotificationAllApiAction(
                title, body, sound, acceptAction, cancelAction
        );

        notificationHelpers.getManager().notify(2, builder.build());
    }

    public void playSoundNotify(Context context) {
        Log.i(TAG, "playSoundNotify type: ");
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.notification);
            mediaPlayer.setVolume(0.09f, 0.09f);
            mediaPlayer.setOnCompletionListener(mp -> {
                if (mediaPlayer != null) {
                    mediaPlayer.reset();
                    mediaPlayer.release();
                }
                mediaPlayer = null;
            });
            if (!mediaPlayer.isPlaying())
                mediaPlayer.start();
        } catch (Exception ex) {
            Log.d(TAG, "playSoundNotify " + ex.getMessage());
        }
    }
}