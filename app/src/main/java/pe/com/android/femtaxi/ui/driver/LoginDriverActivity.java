package pe.com.android.femtaxi.ui.driver;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityLoginBinding;
import pe.com.android.femtaxi.ui.admiSoft.OptionDesarrolloActivity;
import pe.com.android.femtaxi.ui.driver.registerDriver.RegistroDriverPrimerActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.TopicProvider;

public class LoginDriverActivity extends AppCompatActivity {
    String TAG = LoginDriverActivity.class.getSimpleName();
    private ActivityLoginBinding binding;
    private ProgressDialog mProgressDialog;
    private AuthProvider mAuthProvider;
    private TopicProvider mTopicProvider;

    Button btnMapaVer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mProgressDialog = new ProgressDialog(this);
        mAuthProvider = new AuthProvider();
        mTopicProvider = new TopicProvider();

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Elegir Opción Conductora");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnMapaVer = findViewById(R.id.btnMapaVer);
        btnMapaVer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                administracion();
            }
        });

        binding.btnRegisterDriver.setOnClickListener((v) -> {
            AlertDialog.Builder alerta = new AlertDialog.Builder(LoginDriverActivity.this);
            alerta.setMessage("Bienvenido a Registrarse en Nuestra Empresa FemTaxi, " +
                    "los datos que se solicitara y posterior ingresar seran administrados en " +
                    "confidencialidad por vuestra Gerencia. Sea usted Bienvenido.")
                    .setCancelable(false)
                    .setPositiveButton("Estoy de Acuerdo", (dialog, which) -> {
                        moveToRegisterDriver();
                    });
            AlertDialog titulo = alerta.create();
            titulo.setTitle("Compromiso Conductor");
            titulo.show();
        });
        binding.btnLogin.setOnClickListener((v) -> {
            Log.d(TAG, "onClick: " + validarCampos());
            if (validarCampos()) {
                mProgressDialog.setMessage("Espere un momento por favor...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();
                mAuthProvider.loginActivity(binding.txtUsuario.getText().toString().trim(),
                        binding.txtPassword.getText().toString().trim())
                        .addOnCompleteListener((task) -> {
                            Log.d(TAG, "onComplete: " + task.isSuccessful());
                            if (task.isSuccessful())
                                moveToMapDriver();
                        })
                        .addOnFailureListener((e) -> {
                            Log.d(TAG, "onFailure: " + e.getMessage());
                            mProgressDialog.dismiss();
                            Toast.makeText(LoginDriverActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void administracion() {
        Intent intent = new Intent(getApplicationContext(), OptionDesarrolloActivity.class);
        startActivity(intent);
    }

    public boolean validarCampos() {
        boolean retorno = true;
        if (binding.txtPassword.getText().toString().isEmpty()) {
            binding.txtPassword.setError("Ingrese Contrasena");
            retorno = false;
        }
        if (binding.txtUsuario.getText().toString().isEmpty()) {
            binding.txtUsuario.setError("Ingrese Email FemTaxi");
            retorno = false;
        }
        return retorno;
    }

    private void moveToRegisterDriver() {
        Intent intent = new Intent(LoginDriverActivity.this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }

    private void moveToMapDriver() {
        mProgressDialog.dismiss();
        new PreferencesManager(this).setIsDriver(true);
        mTopicProvider.registerTopic(mAuthProvider.getId())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "subscrito: ");
                    Intent intent = new Intent(LoginDriverActivity.this, MapDriverActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setAction(Intent.ACTION_RUN);
                    intent.putExtra(Constants.Extras.EXTRA_IS_CONNECTED, false);
                    startActivity(intent);
                    LoginDriverActivity.this.finish();
                });
    }
}

