package pe.com.android.femtaxi.driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnIndicatorPositionChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.permissionx.guolindev.PermissionX;

import org.jetbrains.annotations.NotNull;

import pe.com.android.femtaxi.MainActivity;
import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityMapDriverBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;

public class MapDriverActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    String TAG = MapDriverActivity.class.getSimpleName();

    private ActivityMapDriverBinding binding;

    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private final static int SETTINGS_REQUEST_CODE = 200;
    private boolean mIsconnect = false;

    private LatLng mCurrentLatLng;

    private ValueEventListener mListener;

    private MapboxMap mMapboxMap;
    private LocationComponent mLocationComponent;
    private SymbolManager mSymbolManager;
    private SymbolOptions mSymbolOptionsLocation;
    private Style mStyle;
    private Bitmap iconLocation;

    OnIndicatorPositionChangedListener positionChangedListener = point -> {
        Log.i(TAG, "addOnIndicatorPositionChangedListener point: " + point);
        mCurrentLatLng = new LatLng(point.latitude(), point.longitude());
        LocationTheMap(mCurrentLatLng.getLatitude(), mCurrentLatLng.getLongitude());

        if (mSymbolOptionsLocation == null) {
            mSymbolOptionsLocation = new SymbolOptions()
                    .withLatLng(mCurrentLatLng)
                    .withIconImage("location")
                    //set the below attributes according to your requirements
                    .withIconSize(1.5f)
                    .withIconOffset(new Float[]{0f, -1.5f})
                    .withTextField("Posición Actual")
                    .withTextHaloColor("rgba(255, 255, 255, 100)")
                    .withTextHaloWidth(5.0f)
                    .withTextAnchor("top")
                    .withTextOffset(new Float[]{0f, 1.5f});
            if (mSymbolManager == null)
                if (mMapboxMap != null)
                    if (mStyle != null)
                        mSymbolManager = new SymbolManager(binding.mapView, mMapboxMap, mStyle);
            mSymbolManager.create(mSymbolOptionsLocation);
        } else {
            mSymbolOptionsLocation.withLatLng(mCurrentLatLng);
        }

        if (mIsconnect)
            updateLocation();
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mIsconnect = getIntent().getBooleanExtra(Constants.Extras.EXTRA_IS_CONNECTED, false);
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_ACTIVE);
        mAuthProvider = new AuthProvider();

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        iconLocation = BitmapFactory.decodeResource(getResources(), R.drawable.iconogps);

        //mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        binding.btnConnect.setOnClickListener((v) -> {
            if (mIsconnect) {
                disconnect();
            } else {
                startLocation();
            }
            mIsconnect = !mIsconnect;
            binding.btnConnect.setText(!mIsconnect ? "CONECTAR" : "DESCONECTAR");
        });
        /*nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);*/
        checkPermissionsLocation();
        isDriverWorking();
        Log.d(TAG, "onCreate savedInstanceState: " + savedInstanceState);
        Log.d(TAG, "onCreate mIsconnect: " + mIsconnect);
        if (mIsconnect)
            startLocation();
        binding.btnConnect.setText(!mIsconnect ? "CONECTAR" : "DESCONECTAR");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        binding.mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (positionChangedListener != null) {
            mLocationComponent.removeOnIndicatorPositionChangedListener(positionChangedListener);
            positionChangedListener = null;
        }

        if (mListener != null) {
            Log.d(TAG, "onDestroy mListener: " + mListener);
            mGeofireProvider.isDriverWorking(mAuthProvider.getId())
                    .removeEventListener(mListener);
            mListener = null;
        }
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

    @Override
    @SuppressLint("MissingPermission")
    public void onMapReady(@NonNull @NotNull MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;
        mMapboxMap.setStyle(Style.MAPBOX_STREETS, (style) -> {
            mStyle = style;
            mLocationComponent = mMapboxMap.getLocationComponent();
            mLocationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, mStyle)
                            .build());
            mLocationComponent.setLocationComponentEnabled(true);
            mLocationComponent.setCameraMode(CameraMode.TRACKING);
            mLocationComponent.setRenderMode(RenderMode.NORMAL);

            mMapboxMap.getUiSettings().setLogoEnabled(false);
            mMapboxMap.getUiSettings().setAttributionEnabled(false);
            mMapboxMap.getUiSettings().setAllGesturesEnabled(false);

            mSymbolManager = new SymbolManager(binding.mapView, mMapboxMap, mStyle);
            mMapboxMap.getStyle().addImage("location", iconLocation);

            /*hoveringMarker = new ImageView(getContext());

            hoveringMarker.setImageDrawable(drawableOnMoveEnd);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            hoveringMarker.setLayoutParams(params);
            binding.mapView.addView(hoveringMarker);*/

            new Handler().postDelayed(() -> {
                myLocation();
                mLocationComponent.addOnIndicatorPositionChangedListener(positionChangedListener);
            }, 500);
        });
    }

    private void checkPermissionsLocation() {
        Log.i(TAG, "checkPermissionLocation: ");
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
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
                        Log.i(TAG, "checkPermissionLocation si tiene permisos: ");
                        if (gpsActived()) {
                            binding.mapView.getMapAsync(this::onMapReady);
                        } else {
                            showAlertDialogNoGPS();
                        }
                    }
                });
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
        if (mLocationComponent != null) {
            mLocationComponent.removeOnIndicatorPositionChangedListener(positionChangedListener);
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
                    if (mLocationComponent != null)
                        mLocationComponent.addOnIndicatorPositionChangedListener(positionChangedListener);
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
                if (mLocationComponent != null)
                    mLocationComponent.addOnIndicatorPositionChangedListener(positionChangedListener);
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
                mGeofireProvider.saveLocation(mAuthProvider.getId(),
                        new com.google.android.gms.maps.model.LatLng(
                                mCurrentLatLng.getLatitude(),
                                mCurrentLatLng.getLongitude()
                        ));
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

    private void moveToEditProfile() {
        Intent intent = new Intent(MapDriverActivity.this, ProfileDriverActivity.class);
        startActivity(intent);
    }

    private void moveToHistoryBooking() {
        Intent intent = new Intent(MapDriverActivity.this, HistoryBookingDriverActivity.class);
        startActivity(intent);
    }

    private void myLocation() {
        if (mMapboxMap != null) {
            Location lastKnownLocation = mMapboxMap.getLocationComponent().getLastKnownLocation();
            if (lastKnownLocation != null) {
                LocationTheMap(lastKnownLocation.getLatitude(),
                        lastKnownLocation.getLongitude());
            }
        }
    }

    private void LocationTheMap(Double latitude, Double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .bearing(0)
                .tilt(0)
                .build();

        mMapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 5000);
    }
}