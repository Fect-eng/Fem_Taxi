package pe.com.android.femtaxi.providers;

import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.HistoryBooking;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

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
        return dbFireBase.collection(Constants.Firebase.Nodo.HISTORY_BOOKING)
                .document(historyBooking.getIdHistory())
                .set(newStatus);
    }

    public Task<Void> getUpdateCalificationClient(String idHistory, float calificationClient) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("calificationClient", calificationClient);
        return dbFireBase.collection(Constants.Firebase.Nodo.HISTORY_BOOKING)
                .document(idHistory)
                .update(newStatus);
    }

    public Task<Void> getUpdateCalificationDriver(String idHistory, float calificationDriver) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("calificationDrive", calificationDriver);
        return dbFireBase.collection(Constants.Firebase.Nodo.HISTORY_BOOKING)
                .document(idHistory)
                .update(newStatus);
    }

    public Task<DocumentSnapshot> getHistoryBooking(String idHistory) {
        return dbFireBase.collection(Constants.Firebase.Nodo.HISTORY_BOOKING)
                .document(idHistory)
                .get();
    }

    public Task<QuerySnapshot> getListHistoryBookingClient(String idClient) {
        return dbFireBase.collection(Constants.Firebase.Nodo.HISTORY_BOOKING)
                .whereEqualTo("idClient", idClient)
                //.orderBy("idHostory", Query.Direction.ASCENDING)
                .get();
    }
    public Task<QuerySnapshot> getListHistoryBookingDriver(String idDriver) {
        return dbFireBase.collection(Constants.Firebase.Nodo.HISTORY_BOOKING)
                .whereEqualTo("idDriver", idDriver)
                //.orderBy("idHostory", Query.Direction.ASCENDING)
                .get();
    }
}
