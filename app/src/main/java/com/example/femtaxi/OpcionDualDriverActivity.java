package com.example.femtaxi;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.femtaxi.providers.AuthProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

public class OpcionDualDriverActivity extends AppCompatActivity {
    String TAG = OpcionDualDriverActivity.class.getSimpleName();

    Toolbar mToolbar;
    EditText txtUsuario, txtPassword;
    Button btnlogearDriver;
    Button botonDualRegistro;
    private Button bntMensajeB;
    private Button authenticarLog;
    private AuthProvider mAuthProvider;

        //pruebas exitosas
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcion_dual_driver);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Menu Principal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuthProvider = new AuthProvider();


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
        botonDualRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSelectAuth();
            }
        });
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
        Intent intent = new Intent(getApplicationContext(), MapDriverActivity.class);  //si valida te vas al next
        startActivity(intent);
    }
}

