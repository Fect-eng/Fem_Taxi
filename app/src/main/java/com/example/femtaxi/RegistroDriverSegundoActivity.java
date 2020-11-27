package com.example.femtaxi;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.femtaxi.models.registroDriver1;
import com.example.femtaxi.models.registroDriver2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;

public class RegistroDriverSegundoActivity extends AppCompatActivity {
    //declaramos toolbar
    Toolbar mToolbar;
    //declaramos los textEdit
    TextInputEditText numeroplaca;
    TextInputEditText numerocarro;
    TextInputEditText modelocarro;
    TextInputEditText tipocarro;
    //Finde de las declaraciones de variables de inputText
    Button btnnextdriver;
    //declaramos los botones a trabajar
    Button btnDatosPasar;  //boton primero
    Button btnConductorcar;  //Boton Segundo nos iremos a Imagen Completar.
    //Finde de las declaraciones de botones
    com.example.femtaxi.models.registroDriver2 registroDriver2;
    FirebaseAuth auth;
    //Creamos un button ALertaDialog
    private Button AlertBtn;
    Button btnnextmagen; //boton siguiente imagen
    registroDriver1 mRegistroDriver1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_driver_segundo);
        //===========================================================================================
        //codigo de toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Anterior Registro");
        //Finde de codigo de toolbar
        //===========================================================================================

        mRegistroDriver1 = (registroDriver1) getIntent().getSerializableExtra("driver");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        numeroplaca = findViewById(R.id.txtnumeroplaca);
        numerocarro = findViewById(R.id.txtnumerocarro);
        modelocarro = findViewById(R.id.txtmodelocarro);
        tipocarro = findViewById(R.id.txttipocarro);

        btnConductorcar = findViewById(R.id.btnConductorcar);

        btnConductorcar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCamposSegundo()) {
                    registroDriver2 = getRegisterDriver2();
                    saveDataUserFirebase();
                }
            }
        });
    }

    public boolean validarCamposSegundo() {
        if (numeroplaca.getText().toString().isEmpty()) {
            numeroplaca.setError("Numero Placa");
            return false;
        }
        if (numerocarro.getText().toString().isEmpty()) {
            numerocarro.setError("Numero Carro");
            return false;
        }
        if (modelocarro.getText().toString().isEmpty()) {
            modelocarro.setError("Modelo de Carro");
            return false;
        }
        if (tipocarro.getText().toString().isEmpty()) {
            tipocarro.setError("Tipo de Error");
            return false;
        }
        return true;   //retornamos el valor de retorno
    }

    private registroDriver2 getRegisterDriver2() {
        return new registroDriver2(numeroplaca.getText().toString(),
                numerocarro.getText().toString(),
                modelocarro.getText().toString(),
                tipocarro.getText().toString(),

                mRegistroDriver1.getId());
    }

    private void saveDataUserFirebase() {
        FirebaseFirestore dbFireBase = FirebaseFirestore.getInstance();
        dbFireBase.collection("Driver_Car")
                .document(mRegistroDriver1.getId())
                .set(dataFirebase())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        goToSelectAuth();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(RegistroDriverSegundoActivity.this, EnviarImagenActivity.class);
        intent.putExtra("Detalle_Driver", (Serializable) mRegistroDriver1);
        startActivity(intent);
    }

    private HashMap<String, Object> dataFirebase() {
        HashMap<String, Object> sendFirebase = new HashMap<>();
        sendFirebase.put("Numero_Placa", registroDriver2.getNumeroplaca());
        sendFirebase.put("Modelo_carro", registroDriver2.getModelocarro());
        sendFirebase.put("Tipo_Carro", registroDriver2.getTipocarro());
        sendFirebase.put("Numero_Carro", registroDriver2.getNumerocarro());   //adicional
        sendFirebase.put("user_id", mRegistroDriver1.getId());
        return sendFirebase;
    }
}//final de bloque appcompactivity