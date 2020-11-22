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
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.femtaxi.models.registroDriver1;
import com.google.android.gms.tasks.OnSuccessListener;
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
    private int GALLERY_INTENT = 1;
    private int GALLERY_INTENT1 = 2;
    private int GALLERY_INTENT2 = 3;
    private int GALLERY_INTENT3 = 4;
    private int GALLERY_INTENT4 = 5;

    static final int PERMISSION_CAMERA = 1;

    private registroDriver1 mRegistroDriver1;
    private ImageView photo, photo1, photo2, photo3, photo4;
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

        mStorage = FirebaseStorage.getInstance().getReference();
        mUploadBtn = (Button) findViewById(R.id.btnnextimg);
        photo = (ImageView) findViewById(R.id.iv_photo);
        photo1 = (ImageView) findViewById(R.id.iv_photo1);
        photo2 = (ImageView) findViewById(R.id.iv_photo2);
        photo3 = (ImageView) findViewById(R.id.iv_photo3);
        photo4 = (ImageView) findViewById(R.id.iv_photo4);
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
    }

    private void sendPhoto() {
        for (Uri uri : uriArrayList) {
            StorageReference filepath = mStorage.child(mRegistroDriver1.getId()).child(uri.getLastPathSegment());
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
                pickImageCamera(GALLERY_INTENT_GLOBAL);
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
            boolean granted = hasPermissions(mPermission);//PermissionsUtils.hasPermissions(getActivity(), permissions);
            if (granted) {
                tmpUri = pickImageCamera(GALLERY_INTENT_GLOBAL);
            } else {
                ActivityCompat.requestPermissions(this, mPermission, PERMISSION_CAMERA);
            }
        } else {
            tmpUri = pickImageCamera(GALLERY_INTENT_GLOBAL);
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

    public Uri pickImageCamera(final int requestCode) {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Uri uriTemp = getMediaTempUri("IMG", "jpg");

        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTemp);
            intent.putExtra("return-data", true);
            try {
                startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                startActivityForResult(Intent.createChooser(intent, null),
                        requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uriTemp;
    }

    public Uri getMediaTempUri(String prefix, String extension) {
        Uri uriTemp;
        String timeStamp = new SimpleDateFormat("HHmmssdMMyyyy").format(Calendar.getInstance(Locale.getDefault()).getTime());
        String name = String.format("%s_%s.%s",
                prefix, timeStamp.replace(" ", "-"), extension);
        File fileTemp = new File(getAppMediaProfileFolder(), name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uriTemp = FileProvider.getUriForFile(this,
                    BuildConfig.APPLICATION_ID, fileTemp);
        } else {
            uriTemp = Uri.fromFile(fileTemp);
        }
        return uriTemp;
    }

    public static File getAppMediaProfileFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + File.separator
                + "TaxiFem/" + "Camera");
        //noinspection ResultOfMethodCallIgnored
        folder.mkdirs();
        return folder;
    }
}

