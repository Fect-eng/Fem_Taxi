package com.example.femtaxi.driver;

import androidx.annotation.NonNull;
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
import com.example.femtaxi.models.ClientBooking;
import com.example.femtaxi.models.FCMBody;
import com.example.femtaxi.models.FCMResponse;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.GeofireProvider;
import com.example.femtaxi.providers.GoogleApiProvider;
import com.example.femtaxi.providers.NotificationProvider;
import com.example.femtaxi.providers.TokenProvider;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {

    ActivityRequestDriverBinding binding;

    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;

private double mExtraDestinationLat;
    private double mExtraDestinationLng;

    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private LatLng mDriverFoundLatLng;
    private NotificationProvider mNotificacionProvider;
    private TokenProvider mTokenProvider;

    private ClientBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;
    private GoogleApiProvider mGoogleApiProvider;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.animation.playAnimation();
        mGeofireProvider = new GeofireProvider(Constans.DRIVER_ACTIVE);
        mTokenProvider = new TokenProvider();
        mExtraOriginLat = getIntent().getDoubleExtra(Constans.Extras.ORIGIN_LAT, 0);
        mExtraOriginLng = getIntent().getDoubleExtra(Constans.Extras.ORIGIN_LONG, 0);


        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);
        mNotificacionProvider = new NotificationProvider();
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
                            sendNotification();
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

    private void sendNotification(final String time, final String km) {

    }

    private void sendNotification() {
        mTokenProvider.getTokenUser(mIdDriverFound).addSnapshotListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String token = dataSnapshot.child("token").getValue().toString();
                    Map<String, String> map = new HashMap<>();
                    map.put("title", "SOLICITUD DE SERVICIO A " + time + " DE TU POSICION");
                    map.put("body",
                            "Un cliente esta solicitando un servicio a una distancia de " + km + "\n" +
                                    "Recoger en: " + mExtraOriginLat + "\n" +
                                    "Destino: " + mExtraOriginLng
                    );
                    map.put("idClient", mAuthProvider.getId());
                    FCMBody fcmBody = new FCMBody(token, "high", map);
                    mNotificacionProvider.sendNotification(fcmBody).enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            if (response.body() != null) {
                                if (response.body().getSuccess() == 1) {
                                    ClientBooking clientBooking = new ClientBooking(
                                        /*   mAuthProvider.getId(),
                                            mIdDriverFound,
                                            //  mExtraDestination,
                                            //   mExtraOrigin,
                                          //  time,
                                           // km,
                                            "create",
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            mExtraDestinationLat,
                                            mExtraDestinationLng
                                    );*/

                                    mClientBookingProvider.create(clientBooking).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // checkStatusClientBooking();
                                        }
                                    });
                                    //Toast.makeText(RequestDriverActivity.this, "La notificacion se ha enviado correctamente", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else {
                                Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d("Error", "Error " + t.getMessage());
                        }
                    });
                }
                else {
                    Toast.makeText(RequestDriverActivity.this, "No se pudo enviar la notificacion porque el conductor no tiene un token de sesion", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}