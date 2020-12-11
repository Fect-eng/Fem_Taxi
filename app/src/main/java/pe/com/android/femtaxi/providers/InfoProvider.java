package pe.com.android.femtaxi.providers;

import pe.com.android.femtaxi.helpers.Constants;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class InfoProvider {
    CollectionReference firebaseFirestore;

    public InfoProvider() {
        firebaseFirestore = FirebaseFirestore.getInstance()
                .collection(Constants.Firebase.Nodo.INFO);
    }

    public Task<DocumentSnapshot> getInfo() {
        return firebaseFirestore
                .document(Constants.Firebase.Nodo.INFO)
                .get();
    }

}
