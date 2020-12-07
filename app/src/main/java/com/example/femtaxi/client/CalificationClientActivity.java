package com.example.femtaxi.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femtaxi.databinding.ActivityCalicationClientBinding;
import com.example.femtaxi.models.ClientBooking;
import com.example.femtaxi.models.HistoryBooking;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class CalificationClientActivity extends AppCompatActivity {

    private ActivityCalicationClientBinding binding;

    private ClientBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;

    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private float mCalification = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalicationClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mHistoryBookingProvider = new HistoryBookingProvider();
        mAuthProvider = new AuthProvider();

        mClientBookingProvider = new ClientBookingProvider();

        getClientBooking();

        binding.btnCalication.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callificate();
            }
        });
        binding.rating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                mCalification = v;
            }
        });
    }

    private void getClientBooking() {
        mClientBookingProvider.getClientBooking(mAuthProvider.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ClientBooking clientBooking = documentSnapshot.toObject(ClientBooking.class);
                            binding.txtAddressInit.setText(clientBooking.getOrigin());
                            binding.txtAddressEnd.setText(clientBooking.getDestination());
                            binding.txtPrice.setText("S/ " + String.format("%.2f", clientBooking.getPrice()));
                            mHistoryBooking = new HistoryBooking(
                                    clientBooking.getIdHistory(),
                                    clientBooking.getDestination(),
                                    clientBooking.getDestinationLat(),
                                    clientBooking.getDestinationLong(),
                                    clientBooking.getIdClient(),
                                    clientBooking.getIdDriver(),
                                    clientBooking.getKm(),
                                    clientBooking.getOrigin(),
                                    clientBooking.getOriginLat(),
                                    clientBooking.getOriginLong(),
                                    clientBooking.getStatus(),
                                    clientBooking.getTime());
                        }
                    }
                });
    }

    private void callificate() {
        if (mCalification > 0) {
            mHistoryBooking.setCalificationClient(mCalification);
            mHistoryBooking.setTimesTamp(new Date().getTime());
            mHistoryBookingProvider.getHistoryBooking(mHistoryBooking.getIdHistory())
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                mHistoryBookingProvider.getUpdateCalificationClient(
                                        mHistoryBooking.getIdHistory(),
                                        mCalification)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(CalificationClientActivity.this, MapClienteActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            } else {
                                mHistoryBookingProvider.getCreateHistoryBooking(mHistoryBooking)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent intent = new Intent(CalificationClientActivity.this, MapClienteActivity.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Debe colocar su calificacion", Toast.LENGTH_SHORT).show();
        }
    }
}
