package com.example.femtaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.femtaxi.client.MapClienteActivity;
import com.example.femtaxi.driver.OpcionDualDriverActivity;

public class MainActivity extends AppCompatActivity {
    Button btnncliente; 
    Button btndriverDual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btndriverDual = findViewById(R.id.btndriverDual);
        btndriverDual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSelectAuth();
            }
        });
        btnncliente = findViewById(R.id.btnncliente);
        btnncliente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSelectClient();
            }
        });
    }
    private void goToSelectClient() {
        Intent intent = new Intent(MainActivity.this, MapClienteActivity.class);
        startActivity(intent);
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(  MainActivity.this, OpcionDualDriverActivity.class);
        startActivity(intent);
    }
}