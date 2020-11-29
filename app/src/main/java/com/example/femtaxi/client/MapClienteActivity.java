package com.example.femtaxi.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
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

import com.example.femtaxi.MainActivity;
import com.example.femtaxi.R;

import com.example.femtaxi.databinding.ActivityMapClienteBinding;
import com.example.femtaxi.driver.MapDriverActivity;
import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.providers.GeofireProvider;
import com.example.femtaxi.utils.Utils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    String TAG = MapClienteActivity.class.getSimpleName();

    private ActivityMapClienteBinding binding;

    private static final int REQUEST_CODE_AUTOCOMPLETE_ORIGIN = 100;
    private static final int REQUEST_CODE_AUTOCOMPLETE_DESTINO = 200;

    Toolbar mToolbar;
    private LatLng mCurrentLatLng;
    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private GeofireProvider mGeofireProvider;

    private final static int LOCATION_REQUEST_CODE = 1;
    private final static int SETTINGS_REQUEST_CODE = 2;
    private Marker nMarker;
    private List<Marker> mDriversMarkers = new ArrayList<>();
    private Button mButtonConnect;
    private boolean mIsconnect = false;
    private boolean mISFirstTime = true;

    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDestination;
    private LatLng mDestinationLatLng;
    private GoogleMap.OnCameraIdleListener mCameraListener;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.d(TAG, "mLocationCallback location: " + location);
                if (getApplicationContext() != null) {
                    Log.d(TAG, "mLocationCallback getApplicationContext!=null: ");
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    /*if (nMarker != null) {
                        nMarker.remove();
                    }
                    Log.d(TAG, "mLocationCallback mCurrentLatLng: " + mCurrentLatLng);
                    nMarker = nMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Posición Actual")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps)));*/


                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(mCurrentLatLng)
                                    .zoom(16f)
                                    .build()));
                    Log.d(TAG, "mLocationCallback getLatitude: " + location.getLatitude() + ", getLongitude: " + location.getLongitude());
                    if (mISFirstTime) {
                        mISFirstTime = false;
                        getActiveDrivers();
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mGeofireProvider = new GeofireProvider(Constans.DRIVER_ACTIVE);

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        Mapbox.getInstance(this, getResources().getString(R.string.access_token));
        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);
        checkLocationPermissions();

        binding.cardOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .country("PE")
                                /*.addInjectedFeature(home)
                                .addInjectedFeature(work)*/
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapClienteActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_ORIGIN);
            }
        });

        binding.cardDestino.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .country("PE")
                                /*.addInjectedFeature(home)
                                .addInjectedFeature(work)*/
                                .build(PlaceOptions.MODE_CARDS))
                        .build(MapClienteActivity.this);
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE_DESTINO);
            }
        });

        binding.btnRequestDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestDriver();
            }
        });

        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    Geocoder geocoder = new Geocoder(MapClienteActivity.this);
                    mOriginLatLng = nMap.getCameraPosition().target;
                    mOrigin = Utils.getStreet(MapClienteActivity.this,
                            mOriginLatLng.latitude,
                            mOriginLatLng.longitude);
                    Log.d(TAG, "mOriginLatLng: " + mOriginLatLng);
                    Log.d(TAG, "mOrigin: " + mOrigin);
                    binding.txtOrigin.setText(mOrigin);
                } catch (Exception e) {
                    Log.d(TAG, "error: " + e.getMessage());
                }
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        nMap.getUiSettings().setZoomControlsEnabled(true);
        nMap.setOnCameraIdleListener(mCameraListener);
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    if (gpsActived()) {
                        mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        nMap.setMyLocationEnabled(true);
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

    private void requestDriver() {
        if (mOriginLatLng != null && mDestinationLatLng != null) {
            Intent intent = new Intent(MapClienteActivity.this, DetailRequestActivity.class);
            intent.putExtra(Constans.Extras.ORIGIN_LAT, mOriginLatLng.latitude);
            intent.putExtra(Constans.Extras.ORIGIN_LONG, mOriginLatLng.longitude);
            intent.putExtra(Constans.Extras.DESTINO_LAT, mDestinationLatLng.latitude);
            intent.putExtra(Constans.Extras.DESTINO_LONG, mDestinationLatLng.longitude);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Debe Seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show();
        }
    }

    private void getActiveDrivers() {
        mGeofireProvider.getActiveDrivers(mCurrentLatLng, 10).addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                //añadiremos marcadores de conductores que se conecten en app
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
                            return;
                        }
                    }

                }
                LatLng driverLatlng = new LatLng(location.latitude, location.longitude);
                Marker marker = nMap.addMarker(new MarkerOptions()
                        .position(driverLatlng)
                        .title("Conductor Disponible")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps)));
                marker.setTag(key);
                mDriversMarkers.add(marker);
            }

            @Override
            public void onKeyExited(String key) {
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
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
                for (Marker marker : mDriversMarkers) {
                    if (marker.getTag() != null) {
                        if (marker.getTag().equals(key)) {
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

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            nMap.setMyLocationEnabled(true);
        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogNoGPS();    //mensaje DialogGPS
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE_ORIGIN) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            mOriginLatLng = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            mOrigin = Utils.getStreet(this,
                    ((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            Log.d(TAG, "mOriginLatLng: " + mOriginLatLng);
            Log.d(TAG, "mOrigin: " + mOrigin);
            binding.txtOrigin.setText(mOrigin);
        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE_DESTINO) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            mDestinationLatLng = new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            mDestination = Utils.getStreet(this,
                    ((Point) selectedCarmenFeature.geometry()).latitude(),
                    ((Point) selectedCarmenFeature.geometry()).longitude());
            Log.d(TAG, "mDestinationLatLng: " + mDestinationLatLng);
            Log.d(TAG, "mDestination: " + mOrigin);
            binding.txtDestino.setText(mDestination);
        }
    }

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

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
        return isActive;
    }

    private void startLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    //  mButtonConnect.setText("DESCONECTARSE"); //valores asignados a conectarse
                    //  mIsconnect = true;  //valor asigando a conectarse
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    nMap.setMyLocationEnabled(true);
                } else {
                    showAlertDialogNoGPS();   //mensaje DialogGPS
                }
            } else {
                checkLocationPermissions();
            }
        } else {
            if (gpsActived()) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                nMap.setMyLocationEnabled(true);
            } else {
                showAlertDialogNoGPS();
            }
        }
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Proporciona los permisos nesesarios")
                        .setMessage("Esta Aplicacion requiere los permisos nesesarios para funcionar")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                ActivityCompat.requestPermissions(MapClienteActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapClienteActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
    }

    private void logout() {
        Intent intent = new Intent(MapClienteActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}

