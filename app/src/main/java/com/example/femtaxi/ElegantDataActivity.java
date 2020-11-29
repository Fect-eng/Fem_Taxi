package com.example.femtaxi;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ElegantDataActivity extends AppCompatActivity {
    Button llamaragenc;
    Button btnbacking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_elegant_data);
       //btnbacking = findViewById(R.id.btnbacking);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent llamar = new Intent(Intent.ACTION_CALL, Uri.parse("tel:123456879"));
                if(ActivityCompat.checkSelfPermission(ElegantDataActivity.this, Manifest.permission.CALL_PHONE)!=
                        PackageManager.PERMISSION_GRANTED)
                    return;
                startActivity(llamar);
            }
        });
      }


    }


