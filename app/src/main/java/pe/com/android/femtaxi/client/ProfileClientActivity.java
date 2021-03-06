package pe.com.android.femtaxi.client;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.permissionx.guolindev.PermissionX;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityProfileClientBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.Client;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientProvider;
import pe.com.android.femtaxi.utils.CompressorBitmapImage;
import pe.com.android.femtaxi.utils.FileUtils;

public class ProfileClientActivity extends AppCompatActivity {
    String TAG = ProfileClientActivity.class.getSimpleName();
    private ActivityProfileClientBinding binding;
    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;
    private File mImageFile;
    private Uri mTempUri;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuthProvider = new AuthProvider();
        mClientProvider = new ClientProvider();
        mProgressDialog = new ProgressDialog(this);

        binding.fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        binding.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog();
            }
        });

        binding.btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData();
            }
        });

        binding.btnBackPresset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileClientActivity.this, MapClienteActivity.class);
                startActivity(intent);
                ProfileClientActivity.this.finish();
            }
        });

        getDataUser();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case Constants.Request.REQUEST_CODE_GALLERY:
                try {
                    mImageFile = FileUtils.from(this, data.getData());
                    loadImage(mImageFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Constants.Request.REQUEST_CODE_CAMERA:
                try {
                    mImageFile = FileUtils.from(this, mTempUri);
                    loadImage(mImageFile.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    public void getDataUser() {
        mClientProvider.getDataUser(mAuthProvider.getId())
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot != null && documentSnapshot.exists()) {
                        Client client = documentSnapshot.toObject(Client.class);
                        client.setUId(documentSnapshot.getId());
                        fillData(client);
                    } else {

                    }
                });
    }

    private void fillData(Client client) {
        loadImage(client.getPhoto());
        binding.inputNameLastName.setText(client.getName());
        binding.inputEmail.setText(client.getEmail());
    }

    private void alertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Atención");
        builder.setMessage("¿De donde desea obtener la imagen?");

        builder.setPositiveButton("Galeria", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            checkPermissionGallery();
        });
        builder.setNegativeButton("Camara", (dialogInterface, i) -> {
            dialogInterface.dismiss();
            checkPermissionCamera();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void checkPermissionGallery() {
        PermissionX.init(this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE,
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
                        FileUtils.pickImageGallery(ProfileClientActivity.this);
                    }
                });
    }

    private void checkPermissionCamera() {
        Log.i(TAG, "checkPermissionStorageCamera: ");
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
                        mTempUri = FileUtils.pickImageCamera(ProfileClientActivity.this,
                                "",
                                Constants.Request.REQUEST_CODE_CAMERA);
                    }
                });
    }

    private void updateData() {
        if (!TextUtils.isEmpty(binding.inputNameLastName.getText().toString())) {
            mProgressDialog.setMessage("Espere un momento por favor...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.show();
            saveImage();
        } else {
            Toast.makeText(this, "Debe ingresar su nombre para continuar", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImage() {
        if (mImageFile != null) {
            byte[] imageByte = CompressorBitmapImage.getImage(this, mImageFile.getPath(), 1024, 1024);
            StorageReference storageReference = FirebaseStorage.getInstance()
                    .getReference()
                    .child(Constants.Firebase.Nodo.IMAGE_CLIENT)
                    .child(mAuthProvider.getId());
            UploadTask uploadTask = storageReference.putBytes(imageByte);
            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String image = uri.toString();
                                        Map<String, Object> update = new HashMap<>();
                                        update.put("photo", image);
                                        update.put("name", binding.inputNameLastName.getText().toString());
                                        update.put("phone", binding.inputPhone.getText().toString());
                                        mClientProvider.getUpdateDataUser(mAuthProvider.getId(), update)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mProgressDialog.dismiss();
                                                        Toast.makeText(ProfileClientActivity.this, "Informacion actualizada con exito", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });

                    } else {
                        Toast.makeText(ProfileClientActivity.this, "Ocurrio un problema al subir la imagen", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Map<String, Object> update = new HashMap<>();
            update.put("name", binding.inputNameLastName.getText().toString());
            update.put("phone", binding.inputPhone.getText().toString());
            mClientProvider.getUpdateDataUser(mAuthProvider.getId(), update)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mProgressDialog.dismiss();
                            Toast.makeText(ProfileClientActivity.this, "Informacion actualizada con exito", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void loadImage(String path) {
        Drawable placeholder = getResources().getDrawable(R.drawable.ic_login_user);
        Glide.with(this)
                .load(path)
                .placeholder(placeholder)
                .error(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(binding.photo);
    }
}
