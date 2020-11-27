package com.example.femtaxi.client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import com.example.femtaxi.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;
    Toolbar mToolbar;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinationLat;
    private double mExtradestinationLng;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_request);

    nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
    nMapFragment.getMapAsync((OnMapReadyCallback) this);

        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Detalle Cliente");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mExtraOriginLat = getIntent().getDoubleExtra("origin_lat", 0);
        mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);
      //   mExtraDestinationLat =  getIntent().getDoubleExtra("destination_lat", 0);
      //  mExtradestinationLng =  getIntent().getDoubleExtra("destination_lng", 0);

        mOriginLatLng = new LatLng(mExtraDestinationLat, mExtradestinationLng);
       // mDestinationLatLng = new LatLng(mExtraDestinationLat, mExtradestinationLng);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        nMap.getUiSettings().setZoomControlsEnabled(true);
        nMap.setMyLocationEnabled(true);

        nMap.addMarker(new MarkerOptions().position(mOriginLatLng).title("Origen").icon(BitmapDescriptorFactory.fromResource(R.drawable.rojoiconomarker)));
       // nMap.addMarker(new MarkerOptions().position(mDestinationLatLng).title("Destino").icon(BitmapDescriptorFactory.fromResource(R.drawable.azuliconomarker)));

        nMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                .target(mOriginLatLng)
                .zoom(14f)
                .build()
        ));
    }
}


