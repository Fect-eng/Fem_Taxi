package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.models.ClientBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.crypto.MacSpi;

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

    public Task<Void> create(ClientBooking clientBooking) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("idHistory", clientBooking.getIdHistory());
        newStatus.put("destination", clientBooking.getDestination());
        newStatus.put("destinationLat", clientBooking.getDestinationLat());
        newStatus.put("destinationLng", clientBooking.getDestinationLat());
        newStatus.put("idCliente", clientBooking.getIdClient());
        newStatus.put("idDriver", clientBooking.getIdDriver());
        newStatus.put("km", clientBooking.getKm());
        newStatus.put("Origin", clientBooking.getOrigin());
        newStatus.put("OriginLat", clientBooking.getOriginLat());
        newStatus.put("OriginLong", clientBooking.getOriginLong());
        newStatus.put("status", clientBooking.getStatus());
        newStatus.put("time", clientBooking.getStatus());

        return dbFireBase.collection(Constans.CLIENT_BOOKING)
                .document(clientBooking.getIdClient())
                .set(newStatus);
        //return dbFireBase.collection(clientBooking.getIdClient()).setValue(clientBooking);

    }
}
