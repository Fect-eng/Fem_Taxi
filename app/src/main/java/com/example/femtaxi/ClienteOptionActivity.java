package com.example.femtaxi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ClienteOptionActivity extends AppCompatActivity {
    //toolbar declarado
    Toolbar mToolbar;
    Button autenticarsebtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cliente_option);

        //toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("ANtes de este menu");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        autenticarsebtn = findViewById(R.id.autenticarsebtn); //hacemos uso de la primera variable
        autenticarsebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSelectAuth(); //creamos el methodo para proseguir
            }
        });
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(ClienteOptionActivity.this, ClientAuthentiActivity.class);
        startActivity(intent);
    }
}
