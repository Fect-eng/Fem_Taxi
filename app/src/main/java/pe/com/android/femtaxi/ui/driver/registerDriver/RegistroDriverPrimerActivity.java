package pe.com.android.femtaxi.ui.driver.registerDriver;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.registroDriver1;
import pe.com.android.femtaxi.webRegistroActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;

import static android.media.CamcorderProfile.get;


public class RegistroDriverPrimerActivity extends AppCompatActivity implements View.OnClickListener {

    Button txtfecha;
    Toolbar mToolbar;
    TextInputEditText textoNombresCom;
    TextInputEditText textoFechaNac;
    TextInputEditText textoApe;
    TextInputEditText textodni;
    TextInputEditText textodireccion;
    TextInputEditText textocelular;
    TextInputEditText textemail;
    Button btninfo;
    Button btnnextdriver;
    registroDriver1 registroDriver1;
    FirebaseAuth auth;
    private int dia, mes, ano, hora;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_driver_primer);
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Formulario Conductora");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        txtfecha = findViewById(R.id.txtfecha);
        textoNombresCom = findViewById(R.id.textoNombresCom);
        textoApe = findViewById(R.id.textoApe);
        textodni = findViewById(R.id.textodni);
        textodireccion = findViewById(R.id.textodireccion);
        textoFechaNac = findViewById(R.id.textoFechaNac);
        textocelular = findViewById(R.id.textocelular);
        textemail = findViewById(R.id.textemail);


        btninfo = findViewById(R.id.btninfo);
        btninfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(RegistroDriverPrimerActivity.this);
                alerta.setMessage("Buen dia Tenga Usted estimada Conductora, si usted es Personal Externo o desea usar la Aplicacion de Manera Conductora por favor Sirvase a Comunicar al siguiente numero de Empresa FEMTaxi para Mayor Información +51 941174386")  //ver si se cambia esta Opcion
                        .setCancelable(false)
                        .setPositiveButton("Se Agradece la Atención, La Gerencia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btninfo.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                    }
                                });
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Información Personal Externo");
                titulo.show();
            }
        });

        btnnextdriver = findViewById(R.id.btnnextdriver);
        btnnextdriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validacionCamposForm()) {
                    registroDriver1 = getRegisterDriver1();
                    createUserFirebase(registroDriver1.getEmail(), registroDriver1.getDni());


                }
            }
        });
        txtfecha.setOnClickListener(this);
    }

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
        return true;
    }

    private void movetToRegistroSegundo() {//RegistroDriverSegundoActivity
        Intent intent = new Intent(RegistroDriverPrimerActivity.this, webRegistroActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_DRIVE, (Serializable) registroDriver1);
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
                        movetToRegistroSegundo();
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

  //  @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onClick(View v) {
        if (v == txtfecha) {
            final Calendar c  = Calendar.getInstance();
            dia = c.get(Calendar.DAY_OF_MONTH);
            mes = c.get(Calendar.MONTH);
            ano = c.get(Calendar.YEAR);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    textoFechaNac.setText(dayOfMonth+"/"+(monthOfYear+1)+"/"+year);
                }
            }
                    ,dia, mes, ano);
            datePickerDialog.show();
        }
    }
}









