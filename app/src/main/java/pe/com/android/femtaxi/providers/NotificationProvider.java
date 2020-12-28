package pe.com.android.femtaxi.providers;

import pe.com.android.femtaxi.models.FCMRequest;
import pe.com.android.femtaxi.models.FCMResponse;
import pe.com.android.femtaxi.retrofit.IFCMApi;
import pe.com.android.femtaxi.retrofit.RetrofitUser;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMRequest fcmRequest) {
        return RetrofitUser.getClientObject(url).create(IFCMApi.class).send(fcmRequest);
    }
}
