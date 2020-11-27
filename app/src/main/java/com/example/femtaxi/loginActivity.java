package com.example.femtaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.femtaxi.driver.MapDriverActivity;
import com.example.femtaxi.driver.OpcionDualDriverActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class loginActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar mToolbar;
    Button mButtonDialog;   // Boton de dialog1212121
    private EditText mTextInpuEmail;
    private EditText mTextInputPassword;
    Button registrar;   //boton destinado para un alrteDialog1212121212
    Button mButonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTextInpuEmail = findViewById(R.id.casillaEmail);
        mTextInputPassword = findViewById(R.id.casillaPass);

        mButonLogin = findViewById(R.id.btnLogin);    //para 2 funciones

        mButonLogin.setOnClickListener(this);

        //nuestro toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bienvenido Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //===================================================================================
        mButonLogin.setOnClickListener(new View.OnClickListener() {  //validamos cuando el campo este vacio
            @Override
            public void onClick(View v) {
                validarCamposLogin();
            }
        });
        registrar = findViewById(R.id.registrar);  //instanciar objeto


            //=================================================
        mButtonDialog = (Button) findViewById(R.id.registrar);
        mButtonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(loginActivity.this);
                alerta.setMessage("Bienvenido a Registrarse en Nuestra Empresa FemTaxi, los datos que se solicitara y posterior ingresar seran administrados en confidencialidad por vuestra Gerencia. Sea usted Bienvenido.")  //ver si se cambia esta Opcion
                        .setCancelable(false) // true es para que se salte el no
                        .setPositiveButton("Estoy de Acuerdo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                registrar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        regeditDrimerPrimeraActividad();
                                    }
                                });
                            }
                        });
                       // .setNegativeButton("no", new DialogInterface.OnClickListener() {
                         //   @Override
                           // public void onClick(DialogInterface dialog, int which) {
                             //   dialog.cancel();
                           // }
                        //});
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Compromiso Conductor");
                titulo.show();
            }
        });
        //=================================================


    }//final de onCreate

    private void regeditDrimerPrimeraActividad() {
        Intent intent = new Intent(this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }

    //=================================================================
    //validar campos
    public boolean validarCamposLogin(){    //validar campos vacios y que aparezca un mensajito
        if (mTextInpuEmail.getText().toString().isEmpty()) {
            mTextInpuEmail.setError("Ingrese Correo Electronico");
            return false;
        }
        if (mTextInputPassword.getText().toString().isEmpty()) {
            mTextInputPassword.setError("Ingrese su Contraseña");
        }return true;
    }
    //=================================================================
    @Override
    protected void onStart() {
        super.onStart();
        // FirebaseUser currentUSer = mAuth.getCurrentUser();
        // updateUI(currentUSer);

    } //final de onStart

    @Override
    protected void onStop() {
        super.onStop();
        //aca estas cerrando sei sesion
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                String username = mTextInpuEmail.getText().toString();
                String password = mTextInputPassword.getText().toString();
                FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(username, password)

                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(loginActivity.this, "Ocurrio error Verifique porfavor", Toast.LENGTH_LONG).show();
                                } else {
                                    //aca lo envias a tu siguiente vista
                                    Toast.makeText(loginActivity.this, "Cuenta Registrada", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(loginActivity.this, MapDriverActivity.class);
                                    startActivity(intent);
                                }

                            }
                        });
                break;
        }
    }

/*
    public void login (View view){
    //falta colocar codigo
        String username = mTextInpuEmail.getText().toString();
        String password = mTextInputPassword.getText().toString();
         mAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {
            if (!task.isSuccessful()){
                Toast.makeText(loginActivity.this, "Hubo error", Toast.LENGTH_LONG).show();
            }

             }
         });
    }*/

//no utilizados/*
   public void signUp (View view){
        Intent intent = new Intent(this, OpcionDualDriverActivity.class);
        startActivity(intent);
    }
    public void RegistroPrimerDriver (View view) {
        Intent intent = new Intent(this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }
}
      /*  mButonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        }); //final de bloque */
  /*
    private void login(){
        String email = mTextInpuEmail.getText().toString();
        String password = mTextInputPassword.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){
            if (password.length() >= 6){
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            mDatabase = FirebaseDatabase.getInstance().getReference();
                            Toast.makeText(loginActivity.this, "EL LOgin se realizo Exitosamente", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(loginActivity.this, "La Contraseña o el password son incorrectos", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    } //final de bloque*/