package pe.com.android.femtaxi.driver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.RegistroDriverPrimerActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.AuthProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class OpcionDualDriverActivity extends AppCompatActivity {
    String TAG = OpcionDualDriverActivity.class.getSimpleName();
    Button mButtonDialog;
    EditText txtUsuario, txtPassword;
    Button btnlogearDriver;
    Button botonDualRegistro;
    private Button bntMensajeB;
    private Button authenticarLog;
    private AuthProvider mAuthProvider;

    Button mButonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcion_dual_driver);
        mButonLogin = findViewById(R.id.btnlogearDriver);
        mButtonDialog = findViewById(R.id.botonDualRegistro);


        mAuthProvider = new AuthProvider();

        mButtonDialog = (Button) findViewById(R.id.botonDualRegistro);
        mButtonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(OpcionDualDriverActivity.this);
                alerta.setMessage("Bienvenido a Registrarse en Nuestra Empresa FemTaxi, los datos que se solicitara y posterior ingresar seran administrados en confidencialidad por vuestra Gerencia. Sea usted Bienvenido.")  //ver si se cambia esta Opcion
                        .setCancelable(false) // true es para que se salte el no
                        .setPositiveButton("Estoy de Acuerdo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                botonDualRegistro.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        goToSelectAuth();
                                    }
                                });
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Compromiso Conductor");
                titulo.show();
            }
        });

        botonDualRegistro = findViewById(R.id.botonDualRegistro);  //instanciar objeto

        txtUsuario = findViewById(R.id.txtUsuario);
        txtPassword = findViewById(R.id.txtPassword);
        btnlogearDriver = findViewById(R.id.btnlogearDriver);
        btnlogearDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + validarCampos());
                if (validarCampos()) {
                    mAuthProvider.loginActivity(txtUsuario.getText().toString().trim(),
                            txtPassword.getText().toString().trim())
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
                                }
                            });
                }
            }
        });
        botonDualRegistro = findViewById(R.id.botonDualRegistro);
    }


    public void btnlogearDriver(View v) {
        if (txtUsuario.getText().toString().isEmpty()) {
            Toast.makeText(this, "Ingrese Email FemTaxi", Toast.LENGTH_SHORT).show();
        } else {
            if (txtPassword.getText().toString().isEmpty()) {
                Toast.makeText(this, "Ingrese Password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean validarCampos() {
        boolean retorno = true;
        if (txtPassword.getText().toString().isEmpty()) {
            txtPassword.setError("Ingrese Contrasena");
            retorno = false;
        }
        if (txtUsuario.getText().toString().isEmpty()) {
            txtUsuario.setError("Ingrese Email FemTaxi");
            retorno = false;
        }
        return retorno;
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(OpcionDualDriverActivity.this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }

    private void moveToMapDriver() {
        new PreferencesManager(this).setIsDriver(true);
        Intent intent = new Intent(OpcionDualDriverActivity.this, MapDriverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_RUN);
        intent.putExtra(Constants.Extras.EXTRA_IS_CONNECTED, false);
        startActivity(intent);
        OpcionDualDriverActivity.this.finish();
    }
}
