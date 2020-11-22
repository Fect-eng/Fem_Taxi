package com.example.femtaxi;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class AuthProvider {

    FirebaseAuth mAuth;
    public AuthProvider() {mAuth = FirebaseAuth.getInstance();}

    public Task<AuthResult> register (String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public Task<AuthResult>loginActivity(String email, String password) {
        return mAuth.signInWithEmailAndPassword(email, password);
    }

    public void logout(){ mAuth.signOut();}

    public String getId() {
        return  mAuth.getCurrentUser().getUid();
    }
        //no aparecen estos public
   /* public String getId(){
        return mAuth.getCurrentUser().getUid();
    }*/

    public boolean existSession() {    //esto deberi verse en mapDriver
        boolean exist = false;
        if (mAuth.getCurrentUser() != null) {
            exist = true;
        }
        return exist;
    }
}
