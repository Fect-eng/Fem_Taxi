package com.example.femtaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.femtaxi.models.registroDriver1;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;


public class RegistroDriverPrimerActivity extends AppCompatActivity {

    //toolbar declarado
    Toolbar mToolbar;
    //==================================================================================
    TextInputEditText textoNombresCom;
    TextInputEditText textoFechaNac;
    //nuevos agregados variables
    TextInputEditText textoApe;
    TextInputEditText textodni;
    TextInputEditText textodireccion;
    TextInputEditText textocelular;
    TextInputEditText textemail;
    //==================================================================================

    //boton insertar data / actualizar
    Button btnInsertar;         //Insertar
    Button btnUpdate;           //Update
    Button btnUpdatePrimer;

    //declaramos el boton para otro layout
    Button btnnextdriver;              //boton de layout que nos dirije a otra layout
    registroDriver1 registroDriver1;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_driver_primer);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Formulario Conductor");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //===================================================================================================================================

        //boton de alert dialog
        textoNombresCom = findViewById(R.id.textoNombresCom);                       //texto nombres completos
        textoApe = findViewById(R.id.textoApe);
        textodni = findViewById(R.id.textodni);
        textodireccion = findViewById(R.id.textodireccion);
        textoFechaNac = findViewById(R.id.textoFechaNac);                          //Fecha Nacimiento    cambiamos variable en layout
        textocelular = findViewById(R.id.textocelular);
        textemail = findViewById(R.id.textemail);


        btnnextdriver = findViewById(R.id.btnnextdriver); //hacemos uso de la primera variable
        btnnextdriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validacionCamposForm()) {
                    registroDriver1 = getRegisterDriver1();
                    createUserFirebase(registroDriver1.getEmail(), registroDriver1.getDni());
                    //eliminacion de editet

                } /*else {
                    textoNombresCom.setText("");
                    textoApe.setText("");
                    textodni.setText("");
                    textodireccion.setText("");
                    textoFechaNac.setText("");
                    textocelular.setText("");
                    textemail.setText("");
                }*/
            }
        });


        //========Fin de boton=============================================================================================================================================
        // textoDireccionActual=(TextInputEditText)findViewById(R.id.textoDireccionActual);             // Direccion actual

        //  btnInsertar = (Button) findViewById(R.id.btnInsertar);                                         //btnInsertar
        // btnInsertar.setOnClickListener(new View.OnClickListener() {

        //     @Override
        //  public void onClick(View v) {

        //       ejecutarServicio("http://192.168.56.1:80/Geo_Femtaxi/conexion_MySql/insertarData.php");
        //      //ejecutarServicio("http://192.168.56.1:80/femtaxi/public/conductor/store");
        //      //falta aunrevisar algunas cosas para el crud
        //  }
        //});

//==========================================================================================================================================

    }//oncreate no eliminarsh

    public boolean validacionCamposForm() {
        if (textoNombresCom.getText().toString().isEmpty()) {
            textoNombresCom.setError("Nombre Completo");
            return false;
        }
        if (textoApe.getText().toString().isEmpty()) {
            textoApe.setError("Apellido Completo");
            return false;
        }
        if (textodni.getText().toString().isEmpty()) {
            textodni.setError("Número DNI");
            return false;
        }
        if (textodireccion.getText().toString().isEmpty()) {
            textodireccion.setError("Dirección Actual");
            return false;
        }
        if (textoFechaNac.getText().toString().isEmpty()) {
            textoFechaNac.setError("Fecha Nacimiento");
            return false;
        }
        if (textocelular.getText().toString().isEmpty()) {
            textocelular.setError("Número Celular");
            return false;
        }
        if (textemail.getText().toString().isEmpty()) {
            textemail.setError("Correo FemTaxi");
            return false;
        }
        return true;   //retornamos el valor de retorno
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(RegistroDriverPrimerActivity.this, RegistroDriverSegundoActivity.class);
        intent.putExtra("driver", (Serializable) registroDriver1);
        startActivity(intent);
    }

    private registroDriver1 getRegisterDriver1() {
        return new registroDriver1("",
                textoNombresCom.getText().toString(),
                textoApe.getText().toString(),
                textodni.getText().toString(),
                textodireccion.getText().toString(),
                textoFechaNac.getText().toString(),
                textocelular.getText().toString(),
                textemail.getText().toString());
    }

    private void createUserFirebase(String correo, String password) {
        auth = FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            registroDriver1.setId(firebaseUser.getUid());
                            saveDataUserFirebase();
                            Toast.makeText(RegistroDriverPrimerActivity.this, "Creo usuario exitoso ", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegistroDriverPrimerActivity.this, "error al crear", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegistroDriverPrimerActivity.this, "error al crear" + e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });
    }

    private void saveDataUserFirebase() {
        Toast.makeText(RegistroDriverPrimerActivity.this, "" + dataFirebase(), Toast.LENGTH_LONG).show();
        FirebaseFirestore dbFireBase = FirebaseFirestore.getInstance();
        dbFireBase.collection("Driver")
                .document(registroDriver1.getId())
                .set(dataFirebase())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(RegistroDriverPrimerActivity.this, "Registro exitoso", Toast.LENGTH_LONG).show();
                        goToSelectAuth();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegistroDriverPrimerActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private HashMap<String, Object> dataFirebase() {
        HashMap<String, Object> sendFirebase = new HashMap<>();
        sendFirebase.put("id", registroDriver1.getId());
        sendFirebase.put("name", registroDriver1.getNombres());
        sendFirebase.put("apellido", registroDriver1.getApellidos());
        sendFirebase.put("address", registroDriver1.getDireccion());
        sendFirebase.put("DNI", registroDriver1.getDni());
        sendFirebase.put("fech_nac", registroDriver1.getNacimiento());
        sendFirebase.put("Telefono", registroDriver1.getCelular());
        sendFirebase.put("correo", registroDriver1.getEmail());
        return sendFirebase;
    }
}









