package pe.com.android.femtaxi.client;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityMapClientBookingBinding;
import pe.com.android.femtaxi.driver.MapDriveBookingActivity;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.Client;
import pe.com.android.femtaxi.models.ClientBooking;
import pe.com.android.femtaxi.models.Driver;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.DriverProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;
import pe.com.android.femtaxi.providers.GoogleApiProvider;
import pe.com.android.femtaxi.utils.DecodePoints;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapClientBookingActivity extends AppCompatActivity implements OnMapReadyCallback {

    String TAG = MapClientBookingActivity.class.getSimpleName();

    private ActivityMapClientBookingBinding binding;

    private Marker nMarker;
    private Marker nMarkerDriver;

    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;
    private GoogleApiProvider mGoogleApiProvider;
    private GeofireProvider mGeofireProvider;
    private DriverProvider mDriverProvider ;
    private final static int LOCATION_REQUEST_CODE = 100;
    private final static int SETTINGS_REQUEST_CODE = 200;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocation;
    private LatLng mDestinationLatLng;
    private LatLng mOriginLatLng;
    private LatLng mDriverLatLng;
    private LatLng mCurrentLatLng;
    private LatLngBounds.Builder builder = LatLngBounds.builder();
    private LatLngBounds bounds = null;
    private List<LatLng> mPolyLinesList;
    private PolylineOptions mPolylineOptions;
    private String idDriver;
    private boolean isFirstTime = true;

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
                    /*Log.d(TAG, "mLocationCallback mCurrentLatLng: " + mCurrentLatLng);
                    nMarker = nMap.addMarker(new MarkerOptions().position(
                            new LatLng(location.getLatitude(), location.getLongitude()))
                            .title("Posición Actual")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_location)));

                    nMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(mCurrentLatLng)
                                    .zoom(16f)
                                    .build()));*/
                    startLocation();
                    if (isFirstTime) {
                        isFirstTime = false;
                        getClientBooking();
                    }
                    Log.d(TAG, "mLocationCallback getLatitude: " + location.getLatitude() + ", getLongitude: " + location.getLongitude());
                }
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMapClientBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Mapa Cliente en viaje");
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mGoogleApiProvider = new GoogleApiProvider(this);
        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_WORKING);
        mDriverProvider = new DriverProvider();
        mFusedLocation = LocationServices.getFusedLocationProviderClient(this);
        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);
        checkLocationPermissions();
        checkStatusClientBooking();
        getDriver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (gpsActived()) {
                        startLocation();
                    } else {
                        showAlertDialogNoGPS();    //mensaje DialogGPS
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
                                ActivityCompat.requestPermissions(MapClientBookingActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
                            }
                        })
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(MapClientBookingActivity.this,
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

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ClientBooking clientBooking = documentSnapshot.toObject(ClientBooking.class);
                            Log.d(TAG, "clientBooking: " + clientBooking);
                            idDriver = clientBooking.getIdDriver();
                            mOriginLatLng = new LatLng(clientBooking.getOriginLat(), clientBooking.getOriginLong());
                            mDestinationLatLng = new LatLng(clientBooking.getDestinationLat(), clientBooking.getDestinationLong());
                            binding.txtOrigin.setText("Recoger en. " + clientBooking.getOrigin());
                            binding.txtDestino.setText("Destino: " + clientBooking.getDestination());
                            nMap.addMarker(new MarkerOptions()
                                    .position(mOriginLatLng)
                                    .title(clientBooking.getOrigin())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.azuliconomarker)));
                            nMap.addMarker(new MarkerOptions()
                                    .position(mDestinationLatLng)
                                    .title(clientBooking.getOrigin())
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.rojoiconomarker)));

                            builder.include(mOriginLatLng);
                            //builder.include(mDestinationLatLng);
                            bounds = builder.build();
                            CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                            nMap.animateCamera(camera);

                            getDriverLocation(idDriver);
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

                            builder.include(mCurrentLatLng);
                            builder.include(destino);
                            bounds = builder.build();
                            CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                            nMap.animateCamera(camera);
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

    private void getDriverLocation(String idDriver) {
        mGeofireProvider.getDriveLocation(idDriver)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            double lat = Double.parseDouble(snapshot.child("0").getValue().toString());
                            double lng = Double.parseDouble(snapshot.child("1").getValue().toString());
                            mDriverLatLng = new LatLng(lat, lng);
                            if (nMarkerDriver != null)
                                nMarkerDriver.remove();
                            nMarkerDriver = nMap.addMarker(new MarkerOptions()
                                    .position(mDriverLatLng)
                                    .title("Tu conductor")
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.iconogps)));

                            drawRoute(mOriginLatLng);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void checkStatusClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId())
                .addSnapshotListener((value, error) -> {
                    if (value.exists()) {
                        ClientBooking clientBooking = value.toObject(ClientBooking.class);
                        Log.d(TAG, "sendNotification clientBooking: " + clientBooking);
                        if (clientBooking.getStatus().equals("accept")) {
                            binding.txtStatusBooking.setText("En camino");
                        } else if (clientBooking.getStatus().equals("cancel")) {
                            binding.txtStatusBooking.setText("Cancelado");
                        } else if (clientBooking.getStatus().equals("start")) {
                            binding.txtStatusBooking.setText("Iniciado");
                            startBooking();
                        } else if (clientBooking.getStatus().equals("finish")) {
                            binding.txtStatusBooking.setText("Culminado");
                            finishBooking();
                        }
                    }
                });
    }

    private void startBooking() {
        nMap.clear();
        nMap.addMarker(new MarkerOptions()
                .position(mDestinationLatLng)
                .title("destino")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rojoiconomarker)));
        drawRoute(mDestinationLatLng);
    }

    private void finishBooking() {
        Intent intent = new Intent(MapClientBookingActivity.this, CalificationClientActivity.class);
        startActivity(intent);
        this.finish();
    }

    private void getDriver() {
        mDriverProvider.getDataUser(idDriver)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Driver driver = documentSnapshot.toObject(Driver.class);
                            Drawable placeholder = getResources().getDrawable(R.drawable.ic_login_user);
                            Glide.with(MapClientBookingActivity.this)
                                    .load(driver.getPhoto())
                                    .placeholder(placeholder)
                                    .error(placeholder)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .into(binding.imgUser);
                            binding.txtNameUser.setText(driver.getName()+" "+driver.getApellido());
                            binding.txtEmailUser.setText(driver.getCorreo());
                        }
                    }
                });
    }
}
