package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.models.ClientBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ClientBookingProvider {

    CollectionReference dbFireBase;

    public ClientBookingProvider() {
        dbFireBase = FirebaseFirestore.getInstance()
                .collection(Constants.Firebase.Nodo.CLIENT_BOOKING);
    }

    public DocumentReference getClientBooking(String idClient) {
        return dbFireBase.document(idClient);
    }

    public Task<Void> getUpdateStatus(String idClient, String status) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("status", status);
        return dbFireBase.document(idClient)
                .update(newStatus);
    }

    public Task<Void> create(ClientBooking clientBooking) {
        Map<String, Object> mapClientBooking = new HashMap<>();
        mapClientBooking.put("idHistory", clientBooking.getIdHistory());
        mapClientBooking.put("destination", clientBooking.getDestination());
        mapClientBooking.put("destinationLat", clientBooking.getDestinationLat());
        mapClientBooking.put("destinationLong", clientBooking.getDestinationLong());
        mapClientBooking.put("idClient", clientBooking.getIdClient());
        mapClientBooking.put("idDriver", clientBooking.getIdDriver());
        mapClientBooking.put("km", clientBooking.getKm());
        mapClientBooking.put("origin", clientBooking.getOrigin());
        mapClientBooking.put("originLat", clientBooking.getOriginLat());
        mapClientBooking.put("originLong", clientBooking.getOriginLong());
        mapClientBooking.put("status", clientBooking.getStatus());
        mapClientBooking.put("time", clientBooking.getTime());

        return dbFireBase.document(clientBooking.getIdClient())
                .set(mapClientBooking);
    }
}
