package pe.com.android.femtaxi.driver.registerDriver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.registroDriver1;
import pe.com.android.femtaxi.models.registroDriver2;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.HashMap;

public class RegistroDriverSegundoActivity extends AppCompatActivity {
    Toolbar mToolbar;
    TextInputEditText numeroplaca;
    TextInputEditText numerocarro;
    TextInputEditText modelocarro;
    TextInputEditText tipocarro;
    Button btnConductorcar;
    pe.com.android.femtaxi.models.registroDriver2 registroDriver2;
    FirebaseAuth auth;
    private Button AlertBtn;
    Button btninfo;
    Button btnnextmagen;
    registroDriver1 mRegistroDriver1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_driver_segundo);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Registro Conductora");

        mRegistroDriver1 = (registroDriver1) getIntent().getSerializableExtra(Constants.Extras.EXTRA_DRIVE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        numeroplaca = findViewById(R.id.txtnumeroplaca);
        numerocarro = findViewById(R.id.txtnumerocarro);
        modelocarro = findViewById(R.id.txtmodelocarro);
        tipocarro = findViewById(R.id.txttipocarro);

        btninfo = findViewById(R.id.btninfo);
        btninfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(RegistroDriverSegundoActivity.this);
                alerta.setMessage("Buen dia Tenga Usted estimada Conductora, si usted es Personal Externo o desea usar la Aplicacion de Manera Conductora por favor Sirvase a Comunicar al siguiente numero de Empresa FEMTaxi para Mayor Información +51 941174386")  //ver si se cambia esta Opcion
                        .setCancelable(false)
                        .setPositiveButton("Se Agradece la Atención, La Gerencia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btninfo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //nada
                                    }
                                });
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Información Personal Externo");
                titulo.show();
            }
        });

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
        return true;
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
                        moveToSendImage();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void moveToSendImage() {
        Intent intent = new Intent(RegistroDriverSegundoActivity.this, UploadPolicialActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_DRIVE, (Serializable) mRegistroDriver1);
        startActivity(intent);
    }

    private HashMap<String, Object> dataFirebase() {
        HashMap<String, Object> sendFirebase = new HashMap<>();
        sendFirebase.put("Numero_Placa", registroDriver2.getNumeroplaca());
        sendFirebase.put("Modelo_carro", registroDriver2.getModelocarro());
        sendFirebase.put("Tipo_Carro", registroDriver2.getTipocarro());
        sendFirebase.put("Numero_Carro", registroDriver2.getNumerocarro());
        sendFirebase.put("user_id", mRegistroDriver1.getId());
        return sendFirebase;
    }
}