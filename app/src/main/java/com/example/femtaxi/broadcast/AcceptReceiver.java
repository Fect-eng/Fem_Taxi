package com.example.femtaxi.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.femtaxi.driver.MapDriveBookingActivity;
import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.GeofireProvider;

public class AcceptReceiver extends BroadcastReceiver {

    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider(Constans.DRIVER_ACTIVE);
        mGeofireProvider.removeLocation(mAuthProvider.getId());

        String idClient = intent.getExtras().getString(Constans.Extras.EXTRA_CLIENT_ID);
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.getUpdateStatus(idClient, "Aceptado");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapDriveBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra(Constans.Extras.EXTRA_CLIENT_ID, idClient);
        context.startActivity(intent1);
    }
}
