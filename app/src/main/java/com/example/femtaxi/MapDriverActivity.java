package com.example.femtaxi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.GeofireProvider;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class MapDriverActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    //toolbar declarado
    Toolbar mToolbar;
    private StorageReference mStorage;   //database
    // AuthProvider mAuthProvider;
    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    //ubicacion tn time real
    Button btnConnect;   //boton de conexio n
    private FusedLocationProviderClient ubicacion;
    /*FirebaseDatabase database;
    DatabaseReference refubicacion;*/
    // private AuthProvider mAuthProvider;   //es de firebase
    private GeofireProvider mGeofireProvider; //geofire provider
    private AuthProvider mAuthProvider;
    //==================================================================================
    //VARIABLES ASIGNADAS PARA GEOLOCASLIZACION
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    //==================================================================================
    //marcacion de imagen
    private Marker nMarker;   //exportacion en gps modelado
    //finde de marcacion de img

    //creamos un boton para conexion
    private Button mButtonConnect;
    private boolean mIsconnect = false;
    //finde de boton para conexion

    private LatLng mCurrentLatLng;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    //Geofire codigo
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());   //geofire
                    //finde geofire codigo

                    //validacion para que no duplique la imagen
                    if (nMarker != null) {
                        nMarker.remove();
                    }
                    //Fin de validacion codigo probado ok

                    //nmarker hacemos referencia a colocar un icono en vuestro punto de referencia gps
                    nMarker = nMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude())
                            )
                                    .title("Posición Actual")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps))
                    ); //codigo probado y verificado de icono en mapa
                    //obtener ubicacion de usuario en tiempo real
                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f) //es parque en el mapa inical se visualize  la ruta
                                    .build()
                    ));
                    //   updateLocation();  //metodo agregado
                }
            }
        }
    };

    //===========================================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_driver);
        mStorage = FirebaseStorage.getInstance().getReference();
        //mAuthProvider = new AuthProvider();
        // database = FirebaseDatabase.getInstance();

        //refubicacion = database.getReference("Ubicación Driver Femtaxi");
        //==================================================================

        //==================================================================
        mGeofireProvider = new GeofireProvider();  //agregado
        //toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Mapa Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        //segunda variable instanciada de gps
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        //=======================================================
        mButtonConnect = findViewById(R.id.btnConnect);
        mButtonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //aun falta asiganr codigo
                if (mIsconnect) {
                    disconnect();
                } else {
                    dameUbicacion();  //tiempo real firestore
                    startLocation(); //activa la localizacion
                }
            }
        });
        //finde de nose que XD
        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);
    }

    private void dameUbicacion() {
        if (ContextCompat.checkSelfPermission(MapDriverActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Tenemos Permiso", Toast.LENGTH_SHORT).show();
        } else {
            ActivityCompat.requestPermissions(MapDriverActivity.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        ubicacion = LocationServices.getFusedLocationProviderClient(MapDriverActivity.this);
        ubicacion.getLastLocation().addOnSuccessListener(MapDriverActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    Double latitude = location.getLatitude();
                    Double longitud = location.getLongitude();

                    ubicacionDriver ubi = new ubicacionDriver(latitude, longitud);
                    setLocation(ubi);
                    Toast.makeText(MapDriverActivity.this, "Ubicacion Agregada!", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MapDriverActivity.this, "Latitud: " + latitude + "Longitud: " + longitud, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setLocation(ubicacionDriver ubi) {
        HashMap<String, Object> locationHash = new HashMap<>();
        locationHash.put("latitud", ubi.getLatitud());
        locationHash.put("longitud", ubi.getLongitud());
        FirebaseFirestore dbFireBase = FirebaseFirestore.getInstance();
        dbFireBase.collection("Location")
                .document("locationId")
                .set(locationHash)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapDriverActivity.this, "Resgistro exitoso", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapDriverActivity.this, " " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
    //crearemos el metodo de update maps

   /* private void updateLocation(){
        //===================================================================================================
        if (mAuthProvider.existSession() && mCurrentLatLng != null){  //relativo new

        }*/
    // mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);  //debe de ir el ID de AuthProvider

    //  }  revisar falta
    //find del metodo de maps

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        nMap.getUiSettings().setZoomControlsEnabled(true);


        //habilita opciones de zoom
        //por ahora la aplicacion funciona a lo esperado solo que lo hace desded fake GPS

        //Primera instancia de gps user
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        //startLocation();
        //termine de instanciar al user en time real
    }

    //codigo de opcion de menu  Cerrar Sesion
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    void logout() {
        Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show();
        disconnect();
        Intent intent = new Intent(MapDriverActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    //codigo de opcion de menu
    //==========================================================================================

    //esta parte es por si el user se revela y no quiere poner opcion de gps
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
                    } else {
                        showAlertDialogNoGPS();    //mensaje DialogGPS
                    }
                else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }
    //==================================================================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
        } else {
            showAlertDialogNoGPS();    //mensaje DialogGPS
        }
    }

    //==========================================================================
    //==========================================================================
    private void showAlertDialogNoGPS() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa GPS para continuar")
                .setPositiveButton("configuraciones", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), SETTINGS_REQUEST_CODE);
                    }
                }).create().show();
    }
    //==========================================================================

    //metodo de validacion para verificar que gps este activado
    //hicimos pruebas si funciona
    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }//fin de if primero
        return isActive;
    }//fin de private void
    //Fin de metodo de activacion gps

    private void disconnect() {
        // mButtonConnect.setText("CONECTARSE"); //valores asignados a conectarse
        //mIsconnect = false;  //valor asigando a conectarse
        if (mFusedLocation != null) {
            mButtonConnect.setText("CONECTARSE"); //valores asignados a conectarse
            mIsconnect = false;  //valor asigando a conectarse
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            nMap.setMyLocationEnabled(true);
            // if (mAuthProvider.existSession()){  //en rojo ============================================================================================== recien agregado
            //     mGeofireProvider.removeLocation(mAuthProvider.getName()); //deberia ir el getId de AuthProvider
            //  }//personaliza el punto asignado en gp
        } else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    //escuchador
    private void startLocation() { //23 MARSMELLOWS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mButtonConnect.setText("DESCONECTARSE"); //valores asignados a conectarse
                    mIsconnect = true;  //valor asigando a conectarse
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
                } else {
                    showAlertDialogNoGPS();   //mensaje DialogGPS
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps revisar si pasa algun detalle
            } else {
                showAlertDialogNoGPS();     //mensaje DialogGPS
            }
        }
    }//start location

    //fraccion de codigo por si el user no acepta los permisos != si es diferente
    private void checkLocationPermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos nesesarios")
                        .setMessage("Esta Aplicacion requiere los permisos nesesarios para funcionar")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                ActivityCompat.requestPermissions(MapDriverActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapDriverActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (mIsconnect) {
            dameUbicacion();
            Log.d("onLocationChanged","esta cambiando de ubicacion");
        }
    }
    //checkLocationPermissions final

}//public class
//==========================================================================================
// @Override
// public boolean onCreateOptionsMenu(Menu menu) {
//  getMenuInflater().inflate(R.menu.driver_menu, menu);
//  return super.onCreateOptionsMenu(menu);
// }
//procedimiento para cerrar sesion
// @Override
// public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//  if (item.getItemId() == R.id.action_logout){
//      try {
//          logout();
//      } catch (LoginException e) {
//          e.printStackTrace();
//      }
//  }
//  return super.onOptionsItemSelected(item);
//}
//void  logout() throws LoginException {
//mAuthProvider.logout();
//  Intent intent = new Intent(MapDriverActivity.this, MainActivity.class);
//  startActivity(intent);
//  finish();

