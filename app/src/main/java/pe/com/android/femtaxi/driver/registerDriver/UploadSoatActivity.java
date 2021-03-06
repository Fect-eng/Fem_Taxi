package pe.com.android.femtaxi.driver.registerDriver;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.permissionx.guolindev.PermissionX;

import pe.com.android.femtaxi.databinding.ActivityUploadSoatBinding;
import pe.com.android.femtaxi.driver.OpcionDualDriverActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.registroDriver1;
import pe.com.android.femtaxi.utils.FileUtils;

public class UploadSoatActivity extends AppCompatActivity {
    String TAG = UploadSoatActivity.class.getSimpleName();
    private ActivityUploadSoatBinding binding;
    private StorageReference mStorage;
    private ProgressDialog mProgressDialog;
    private Uri mTempUri;
    private registroDriver1 mRegistroDriver1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUploadSoatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Agrege sus imagenes");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mStorage = FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(this);

        mRegistroDriver1 = (registroDriver1) getIntent().getSerializableExtra(Constants.Extras.EXTRA_DRIVE);

        binding.btnSelect.setOnClickListener(view -> {
            checkPermissionCamera(Constants.Request.REQUEST_CODE_CAMERA_SOAT, "SOAT");
        });

        binding.btnNext.setOnClickListener(view -> {
            if (mTempUri != null) {
                sendImage();
            } else {
                Toast.makeText(UploadSoatActivity.this, "Debe seleccionar una foto antes de continuar ", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnHelp.setOnClickListener(view -> {
            AlertDialog.Builder alerta = new AlertDialog.Builder(UploadSoatActivity.this);
            alerta.setMessage("Buen dia Tenga Usted estimada Conductora, si usted es Personal Externo o desea usar la Aplicacion de Manera Conductora por favor Sirvase a Comunicar al siguiente numero de Empresa FEMTaxi para Mayor Información +51 941174386")  //ver si se cambia esta Opcion
                    .setCancelable(false)
                    .setPositiveButton("Le Saluda La Gerencia", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog titulo = alerta.create();
            titulo.setTitle("Información Exclusiva Personal Externo");
            titulo.show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case Constants.Request.REQUEST_CODE_CAMERA_SOAT:
                Glide.with(this)
                        .load(mTempUri.getPath())
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .into(binding.imgSoat);
                break;
        }
    }

    private void checkPermissionCamera(int codeRequest, String prefix) {
        PermissionX.init(this)
                .permissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList, beforeRequest) -> {
                    scope.showRequestReasonDialog(deniedList,
                            "Para un buen uso de la apolicación es necesario que habilite los permisos correspodientes",
                            "Aceptar",
                            "Cancelar");
                })
                .onForwardToSettings((scope, deniedList) -> {
                    scope.showForwardToSettingsDialog(deniedList,
                            "Para continuar con el uso de la apolicación es necesario que habilite los permisos de manera manual",
                            "Config. manual",
                            "Cancelar");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Log.i(TAG, "checkPermissionStorageCamera si tiene permisos: ");
                        mTempUri = FileUtils.pickImageCamera(UploadSoatActivity.this,
                                prefix,
                                codeRequest);
                    }
                });
    }

    private void sendImage() {
        mProgressDialog.setTitle("Subiendo...");
        mProgressDialog.setMessage("Subiendo foto espere...");
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();
        StorageReference filepath = mStorage.child(mRegistroDriver1.getId())
                .child(mTempUri.getLastPathSegment());
        filepath.putFile(mTempUri)
                .addOnSuccessListener((taskSnapshot) -> {
                    Toast.makeText(UploadSoatActivity.this, "Se subio Correctamente la foto", Toast.LENGTH_SHORT).show();
                    moveToNext();
                    mProgressDialog.dismiss();
                });
    }

    private void moveToNext() {
        Intent intent = new Intent(UploadSoatActivity.this, OpcionDualDriverActivity.class);
        startActivity(intent);
        finish();
    }
}