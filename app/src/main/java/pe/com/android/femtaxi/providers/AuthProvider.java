package pe.com.android.femtaxi.providers;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthProvider {

    FirebaseAuth firebaseAuth;

    public AuthProvider() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult> loginActivity(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public String getId() {
        return firebaseAuth.getCurrentUser().getUid();
    }

    public boolean existSession() {
        if (firebaseAuth.getCurrentUser() != null)
            return true;
        return false;
    }

    public Task<AuthResult> signInWithEmailAndPassword(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public Task<AuthResult> signInWithCredential(AuthCredential credential) {
        return firebaseAuth.signInWithCredential(credential);
    }
}
