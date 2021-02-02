package pe.com.android.femtaxi.providers;

import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.FCMResponse;
import pe.com.android.femtaxi.models.PushNotification;
import pe.com.android.femtaxi.retrofit.IFCMApi;
import pe.com.android.femtaxi.retrofit.RetrofitUser;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.Body;

public class NotificationProvider {

    Retrofit retrofit;

    public NotificationProvider() {
        retrofit = RetrofitUser.getClientObject(Constants.URL_FCM);
    }

    public Call<FCMResponse> sendNotification(PushNotification pushNotification) {
        return retrofit.create(IFCMApi.class).send(pushNotification);
    }
}
