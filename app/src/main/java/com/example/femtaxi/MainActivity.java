package com.example.femtaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.femtaxi.client.LoginClientActivity;
import com.example.femtaxi.client.MapClienteActivity;
import com.example.femtaxi.driver.MapDriverActivity;
import com.example.femtaxi.driver.OpcionDualDriverActivity;
import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.helpers.PreferencesManager;

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

    @Override
    protected void onResume() {
        super.onResume();
        if (new PreferencesManager(this).getIsClient()) {
            Intent intent = new Intent(MainActivity.this, MapClienteActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setAction(Intent.ACTION_RUN);
            intent.putExtra(Constants.Extras.EXTRA_IS_CONNECTED, true);
            startActivity(intent);
            MainActivity.this.finish();
        }
        if (new PreferencesManager(this).getIsDriver()) {
            Intent intent = new Intent(MainActivity.this, MapDriverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setAction(Intent.ACTION_RUN);
            intent.putExtra(Constants.Extras.EXTRA_IS_CONNECTED, true);
            startActivity(intent);
            MainActivity.this.finish();
        }
    }

    private void goToSelectClient() {
        Intent intent = new Intent(MainActivity.this, LoginClientActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }

    private void goToSelectAuth() {
        Intent intent = new Intent(MainActivity.this, OpcionDualDriverActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }
}
