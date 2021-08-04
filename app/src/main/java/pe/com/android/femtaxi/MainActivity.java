package pe.com.android.femtaxi;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.multidex.MultiDex;

import com.google.firebase.FirebaseApp;
import com.mapbox.mapboxsdk.Mapbox;

import pe.com.android.femtaxi.ui.client.LoginClientActivity;
import pe.com.android.femtaxi.ui.client.MapClienteActivity;
import pe.com.android.femtaxi.ui.driver.MapDriverActivity;
import pe.com.android.femtaxi.ui.driver.LoginDriverActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;

public class MainActivity extends AppCompatActivity {
    Button btnncliente;
    Button btndriverDual;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiDex.install(this);
        btndriverDual = findViewById(R.id.btndriverDual);
        btndriverDual.setOnClickListener((view) -> {
            moveToLoginDriver();
        });
        btnncliente = findViewById(R.id.btnncliente);
        btnncliente.setOnClickListener((view) -> {
            moveToLoginClient();
        });
        Mapbox.getInstance(this, getString(R.string.access_token));
        FirebaseApp.initializeApp(this);
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

    private void moveToLoginClient() {
        Intent intent = new Intent(MainActivity.this, LoginClientActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }


    private void moveToLoginDriver() {
        Intent intent = new Intent(MainActivity.this, LoginDriverActivity.class);
        startActivity(intent);
        MainActivity.this.finish();
    }
}
