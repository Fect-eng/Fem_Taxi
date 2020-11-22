package com.example.femtaxi;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button btnncliente;   //cliente boton
    Button btndriverDual; //driver boton


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btndriverDual = findViewById(R.id.btndriverDual); //hacemos uso de la primera variable
        btndriverDual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSelectAuth(); //creamos el methodo para proseguir
            }
        });
        //============================================================================================
        //============================================================================================
        btnncliente = findViewById(R.id.btnncliente); //hacemos uso de la primera variable
        btnncliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSelectClient(); //creamos el methodo para proseguir
            }
        }); //finde de boton escucha
    }
            //boton cliente
    private void goToSelectClient() {   //ClientAuthentiActivity esta linea colocar
        //cambiaremos mometaneamente por este layout MapClienteActivity   ====== GpsDriverActivity === ClienteOptionActivity
        Intent intent = new Intent(MainActivity.this, MapClienteActivity.class);
        startActivity(intent);
    }

//==========================================================================================================
//==========================================================================================================
    private void goToSelectAuth() {   //Driver
        //cambiaremos mometaneamente por este layout MapDriverActivity   ==== OpcionDualDriverActivity
        //ConductorOptionDualActivity
        //cambiaremos el this hacia MapDriverActivity  OpcionDualDriverActivity  ===========loginActivity
        Intent intent = new Intent(  MainActivity.this, MapDriverActivity.class);
        startActivity(intent);
    }
}