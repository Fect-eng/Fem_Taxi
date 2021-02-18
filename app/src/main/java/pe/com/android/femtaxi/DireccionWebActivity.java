package pe.com.android.femtaxi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DireccionWebActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnPagina;
    private final static String FemTaxi_URL = "https://femtaxiarequipa.000webhostapp.com/DataIngresoPersonal.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direccion_web);

        btnPagina = findViewById(R.id.btnPagina);

        btnPagina.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        switch (v.getId()){
            case R.id.btnPagina:
                intent.setData(Uri.parse(FemTaxi_URL));
                startActivity(intent);
            break;
            //agregamos mas si deseamos
        }
    }
}