package com.example.femtaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientAuthentiActivity extends AppCompatActivity {
    Toolbar mToolbar;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;
    private SignInButton loginGoodle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_authenti);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Autenticación Google");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initGoogle();
        loginGoodle = findViewById(R.id.btn_login_google);

        loginGoodle.setOnClickListener(new View.OnClickListener() {
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
                final FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.signInWithCredential(credential)
                        .addOnCompleteListener(this,
                                new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            FirebaseUser firebaseUser = auth.getCurrentUser();
                                            getDataUser(firebaseUser);
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
        FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseFirestore.collection("nombre de tu tabla en firestore")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        /*User user = documentSnapshot.toObject(User.class);
                        user.setId(documentSnapshot.getId());
                        LogUtils.e(TAG, "getDataUser user: " + user);
                        preferenceManager.setUser(user);
                        preferenceManager.isLoggedIn(true);
                        if (!TextUtils.isEmpty(preferenceManager.getCountry())) {
                            FirebaseMessaging.getInstance()
                                    .subscribeToTopic(preferenceManager.getCountry())
                                    .addOnCompleteListener((task) -> {
                                        LogUtils.e(TAG, "suscrito al canal de reportes: " + task.getResult());
                                    });
                        }

                        if (!TextUtils.isEmpty(preferenceManager.getUId())) {
                            FirebaseMessaging.getInstance()
                                    .subscribeToTopic(preferenceManager.getUId())
                                    .addOnCompleteListener((task) -> {
                                        LogUtils.e(TAG, "suscrito al canal de chat: " + task.getResult());
                                    });
                        }
                        Utils.showProgress(getView().mProgressDialog(), false, null);
                        getView().moveToMain();

                        te lo dejo de guia despues de loguearse con google se revisa firestore si existe o no y somo si existe aca obtienes su data la guardas o tu ves lo que haces y la envias a la siguente pantalla

                        */
                    } else {
                        /*if (mUpdateDataFireStore == null)
                            mUpdateDataFireStore = new HashMap<>();
                        if (!TextUtils.isEmpty(firebaseUser.getDisplayName()))
                            mUpdateDataFireStore.put(Constants.FireBase.User.NAME, firebaseUser.getDisplayName());
                        if (!TextUtils.isEmpty(firebaseUser.getEmail()))
                            mUpdateDataFireStore.put(Constants.FireBase.User.EMAIL, firebaseUser.getEmail());
                        if (!TextUtils.isEmpty(firebaseUser.getPhotoUrl().toString()))
                            mUpdateDataFireStore.put(Constants.FireBase.User.PHOTO, firebaseUser.getPhotoUrl().toString());
                        mUpdateDataFireStore.put(Constants.FireBase.User.RESIDENCE, preferenceManager.getResidence());
                        mUpdateDataFireStore.put(Constants.FireBase.User.COUNTRY, preferenceManager.getCountry());
                        mUpdateDataFireStore.put(Constants.FireBase.User.CODE_PHONE, preferenceManager.getCodPhone());
                        mUpdateDataFireStore.put(Constants.FireBase.User.TOKEN_PUSH, preferenceManager.getToken());

                        //este es el siguiente metodo
                        setUserInFireStore(firebaseUser.getUid());

                        en esta como el usuario es nuevo obtienes la data que te envia google y la guardas en un hashmap como en el ejemplo y ese hash lo envias para registrarlo

                        */
                    }
                });
    }

    public void setUserInFireStore(String UId) {
        //ya aca esta toda la logica para que lo guardes en firestores y lo envias a la siguente patalla te lo dejo de ejemplo tu mismo eres
        //y con eso lo terminas
        /*LogUtils.e(TAG, "setUserInFireStore UId :" + UId);
        FirebaseFirestore mFirebaseFirestore = FirebaseFirestore.getInstance();
        mFirebaseFirestore.collection(Constants.FireBase.ClasesFirebase.USERS)
                .document(UId)
                .set(mUpdateDataFireStore)
                .addOnSuccessListener((aVoid) -> {
                    Iterator it = mUpdateDataFireStore.keySet().iterator();
                    User user = new User();
                    while (it.hasNext()) {
                        String key = it.next().toString();
                        if (mUpdateDataFireStore.get(key) != null) {
                            switch (key) {
                                case Constants.FireBase.User.EMAIL:
                                    user.setEmail(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.NAME:
                                    user.setName(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.PHOTO:
                                    user.setPhoto(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.RESIDENCE:
                                    user.setResidence(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.COUNTRY:
                                    user.setCountry(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.CODE_PHONE:
                                    user.setCodePhone(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.PHONE:
                                    user.setPhone(mUpdateDataFireStore.get(key).toString());
                                    break;
                                case Constants.FireBase.User.TOKEN_PUSH:
                                    user.setTokenPush(mUpdateDataFireStore.get(key).toString());
                                    break;
                            }
                        }
                    }
                    user.setId(UId);
                    preferenceManager.setUser(user);
                    preferenceManager.isLoggedIn(true);
                    if (!TextUtils.isEmpty(preferenceManager.getCountry())) {
                        FirebaseMessaging.getInstance()
                                .subscribeToTopic(preferenceManager.getCountry())
                                .addOnCompleteListener((task) -> {
                                    LogUtils.e(TAG, "suscrito al canal de reportes: " + task.getResult());
                                });
                    }

                    if (!TextUtils.isEmpty(preferenceManager.getUId())) {
                        FirebaseMessaging.getInstance()
                                .subscribeToTopic(preferenceManager.getUId())
                                .addOnCompleteListener((task) -> {
                                    LogUtils.e(TAG, "suscrito al canal de chat: " + task.getResult());
                                });
                    }
                    Utils.showProgress(getView().mProgressDialog(), false, null);
                    getView().moveToMain();
                })
                .addOnFailureListener((e) -> {
                    Utils.showProgress(getView().mProgressDialog(), false, null);
                    preferenceManager.isLoggedIn(false);
                    LogUtils.e(TAG, "onFailure: " + e.getMessage());
                    if (getView() != null)
                        getView().showError("Error al crear el usuario");
                });*/
    }
}
