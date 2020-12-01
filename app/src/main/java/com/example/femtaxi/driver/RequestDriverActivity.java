package com.example.femtaxi.driver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.femtaxi.R;
import com.example.femtaxi.databinding.ActivityRegisterBinding;
import com.example.femtaxi.databinding.ActivityRequestDriverBinding;
import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.providers.GeofireProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class RequestDriverActivity extends AppCompatActivity {

    ActivityRequestDriverBinding binding;

    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;

    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.animation.playAnimation();
        mGeofireProvider = new GeofireProvider(Constans.DRIVER_ACTIVE);

        mExtraOriginLat = getIntent().getDoubleExtra(Constans.Extras.ORIGIN_LAT, 0);
        mExtraOriginLng = getIntent().getDoubleExtra(Constans.Extras.ORIGIN_LONG, 0);

        binding.btnCancelViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getClosestDriver();
            }
        });
        LatLng latLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        if (latLng != null) {
            getClosestDriver(latLng);
        }
    }

    private void getClosestDriver(LatLng LatLng) {
        mGeofireProvider.getActiveDrivers(LatLng, mRadius)
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {

                        if (!mDriverFound) {
                            mDriverFound = true;
                            mIdDriverFound = key;
                            mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                            binding.textViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");

                            Log.d("DRIVER", "ID: " + mIdDriverFound);
                        }

                    }

                    @Override
                    public void onKeyExited(String key) {

                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {

                    }

                    @Override
                    public void onGeoQueryReady() {
                        if (!mDriverFound) {
                            mRadius = mRadius + 0.1f;
                            if (mRadius > 5) {
                                binding.textViewLookingFor.setText("NO SE ENCONTRO UN CONDUCTOR");
                                Toast.makeText(RequestDriverActivity.this, "NO SE ENCONTRO UN CONDUCTOR", Toast.LENGTH_SHORT).show();
                                return;
                            } else {
                                getClosestDriver(LatLng);
                            }
                        }
                    }

                    @Override
                    public void onGeoQueryError(DatabaseError error) {

                    }
                });
    }
}