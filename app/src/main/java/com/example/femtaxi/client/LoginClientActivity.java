package com.example.femtaxi.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femtaxi.R;
import com.example.femtaxi.databinding.ActivityLoginClienteBinding;
import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.models.Client;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientProvider;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;

public class LoginClientActivity extends AppCompatActivity {

    private ActivityLoginClienteBinding binding;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Autenticación Google");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initGoogle();
        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();

        binding.btnLoginGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case 666:
                if (data != null) {
                    Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                    if (completedTask.isSuccessful()) {
                        try {
                            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                            signInWithCredential(account);
                        } catch (ApiException e) {
                            e.printStackTrace();
                        }
                    } else {
                    }
                }
                break;
        }
    }

    private void initGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    private void signInWithGoogle() {
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        final Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 666);
    }

    public void signInWithCredential(GoogleSignInAccount acct) {
        if (acct.getIdToken() != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            if (credential != null) {
                mAuthProvider.signInWithCredential(credential)
                        .addOnCompleteListener(this,
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            getDataUser(mAuthProvider.getCurrentUser());
                                        } else {
                                        }
                                    }
                                });
            } else {
            }
        } else {
        }
    }

    public void getDataUser(FirebaseUser firebaseUser) {
        mClientProvider.getDataUser(firebaseUser.getUid())
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Client client = documentSnapshot.toObject(Client.class);
                        client.setUId(documentSnapshot.getId());
                        moveToMain();
                    } else {
                        Map<String, Object> insert = new HashMap<>();
                        if (!TextUtils.isEmpty(firebaseUser.getDisplayName()))
                            insert.put(Constants.Firebase.Client.NAME, firebaseUser.getDisplayName());
                        if (!TextUtils.isEmpty(firebaseUser.getEmail()))
                            insert.put(Constants.Firebase.Client.EMAIL, firebaseUser.getEmail());
                        if (!TextUtils.isEmpty(firebaseUser.getPhotoUrl().toString()))
                            insert.put(Constants.Firebase.Client.PHOTO, firebaseUser.getPhotoUrl().toString());
                        insert.put(Constants.Firebase.Client.UID, firebaseUser.getUid());

                        setUserInFireStore(firebaseUser.getUid(), insert);
                    }
                });
    }

    public void setUserInFireStore(String UId, Map<String, Object> insert) {
        mClientProvider.setDataUser(UId, insert)
                .addOnSuccessListener((aVoid) -> {
                    moveToMain();
                })
                .addOnFailureListener((e) -> {
                });
    }

    private void moveToMain() {
        Intent intent = new Intent(this, MapClienteActivity.class);
        startActivity(intent);
        finish();
    }
}