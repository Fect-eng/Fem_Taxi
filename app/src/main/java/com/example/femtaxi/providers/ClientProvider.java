package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constans;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class ClientProvider {
    FirebaseFirestore dbFireBase;

    public ClientProvider() {
        dbFireBase = FirebaseFirestore.getInstance();
    }

    public DocumentReference getClientId(String clienId) {
        return dbFireBase.collection(Constans.CLIENT)
                .document(clienId);
    }
}
