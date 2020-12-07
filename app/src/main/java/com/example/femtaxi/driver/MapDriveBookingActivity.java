package com.example.femtaxi.driver;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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

import com.example.femtaxi.R;
import com.example.femtaxi.client.RequestDriverActivity;
import com.example.femtaxi.databinding.ActivityMapDriverBookingBinding;
import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.models.ClientBooking;
import com.example.femtaxi.models.FCMBody;
import com.example.femtaxi.models.FCMResponse;
import com.example.femtaxi.models.Info;
import com.example.femtaxi.models.Token;
import com.example.femtaxi.models.User;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.ClientProvider;
import com.example.femtaxi.providers.GeofireProvider;
import com.example.femtaxi.providers.GoogleApiProvider;
import com.example.femtaxi.providers.InfoProvider;
import com.example.femtaxi.providers.NotificationProvider;
import com.example.femtaxi.providers.TokenProvider;
import com.example.femtaxi.utils.DecodePoints;
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
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapDriveBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    String TAG = MapDriveBookingActivity.class.getSimpleName();

    private ActivityMapDriverBookingBinding binding;

    private LatLng mCurrentLatLng;
    private Marker nMarker;

    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    private GoogleApiProvider mGoogleApiProvider;
    private ClientProvider mClientProvider;
    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private TokenProvider mTokenProvider;
    private ClientBookingProvider mClientBookingProvider;
    private NotificationProvider mNotificacionProvider;
    private InfoProvider mInfoProvider;
    private final static int LOCATION_REQUEST_CODE = 100;
    private final static int SETTINGS_REQUEST_CODE = 200;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private String mExtraClientId = "";
    private LatLng mDestinationLatLng;
    private LatLng mOriginLatLng;

    private boolean mIsFistTime = true;
    private boolean mClientBoarding = false;
    private boolean mRideStart = false;

    private List<LatLng> mPolyLinesList;
    private PolylineOptions mPolylineOptions;
    private LatLngBounds.Builder builder = LatLngBounds.builder();
    private LatLngBounds bounds = null;

    private double mDistanceAndMeters = 1;
    private int mMinutes = 0;
    private int mSeconds = 0;
    boolean mSecondsIsOver = false;
    private Handler mHandler = new Handler();
    private Info mInfo;
    private Location mPreviusLocation = new Location("");

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

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            for (Location location : locationResult.getLocations()) {
                Log.d(TAG, "mLocationCallback location: " + location);
                if (getApplicationContext() != null) {
                    Log.d(TAG, "mLocationCallback getApplicationContext!=null: ");
                    mCurrentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (nMarker != null) {
                        nMarker.remove();
                    }

                    if (mRideStart) {
                        mDistanceAndMeters = mDistanceAndMeters + mPreviusLocation.distanceTo(location);

                    }
                    mPreviusLocation = location;
                    Log.d(TAG, "mLocationCallback mCurrentLatLng: " + mCurrentLatLng);
                    nMarker = nMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Posición Actual")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps)));

                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(mCurrentLatLng)
                                    .zoom(16f)
                                    .build()));

                    startLocation();
                    updateLocation();

                    if (mIsFistTime) {
                        mIsFistTime = false;
                        getClientBooking();
                    }

                    /*if (mClientBoarding) {
                        builder.include(mCurrentLatLng);
                        builder.include(mDestinationLatLng);
                        bounds = builder.build();
                        nMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
                        drawRoute(mCurrentLatLng, mDestinationLatLng);
                    } else {
                        if (mOriginLatLng != null &&
                                mCurrentLatLng != null) {
                            builder.include(mCurrentLatLng);
                            builder.include(mOriginLatLng);
                            bounds = builder.build();
                            nMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
                            drawRoute(mCurrentLatLng, mOriginLatLng);
                        }
                    }*/

                    Log.d(TAG, "mLocationCallback getLatitude: " + location.getLatitude() + ", getLongitude: " + location.getLongitude());
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapDriverBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Conductor");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mExtraClientId = getIntent().getStringExtra(Constants.Extras.EXTRA_CLIENT_ID);
        mGoogleApiProvider = new GoogleApiProvider(this);
        mClientProvider = new ClientProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_WORKING);
        mAuthProvider = new AuthProvider();
        mTokenProvider = new TokenProvider();
        mNotificacionProvider = new NotificationProvider();
        mInfoProvider = new InfoProvider();
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);
        checkLocationPermissions();
        getClient();
        binding.btnStartBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mClientBoarding) {
                    startBooking();
                } else {
                    Toast.makeText(MapDriveBookingActivity.this, "Debe estar mas cerca para poder inicar el viaje", Toast.LENGTH_SHORT).show();
                }
            }
        });
        binding.btnEndBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishBooking();
            }
        });
        getInfo();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        nMap.getUiSettings().setZoomControlsEnabled(true);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        nMap.setMyLocationEnabled(true);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(5);

        startLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        startLocation();
                    } else {
                        showAlertDialogNoGPS();
                    }
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
            if (gpsActived()) {
                startLocation();
            } else {
                showAlertDialogNoGPS();
            }
        } else {
            showAlertDialogNoGPS();
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
                                ActivityCompat.requestPermissions(MapDriveBookingActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapDriveBookingActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
        }
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

    private void startLocation() {
        Log.d(TAG, "startLocation");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "startLocation mayo a m");
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "startLocation permiso activado");
                if (gpsActived()) {
                    Log.d(TAG, "startLocation gps activado");
                    mFusedLocation.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                    nMap.setMyLocationEnabled(true);
                } else {
                    showAlertDialogNoGPS();
                }
            } else {
                checkLocationPermissions();
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
        if (mAuthProvider.existSession() && mCurrentLatLng != null) {
            mGeofireProvider.saveLocation(mAuthProvider.getId(), mCurrentLatLng);
            if (mOriginLatLng != null) {
                double distance = getDistanceOrigin(mOriginLatLng, mCurrentLatLng);
                if (distance <= 100) {
                    mClientBoarding = true;
                    Toast.makeText(this, "Ya puedes iniciar tu viaje", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void getClient() {
        mClientProvider.getClientId(mExtraClientId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            binding.txtNameUser.setText(user.getName());
                            binding.txtEmailUser.setText(user.getEmail());
                        }
                    }
                });
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mExtraClientId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ClientBooking clientBooking = documentSnapshot.toObject(ClientBooking.class);
                            Log.d(TAG, "clientBooking: " + clientBooking);
                            mOriginLatLng = new LatLng(clientBooking.getOriginLat(), clientBooking.getOriginLong());
                            mDestinationLatLng = new LatLng(clientBooking.getDestinationLat(), clientBooking.getDestinationLong());
                            binding.txtOrigin.setText("Recoger en. " + clientBooking.getOrigin());
                            binding.txtDestino.setText("Destino: " + clientBooking.getDestination());
                            nMap.addMarker(new MarkerOptions()
                                    .position(mOriginLatLng)
                                    .title(clientBooking.getOrigin())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.azuliconomarker)));
                            builder.include(mCurrentLatLng);
                            builder.include(mOriginLatLng);
                            bounds = builder.build();
                            nMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                            drawRoute(mOriginLatLng);
                        }
                    }
                });
    }

    private void drawRoute(LatLng destino) {
        mGoogleApiProvider.getDirections(mCurrentLatLng, destino)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            nMap.clear();
                            JSONObject jsonObject = new JSONObject(response.body());
                            Log.d(TAG, "drawRoute jsonObject: " + jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                            Log.d(TAG, "drawRoute jsonArray: " + jsonArray);
                            JSONObject route = jsonArray.getJSONObject(0);
                            Log.d(TAG, "drawRoute route: " + route);
                            JSONObject polyLines = route.getJSONObject("overview_polyline");
                            Log.d(TAG, "drawRoute polyLines: " + polyLines);
                            String points = polyLines.getString("points");
                            Log.d(TAG, "drawRoute points: " + points);
                            mPolyLinesList = DecodePoints.decodePoly(points);
                            mPolylineOptions = new PolylineOptions();
                            mPolylineOptions.color(Color.DKGRAY);
                            mPolylineOptions.width(13f);
                            mPolylineOptions.startCap(new SquareCap());
                            mPolylineOptions.jointType(JointType.ROUND);
                            mPolylineOptions.addAll(mPolyLinesList);
                            nMap.addPolyline(mPolylineOptions);

                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);
                            JSONObject distance = leg.getJSONObject("distance");
                            JSONObject duration = leg.getJSONObject("duration");
                            String distanceText = distance.getString("text");
                            String durationText = duration.getString("text");
                        } catch (Exception e) {
                            Log.d(TAG, "drawRoute Error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(TAG, "drawRoute onFailure Error: " + t.getMessage());
                    }
                });
    }

    private double getDistanceOrigin(LatLng clientLatLng, LatLng driverLatLng) {
        double distance = 0;
        Location clientLocation = new Location("");
        Location driveLocation = new Location("");
        clientLocation.setLatitude(clientLatLng.latitude);
        clientLocation.setLongitude(clientLatLng.longitude);
        driveLocation.setLatitude(driverLatLng.latitude);
        driveLocation.setLongitude(driverLatLng.longitude);
        distance = clientLocation.distanceTo(driveLocation);
        return distance;

    }

    private void startBooking() {
        mRideStart = true;
        mHandler.postDelayed(runnable, 1000);
        sendNotification("Viaje iniciado");
        mClientBookingProvider.getUpdateStatus(mExtraClientId, "start");
        binding.btnEndBooking.setVisibility(View.VISIBLE);
        binding.btnStartBooking.setVisibility(View.GONE);
        nMap.clear();
        nMap.addMarker(new MarkerOptions()
                .position(mDestinationLatLng)
                .title("destino")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rojoiconomarker)));
        drawRoute(mDestinationLatLng);
    }

    private void finishBooking() {
        sendNotification("Viaje Finalizado");
        mClientBookingProvider.getUpdateHistoryBooking(mExtraClientId);
        if (mFusedLocation != null)
            mFusedLocation.removeLocationUpdates(mLocationCallback);
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        calculateRide();
    }

    private void sendNotification(String status) {
        Log.i(TAG, "sendNotification");
        if (!TextUtils.isEmpty(mExtraClientId)) {
            mTokenProvider.getTokenUser(mExtraClientId)
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Token token = documentSnapshot.toObject(Token.class);
                                Log.d(TAG, "sendNotification onSuccess token: " + token);
                                Map<String, String> map = new HashMap<>();
                                map.put("title", "ESTADO DE TU VIAJE");
                                map.put("body", "El estado de tu viaje es " + status);
                                FCMBody fcmBody = new FCMBody(token.getToken(), "high", "4500s", map);
                                Log.d(TAG, "sendNotification onSuccess fcmBody: " + fcmBody);
                                mNotificacionProvider.sendNotification(fcmBody)
                                        .enqueue(new Callback<FCMResponse>() {
                                            @Override
                                            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                                Log.d(TAG, "sendNotification onResponse: " + response);
                                                if (response.body() != null) {
                                                    if (response.body().getSuccess() == 1) {
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
                                Toast.makeText(MapDriveBookingActivity.this, "No existe el token", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "sendNotification onFailure: " + e.getMessage());
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
                        mInfo = snapshots.toObject(Info.class);
                    }
                });
    }

    private void calculateRide() {
        if (mMinutes == 0) {
            mMinutes = 1;
        }
        double priceMin = mMinutes * mInfo.getMin();
        double pricekm = (mDistanceAndMeters / 1000) * mInfo.getKm();
        double total = priceMin + pricekm;
        mClientBookingProvider.getUpdatePrice(mExtraClientId, total)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mClientBookingProvider.getUpdateStatus(mExtraClientId, "finish");
                        Intent intent = new Intent(MapDriveBookingActivity.this, CalificationDriverActivity.class);
                        intent.putExtra(Constants.Extras.EXTRA_CLIENT_ID, mExtraClientId);
                        intent.putExtra(Constants.Extras.EXTRA_PRICE, total);
                        startActivity(intent);
                        MapDriveBookingActivity.this.finish();
                    }
                });
    }
}
