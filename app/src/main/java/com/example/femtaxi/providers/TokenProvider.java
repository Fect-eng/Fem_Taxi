package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.models.Token;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class TokenProvider {

    FirebaseFirestore firebaseFirestore;
    public TokenProvider() {
        firebaseFirestore = FirebaseFirestore.getInstance();
    }
    public void createdToken(String idUser) {
        if (idUser.isEmpty())
            return;
        FirebaseInstanceId
                .getInstance()
                .getInstanceId()
                .addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        Token token = new Token(instanceIdResult.getToken());
                        Map<String, Object> hashToken = new HashMap<>();
                        hashToken.put("token", token.getToken());
                        firebaseFirestore.collection(Constants.Firebase.Nodo.TOKEN)
                                .document(idUser)
                                .set(hashToken);
                    }
                });
    }


    public Task<DocumentSnapshot> getTokenUser(String idClient) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.TOKEN)
                .document(idClient)
                .get();
    }
}
