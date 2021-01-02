package pe.com.android.femtaxi.driver.registerDriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import pe.com.android.femtaxi.databinding.ActivityEnviarImagenSegundoBinding;
import pe.com.android.femtaxi.driver.OpcionDualDriverActivity;
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

public class EnviarImagenSegundoActivity extends AppCompatActivity {
    private StorageReference mStorage;
    private ActivityEnviarImagenSegundoBinding binding;

    private registroDriver1 mRegistroDriver1;
    private ArrayList<Uri> uriArrayList = new ArrayList<>();

    private Uri mTempUri;
    private PermissionUtil.PermissionRequestObject mRequestObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEnviarImagenSegundoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Agregar imagenes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStorage = FirebaseStorage.getInstance().getReference();

        mRegistroDriver1 = (registroDriver1) getIntent().getSerializableExtra(Constants.Extras.EXTRA_DRIVE);
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendPhoto();
            }
        });
        binding.imageDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_DRIVER, "DRIVER");
            }
        });
        binding.imageRevTec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_REV_TEC, "REV_TEC");
            }
        });
        binding.imageSetare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_SETARE, "SETARE");
            }
        });
        binding.imageTarjProp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_TAR_PROP, "TARJ_PROP");
            }
        });
    }

    private void sendPhoto() {
        for (Uri uri : uriArrayList) {
            StorageReference filepath = mStorage.child(mRegistroDriver1.getId()).child(uri.getLastPathSegment());
            filepath.putFile(uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(EnviarImagenSegundoActivity.this, "Se subio Correctamente la foto", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        moveToOpcionDualDriver();
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
            case Constants.Request.REQUEST_CODE_CAMERA_TAR_PROP:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageTarjProp);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_REV_TEC:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageRevTec);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_SETARE:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageSetare);
                break;
            case Constants.Request.REQUEST_CODE_CAMERA_DRIVER:
                uriArrayList.add(mTempUri);
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imageDriver);
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
                        mTempUri = FileUtils.pickImageCamera(EnviarImagenSegundoActivity.this,
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

    private void moveToOpcionDualDriver() {
        Intent intent = new Intent(EnviarImagenSegundoActivity.this, OpcionDualDriverActivity.class);
        startActivity(intent);
    }
}