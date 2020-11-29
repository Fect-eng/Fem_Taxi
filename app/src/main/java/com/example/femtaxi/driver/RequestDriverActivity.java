package com.example.femtaxi.driver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.example.femtaxi.R;
import com.example.femtaxi.providers.GeofireProvider;

public class RequestDriverActivity extends AppCompatActivity {

    private LottieAnimationView mAnimation;
    private TextView mTextViewLookingFor;
    private Button mButtonCancelRequest;
    private GeofireProvider mGeofireProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);


    mAnimation = findViewById(R.id.animation);
    mTextViewLookingFor = findViewById(R.id.textViewLookingFor);
    mButtonCancelRequest = findViewById(R.id.btncancelRequest);

    mAnimation.playAnimation();
  //  mGeofireProvider = new GeofireProvider();


    }
}