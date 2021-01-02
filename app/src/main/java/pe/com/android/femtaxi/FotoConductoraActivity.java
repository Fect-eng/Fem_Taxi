package pe.com.android.femtaxi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class FotoConductoraActivity extends AppCompatActivity {


    private Button mUploadBtn;
    private Button activityPolicia;
    private StorageReference mStorage;
    private static final int GALLERY_INTENT = 1;
    private ImageView mImageView;
    private ProgressDialog mProgressDialog;

    Toolbar mToolbar;

    private Button btnconsulta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foto_conductora);

        mStorage = FirebaseStorage.getInstance().getReference();

        mUploadBtn = (Button) findViewById(R.id.mUploadBtn);
        activityPolicia = (Button) findViewById(R.id.activityPolicia);
        mImageView = (ImageView) findViewById(R.id.SubirImagen);
        mProgressDialog = new ProgressDialog(this);

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_INTENT);
                mUploadBtn.setEnabled(false);
                activityPolicia.setEnabled(true);

            }
        });

        activityPolicia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FotoConductoraActivity.this, RevisionTecnicaActivity.class);
                startActivity(intent);
                FotoConductoraActivity.this.finish();
            }
        });

        btnconsulta = (Button) findViewById(R.id.btnconsulta);
        btnconsulta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alerta = new AlertDialog.Builder(FotoConductoraActivity.this);
                alerta.setMessage("Buen dia Tenga Usted estimada Conductora, si usted es Personal Externo o desea usar la Aplicacion de Manera Conductora por favor Sirvase a Comunicar al siguiente numero de Empresa FEMTaxi para Mayor Información +51 941174386")  //ver si se cambia esta Opcion
                        .setCancelable(false)
                        .setPositiveButton("Le Saluda La Gerencia", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnconsulta.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //nada
                                    }
                                });
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Información Exclusiva Personal Externo");
                titulo.show();
            }
        });
    } // final oncreate


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_INTENT && resultCode == RESULT_OK) {

            mProgressDialog.setTitle("Subiendo...");
            mProgressDialog.setMessage("Subiendo foto espere...");
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();

            Uri uri = data.getData();

            StorageReference filePath = mStorage.child("Foto_Conductora").child(uri.getLastPathSegment());    //getLastPathSegment     getEncodedPath

            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgressDialog.dismiss();
                    Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!urlTask.isSuccessful());
                    Uri downloadUrl = urlTask.getResult();

                    Glide.with(FotoConductoraActivity.this)
                            .load(downloadUrl)
                            .fitCenter()
                            .centerCrop()
                            .into(mImageView);


                    Toast.makeText(FotoConductoraActivity.this, "Se subio Correctamente la Foto", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }
}