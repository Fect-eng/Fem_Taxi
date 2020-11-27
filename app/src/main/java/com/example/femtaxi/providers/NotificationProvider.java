package com.example.femtaxi.providers;

import com.example.femtaxi.models.FCMRequest;
import com.example.femtaxi.models.FCMResponse;
import com.example.femtaxi.retrofit.IFCMApi;
import com.example.femtaxi.retrofit.RetrofitUser;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMRequest fcmRequest) {
        return RetrofitUser.getClientObject(url).create(IFCMApi.class).send(fcmRequest);
    }
}
