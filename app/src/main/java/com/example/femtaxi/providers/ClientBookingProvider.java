package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constans;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ClientBookingProvider {

    FirebaseFirestore dbFireBase;

    public ClientBookingProvider() {
        dbFireBase = FirebaseFirestore.getInstance();
    }

    public DocumentReference getClientBooking(String idClient) {
        return dbFireBase.collection(Constans.CLIENT_BOOKING)
                .document(idClient);
    }

    public Task<Void> getUpdateStatus(String idClient, String status) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("status", status);
        return dbFireBase.collection(Constans.CLIENT_BOOKING)
                .document(idClient)
                .update(newStatus);
    }

    public Task<Void> getUpdateIdHistoryBooking(String idClient) {
        String idPush = new SimpleDateFormat("HHmmssddmmyyyy").format(
                Calendar.getInstance(Locale.getDefault()).getTime());
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("idHistory", idPush);
        return dbFireBase.collection(Constans.CLIENT_BOOKING)
                .document(idClient)
                .update(newStatus);
    }
}
