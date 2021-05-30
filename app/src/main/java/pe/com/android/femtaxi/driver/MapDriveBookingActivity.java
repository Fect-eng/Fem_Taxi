package pe.com.android.femtaxi.driver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
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
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.permissionx.guolindev.PermissionX;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.annotation.StatusTrip;
import pe.com.android.femtaxi.client.MapClientBookingActivity;
import pe.com.android.femtaxi.databinding.ActivityMapDriverBookingBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.Client;
import pe.com.android.femtaxi.models.ClientBooking;
import pe.com.android.femtaxi.models.FCMResponse;
import pe.com.android.femtaxi.models.FieldNotification;
import pe.com.android.femtaxi.models.Info;
import pe.com.android.femtaxi.models.PushNotification;
import pe.com.android.femtaxi.models.ServiceNotification;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.ClientProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;
import pe.com.android.femtaxi.providers.GoogleApiProvider;
import pe.com.android.femtaxi.providers.InfoProvider;
import pe.com.android.femtaxi.providers.NotificationProvider;
import pe.com.android.femtaxi.utils.DecodePoints;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.api.directions.v5.DirectionsCriteria.GEOMETRY_POLYLINE;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MapDriveBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    static String TAG = MapDriveBookingActivity.class.getSimpleName();

    private ActivityMapDriverBookingBinding binding;

    private LatLng mCurrentLatLng;

    private ClientProvider mClientProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;
    private NotificationProvider mNotificacionProvider;
    private InfoProvider mInfoProvider;
    private final static int SETTINGS_REQUEST_CODE = 200;

    private boolean mIsFistTime = true;
    private boolean mClientBoarding = false;

    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private LatLngBounds bounds = null;

    private double mDistanceAndMeters = 1;
    private int mMinutes = 0;
    private int mSeconds = 0;
    boolean mSecondsIsOver = false;
    private Handler mHandler = new Handler();
    private Handler mHandlerLocation = new Handler();
    private String statusBooking = "create";
    private Runnable runnableLocation;
    private String clientId;

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mSeconds++;
            if (!mSecondsIsOver) {
                binding.txtPrice.setText(mSeconds + " segundos");
            } else {
                binding.txtPrice.setText(mMinutes + " minutos " + mSeconds);
            }
            if (mSeconds == 59) {
                mSeconds = 0;
                mSecondsIsOver = true;
                mMinutes++;
            }
            mHandler.postDelayed(this, 1000);
        }
    };

    private MapboxMap mMapboxMap;
    private LocationComponent mLocationComponent;
    private Bitmap iconOrigin, iconDestination, iconLocationDriver;
    private SymbolManager mSymbolManager;
    private SymbolOptions mSymbolOptionsLocation;
    private MapboxDirections mapboxDirectionsClient;

    OnIndicatorPositionChangedListener positionChangedListener = point -> {
        Log.i(TAG, "addOnIndicatorPositionChangedListener point: " + point);
        mCurrentLatLng = new LatLng(point.latitude(), point.longitude());

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
            if (mSymbolManager != null)
                mSymbolManager.create(mSymbolOptionsLocation);
        } else {
            mSymbolOptionsLocation.withLatLng(mCurrentLatLng);
        }

        updateLocation(mCurrentLatLng);
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapDriverBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        clientId = getIntent().getStringExtra(Constants.Extras.EXTRA_CLIENT_ID);
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_WORKING);
        mAuthProvider = new AuthProvider();
        mNotificacionProvider = new NotificationProvider();
        mInfoProvider = new InfoProvider();
        iconOrigin = BitmapFactory.decodeResource(getResources(), R.drawable.azuliconomarker);
        iconDestination = BitmapFactory.decodeResource(getResources(), R.drawable.rojoiconomarker);
        iconLocationDriver = BitmapFactory.decodeResource(getResources(), R.drawable.iconogps);

        checkLocationPermissions();
        getClient(clientId);
        getInfo();

        binding.btnStartBooking.setOnClickListener((view) -> {
            if (mClientBoarding) {
                getClientBooking(clientId, StatusTrip.BOARDING);
            } else {
                Toast.makeText(MapDriveBookingActivity.this, "Debe estar mas cerca para poder inicar el viaje", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnEndBooking.setOnClickListener((view) -> {
            getClientBooking(clientId, StatusTrip.FINISH);
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull @NotNull MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;
        mMapboxMap.setStyle(new Style.Builder().fromUri(Style.LIGHT)
                .withSource(new GeoJsonSource(Constants.DRIVING_ROUTE_POLYLINE_SOURCE_ID))
                .withLayerBelow(new LineLayer(Constants.DRIVING_ROUTE_POLYLINE_LINE_LAYER_ID,
                        Constants.DRIVING_ROUTE_POLYLINE_SOURCE_ID)
                        .withProperties(
                                lineWidth(Constants.NAVIGATION_LINE_WIDTH),
                                lineOpacity(Constants.NAVIGATION_LINE_OPACITY),
                                lineCap(LINE_CAP_ROUND),
                                lineJoin(LINE_JOIN_ROUND),
                                lineColor(getResources().getColor(R.color.colorRed))
                        ), "layer-id"), (style) -> {
            mMapboxMap.getUiSettings().setLogoEnabled(false);
            mMapboxMap.getUiSettings().setAllGesturesEnabled(true);
            mMapboxMap.getUiSettings().setAllVelocityAnimationsEnabled(true);

            mLocationComponent = mMapboxMap.getLocationComponent();
            mLocationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, style)
                            .build());
            mLocationComponent.setLocationComponentEnabled(true);
            mLocationComponent.setCameraMode(CameraMode.TRACKING);
            mLocationComponent.setRenderMode(RenderMode.NORMAL);
            mLocationComponent.addOnIndicatorPositionChangedListener(positionChangedListener);

            mSymbolManager = new SymbolManager(binding.mapView, mMapboxMap, style);
            mMapboxMap.getStyle().addImage("origin", iconOrigin);
            mMapboxMap.getStyle().addImage("destination", iconDestination);
            mMapboxMap.getStyle().addImage("location", iconLocationDriver);

            getClientBooking(clientId, StatusTrip.TRIP);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE) {
            if (gpsActived()) {
                binding.mapView.getMapAsync(this::onMapReady);
            } else {
                showAlertDialogNoGPS();
            }
        } else {
            showAlertDialogNoGPS();
        }
    }

    private void checkLocationPermissions() {
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
                        Log.i(TAG, "checkPermissionStorageCamera si tiene permisos: ");
                        if (gpsActived())
                            binding.mapView.getMapAsync(this::onMapReady);
                        else
                            showAlertDialogNoGPS();
                    }
                });
    }

    private boolean gpsActived() {
        boolean isActive = false;
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            isActive = true;
        }
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

    private void updateLocation(LatLng origin) {
        Log.d(TAG, "updateLocation");
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(),
                    new com.google.android.gms.maps.model.LatLng(
                            origin.getLatitude(),
                            origin.getLongitude()
                    ));
            switch (statusBooking) {
                case "create":
                    mClientBoarding = true;
                    Toast.makeText(this, "Ya puedes iniciar tu viaje", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private void getClient(String idClient) {
        mClientProvider.getClientId(idClient)
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot.exists()) {
                        Client client = documentSnapshot.toObject(Client.class);
                        Drawable placeholder = getResources().getDrawable(R.drawable.ic_login_user);
                        Glide.with(MapDriveBookingActivity.this)
                                .load(client.getPhoto())
                                .placeholder(placeholder)
                                .error(placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .into(binding.imgUser);
                        binding.txtNameUser.setText(client.getName());
                        binding.txtEmailUser.setText(client.getEmail());
                    }
                });
    }

    private void getClientBooking(String idClient, @StatusTrip int status) {
        mClientBookingProvider.getClientBooking(idClient)
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot.exists()) {
                        ClientBooking clientBooking = documentSnapshot.toObject(ClientBooking.class);
                        Log.d(TAG, "clientBooking: " + clientBooking);
                        LatLng origin = new LatLng(clientBooking.getOriginLat(), clientBooking.getOriginLong());
                        LatLng destination = new LatLng(clientBooking.getDestinationLat(), clientBooking.getDestinationLong());
                        binding.txtOrigin.setText("Recoger en. " + clientBooking.getOrigin());
                        binding.txtDestino.setText("Destino: " + clientBooking.getDestination());
                        if (mSymbolManager != null) {
                            mSymbolManager.create(new SymbolOptions()
                                    .withLatLng(origin)
                                    .withIconImage("origin")
                                    .withIconOffset(new Float[]{0f, -8f}));
                            mSymbolManager.create(new SymbolOptions()
                                    .withLatLng(destination)
                                    .withIconImage("destination")
                                    .withIconOffset(new Float[]{0f, -8f}));
                        }
                        switch (status) {
                            case StatusTrip.TRIP:
                                drawRoute(origin, destination);
                                break;
                            case StatusTrip.BOARDING:
                                startBooking(idClient, origin, destination);
                                break;
                            case StatusTrip.FINISH:
                                finishBooking(idClient);
                                break;
                        }
                    }
                });
    }

    private void drawRoute(LatLng origin, LatLng destino) {
        mapboxDirectionsClient = MapboxDirections.builder()
                .origin(Point.fromLngLat(origin.getLongitude(), origin.getLatitude()))
                .destination(Point.fromLngLat(destino.getLongitude(), destino.getLatitude()))
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .geometries(GEOMETRY_POLYLINE)
                .alternatives(true)
                .steps(true)
                .accessToken(getResources().getString(R.string.access_token))
                .build();

        mapboxDirectionsClient.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call,
                                   Response<DirectionsResponse> response) {
                if (response.body() == null) {
                    Log.i(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Log.i(TAG, "No routes found");
                    return;
                }
                DirectionsRoute currentRoute = response.body().routes().get(0);
                Log.i(TAG, "currentRoute: " + currentRoute);
                try {
                    JSONObject jsonObject = new JSONObject(currentRoute.toJson());
                    //LogUtils.i(TAG, "jsonObject: " + jsonObject);
                    Log.i(TAG, "distance: " + jsonObject.getString("distance"));
                    Log.i(TAG, "duration: " + jsonObject.getString("duration"));
                    double distance = jsonObject.getDouble("distance") / 1000;
                    double minutes = jsonObject.getDouble("duration") / 60;
                    builder = new LatLngBounds.Builder();
                    builder.include(origin);
                    builder.include(destino);
                    bounds = builder.build();
                    CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                    mMapboxMap.animateCamera(camera);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runnableLocation = new DrawRouteRunnable(mMapboxMap, currentRoute.legs().get(0).steps(), mHandlerLocation);
                mHandlerLocation.postDelayed(runnableLocation, Constants.DRAW_SPEED_MILLISECONDS);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                //getView().showError(throwable.getMessage());
            }
        });
    }

    private void startBooking(String idClient, LatLng origin, LatLng destino) {
        mHandler.postDelayed(runnable, 1000);
        sendNotification("Viaje iniciado", idClient);
        statusBooking = "start";
        mClientBookingProvider.getUpdateStatus(idClient, statusBooking);
        binding.btnEndBooking.setVisibility(View.VISIBLE);
        binding.btnStartBooking.setVisibility(View.GONE);
        drawRoute(origin, destino);
    }

    private void finishBooking(String idClient) {
        sendNotification("Viaje Finalizado", idClient);
        statusBooking = "finish";
        mClientBookingProvider.getUpdateStatus(idClient, statusBooking);
        if (positionChangedListener != null)
            mLocationComponent.removeOnIndicatorPositionChangedListener(positionChangedListener);
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        moveToCalificationDriverActivity(idClient);
    }

    private void sendNotification(String status, String idClient) {
        Log.i(TAG, "sendNotification");
        if (!TextUtils.isEmpty(idClient)) {
            String title = "ESTADO DE TU VIAJE";
            String body = "El estado de tu viaje es " + status;
            ServiceNotification serviceNotification = new ServiceNotification(
                    title,
                    body,
                    idClient,
                    null,
                    null,
                    null,
                    null
            );
            FieldNotification fieldNotification = new FieldNotification(
                    "/topics/" + idClient,
                    title,
                    body,
                    -1,
                    "high",
                    "4500s",
                    serviceNotification
            );
            PushNotification pushNotification = new PushNotification(
                    "/topics/" + idClient,
                    fieldNotification
            );
            mNotificacionProvider.sendNotification(pushNotification)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            Log.d(TAG, "sendNotification onResponse: " + response);
                            if (response.body() != null) {
                                if (!response.body().getMessage_id().isEmpty()) {
                                    Toast.makeText(MapDriveBookingActivity.this, "Notificacion enviada con exito", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MapDriveBookingActivity.this, "error al enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d(TAG, "sendNotification onFailure: " + t.getMessage());
                        }
                    });
        } else {
            Toast.makeText(MapDriveBookingActivity.this, "El cliente no cuenta con token de notificacion", Toast.LENGTH_SHORT).show();
        }
    }

    private void getInfo() {
        mInfoProvider.getInfo()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.exists()) {
                        Info mInfo = snapshots.toObject(Info.class);
                    }
                });
    }

    private void moveToCalificationDriverActivity(String idClient) {
        Intent intent = new Intent(MapDriveBookingActivity.this, CalificationDriverActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_CLIENT_ID, idClient);
        startActivity(intent);
        MapDriveBookingActivity.this.finish();
    }

    private static class DrawRouteRunnable implements Runnable {
        private MapboxMap mapboxMap;
        private List<LegStep> steps;
        private List<Feature> drivingRoutePolyLineFeatureList;
        private Handler handler;
        private int counterIndex;

        DrawRouteRunnable(MapboxMap mapboxMap, List<LegStep> steps, Handler handler) {
            this.mapboxMap = mapboxMap;
            this.steps = steps;
            this.handler = handler;
            this.counterIndex = 0;
            drivingRoutePolyLineFeatureList = new ArrayList<>();
        }

        @Override
        public void run() {
            Log.i(TAG, "DrawRouteRunnable counterIndex: " + counterIndex);
            if (counterIndex < steps.size()) {
                LegStep singleStep = steps.get(counterIndex);
                if (singleStep != null && singleStep.geometry() != null) {
                    LineString lineStringRepresentingSingleStep = LineString.fromPolyline(
                            singleStep.geometry(), 5);
                    Feature featureLineString = Feature.fromGeometry(lineStringRepresentingSingleStep);
                    drivingRoutePolyLineFeatureList.add(featureLineString);
                }
                if (mapboxMap.getStyle() != null) {
                    GeoJsonSource source = mapboxMap.getStyle().getSourceAs(
                            Constants.DRIVING_ROUTE_POLYLINE_SOURCE_ID);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(drivingRoutePolyLineFeatureList));
                    }
                }
                counterIndex++;
                handler.postDelayed(this,
                        Constants.DRAW_SPEED_MILLISECONDS);
            }
        }
    }
}
