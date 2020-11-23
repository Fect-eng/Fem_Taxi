package com.example.femtaxi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class OpcionDualDriverActivity extends AppCompatActivity {
    //toolbar declarado
    Toolbar mToolbar;
    EditText txtUsuario, txtPassword;  //registro usemos este variable de diferente manera
    Button btnlogearDriver;
    Button botonDualRegistro;
    private Button bntMensajeB;   //intento de alertdialog salio exitoso
    private Button authenticarLog; //authenticacion log

        //pruebas exitosas
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcion_dual_driver);
        //variables
        //tollabr
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Menu Principal");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //fin de tolbar

        txtUsuario=findViewById(R.id.txtUsuario);
        txtPassword=findViewById(R.id.txtPassword);
        //fin de variables
        btnlogearDriver=findViewById(R.id.btnlogearDriver);
        //===========================================================================
        btnlogearDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  // http://localhost/xampp/Geo_Femtaxi/Inicio.php   // "http://192.168.163.2/Geo_femtaxi/conexion_Mysql/validarUser_app.php"
               // validarUsuario("http://192.167.1.105:80/xampp/Geo_Femtaxi/conexion_Mysql/validarUser_app.php");
                //validacion
                if (txtUsuario.getText().toString().isEmpty()){
                    Toast.makeText(OpcionDualDriverActivity.this, "Ingrese Email Fem Taxi", Toast.LENGTH_SHORT).show();
                    validarCampos();  //busca validar campo esd un if que da el color rojo
                }else{
                    if (txtPassword.getText().toString().isEmpty()){
                        Toast.makeText(OpcionDualDriverActivity.this, "Ingrese Contrasena", Toast.LENGTH_SHORT).show();
                        validarCampos();
                    }else{  //else de selccion aca esta la consulta
                        bntMensajeB = (Button) findViewById(R.id.btnlogearDriver);
                        bntMensajeB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder alerta = new AlertDialog.Builder(OpcionDualDriverActivity.this);
                                alerta.setMessage("Esta seguro de los datos Ingresados")
                                        .setCancelable(false) // true es para que se salte el no
                                        .setPositiveButton("si", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                //finish();
                                                // validarUsuario("https://keraph.000webhostapp.com/public/appLogin");
                                                validarUsuario("https://keraph.000webhostapp.com/public/appLogin");  //=========
                                                txtPassword.setText(""); //borramos los edit text
                                                txtUsuario.setText("");
                                            }
                                        })
                                        .setNegativeButton("no", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel(); //cancela
                                            }
                                        });
                                AlertDialog titulo = alerta.create();
                                titulo.setTitle("Cuenta Conductor");
                                titulo.show();
                            }
                        });
                        //=================================================================


                    }
                }
            }
        });
        //=======================================================================================================

        botonDualRegistro = findViewById(R.id.botonDualRegistro); //hacemos uso de la primera variable
        botonDualRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View view) {
                goToSelectAuth(); //creamos el methodo para proseguir
            }
        });

    }  //final de Oncreate

    public void btnlogearDriver(View v)
    {
        //txtUsuario, txtPassword;
        if (txtUsuario.getText().toString().isEmpty()){
            Toast.makeText(this, "Ingrese Email FemTaxi", Toast.LENGTH_SHORT).show();
        }else{
            if (txtPassword.getText().toString().isEmpty()){
                Toast.makeText(this, "Ingrese Password", Toast.LENGTH_SHORT).show();
            }
        }
       /* if (validarCampos())
        {
            Toast.makeText(this, "Ingreso Datos", Toast.LENGTH_SHORT).show();
        }*/
    }//fin de else

    public boolean validarCampos()  //es para que apare
    {
        boolean retorno = true;

        String c1 = txtPassword.getText().toString();
        String c2 = txtUsuario.getText().toString();
        if (c1.isEmpty())
        {
            txtPassword.setError("Ingrese Contrasena");
            retorno = false;
        }
        if (c2.isEmpty())
        {
            txtUsuario.setError("Ingrese Email FemTaxi");
            retorno = false;
        }
        return  retorno;
    }
        //========================================================================================================
    //datos que estarian en un service hosting
    private void validarUsuario ( String URL){      //EditText txtPassword, EditText txtUsuario, por si acaso lo vemos usar
        String url1 = URL+"?user="+txtUsuario.getText().toString()+"&pass="+txtPassword.getText().toString();
        StringRequest stringRequest=new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()){
                    Toast.makeText(getApplicationContext(), ""+response, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MapDriverActivity.class);  //si valida te vas al next

                    startActivity(intent);
                }else{
                    Toast.makeText(OpcionDualDriverActivity.this, "Usuario o Contraseña Incorrecta", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(OpcionDualDriverActivity.this,error.toString(), Toast.LENGTH_SHORT).show();  //solo el desarrollador sabe el mensaje
                Log.i("error 401","401"+error.toString());
                Intent intent=new Intent(OpcionDualDriverActivity.this,MainActivity.class);
                startActivity(intent);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> parametros = new HashMap<String, String>();
                parametros.put("usuario",txtUsuario.getText().toString());
                parametros.put("password",txtPassword.getText().toString());
                return parametros;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
        //boton cambio de layout
        //=========================================================================================================
    private void goToSelectAuth() {  //boton registro  RegistroDriverPrimerActivity Eliminar despues de presentacion
        //ConductorOptionDualActivity
        Intent intent = new Intent(  OpcionDualDriverActivity.this, RegistroDriverPrimerActivity.class);
        startActivity(intent);
    }
}

