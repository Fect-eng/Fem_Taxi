package pe.com.android.femtaxi.providers;

import android.content.Context;
import android.util.Log;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.retrofit.IGoogleApi;
import pe.com.android.femtaxi.retrofit.RetrofitUser;
import com.google.android.gms.maps.model.LatLng;

import retrofit2.Call;

public class GoogleApiProvider {
    String TAG = GoogleApiProvider.class.getSimpleName();

    private Context context;

    public GoogleApiProvider(Context context) {
        this.context = context;
    }

    public Call<String> getDirections(LatLng origin, LatLng destino) {
        String baseUrl = "https://maps.googleapis.com";
        String query = "/maps/api/directions/json?mode=driving&transit_routing_preferences=less_driving&"
                + "origin=" + origin.latitude + "," + origin.longitude + "&"
                + "destination=" + destino.latitude + "," + destino.longitude + "&"
                + "key=" + context.getResources().getString(R.string.google_api_key);
        Log.d(TAG, "GoogleApiProvider: " + baseUrl + query);
        return RetrofitUser.getClient(baseUrl).create(IGoogleApi.class).getDirections(baseUrl + query);
    }
}
