package com.example.femtaxi.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.femtaxi.R;
import com.example.femtaxi.providers.GeofireProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;
    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private LatLng mOriginLatLng;
    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);


    mAnimation = findViewById(R.id.animation);
    mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
    mButtonCancelRequest = findViewById(R.id.btncancelRequest);

    mAnimation.playAnimation();
    mGeofireProvider = new GeofireProvider();

    mExtraOriginLat = getIntent().getDoubleExtra("Origin_lat", 0);
    mExtraOriginLng = getIntent().getDoubleExtra("origin_lng", 0);

  //  mOriginLatLng = new LatLng(),(mExtraOriginLat, mExtraOriginLng);

    }

    private void getClosestDriver(){
       mGeofireProvider.getActiveDrivers(mOriginLatLng, mRadius).addGeoQueryEventListener(new GeoQueryEventListener() {
           @Override
           public void onKeyEntered(String key, GeoLocation location) {

               if (!mDriverFound) {
                   mDriverFound = true;
                   mIdDriverFound = key;
                   mDriverFoundLatLng = new LatLng(location.latitude, location.longitude);
                   mTextViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO");
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
        //ingresa cunado termina la busqueda de conductor 0.1 KM
               if (!mDriverFound) {
                   mRadius = mRadius + 0.1f;

                   if (mRadius > 5){
                       Toast.makeText(RequestDriverActivity.this, "NO SE ENCONTRO CONDUCTOR", Toast.LENGTH_SHORT).show();
                       return;
                   }
                   else {
                       getClosestDriver();
                   }
               }
           }

           @Override
           public void onGeoQueryError(DatabaseError error) {

           }
       });

    }

}