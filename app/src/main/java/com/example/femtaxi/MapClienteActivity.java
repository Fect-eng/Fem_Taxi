package com.example.femtaxi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
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

import com.example.femtaxi.providers.GeofireProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
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
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseError;

import java.security.AuthProvider;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    //toolbar declarado
    Toolbar mToolbar;
    private LatLng mCurrentLatLng;
    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;

    //==================================================================================
    //VARIABLES ASIGNADAS PARA GEOLOCASLIZACION
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private GeofireProvider mGeofireProvider;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    //==================================================================================
    //marcacion de imagen
    private Marker nMarker;   //exportacion en gps modelado
    private List<Marker> mDriversMarkers = new ArrayList<>();
    //finde de marcacion de img

    //creamos un boton para conexion
    private Button mButtonConnect;
    private boolean mIsconnect = false;

    private boolean mISFirstTime = true;

    //place
    private PlacesClient mPlaces;
    private AutocompleteSupportFragment mAutocomplete;
    private AutocompleteSupportFragment mAutocompleteDestination;

    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDestination;
    private LatLng mDestinationLatLng;
    //finde de boton para conexion

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                if (getApplicationContext() != null) {
                    //validacion para que no duplique la imagen
                    if (nMarker != null){
                        nMarker.remove();
                    }
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());   //geofire
                    //Fin de validacion codigo probado ok

                    //nmarker hacemos referencia a colocar un icono en vuestro punto de referencia gps
                    nMarker = nMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(),location.getLongitude())
                            )
                                    .title("Posición Actual Cliente")  //pones el dedo y
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.clienttaxi))
                    ); //codigo probado y verificado de icono en mapa
                    //obtener ubicacion de usuario en tiempo real
                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                                    .zoom(16f) //es parque en el mapa inical se visualize  la ruta
                                    .build()
                    ));
                    if (mISFirstTime){
                        mISFirstTime = false;
                        getActiveDrivers();

                    }
                }
            }
        }
    };

    //===========================================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_cliente);
        mGeofireProvider = new GeofireProvider();
       // mAuthProvider = new AuthProvider(); esto es de firebase
        //toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Mapa Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        //============================================================================================
        //segunda variable instanciada de gps
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        //=======================================================
       // mButtonConnect = findViewById(R.id.btnConnect);
        //mButtonConnect.setOnClickListener(new View.OnClickListener() {  //manda error
          /*  @Override
            public void onClick(View v) {
                //aun falta asiganr codigo
                if (mIsconnect) {
                    disconnect();
                }
                else{  NO borrar es para pruebas unicas
                    startLocation(); //activa la localizacion
                }
            }*/
      //  });

        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapaCliente);
        nMapFragment.getMapAsync(this);
        //Place Autocomplete

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getResources().getString(R.string.google_maps_key));
        }
        mPlaces = Places.createClient(this);
        mAutocomplete = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.pleaceAutocompleteOrigin);
        mAutocomplete.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocomplete.setHint("Lugar de recogida");
        mAutocomplete.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatLng = place.getLatLng();
                Log.d("PLACE", "Name: " + mOrigin);
                Log.d("PLACE", "Lat: " + mOriginLatLng.latitude);
                Log.d("PLACE", "Lng: " + mOriginLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
            mAutocompleteDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.placeAutocompleteDestination);
        mAutocompleteDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        mAutocompleteDestination.setHint("Destino");
        mAutocompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLatLng = place.getLatLng();
                Log.d("PLACE", "Name: " + mDestination);
                Log.d("PLACE", "Lat: " + mDestinationLatLng.latitude);
                Log.d("PLACE", "Lng: " + mDestinationLatLng.longitude);
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });


    }
        //metodo de locaclizacion geofire
        private void getActiveDrivers(){
        mGeofireProvider.getActiveDrivers(mCurrentLatLng).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //añadiremos marcadores de conductores que se conecten en app
                for (Marker marker: mDriversMarkers){
                    if (marker.getTag() != null){
                        if (marker.getTag().equals(key)){
                            return;
                        }
                    }

                }
                LatLng driverLatlng = new LatLng(location.latitude, location.longitude);
                Marker marker = nMap.addMarker(new MarkerOptions().position(driverLatlng).title("Conductor Disponible").icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps)));
                marker.setTag(key);
                mDriversMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                //cuando se hayan desconectado
                for (Marker marker: mDriversMarkers){
                    if (marker.getTag() != null){
                        if (marker.getTag().equals(key)){
                            marker.remove();
                            mDriversMarkers.remove(marker);

                            return;
                        }
                    }

                }
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                //update posicion de dribver
                for (Marker marker: mDriversMarkers){
                    if (marker.getTag() != null){
                        if (marker.getTag().equals(key)){
                            marker.setPosition(new LatLng(location.latitude, location.longitude));
                        }
                    }

                }
            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
        }


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

        startLocation();
        //termine de instanciar al user en time real

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout ){
            logout();
        }
        return super.onOptionsItemSelected(item);
    }
    void logout(){
        Intent intent = new Intent(MapClienteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    //===============================================================================================
    //esta parte es por si el user se revela y no quiere poner opcion de gps
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    if (gpsActived()){
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
                    }
                    else{
                        showAlertDialogNoGPS();    //mensaje DialogGPS
                    }
                else{
                    checkLocationPermissions();
                }
            }
            else{
                checkLocationPermissions();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()){
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
        }
        else{
            showAlertDialogNoGPS();    //mensaje DialogGPS
        }
    }

    //==================================================================================================================

    //=================================================================================================================
    private void showAlertDialogNoGPS(){
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
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            isActive = true;
        }//fin de if primero
        return isActive;
    }//fin de private void
    //Fin de metodo de activacion gps

   /* private void disconnect(){
        mButtonConnect.setText("CONECTARSE"); //valores asignados a conectarse
        mIsconnect = false;  //valor asigando a conectarse
        if (mFusedLocation != null){
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
        }
    }*/
    //escuchador
    private void startLocation() { //23 MARSMELLOWS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                if (gpsActived()){
                  //  mButtonConnect.setText("DESCONECTARSE"); //valores asignados a conectarse
                  //  mIsconnect = true;  //valor asigando a conectarse
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps
                }
                else{
                    showAlertDialogNoGPS();   //mensaje DialogGPS
                }
            }
            else{
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()){
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                nMap.setMyLocationEnabled(true); //personaliza el punto asignado en gps revisar si pasa algun detalle
            }
            else{
                showAlertDialogNoGPS();     //mensaje DialogGPS
            }
        }
    }//start location

    //fraccion de codigo por si el user no acepta los permisos != si es diferente
    private void checkLocationPermissions () {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED);{
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos nesesarios")
                        .setMessage("Esta Aplicacion requiere los permisos nesesarios para funcionar")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                ActivityCompat.requestPermissions(MapClienteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else{
                ActivityCompat.requestPermissions(MapClienteActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }
    //checkLocationPermissions final

    }



