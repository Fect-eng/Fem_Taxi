package com.example.femtaxi.client;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.femtaxi.R;
import com.example.femtaxi.databinding.ActivityLoginClienteBinding;
import com.example.femtaxi.databinding.ActivityMapClienteBinding;
import com.example.femtaxi.databinding.ActivityProfileClientBinding;
import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.models.Client;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientProvider;
import com.example.femtaxi.utils.CompressorBitmapImage;
import com.example.femtaxi.utils.FileUtils;
import com.example.femtaxi.utils.Utils;
import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileClientActivity extends AppCompatActivity {

    private ActivityProfileClientBinding binding;
    private AuthProvider mAuthProvider;
    private ClientProvider mClientProvider;
    private PermissionUtil.PermissionRequestObject mRequestObject;

    private File mImageFile;
    private Uri mTempUri;
    private String mImage;
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;
        switch (requestCode) {
            case Constants.PERMISSION.PICK_IMAGE_REQUEST:
                try {
                    mImageFile = FileUtils.from(this, data.getData());
                    loadImage(mImageFile.getPath());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case Constants.PERMISSION.PICK_CAMERA_REQUEST:
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
        mRequestObject = PermissionUtil.with(this)
                .request(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .onAllGranted(
                        new Func() {
                            @Override
                            protected void call() {
                                FileUtils.pickImageGallery(ProfileClientActivity.this);
                            }
                        })
                .onAnyDenied(
                        new Func() {
                            @Override
                            protected void call() {
                                checkPermissionGallery();
                            }
                        }).ask(Constants.REQUEST.REQUEST_CODE_GALLERY);
    }

    private void checkPermissionCamera() {
        mRequestObject = PermissionUtil.with(this)
                .request(Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .onAllGranted(
                        new Func() {
                            @Override
                            protected void call() {
                                mTempUri = FileUtils.pickImageCamera(ProfileClientActivity.this,
                                        Constants.PERMISSION.PICK_CAMERA_REQUEST);
                            }
                        })
                .onAnyDenied(
                        new Func() {
                            @Override
                            protected void call() {
                                checkPermissionCamera();
                            }
                        }).ask(Constants.REQUEST.REQUEST_CODE_CAMERA);
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
