package pe.com.android.femtaxi.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.HashMap;
import java.util.Map;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityLoginClienteBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.models.Client;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientProvider;
import pe.com.android.femtaxi.providers.TopicProvider;

public class LoginClientActivity extends AppCompatActivity {
    String TAG = LoginClientActivity.class.getSimpleName();
    private ActivityLoginClienteBinding binding;
    private GoogleSignInOptions gso;
    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;
    private TopicProvider mTopicProvider;
    private ProgressDialog mProgressDialog;
    private ActivityResultLauncher<Intent> signInWithGoogleResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityLoginClienteBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        FirebaseApp.initializeApp(this);
        initGoogle();
        mProgressDialog = new ProgressDialog(this);
        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();
        mTopicProvider = new TopicProvider();

        binding.btnLoginGoogle.setOnClickListener((v) -> {
            signInWithGoogle();
        });

        signInWithGoogleResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), (resultCode) -> {
            Log.i(TAG, "registerForresult requestCode: " + resultCode);
            if (resultCode.getResultCode() != Activity.RESULT_OK) {
                mProgressDialog.dismiss();
                Toast.makeText(LoginClientActivity.this, "Error desconocido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (resultCode.getData() != null) {
                Log.d(TAG, "registerForresult data != null");
                Task<GoogleSignInAccount> completedTask = GoogleSignIn.getSignedInAccountFromIntent(resultCode.getData());
                Log.d(TAG, "registerForresult completedTask: " + completedTask.toString());
                if (completedTask.isSuccessful()) {
                    try {
                        GoogleSignInAccount account = completedTask.getResult(ApiException.class);
                        Log.d(TAG, "registerForresult account: " + account.toString());
                        signInWithCredential(account);
                    } catch (ApiException e) {
                        e.printStackTrace();
                        Log.d(TAG, "registerForresult ApiException: " + e.getMessage());
                    }
                } else {
                    Log.d(TAG, "registerForresult error: ");
                }
            } else {
                mProgressDialog.dismiss();
                Toast.makeText(LoginClientActivity.this, "Sin informacion para mostrar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initGoogle() {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
    }

    private void signInWithGoogle() {
        Log.i(TAG, "signInWithGoogle");
        mProgressDialog.setMessage("Espere un momento por favor...");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        Log.i(TAG, "signInWithGoogle mGoogleSignInClient: " + mGoogleSignInClient.toString());
        final Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Log.i(TAG, "signInWithGoogle signInIntent: " + signInIntent);
        signInWithGoogleResult.launch(signInIntent);
    }

    public void signInWithCredential(GoogleSignInAccount acct) {
        Log.d(TAG, "signInWithCredential acct: " + acct.toString());
        if (acct.getIdToken() != null) {
            AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
            if (credential != null) {
                mAuthProvider.signInWithCredential(credential)
                        .addOnCompleteListener(this, (task) -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "signInWithCredential acct: " + task.toString());
                                getDataUser(mAuthProvider.getCurrentUser());
                            } else {
                                Log.d(TAG, "subscrito ocurrio un error: ");
                            }
                        });
            } else {
                Log.d(TAG, "subscrito credential es null: ");
            }
        } else {
            Log.d(TAG, "subscrito acct.getIdToken() es null: ");
        }
    }

    public void getDataUser(FirebaseUser firebaseUser) {
        Log.d(TAG, "getDataUser firebaseUser: " + firebaseUser.toString());
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
        mProgressDialog.dismiss();
        new PreferencesManager(this).setIsClient(true);
        mTopicProvider.registerTopic(mAuthProvider.getId())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "subscrito: ");
                });
        Intent intent = new Intent(LoginClientActivity.this, MapClienteActivity.class);
        startActivity(intent);
        LoginClientActivity.this.finish();
    }
}