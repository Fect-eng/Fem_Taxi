package com.example.femtaxi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.tileprovider.modules.IFilesystemCache;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_CONTACTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class imagenesCelularActivity extends AppCompatActivity {

    private TextView lblPermiso;
    private TextView lblContacto;
    private String estado;
    private final int MY_PERMISSIONS = 100;

    private final int OPEN_CONTACT = 200;

    private final String str_permitido = "PERMITIDO";
    private final String str_denegado = "DENEGADO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagenes_celular);

        estado = getResources().getString(R.string.lblName);
        lblPermiso = (TextView) findViewById(R.id.lblPermiso);

        if (verificarPermiso())
            lblPermiso.setText(estado + " " + str_permitido);
        else
            lblPermiso.setText(estado + " " + str_denegado);
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void on_Click(View view) {
        if (verificarPermiso()) {
            lblPermiso.setText(estado + " " + str_permitido);
            Intent intent = new Intent(Intent.ACTION_CAMERA_BUTTON); //camera button
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE,
                    WRITE_EXTERNAL_STORAGE,
                    CAMERA}, MY_PERMISSIONS);
        }
    }

    // @Override //falta verificar errro
    public void OnActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
             /*   case CAMERA:
                    Uri cameraUri = data.getData();
                    Cursor cursor = getContentResolver().query(cameraUri, null, null, null, null);

                    String nombre = "\n"+"Camara seleccion:"+ "\n";
                    if (cursor.moveToFirst()) {
                      //  nombre = nombre + cursor.getString(cursor.getColumnIndex(Camera.DISPLAY_NAME));
                    }
                    lblPermiso.setText(nombre);
                    break;*/
            }
        }
    }

    //==========================================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permsissions, @NonNull int[] grantResults) {

        if (requestCode == MY_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) ;
            lblPermiso.setText(estado + " " + str_permitido);
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);

        } else {
            lblPermiso.setText(estado + " " + str_denegado);
        }
    }


    //verificar permiso
    public boolean verificarPermiso() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
            return true;

        return false;
    }
}
