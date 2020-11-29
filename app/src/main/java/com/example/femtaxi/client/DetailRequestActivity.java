package com.example.femtaxi.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.femtaxi.R;
import com.example.femtaxi.databinding.ActivityDetailRequestBinding;
import com.example.femtaxi.driver.RequestDriverActivity;
import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.providers.GoogleApiProvider;
import com.example.femtaxi.utils.DecodePoints;
import com.example.femtaxi.utils.Utils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    String TAG = DetailRequestActivity.class.getSimpleName();
    private Button mButtonRequest;
    private ActivityDetailRequestBinding binding;

    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinoLat;
    private double mExtradestinoLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private List<LatLng> mPolyLinesList;
    private PolylineOptions mPolylineOptions;
    private LatLngBounds.Builder builder = LatLngBounds.builder();
    private LatLngBounds bounds = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //button
       // btnRequestNow
        mButtonRequest = findViewById(R.id.btnRequestNow);
        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestDriver();
            }
        });
        //button


        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);

        setSupportActionBar(binding.includeToolbar.toolbar);
        getSupportActionBar().setTitle("Detalle Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGoogleApiProvider = new GoogleApiProvider(this);

        mExtraOriginLat = getIntent().getDoubleExtra(Constans.Extras.ORIGIN_LAT, 0);
        mExtraOriginLng = getIntent().getDoubleExtra(Constans.Extras.ORIGIN_LONG, 0);
        mExtraDestinoLat = getIntent().getDoubleExtra(Constans.Extras.DESTINO_LAT, 0);
        mExtradestinoLng = getIntent().getDoubleExtra(Constans.Extras.DESTINO_LONG, 0);

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinoLat, mExtradestinoLng);

        String addressOrigin = Utils.getStreet(this, mExtraOriginLat, mExtraOriginLng);
        String addressDestino = Utils.getStreet(this, mExtraDestinoLat, mExtradestinoLng);
        binding.textViewOrigin.setText(addressOrigin);        //cambio de variable
        binding.textViewDestination.setText(addressDestino);  //cambio de variable

    }

    private void goToRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra("origin_lat", mOriginLatLng.latitude);
        intent.putExtra("origin_lng", mOriginLatLng.longitude);
        startActivity(intent);
        finish();
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        nMap.setMyLocationEnabled(true);

        nMap.addMarker(new MarkerOptions()
                .position(mOriginLatLng)
                .title("Origen")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rojoiconomarker)));
        nMap.addMarker(new MarkerOptions()
                .position(mDestinationLatLng)
                .title("Destino")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.azuliconomarker)));

        builder.include(mOriginLatLng);
        builder.include(mDestinationLatLng);
        bounds = builder.build();
        CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        nMap.animateCamera(camera);
        drawRoute();
    }

    private void drawRoute() {
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            Log.d(TAG, "drawRoute jsonObject: " + jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                            Log.d(TAG, "drawRoute jsonArray: " + jsonArray);
                            JSONObject route = jsonArray.getJSONObject(0);
                            Log.d(TAG, "drawRoute route: " + route);
                            JSONObject polyLines = route.getJSONObject("overview_polilyne");
                            Log.d(TAG, "drawRoute polyLines: " + polyLines);
                            String points = polyLines.getString("point");
                            Log.d(TAG, "drawRoute points: " + points);
                            mPolyLinesList = DecodePoints.decodePoly(points);
                            mPolylineOptions = new PolylineOptions();
                            mPolylineOptions.color(Color.DKGRAY);
                            mPolylineOptions.width(8f);
                            mPolylineOptions.startCap(new SquareCap());
                            mPolylineOptions.jointType(JointType.ROUND);
                            mPolylineOptions.addAll(mPolyLinesList);
                            nMap.addPolyline(mPolylineOptions);
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
}


