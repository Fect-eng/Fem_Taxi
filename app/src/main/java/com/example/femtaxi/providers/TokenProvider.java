package com.example.femtaxi.providers;

import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.models.Token;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.HashMap;
import java.util.Map;

public class TokenProvider {

    FirebaseFirestore dbFireBase;
    DatabaseReference mDatabase;
    public TokenProvider() {
        dbFireBase = FirebaseFirestore.getInstance();
    }
//123456789
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
                        dbFireBase.collection(Constans.TOKEN)
                                .document(idUser)
                                .set(hashToken);
                    }
                });
    }


    public DocumentReference getTokenUser(String idClient) {
        return dbFireBase.collection(Constans.TOKEN)
                .document(idClient);
    }
}
