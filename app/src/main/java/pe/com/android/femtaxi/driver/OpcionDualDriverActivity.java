package pe.com.android.femtaxi.driver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import pe.com.android.femtaxi.driver.registerDriver.RegistroDriverPrimerActivity;
import pe.com.android.femtaxi.databinding.ActivityOpcionDualDriverBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.AuthProvider;

public class OpcionDualDriverActivity extends AppCompatActivity {
    String TAG = OpcionDualDriverActivity.class.getSimpleName();
    private AuthProvider mAuthProvider;
    private ProgressDialog mProgressDialog;

    private ActivityOpcionDualDriverBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOpcionDualDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mProgressDialog = new ProgressDialog(this);
        mAuthProvider = new AuthProvider();

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Elegir Opción Conductora");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.btnRegisterDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(OpcionDualDriverActivity.this);
                alerta.setMessage("Bienvenido a Registrarse en Nuestra Empresa FemTaxi, los datos que se solicitara y posterior ingresar seran administrados en confidencialidad por vuestra Gerencia. Sea usted Bienvenido.")  //ver si se cambia esta Opcion
                        .setCancelable(false)
                        .setPositiveButton("Estoy de Acuerdo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                moveToRegisterDriver();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Compromiso Conductor");
                titulo.show();
            }
        });
        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + validarCampos());
                if (validarCampos()) {
                    mProgressDialog.setMessage("Espere un momento por favor...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    mAuthProvider.loginActivity(binding.txtUsuario.getText().toString().trim(),
                            binding.txtPassword.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "onComplete: " + task.isSuccessful());
                                    if (task.isSuccessful()) {
                                        moveToMapDriver();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "onFailure: " + e.getMessage());
                                    mProgressDialog.dismiss();
                                    Toast.makeText(OpcionDualDriverActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
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
        Intent intent = new Intent(OpcionDualDriverActivity.this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }

    private void moveToMapDriver() {
        mProgressDialog.dismiss();
        new PreferencesManager(this).setIsDriver(true);
        Intent intent = new Intent(OpcionDualDriverActivity.this, MapDriverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_RUN);
        intent.putExtra(Constants.Extras.EXTRA_IS_CONNECTED, false);
        startActivity(intent);
        OpcionDualDriverActivity.this.finish();
    }
}

