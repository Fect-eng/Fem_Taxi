package pe.com.android.femtaxi.broadcast;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pe.com.android.femtaxi.ui.driver.MapDriveBookingActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;

public class AcceptReceiver extends BroadcastReceiver {
    String TAG = AcceptReceiver.class.getSimpleName();

    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        String idClient = intent.getExtras().getString(Constants.Extras.EXTRA_CLIENT_ID);
        Log.d(TAG, "onReceive idClient: " + idClient);
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_WORKING);
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.getUpdateStatus(idClient, "accept");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(context, MapDriveBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
        context.startActivity(intent1);
    }
}
