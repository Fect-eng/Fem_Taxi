package pe.com.android.femtaxi.retrofit;

import pe.com.android.femtaxi.models.FCMResponse;

import pe.com.android.femtaxi.models.PushNotification;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAusFefGI:APA91bEO5ZCzBOFrwqbdpXWpqjYkV0OgFqdza5GkwHD0KDDWRWEvy1rRRjJ1bjJbuxw1Rv_dHQdvqCm2cpB1CYvA5fiXV98WhFB6_m5I0MCpZNdd-AMVqWPeZ3iZTUNEq3_fuLbXdyEM"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body PushNotification pushNotification);
}
