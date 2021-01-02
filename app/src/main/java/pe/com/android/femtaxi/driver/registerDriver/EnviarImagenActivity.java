package pe.com.android.femtaxi.driver.registerDriver;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import pe.com.android.femtaxi.databinding.ActivityEnviarImagenBinding;

import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.registroDriver1;
import pe.com.android.femtaxi.utils.FileUtils;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;

public class EnviarImagenActivity extends AppCompatActivity {

    private StorageReference mStorage;
    private PermissionUtil.PermissionRequestObject mRequestObject;
    private ActivityEnviarImagenBinding binding;

    private registroDriver1 mRegistroDriver1;
    private ArrayList<Uri> uriArrayList = new ArrayList<>();

    private Uri mTempUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnviarImagenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Agrege sus imagenes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStorage = FirebaseStorage.getInstance().getReference();


        mRegistroDriver1 = (registroDriver1) getIntent().getSerializableExtra(Constants.Extras.EXTRA_DRIVE);

        binding.imagePen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_PEN, "PEN");
            }
        });

        binding.imagePol.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_POL, "POL");
            }
        });

        binding.imageDni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_DNI, "DNI");
            }
        });

        binding.imageVehiculo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_VEHICULO, "VEHICULO");
            }
        });

        binding.imageSoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_SOAT, "SOAT");
            }
        });

        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPhoto();
            }
        });


    }

    private void moveToSendNextImage() {
        Intent intent = new Intent(EnviarImagenActivity.this, EnviarImagenSegundoActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_DRIVE, mRegistroDriver1);
        startActivity(intent);
    }

    private void sendPhoto() {
        for (Uri uri : uriArrayList) {
            StorageReference filepath = mStorage.child(mRegistroDriver1.getId())
                    .child(uri.getLastPathSegment());
            filepath.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //  Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                            Toast.makeText(EnviarImagenActivity.this, "Se subio Correctamente la foto", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        moveToSendNextImage();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case Constants.Request.REQUEST_CODE_CAMERA_PEN:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imagePen);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_POL:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imagePol);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_DNI:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageDni);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_VEHICULO:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageVehiculo);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_SOAT:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageSoat);
                break;
        }
    }

    private void checkPermissionCamera(int codeRequest, String prefix) {
        mRequestObject = PermissionUtil.with(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        mTempUri = FileUtils.pickImageCamera(EnviarImagenActivity.this,
                                prefix,
                                codeRequest);
                    }
                })
                .onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        checkPermissionCamera(codeRequest, prefix);
                    }
                }).ask(Constants.Request.REQUEST_CODE_CAMERA);
    }
}

