package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constants;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoProvider {
    CollectionReference firebaseFirestore;

    public InfoProvider() {
        firebaseFirestore = FirebaseFirestore.getInstance()
                .collection(Constants.Firebase.Nodo.INFO);
    }

    public CollectionReference getInfo() {
        return firebaseFirestore;
    }

}
