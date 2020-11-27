package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.models.HistoryBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HistoryBookingProvider {

    FirebaseFirestore dbFireBase;

    public HistoryBookingProvider() {
        dbFireBase = FirebaseFirestore.getInstance();
    }

    public Task<Void> getCreateHistoryBooking(HistoryBooking historyBooking) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("idHistory", historyBooking.getIdHistory());
        newStatus.put("destination", historyBooking.getDestination());
        newStatus.put("destinationLat", historyBooking.getDestinationLat());
        newStatus.put("destinationLong", historyBooking.getDestinationLong());
        newStatus.put("idClient", historyBooking.getIdClient());
        newStatus.put("idDriver", historyBooking.getIdDriver());
        newStatus.put("km", historyBooking.getKm());
        newStatus.put("origin", historyBooking.getOrigin());
        newStatus.put("originLat", historyBooking.getOriginLat());
        newStatus.put("originLong", historyBooking.getOriginLong());
        newStatus.put("status", historyBooking.getStatus());
        newStatus.put("time", historyBooking.getTime());
        newStatus.put("calificationClient", historyBooking.getCalificationClient());
        newStatus.put("calificationDrive", historyBooking.getCalificationDrive());
        return dbFireBase.collection(Constans.HISTORY_BOOKING)
                .document(historyBooking.getIdHistory())
                .set(newStatus);
    }

    public Task<Void> getUpdateCalificationClient(String idHistory, float calificationClient) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("calificationClient", calificationClient);
        return dbFireBase.collection(Constans.HISTORY_BOOKING)
                .document(idHistory)
                .update(newStatus);
    }

    public Task<Void> getUpdateCalificationDriver(String idHistory, float calificationDriver) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("calificationDrive", calificationDriver);
        return dbFireBase.collection(Constans.HISTORY_BOOKING)
                .document(idHistory)
                .update(newStatus);
    }

    public Task<DocumentSnapshot> getHistoryBooking(String idHistory) {
        return dbFireBase.collection(Constans.HISTORY_BOOKING)
                .document(idHistory)
                .get();
    }
}
