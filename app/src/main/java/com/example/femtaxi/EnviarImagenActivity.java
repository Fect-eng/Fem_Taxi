package com.example.femtaxi;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.femtaxi.databinding.ActivityMapClienteBinding;
import com.example.femtaxi.driver.OpcionDualDriverActivity;
import com.example.femtaxi.models.registroDriver1;
import com.example.femtaxi.utils.FileUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.util.FileUtil;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class EnviarImagenActivity extends AppCompatActivity {
    private Button mUploadBtn;

    private StorageReference mStorage;
    private int GALLERY_INTENT_GLOBAL;
    private int GALLERY_INTENT = 1;         //antecedentes penales
    private int GALLERY_INTENT1 = 2;        //Policiales
    private int GALLERY_INTENT2 = 3;        //DNI
    private int GALLERY_INTENT3 = 4;        //foto con vehiculo
    private int GALLERY_INTENT4 = 5;        //foto con SOAT
    //nuevos agregados

    private ActivityMapClienteBinding binding;
    static final int PERMISSION_CAMERA = 1;
    Toolbar mToolbar;

    private registroDriver1 mRegistroDriver1;
    private ImageView photo, photo1, photo2, photo3, photo4, photo5, photo6, photo7, photo8;
    private ArrayList<Uri> uriArrayList = new ArrayList<>();

    private String[] mPermission = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,};

    private Uri tmpUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enviar_imagen);
        binding = ActivityMapClienteBinding.inflate(getLayoutInflater());

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

      /*  mToolbar = findViewById(R.id.toolbar);
        getSupportActionBar().setTitle("Autenticación Google");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        mStorage = FirebaseStorage.getInstance().getReference();
        mUploadBtn = (Button) findViewById(R.id.btnnextimg);       //aca estamos
        photo = (ImageView) findViewById(R.id.iv_photo);
        photo1 = (ImageView) findViewById(R.id.iv_photo1);
        photo2 = (ImageView) findViewById(R.id.iv_photo2);
        photo3 = (ImageView) findViewById(R.id.iv_photo3);
        photo4 = (ImageView) findViewById(R.id.iv_photo4);
        //new agregate

        mRegistroDriver1 = (registroDriver1) getIntent().getSerializableExtra("Detalle_Driver");
        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPhoto();
            }
        });
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifiedPermision(GALLERY_INTENT);
            }
        });
        photo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifiedPermision(GALLERY_INTENT1);
            }
        });
        photo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifiedPermision(GALLERY_INTENT2);
            }
        });
        photo3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifiedPermision(GALLERY_INTENT3);
            }
        });
        photo4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifiedPermision(GALLERY_INTENT4);
            }
        });
        //btnnextimg
       /* mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siguienteImagen();
            }
        });*/
    }

    private void siguienteImagen() {
        Intent intent = new Intent(EnviarImagenActivity.this, EnviarImagenSegundoActivity.class);
        startActivity(intent);
    }

    private void sendPhoto() {
        for (Uri uri : uriArrayList) {
            StorageReference filepath = mStorage.child(mRegistroDriver1.getId()).child(uri.getLastPathSegment()); //storage
            filepath.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EnviarImagenActivity.this, "Se subio Correctamente la foto", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    //camare
//==
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permsissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                FileUtils.pickImageCamera(this, GALLERY_INTENT_GLOBAL);
            else
                verifiedPermision(GALLERY_INTENT_GLOBAL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {
            uriArrayList.add(tmpUri);
            Glide.with(this)
                    .load(tmpUri.getPath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(photo);
        } else if (requestCode == GALLERY_INTENT1 && resultCode == RESULT_OK) {
            uriArrayList.add(tmpUri);
            Glide.with(this)
                    .load(tmpUri.getPath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(photo1);
        } else if (requestCode == GALLERY_INTENT2 && resultCode == RESULT_OK) {
            uriArrayList.add(tmpUri);
            Glide.with(this)
                    .load(tmpUri.getPath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(photo2);
        } else if (requestCode == GALLERY_INTENT3 && resultCode == RESULT_OK) {
            uriArrayList.add(tmpUri);
            Glide.with(this)
                    .load(tmpUri.getPath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(photo3);
        } else if (requestCode == GALLERY_INTENT4 && resultCode == RESULT_OK) {
            uriArrayList.add(tmpUri);
            Glide.with(this)
                    .load(tmpUri.getPath())
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(photo4);
        }
    }

    private void verifiedPermision(int codeRequest) {
        GALLERY_INTENT_GLOBAL = codeRequest;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean granted = hasPermissions(mPermission);
            if (granted) {
                tmpUri = FileUtils.pickImageCamera(this, GALLERY_INTENT_GLOBAL);
            } else {
                ActivityCompat.requestPermissions(this, mPermission, PERMISSION_CAMERA);
            }
        } else {
            tmpUri = FileUtils.pickImageCamera(this, GALLERY_INTENT_GLOBAL);
        }
    }

    public boolean hasPermissions(String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (!hasPermission(permission)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permission != null) {
            return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }
}

