package pe.com.android.femtaxi.ui.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DatabaseError;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.permissionx.guolindev.PermissionX;

import org.jetbrains.annotations.NotNull;

import pe.com.android.femtaxi.MainActivity;
import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.annotation.ServiceType;
import pe.com.android.femtaxi.databinding.ActivityMapClienteBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.helpers.PreferencesManager;
import pe.com.android.femtaxi.providers.GeofireProvider;
import pe.com.android.femtaxi.utils.Utils;

public class MapClienteActivity extends AppCompatActivity implements OnMapReadyCallback {

    String TAG = MapClienteActivity.class.getSimpleName();

    private ActivityMapClienteBinding binding;
    private GeofireProvider mGeofireProvider;

    private final static int SETTINGS_REQUEST_CODE = 2;

    private LocationComponent mLocationComponent;
    private ImageView hoveringMarker;
    private ActivityResultLauncher<Intent> mResultOrigin;
    private ActivityResultLauncher<Intent> mResultDestination;
    private MapboxMap mMapboxMap;
    private LatLng mOriginLatLong, mDestinoLatLong;
    private String mAddressOrigin, mAddressDestino;

    @ServiceType
    private int mServiceType = ServiceType.TAXI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapClienteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Mapbox.getInstance(this, getString(R.string.access_token));
        initResult();
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

        checkPermissionsLocation();

        binding.containerMap.btnRequestDrive.setOnClickListener((view) -> {
            moveToDetailRequestDriver();
        });

        binding.containerMap.btnMenu.setOnClickListener((view) -> {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        instanceAutoCompleteOrigin();
        instanceAutoCompleteDestino();

        binding.containerMap.btnLlamada.setOnClickListener((v) -> {
            checkPermissionCall();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*if (mFusedLocation != null)
            mFusedLocation.removeLocationUpdates(mLocationCallback);*/
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull @NotNull MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, (style) -> {
            mapboxMap.getUiSettings().setLogoEnabled(false);
            mapboxMap.getUiSettings().setAttributionEnabled(false);
            mLocationComponent = mapboxMap.getLocationComponent();
            mLocationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(
                    this, style).build());
            mLocationComponent.setLocationComponentEnabled(true);
            mLocationComponent.setCameraMode(CameraMode.TRACKING);
            mLocationComponent.setRenderMode(RenderMode.NORMAL);

            hoveringMarker = new ImageView(this);

            hoveringMarker.setImageDrawable(getResources().getDrawable(R.drawable.mapilogs));
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
            hoveringMarker.setLayoutParams(params);
            binding.containerMap.mapView.addView(hoveringMarker);
            //mapboxMap.getStyle().addImage(Constants.LOCATION_DRIVE_ICON_ID, iconDriver);
            mapboxMap.addOnMoveListener(new MapboxMap.OnMoveListener() {
                @Override
                public void onMoveBegin(@NonNull MoveGestureDetector detector) {
                }

                @Override
                public void onMove(@NonNull MoveGestureDetector detector) {
                }

                @Override
                public void onMoveEnd(@NonNull MoveGestureDetector detector) {
                    mOriginLatLong = mapboxMap.getCameraPosition().target;
                    locationTheMap(mOriginLatLong.getLatitude(), mOriginLatLong.getLongitude());
                }
            });
            new Handler().postDelayed(() -> {

                mOriginLatLong = new LatLng(
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()
                );
                locationTheMap(
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude(),
                        mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude()
                );
                getActiveDrivers(mOriginLatLong);
            }, 1500);
        });
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
            binding.containerMap.mapView.getMapAsync(this::onMapReady);
        } else if (requestCode == SETTINGS_REQUEST_CODE && !gpsActived()) {
            showAlertDialogNoGPS();
        }
    }

    private void instanceAutoCompleteOrigin() {
        binding.containerMap.cardOrigin.setOnClickListener(view -> {
            mResultOrigin.launch(moveToSelectLocation());
        });
    }

    private void instanceAutoCompleteDestino() {
        binding.containerMap.cardDestino.setOnClickListener(view -> {
            mResultDestination.launch(moveToSelectLocation());
        });
    }

    private void getActiveDrivers(LatLng origin) {
        mGeofireProvider.getActiveDrivers(
                new com.google.android.gms.maps.model.LatLng(
                        origin.getLatitude(),
                        origin.getLongitude()
                ),
                10)
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        /*for (Marker marker : mDriversMarkers) {
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
                        mDriversMarkers.add(marker);*/
                    }

                    @Override
                    public void onKeyExited(String key) {/*
                        for (Marker marker : mDriversMarkers) {
                            if (marker.getTag() != null) {
                                if (marker.getTag().equals(key)) {
                                    marker.remove();
                                    mDriversMarkers.remove(marker);
                                    return;
                                }
                            }

                        }*/
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        /*for (Marker marker : mDriversMarkers) {
                            if (marker.getTag() != null) {
                                if (marker.getTag().equals(key)) {
                                    marker.setPosition(new LatLng(location.latitude, location.longitude));
                                }
                            }

                        }*/
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

    private void logout() {
        new PreferencesManager(this).setIsClient(false);
        Intent intent = new Intent(MapClienteActivity.this, MainActivity.class);
        startActivity(intent);
        MapClienteActivity.this.finish();
    }

    private void moveToDetailRequestDriver() {
        if (!TextUtils.isEmpty(binding.containerMap.txtOrigin.getText().toString().trim()) &&
                !TextUtils.isEmpty(binding.containerMap.txtDestino.getText().toString().trim())) {
            Intent intent = new Intent(MapClienteActivity.this, DetailRequestActivity.class);
            intent.putExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN, mAddressOrigin);
            intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LAT, mOriginLatLong.getLatitude());
            intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LONG, mOriginLatLong.getLongitude());
            intent.putExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO, mAddressDestino);
            intent.putExtra(Constants.Extras.EXTRA_DESTINO_LAT, mDestinoLatLong.getLatitude());
            intent.putExtra(Constants.Extras.EXTRA_DESTINO_LONG, mDestinoLatLong.getLongitude());
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
                            binding.containerMap.mapView.getMapAsync(this::onMapReady);
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
            default:
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
        }
        binding.containerMap.btnRequestDrive.setText(message);
    }

    private void initResult() {

        mResultOrigin = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                (resultCode) -> {
                    CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(resultCode.getData());
                    double lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                    double lon = ((Point) selectedCarmenFeature.geometry()).longitude();
                    mOriginLatLong = new LatLng(
                            lat,
                            lon
                    );
                    mAddressOrigin = Utils.getStreet(this, lat, lon);
                    binding.containerMap.txtOrigin.setText(mAddressOrigin);
                });

        mResultDestination = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                (resultCode) -> {
                    CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(resultCode.getData());
                    double lat = ((Point) selectedCarmenFeature.geometry()).latitude();
                    double lon = ((Point) selectedCarmenFeature.geometry()).longitude();
                    mDestinoLatLong = new LatLng(
                            lat,
                            lon
                    );
                    mAddressDestino = Utils.getStreet(this, lat, lon);
                    binding.containerMap.txtDestino.setText(mAddressDestino);
                });
    }

    private Intent moveToSelectLocation() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.access_token))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        .country("PE")
                        .build(PlaceOptions.MODE_CARDS))
                .build(this);
        return intent;
    }

    private void locationTheMap(Double latitude, Double longitude) {
        mAddressOrigin = Utils.getStreet(this, latitude, longitude);
        binding.containerMap.txtOrigin.setText(mAddressOrigin);
        LatLng latLng = new LatLng(latitude, longitude);
        CameraPosition position = new CameraPosition.Builder()
                .target(latLng)
                .zoom(14)
                .bearing(0)
                .tilt(0)
                .build();

        mMapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 7000);
    }
}