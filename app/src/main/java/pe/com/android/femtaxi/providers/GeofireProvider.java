package pe.com.android.femtaxi.providers;

import android.util.Log;

import pe.com.android.femtaxi.helpers.Constants;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GeofireProvider {
    String TAG = GeofireProvider.class.getSimpleName();

    private DatabaseReference databaseReference;
    private GeoFire geoFire;

    public GeofireProvider(String nodo) {
        Log.d(TAG, "GeofireProvider nodo: " + nodo);
        databaseReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(nodo);
        geoFire = new GeoFire(databaseReference);
    }


    public void saveLocation(String idDriver, LatLng latLng) {
        geoFire.setLocation(idDriver, new GeoLocation(latLng.latitude, latLng.longitude));
    }

    public void removeLocation(String idDriver) {
        geoFire.removeLocation(idDriver);
    }

    public DatabaseReference isDriverWorking(String idDriver) {
        return FirebaseDatabase.getInstance()
                .getReference()
                .child(Constants.Firebase.Nodo.DRIVER_WORKING)
                .child(idDriver);
    }

    public GeoQuery getActiveDrivers(LatLng latLng, double radius) {
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), radius);
        geoQuery.removeAllListeners();
        return geoQuery;
    }

    public DatabaseReference getDriveLocation(String idDriver) {
        return databaseReference
                .child(idDriver)
                .child("l");
    }

}
