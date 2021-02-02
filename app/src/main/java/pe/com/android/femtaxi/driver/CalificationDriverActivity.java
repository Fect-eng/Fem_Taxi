package pe.com.android.femtaxi.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import pe.com.android.femtaxi.databinding.ActivityCalicationDriveBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.ClientBooking;
import pe.com.android.femtaxi.models.HistoryBooking;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.HistoryBookingProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.Date;

public class CalificationDriverActivity extends AppCompatActivity {

    private ActivityCalicationDriveBinding binding;

    private ClientBookingProvider mClientBookingProvider;

    private String mClientId;
    private HistoryBooking mHistoryBooking;
    private HistoryBookingProvider mHistoryBookingProvider;
    private float mCalification = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCalicationDriveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mHistoryBookingProvider = new HistoryBookingProvider();

        mClientBookingProvider = new ClientBookingProvider();
        mClientId = getIntent().getStringExtra(Constants.Extras.EXTRA_CLIENT_ID);

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
                                    clientBooking.getTime(),
                                    clientBooking.getPrice());
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
                                                moveToMapDriverActivity();
                                            }
                                        });
                            } else {
                                mHistoryBookingProvider.setCreateHistoryBooking(mHistoryBooking)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                moveToMapDriverActivity();
                                            }
                                        });
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Debe colocar su calificacion", Toast.LENGTH_SHORT).show();
        }
    }

    private void moveToMapDriverActivity() {
        Intent intent = new Intent(CalificationDriverActivity.this, MapDriverActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setAction(Intent.ACTION_RUN);
        intent.putExtra(Constants.Extras.EXTRA_IS_CONNECTED, true);
        startActivity(intent);
        CalificationDriverActivity.this.finish();
    }
}
