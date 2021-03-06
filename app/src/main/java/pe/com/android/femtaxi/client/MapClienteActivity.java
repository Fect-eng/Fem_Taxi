package pe.com.android.femtaxi.client;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.view.GravityCompat;

import com.firebase.geofire.GeoLocation;
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
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseError;
import com.google.maps.android.SphericalUtil;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pe.com.android.femtaxi.MainActivity;
import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.annotation.ServiceType;
import pe.com.android.femtaxi.databinding.ActivityMapClienteBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;
import pe.com.android.femtaxi.utils.Utils;

public class MapClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    String TAG = MapClienteActivity.class.getSimpleName();

    private ActivityMapClienteBinding binding;

    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private GeofireProvider mGeofireProvider;

    private final static int SETTINGS_REQUEST_CODE = 2;
    private List<Marker> mDriversMarkers = new ArrayList<>();
    private boolean mISFirstTime = true;

    private String mOrigin;
    private LatLng mOriginLatLng;
    private String mDestination;
    private LatLng mDestinationLatLng;
    private GoogleMap.OnCameraIdleListener mCameraListener;
    private PlacesClient mPlacesClient;
    private AutocompleteSupportFragment autocompleteOrigin;
    private AutocompleteSupportFragment autocompleteDestino;

    @ServiceType
    private int mServiceType = ServiceType.TAXI;

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.d(TAG, "mLocationCallback location: " + location);
                if (getApplicationContext() != null) {
                    Log.d(TAG, "mLocationCallback getApplicationContext!=null: ");
                    Log.d(TAG, "mLocationCallback getLatitude: " + location.getLatitude());
                    Log.d(TAG, "mLocationCallback getLongitude: " + location.getLongitude());
                    mOriginLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(mOriginLatLng)
                                    .zoom(16f)
                                    .build()));
                    Log.d(TAG, "mLocationCallback getLatitude: " + location.getLatitude() + ", getLongitude: " + location.getLongitude());
                    if (mISFirstTime) {
                        mISFirstTime = false;
                        getActiveDrivers();
                        limitSearch();
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
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_ACTIVE);
        binding.navView.setNavigationItemSelectedListener((item) -> {
            switch (item.getItemId()) {
                case R.id.nav_taxi:
                    mServiceType = ServiceType.TAXI;
                    break;
                case R.id.nav_urbano:
                    mServiceType = ServiceType.INTRA_URBANO;
                    break;
                case R.id.nav_delivery:
                    mServiceType = ServiceType.DELIVERY;
                    break;
                case R.id.nav_mensaje:
                    mServiceType = ServiceType.MESSAGING;
                    break;
                case R.id.nav_carga:
                    mServiceType = ServiceType.CARGA;
                    break;
                case R.id.nav_mascota:
                    mServiceType = ServiceType.PET;
                    break;
                case R.id.nav_elegida:
                    mServiceType = ServiceType.FRIEND;
                    friend();
                    break;
                case R.id.nav_historial:
                    moveToHistoryBooking();
                    break;
                case R.id.nav_perfil:
                    moveToEditProfile();
                    break;
                case R.id.nav_cerrar:
                    logout();
                    break;
            }
            textBottonRequest(mServiceType);
            binding.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        MenuItem menuItem = binding.navView.getMenu().getItem(0);
        menuItem.setChecked(true);

        textBottonRequest(mServiceType);

        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        checkPermissionsLocation();

        binding.rootLayout.btnRequestDrive.setOnClickListener((view) -> {
            moveToDetailRequestDriver();
        });

        binding.rootLayout.btnMenu.setOnClickListener((view) -> {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        if (!Places.isInitialized())
            Places.initialize(this, getResources().getString(R.string.google_api_key));

        mPlacesClient = Places.createClient(this);

        instanceAutoCompleteOrigin();
        instanceAutoCompleteDestino();
        instanceCameraListener();

        binding.rootLayout.btnLlamada.setOnClickListener((v) -> {
            checkPermissionCall();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFusedLocation != null)
            mFusedLocation.removeLocationUpdates(mLocationCallback);
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
        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);           //client_menu
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE && gpsActived()) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                nMap.setMyLocationEnabled(true);
            }
        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogNoGPS();
        }
    }

    private void instanceCameraListener() {
        mCameraListener = new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                try {
                    mOriginLatLng = nMap.getCameraPosition().target;
                    mOrigin = Utils.getStreet(MapClienteActivity.this,
                            mOriginLatLng.latitude,
                            mOriginLatLng.longitude);
                    autocompleteOrigin.setText(mOrigin);
                } catch (Exception e) {
                    Log.d(TAG, "error: " + e.getMessage());
                }
            }
        };
    }

    private void instanceAutoCompleteDestino() {
        autocompleteDestino = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.txt_destino);
        autocompleteDestino.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteDestino.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mDestination = place.getName();
                mDestinationLatLng = place.getLatLng();
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void instanceAutoCompleteOrigin() {
        autocompleteOrigin = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.txt_origin);
        autocompleteOrigin.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME));
        autocompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                mOrigin = place.getName();
                mOriginLatLng = place.getLatLng();
                nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                        new CameraPosition.Builder()
                                .target(mOriginLatLng)
                                .zoom(14f)
                                .build()));
            }

            @Override
            public void onError(@NonNull Status status) {

            }
        });
    }

    private void getActiveDrivers() {
        mGeofireProvider.getActiveDrivers(mOriginLatLng, 10)
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        for (Marker marker : mDriversMarkers) {
                            if (marker.getTag() != null) {
                                if (marker.getTag().equals(key)) {
                                    return;
                                }
                            }

                        }
                        Marker marker = nMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.latitude, location.longitude))
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

    private void friend() {
        AlertDialog.Builder alerta = new AlertDialog.Builder(MapClienteActivity.this);
        alerta.setTitle("Amiga elegida");
        alerta.setMessage("Si tomas no manejes.... \nTe colocamos una conductora de reemplazo")
                .setCancelable(false);
        AlertDialog titulo = alerta.create();
        titulo.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                titulo.dismiss();
            }
        }, 3000);
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
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                if (gpsActived()) {
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    nMap.setMyLocationEnabled(true);
                } else {
                    showAlertDialogNoGPS();
                }
            } else {
                checkPermissionsLocation();
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

    private void logout() {
        new PreferencesManager(this).setIsClient(false);
        Intent intent = new Intent(MapClienteActivity.this, MainActivity.class);
        startActivity(intent);
        MapClienteActivity.this.finish();
    }

    private void moveToDetailRequestDriver() {
        if (mOriginLatLng != null && mDestinationLatLng != null) {
            Intent intent = new Intent(MapClienteActivity.this, DetailRequestActivity.class);
            intent.putExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN, mOrigin);
            intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LAT, mOriginLatLng.latitude);
            intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LONG, mOriginLatLng.longitude);
            intent.putExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO, mDestination);
            intent.putExtra(Constants.Extras.EXTRA_DESTINO_LAT, mDestinationLatLng.latitude);
            intent.putExtra(Constants.Extras.EXTRA_DESTINO_LONG, mDestinationLatLng.longitude);
            intent.putExtra(Constants.Extras.EXTRA_SERVICE_TYPE, mServiceType);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Debe Seleccionar el lugar de recogida y el destino", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveToEditProfile() {
        Intent intent = new Intent(MapClienteActivity.this, ProfileClientActivity.class);
        startActivity(intent);
    }

    private void moveToHistoryBooking() {
        Intent intent = new Intent(MapClienteActivity.this, HistoryBookingClientActivity.class);
        startActivity(intent);
    }

    private void limitSearch() {
        LatLng northSide = SphericalUtil.computeOffset(mOriginLatLng, 10000, 0);
        LatLng southSide = SphericalUtil.computeOffset(mOriginLatLng, 10000, 180);
        autocompleteOrigin.setCountry("PE");
        autocompleteOrigin.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
        autocompleteDestino.setCountry("PE");
        autocompleteDestino.setLocationBias(RectangularBounds.newInstance(southSide, northSide));
    }

    private void checkPermissionsLocation() {
        Log.i(TAG, "checkPermissionLocation: ");
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .onExplainRequestReason((scope, deniedList, beforeRequest) -> {
                    scope.showRequestReasonDialog(deniedList,
                            "Para un buen uso de la aplicación es necesario que habilite los permisos correspodientes",
                            "Aceptar",
                            "Cancelar");
                })

                .onForwardToSettings((scope, deniedList) -> {
                    scope.showForwardToSettingsDialog(deniedList,
                            "Para continuar con el uso de la aplicación es necesario que habilite los permisos de manera manual",
                            "Config. manual",
                            "Cancelar");
                })
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Log.i(TAG, "checkPermissionLocation si tiene permisos: ");
                        if (gpsActived()) {
                            if (ActivityCompat.checkSelfPermission(MapClienteActivity.this,
                                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                    ActivityCompat.checkSelfPermission(MapClienteActivity.this,
                                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                checkPermissionsLocation();
                                return;
                            } else {
                                nMapFragment.getMapAsync(this);
                                mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                                if (nMap != null)
                                    nMap.setMyLocationEnabled(true);
                            }
                        } else {
                            showAlertDialogNoGPS();
                        }
                    }
                });
    }

    private void checkPermissionCall() {
        Log.i(TAG, "checkPermissionCall: ");
        PermissionX.init(this)
                .permissions(Manifest.permission.CALL_PHONE)
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
                        Log.i(TAG, "checkPermissionCall si tiene permisos: ");
                        calling();
                    }
                });
    }

    private void calling() {
        String dial = "tel:+51941174386";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse(dial));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void textBottonRequest(@ServiceType int serviceType) {
        String message;
        switch (serviceType) {
            case ServiceType.TAXI:
                message = "Solicitar Taxi";
                break;
            case ServiceType.INTRA_URBANO:
                message = "Solicitar Intra-Urbano";
                break;
            case ServiceType.DELIVERY:
                message = "Solicitar Delivery";
                break;
            case ServiceType.MESSAGING:
                message = "Solicitar Mensajeria";
                break;
            case ServiceType.CARGA:
                message = "Solicitar Carga";
                break;
            case ServiceType.PET:
                message = "Solicitar Mascota";
                break;
            case ServiceType.FRIEND:
                message = "Solicitar Amiga";
                break;
            default:
                message = "Solicitar Taxi";
                break;
        }
        binding.rootLayout.btnRequestDrive.setText(message);
    }
}