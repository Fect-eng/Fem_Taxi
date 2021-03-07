package pe.com.android.femtaxi.providers;

import pe.com.android.femtaxi.helpers.Constants;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class DriverProvider {
    FirebaseFirestore firebaseFirestore;

    public DriverProvider() {
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    public Task<DocumentSnapshot> getDataDriver(FirebaseUser firebaseUser) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.DRIVER)
                .document(firebaseUser.getUid())
                .get();
    }

    public DocumentReference getDataUser(String UId) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.DRIVER)
                .document(UId);
    }

    public Task<Void> setDataUser(String UId, Map<String, Object> user) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.DRIVER)
                .document(UId)
                .set(user);
    }

    public Task<Void> getUpdateDataUser(String UId, Map<String, Object> dataUser) {
        return firebaseFirestore.collection(Constants.Firebase.Nodo.DRIVER)
                .document(UId)
                .update(dataUser);
    }
}
