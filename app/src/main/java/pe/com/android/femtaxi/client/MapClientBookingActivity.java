package pe.com.android.femtaxi.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityMapClientBookingBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.ClientBooking;
import pe.com.android.femtaxi.models.Driver;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.DriverProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;
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

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    static String TAG = MapClientBookingActivity.class.getSimpleName();

    private ActivityMapClientBookingBinding binding;

    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;
    private GeofireProvider mGeofireProvider;
    private DriverProvider mDriverProvider;
    private final static int LOCATION_REQUEST_CODE = 100;
    private final static int SETTINGS_REQUEST_CODE = 200;
    private LatLngBounds.Builder builder;
    private LatLngBounds bounds = null;
    private boolean isFirstTime = true;

    private MapboxDirections mapboxDirectionsClient;
    private Handler handler = new Handler();
    private Runnable runnable;
    private MapboxMap mMapboxMap;
    private LocationComponent mLocationComponent;
    private Bitmap iconOrigin, iconDestination, iconLocationDriver;
    private SymbolManager mSymbolManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        binding = ActivityMapClientBookingBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Cliente en viaje");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_WORKING);
        mDriverProvider = new DriverProvider();
        iconOrigin = BitmapFactory.decodeResource(getResources(), R.drawable.azuliconomarker);
        iconDestination = BitmapFactory.decodeResource(getResources(), R.drawable.rojoiconomarker);
        iconLocationDriver = BitmapFactory.decodeResource(getResources(), R.drawable.iconogps);
        checkLocationPermissions();
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
        binding.mapView.onDestroy();
    }

    /*@Override
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
    }*/

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    binding.mapView.getMapAsync(this::onMapReady);
                } else {
                    checkLocationPermissions();
                }
            } else {
                checkLocationPermissions();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SETTINGS_REQUEST_CODE) {
            binding.mapView.getMapAsync(this::onMapReady);
        }
    }

    @Override
    @SuppressLint("MissingPermission")
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

            mSymbolManager = new SymbolManager(binding.mapView, mMapboxMap, style);
            mMapboxMap.getStyle().addImage("origin", iconOrigin);
            mMapboxMap.getStyle().addImage("destination", iconDestination);
            mMapboxMap.getStyle().addImage("locationDriver", iconLocationDriver);

            if (isFirstTime) {
                isFirstTime = false;
                getClientBooking();
            }
        });
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
                        binding.mapView.getMapAsync(this::onMapReady);
                    }
                });
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId())
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot.exists()) {
                        ClientBooking clientBooking = documentSnapshot.toObject(ClientBooking.class);
                        Log.d(TAG, "clientBooking: " + clientBooking);
                        String idDriver = clientBooking.getIdDriver();
                        LatLng originLatLng = new LatLng(clientBooking.getOriginLat(), clientBooking.getOriginLong());
                        LatLng destinationLatLng = new LatLng(clientBooking.getDestinationLat(), clientBooking.getDestinationLong());
                        binding.txtOrigin.setText("Recoger en. " + clientBooking.getOrigin());
                        binding.txtDestino.setText("Destino: " + clientBooking.getDestination());
                        mSymbolManager.create(new SymbolOptions()
                                .withLatLng(originLatLng)
                                .withIconImage("origin")
                                //set the below attributes according to your requirements
                                .withIconSize(1.5f)
                                .withIconOffset(new Float[]{0f, -1.5f})
                                .withTextField(clientBooking.getOrigin())
                                .withTextHaloColor("rgba(255, 255, 255, 100)")
                                .withTextHaloWidth(5.0f)
                                .withTextAnchor("top")
                                .withTextOffset(new Float[]{0f, 1.5f}));

                        mSymbolManager.create(new SymbolOptions()
                                .withLatLng(destinationLatLng)
                                .withIconImage("destination")
                                //set the below attributes according to your requirements
                                .withIconSize(1.5f)
                                .withIconOffset(new Float[]{0f, -1.5f})
                                .withTextField(clientBooking.getDestination())
                                .withTextHaloColor("rgba(255, 255, 255, 100)")
                                .withTextHaloWidth(5.0f)
                                .withTextAnchor("top")
                                .withTextOffset(new Float[]{0f, 1.5f}));

                        getDriver(idDriver);
                        checkStatusClientBooking(idDriver, originLatLng, destinationLatLng);
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
                    /*binding.txtTimeAndDistance.setText(String.format("%.2f", distance) + " KM , " + String.format("%.2f", minutes) + " Minutos");
                    calcularPrice(distance, minutes);*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                runnable = new DrawRouteRunnable(mMapboxMap, currentRoute.legs().get(0).steps(), handler);
                handler.postDelayed(runnable, Constants.DRAW_SPEED_MILLISECONDS);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                //getView().showError(throwable.getMessage());
            }
        });
    }

    private void getDriverLocation(String idDriver, LatLng destino) {
        mGeofireProvider.getDriveLocation(idDriver)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                            double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                            LatLng driverLatLng = new LatLng(lat, lng);
                            mSymbolManager.create(new SymbolOptions()
                                    .withLatLng(driverLatLng)
                                    .withIconImage("locationDriver")
                                    //set the below attributes according to your requirements
                                    .withIconSize(1.5f)
                                    .withIconOffset(new Float[]{0f, -1.5f})
                                    .withTextField("Tu conductor")
                                    .withTextHaloColor("rgba(255, 255, 255, 100)")
                                    .withTextHaloWidth(5.0f)
                                    .withTextAnchor("top")
                                    .withTextOffset(new Float[]{0f, 1.5f}));

                            drawRoute(driverLatLng, destino);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkStatusClientBooking(String idDriver, LatLng origin, LatLng destino) {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId())
                .addSnapshotListener((value, error) -> {
                    if (value.exists()) {
                        ClientBooking clientBooking = value.toObject(ClientBooking.class);
                        Log.d(TAG, "sendNotification clientBooking: " + clientBooking);
                        if (clientBooking.getStatus().equals("accept")) {
                            binding.txtStatusBooking.setText("En camino");
                            getDriverLocation(idDriver, origin);
                        } else if (clientBooking.getStatus().equals("cancel")) {
                            binding.txtStatusBooking.setText("Cancelado");
                        } else if (clientBooking.getStatus().equals("start")) {
                            binding.txtStatusBooking.setText("Iniciado");
                            getDriverLocation(idDriver, destino);
                        } else if (clientBooking.getStatus().equals("finish")) {
                            binding.txtStatusBooking.setText("Culminado");
                            finishBooking();
                        }
                    }
                });
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationClientActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void getDriver(String idDriver) {
        mDriverProvider.getDataUser(idDriver)
                .get()
                .addOnSuccessListener((documentSnapshot) -> {
                    if (documentSnapshot.exists()) {
                        Driver driver = documentSnapshot.toObject(Driver.class);
                        Drawable placeholder = getResources().getDrawable(R.drawable.ic_login_user);
                        Glide.with(MapClientBookingActivity.this)
                                .load(driver.getPhoto())
                                .placeholder(placeholder)
                                .error(placeholder)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                .into(binding.imgUser);
                        binding.txtNameUser.setText(driver.getName() + " " + driver.getApellido());
                        binding.txtEmailUser.setText(driver.getCorreo());
                    }
                });
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
