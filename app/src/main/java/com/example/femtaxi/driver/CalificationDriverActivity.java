package com.example.femtaxi.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femtaxi.databinding.ActivityDriveCalicationBinding;
import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.models.ClientBooking;
import com.example.femtaxi.models.HistoryBooking;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CalificationDriverActivity extends AppCompatActivity {

    private ActivityDriveCalicationBinding binding;

    private ClientBookingProvider mClientBookingProvider;

    private String mClientId;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private float mCalification = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDriveCalicationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mHistoryBookingProvider = new HistoryBookingProvider();

        mClientBookingProvider = new ClientBookingProvider();
        mClientId = getIntent().getStringExtra(Constans.Extras.EXTRA_CLIENT_ID);

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
        mClientBookingProvider.getClientBooking(mClientId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            ClientBooking clientBooking = documentSnapshot.toObject(ClientBooking.class);
                            binding.txtAddressInit.setText(clientBooking.getOrigin());
                            binding.txtAddressEnd.setText(clientBooking.getDestination());
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

                                            }
                                        });
                            } else {
                                mHistoryBookingProvider.getCreateHistoryBooking(mHistoryBooking)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(CalificationDriverActivity.this, "Debe colocar su calificacion", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(CalificationDriverActivity.this, MapDriverActivity.class);
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
