package com.example.femtaxi.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.GeofireProvider;

public class CancelReceiver extends BroadcastReceiver {

    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient = intent.getExtras().getString(Constants.Extras.EXTRA_CLIENT_ID);
        mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.getUpdateStatus(idClient, "cancel");

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_ACTIVE);
        mGeofireProvider.removeLocation(mAuthProvider.getId());
    }
}
