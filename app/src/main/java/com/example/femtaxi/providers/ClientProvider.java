package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class ClientProvider {
    FirebaseFirestore firebaseFirestore;

    public DocumentReference getClientId(String clienId) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.CLIENT)
                .document(clienId);
    }

    public ClientProvider() {
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public Task<DocumentSnapshot> getDataUser(FirebaseUser firebaseUser) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.CLIENT)
                .document(firebaseUser.getUid())
                .get();
    }

    public DocumentReference getDataUser(String UId) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.CLIENT)
                .document(UId);
    }

    public Task<Void> setDataUser(String UId, Map<String, Object> user) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.CLIENT)
                .document(UId)
                .set(user);
    }

    public Task<Void> getUpdateDataUser(String UId, Map<String, Object> dataUser) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.CLIENT)
                .document(UId)
                .update(dataUser);
    }
}
