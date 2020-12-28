package pe.com.android.femtaxi.driver;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.kayvannj.permission_utils.Func;
import com.github.kayvannj.permission_utils.PermissionUtil;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import pe.com.android.femtaxi.MainActivity;
import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityMapDriverBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;
import pe.com.android.femtaxi.providers.TokenProvider;

public class MapDriverActivity extends AppCompatActivity
        implements OnMapReadyCallback {
        String TAG = MapDriverActivity.class.getSimpleName();

    private ActivityMapDriverBinding binding;

    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    private GeofireProvider mGeofireProvider;
    private ClientBookingProvider mClientBookingProvider;
    private TokenProvider mTokenProvider;
    private AuthProvider mAuthProvider;
    private final static int SETTINGS_REQUEST_CODE = 200;
    private Marker nMarker;
    private boolean mIsconnect = false;

    private LatLng mCurrentLatLng;

    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private ValueEventListener mListener;
    private PermissionUtil.PermissionRequestObject mRequestObject;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                //Log.d(TAG, "mLocationCallback location: " + location);
                if (getApplicationContext() != null) {
                    //Log.d(TAG, "mLocationCallback getApplicationContext!=null: ");
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (nMarker != null) {
                        nMarker.remove();
                    }
                    //Log.d(TAG, "mLocationCallback mCurrentLatLng: " + mCurrentLatLng);
                    nMarker = nMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Posición Actual")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps)));

                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(mCurrentLatLng)
                                    .zoom(16f)
                                    .build()));
                    if (mIsconnect)
                        updateLocation();
                    //Log.d(TAG, "mLocationCallback getLatitude: " + location.getLatitude() + ", getLongitude: " + location.getLongitude());
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mIsconnect = getIntent().getBooleanExtra(Constants.Extras.EXTRA_IS_CONNECTED, false);
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_ACTIVE);
        mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mTokenProvider = new TokenProvider();

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        binding.btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsconnect) {
                    disconnect();
                } else {
                    startLocation();
                }
                mIsconnect = !mIsconnect;
                binding.btnConnect.setText(!mIsconnect ? "CONECTAR" : "DESCONECTAR");
            }
        });
        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);
        checkPermissionsLocation();
        generatedToken();
        isDriverWorking();
        Log.d(TAG, "onCreate savedInstanceState: " + savedInstanceState);
        Log.d(TAG, "onCreate mIsconnect: " + mIsconnect);
        if (mIsconnect)
            startLocation();
        binding.btnConnect.setText(!mIsconnect ? "CONECTAR" : "DESCONECTAR");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mListener != null) {
            Log.d(TAG, "onDestroy mListener: " + mListener);
            mGeofireProvider.isDriverWorking(mAuthProvider.getId())
                    .removeEventListener(mListener);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        nMap.getUiSettings().setZoomControlsEnabled(true);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.driver_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_history_booking:
                moveToHistoryBooking();
                break;
            case R.id.menu_exit:
                logout();
                break;
            case R.id.menu_edit_profile:
                moveToEditProfile();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mRequestObject.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (gpsActived()) {
                startLocation();
            } else {
                showAlertDialogNoGPS();
            }
        } else {
            showAlertDialogNoGPS();
        }
    }

    private void checkPermissionsLocation() {
        mRequestObject = PermissionUtil.with(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION)
                .onAllGranted(new Func() {
                    @Override
                    protected void call() {
                        if (gpsActived()) {
                            if (ActivityCompat.checkSelfPermission(MapDriverActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(MapDriverActivity.this,
                                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                            if (nMap != null)
                                nMap.setMyLocationEnabled(true);
                        } else {
                            showAlertDialogNoGPS();
                        }
                    }
                })
                .onAnyDenied(new Func() {
                    @Override
                    protected void call() {
                        checkPermissionsLocation();
                    }
                }).ask(Constants.REQUEST.REQUEST_CODE_LOCATION);
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }//fin de if primero
        return isActive;
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

    private void disconnect() {
        Log.d(TAG, "disconnect");
        if (mFusedLocation != null) {
            Log.d(TAG, "disconnect");
            mFusedLocation.removeLocationUpdates(mLocationCallback);
            if (mAuthProvider.existSession()) {
                Log.d(TAG, "disconnect");
                mGeofireProvider.removeLocation(mAuthProvider.getId());
            }
        } else {
            Toast.makeText(this, "No te puedes desconectar", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocation() {
        //Log.d(TAG, "startLocation");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //Log.d(TAG, "startLocation mayo a m");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Log.d(TAG, "startLocation permiso activado");
                if (gpsActived()) {
                    //Log.d(TAG, "startLocation gps activado");
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    if (nMap != null)
                        nMap.setMyLocationEnabled(true);
                } else {
                    showAlertDialogNoGPS();
                }
            } else {
                checkPermissionsLocation();
            }
        } else {
            Log.d(TAG, "startLocation menor a M");
            if (gpsActived()) {
                Log.d(TAG, "startLocation gps activado");
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                nMap.setMyLocationEnabled(true);
            } else {
                showAlertDialogNoGPS();
            }
        }
    }

    private void updateLocation() {
        Log.d(TAG, "updateLocation");
        if (mIsconnect) {
            Log.d(TAG, "updateLocation mIsconnect: " + mIsconnect);
            if (mAuthProvider.existSession() && mCurrentLatLng != null) {
                Log.d(TAG, "updateLocation mAuthProvider.existSession(): " + mAuthProvider.existSession());
                isDriverWorking();
                mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
            }
        }
    }

    private void logout() {
        Toast.makeText(this, "Sesion Cerrada", Toast.LENGTH_SHORT).show();
        new PreferencesManager(this).setIsDriver(false);
        disconnect();
        Intent intent = new Intent(MapDriverActivity.this, MainActivity.class);
        startActivity(intent);
        MapDriverActivity.this.finish();
    }

    private void isDriverWorking() {
        Log.d(TAG, "isDriverWorking");
        mListener = mGeofireProvider.isDriverWorking(mAuthProvider.getId())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            disconnect();
                            Log.d(TAG, "isDriverWorking");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void generatedToken() {
        mTokenProvider.createdToken(mAuthProvider.getId());
    }

    private void moveToEditProfile() {
        Intent intent = new Intent(MapDriverActivity.this, ProfileDriverActivity.class);
        startActivity(intent);
    }

    private void moveToHistoryBooking() {
        Intent intent = new Intent(MapDriverActivity.this, HistoryBookingDriverActivity.class);
        startActivity(intent);
    }
}