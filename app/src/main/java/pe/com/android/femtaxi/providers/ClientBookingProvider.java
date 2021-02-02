package pe.com.android.femtaxi.providers;

import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.ClientBooking;
import pe.com.android.femtaxi.utils.Utils;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ClientBookingProvider {

    CollectionReference collectionReference;
    FirebaseFirestore firebaseFirestore;

    public ClientBookingProvider() {
        firebaseFirestore = FirebaseFirestore.getInstance();
        collectionReference = firebaseFirestore
                .collection(Constants.Firebase.Nodo.CLIENT_BOOKING);
    }

    public DocumentReference getClientBooking(String idClient) {
        return collectionReference.document(idClient);
    }

    public Task<Void> getUpdateStatus(String idClient, String status) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("status", status);
        return collectionReference.document(idClient)
                .update(newStatus);
    }

    public Task<Void> getUpdatePrice(String idClient, double price) {
        Map<String, Object> newStatus = new HashMap<>();
        newStatus.put("price", price);
        return collectionReference.document(idClient)
                .update(newStatus);
    }

    public Task<Void> createClentBooking(ClientBooking clientBooking) {
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
        mapClientBooking.put("price", clientBooking.getPrice());

        return collectionReference.document(clientBooking.getIdClient())
                .set(mapClientBooking);
    }

    public Task<Void> deleteClientBooking(String clientId) {
        return collectionReference.document(clientId)
                .delete();
    }
}