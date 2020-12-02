package com.example.femtaxi.retrofit;

import com.example.femtaxi.models.FCMBody;
import com.example.femtaxi.models.FCMRequest;
import com.example.femtaxi.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAA8NjuTdM:APA91bELYUS8X2wYN4elK6V9KQAmOnPkxipLyZ51laI-5ZxXPpW7Uy12SiJe87LI5GRFVCYJ8Btb43QR9RKMq3PAlUbstmdEe4vMTG2j9yXsvNky93M87C40D1f67NoP41YEwdrE7wtF"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);
}
