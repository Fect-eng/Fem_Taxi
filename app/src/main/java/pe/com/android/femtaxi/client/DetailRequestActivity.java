package pe.com.android.femtaxi.client;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.LegStep;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.permissionx.guolindev.PermissionX;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.annotation.ServiceType;
import pe.com.android.femtaxi.databinding.ActivityDetailRequestBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.Info;
import pe.com.android.femtaxi.providers.InfoProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.api.directions.v5.DirectionsCriteria.GEOMETRY_POLYLINE;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND;
import static com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    static String TAG = DetailRequestActivity.class.getSimpleName();
    private ActivityDetailRequestBinding binding;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinoLat;
    private double mExtradestinoLng;
    private String mExtraOrigin;
    private String mExtraDestination;
    @ServiceType
    private int mServiceType;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private InfoProvider mInfoProvider;
    private LatLngBounds.Builder builder = new LatLngBounds.Builder();
    private LatLngBounds bounds = null;
    private double mPrice;

    private MapboxDirections mapboxDirectionsClient;
    private Handler handler = new Handler();
    private Runnable runnable;
    private MapboxMap mMapboxMap;
    private SymbolManager mSymbolManager;
    private Bitmap iconOrigin, iconDestination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityDetailRequestBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        mInfoProvider = new InfoProvider();

        mExtraOrigin = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN);
        mExtraOriginLat = getIntent().getDoubleExtra(Constants.Extras.EXTRA_ORIGIN_LAT, 0);
        mExtraOriginLng = getIntent().getDoubleExtra(Constants.Extras.EXTRA_ORIGIN_LONG, 0);
        mExtraDestination = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO);
        mExtraDestinoLat = getIntent().getDoubleExtra(Constants.Extras.EXTRA_DESTINO_LAT, 0);
        mExtradestinoLng = getIntent().getDoubleExtra(Constants.Extras.EXTRA_DESTINO_LONG, 0);
        mServiceType = getIntent().getIntExtra(Constants.Extras.EXTRA_SERVICE_TYPE, 0);

        iconOrigin = BitmapFactory.decodeResource(getResources(), R.drawable.azuliconomarker);
        iconDestination = BitmapFactory.decodeResource(getResources(), R.drawable.rojoiconomarker);

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinoLat, mExtradestinoLng);

        binding.txtOrigin.setText(mExtraOrigin);
        binding.txtDestino.setText(mExtraDestination);

        binding.btnBackPresset.setOnClickListener(view -> {
            this.finish();
        });

        binding.btnSolicitar.setOnClickListener((v) -> {
            if (mPrice > 0)
                moveToRequestDriver();
            else {
                if (mMapboxMap != null)
                    drawRoute(mMapboxMap);
            }
        });

        textBottonRequest(mServiceType);
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

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
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
            mMapboxMap.getUiSettings().setAllGesturesEnabled(false);
            mMapboxMap.getUiSettings().setAllVelocityAnimationsEnabled(true);
            drawRoute(mMapboxMap);

            mSymbolManager = new SymbolManager(binding.mapView, mMapboxMap, style);
            mMapboxMap.getStyle().addImage("origin", iconOrigin);
            mMapboxMap.getStyle().addImage("destination", iconDestination);
            mSymbolManager.create(new SymbolOptions()
                    .withLatLng(mOriginLatLng)
                    .withIconImage("origin")
                    .withIconOffset(new Float[]{0f, -8f}));
            mSymbolManager.create(new SymbolOptions()
                    .withLatLng(mDestinationLatLng)
                    .withIconImage("destination")
                    .withIconOffset(new Float[]{0f, -8f}));
            builder.include(mOriginLatLng);
            builder.include(mDestinationLatLng);
            bounds = builder.build();
            com.mapbox.mapboxsdk.camera.CameraUpdate camera = com.mapbox.mapboxsdk.camera.CameraUpdateFactory.newLatLngBounds(bounds,
                    100,
                    100,
                    100,
                    100);
            mapboxMap.animateCamera(camera);
        });
    }

    private void checkLocationPermissions() {
        Log.i(TAG, "checkPermissionStorageCamera si tiene permisos: ");
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

    private void moveToRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN, mExtraOrigin);
        intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LAT, mExtraOriginLat);
        intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LONG, mExtraOriginLng);
        intent.putExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO, mExtraDestination);
        intent.putExtra(Constants.Extras.EXTRA_DESTINO_LAT, mExtraDestinoLat);
        intent.putExtra(Constants.Extras.EXTRA_DESTINO_LONG, mExtradestinoLng);
        intent.putExtra(Constants.Extras.EXTRA_SERVICE_TYPE, mServiceType);
        intent.putExtra(Constants.Extras.EXTRA_PRICE, mPrice);
        startActivity(intent);
        this.finish();
    }

    private void drawRoute(MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;
        mapboxDirectionsClient = MapboxDirections.builder()
                .origin(Point.fromLngLat(mExtraOriginLng, mExtraOriginLat))
                .destination(Point.fromLngLat(mExtradestinoLng, mExtraDestinoLat))
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
                    binding.txtTimeAndDistance.setText(String.format("%.2f", distance) + " KM , " + String.format("%.2f", minutes) + " Minutos");
                    calcularPrice(distance, minutes);
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

    private void calcularPrice(double distanceValor, double minutosValor) {
        mInfoProvider.getInfo()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.exists()) {
                        Info info = snapshots.toObject(Info.class);
                        Log.d(TAG, "calcularPrice addOnSuccessListener info: " + info);
                        double totalDistance = distanceValor * info.getKm();
                        double totalMinutes = minutosValor * info.getMin();
                        mPrice = totalDistance + totalMinutes;
                        viewPrice(mPrice);
                    } else {
                        Log.d(TAG, "calcularPrice addOnSuccessListener info vacia: ");
                    }
                })
                .addOnFailureListener((e) -> {
                    Log.d(TAG, "calcularPrice addOnFailureListener Error: " + e.getMessage());
                });
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
        binding.btnSolicitar.setText(message);
    }

    private void viewPrice(double price) {
        switch (mServiceType) {
            case ServiceType.INTRA_URBANO:
            case ServiceType.DELIVERY:
            case ServiceType.MESSAGING:
            case ServiceType.CARGA:
            case ServiceType.PET:
            case ServiceType.FRIEND:
                price = price + 5;
                break;
        }
        binding.txtPrice.setText("S/ " + String.format("%.2f", price));
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


