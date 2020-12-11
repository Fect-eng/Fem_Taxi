package pe.com.android.femtaxi;

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

import pe.com.android.femtaxi.driver.MapDriverActivity;
import pe.com.android.femtaxi.driver.OpcionDualDriverActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class loginActivity extends AppCompatActivity implements View.OnClickListener {
    Toolbar mToolbar;
    Button mButtonDialog;
    private EditText mTextInpuEmail;
    private EditText mTextInputPassword;
    Button registrar;
    Button mButonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mTextInpuEmail = findViewById(R.id.casillaEmail);
        mTextInputPassword = findViewById(R.id.casillaPass);

        mButonLogin = findViewById(R.id.btnLogin);

        mButonLogin.setOnClickListener(this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bienvenido Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mButonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarCamposLogin();
            }
        });
        registrar = findViewById(R.id.registrar);
        mButtonDialog = (Button) findViewById(R.id.registrar);
        mButtonDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(loginActivity.this);
                alerta.setMessage("Bienvenido a Registrarse en Nuestra Empresa FemTaxi, los datos que se solicitara y posterior ingresar seran administrados en confidencialidad por vuestra Gerencia. Sea usted Bienvenido.")  //ver si se cambia esta Opcion
                        .setCancelable(false)
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
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Compromiso Conductor");
                titulo.show();
            }
        });
    }

    private void regeditDrimerPrimeraActividad() {
        Intent intent = new Intent(this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }

    public boolean validarCamposLogin(){    //validar campos vacios y que aparezca un mensajito
        if (mTextInpuEmail.getText().toString().isEmpty()) {
            mTextInpuEmail.setError("Ingrese Correo Electronico");
            return false;
        }
        if (mTextInputPassword.getText().toString().isEmpty()) {
            mTextInputPassword.setError("Ingrese su Contraseña");
        }return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
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
                                    Toast.makeText(loginActivity.this, "Cuenta Registrada", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(loginActivity.this, MapDriverActivity.class);
                                    startActivity(intent);
                                }

                            }
                        });
                break;
        }
    }

   public void signUp (View view){
        Intent intent = new Intent(this, OpcionDualDriverActivity.class);
        startActivity(intent);
    }
    public void RegistroPrimerDriver (View view) {
        Intent intent = new Intent(this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }
}