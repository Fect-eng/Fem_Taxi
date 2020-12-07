package com.example.femtaxi.driver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femtaxi.databinding.ActivityNotificationBookingBinding;
import com.example.femtaxi.helpers.Constants;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.GeofireProvider;
import com.google.firebase.firestore.ListenerRegistration;

public class NotificationBookingActivity extends AppCompatActivity {
    private ActivityNotificationBookingBinding binding;

    private String mExtraIdClient;
    private String mExtraAddressOrigin;
    private String mExtraAddressDetino;
    private String mExtraMinutes;
    private String mExtraKM;
    private ClientBookingProvider mClientBookingProvider;
    private int mCounter = 10;
    private Handler mHandler;
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mCounter--;
            binding.txtTimeOut.setText(String.valueOf(mCounter));
            if (mCounter > 0) {
                initTimer();
            } else {
                cancelBooking();
            }
        }
    };
    ListenerRegistration mListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mClientBookingProvider = new ClientBookingProvider();
        mExtraIdClient = getIntent().getStringExtra(Constants.Extras.EXTRA_CLIENT_ID);
        mExtraAddressOrigin = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN);
        mExtraAddressDetino = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO);
        mExtraMinutes = getIntent().getStringExtra(Constants.Extras.EXTRA_MINUT);
        mExtraKM = getIntent().getStringExtra(Constants.Extras.EXTRA_KM);

        binding.txtAddressInit.setText(mExtraAddressOrigin);
        binding.txtAddressEnd.setText(mExtraAddressDetino);
        binding.txtTimeArrive.setText(mExtraMinutes);
        binding.txtKmArrive.setText(mExtraKM);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON

        );
        initTimer();

        binding.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                acceptBooking();
            }
        });

        binding.btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelBooking();
            }
        });
        checkClientCancelBooking();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        if (mListener != null)
            mListener.remove();
    }

    private void initTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }

    private void acceptBooking() {
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        AuthProvider authProvider = new AuthProvider();
        GeofireProvider geofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_WORKING);
        geofireProvider.removeLocation(authProvider.getId());

        ClientBookingProvider clientBookingProvider = new ClientBookingProvider();
        clientBookingProvider.getUpdateStatus(mExtraIdClient, "accept");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(NotificationBookingActivity.this, MapDriveBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra(Constants.Extras.EXTRA_CLIENT_ID, mExtraIdClient);
        startActivity(intent1);
    }

    private void cancelBooking() {
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        ClientBookingProvider clientBookingProvider = new ClientBookingProvider();
        clientBookingProvider.getUpdateStatus(mExtraIdClient, "cancel");
        AuthProvider authProvider = new AuthProvider();
        GeofireProvider geofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_ACTIVE);
        geofireProvider.removeLocation(authProvider.getId());

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);

        startActivity(new Intent(this, MapDriverActivity.class));
        finish();
    }

    private void checkClientCancelBooking() {
        mListener = mClientBookingProvider.getClientBooking(mExtraIdClient)
                .addSnapshotListener((value, error) -> {
                    if (!value.exists()) {
                        Toast.makeText(NotificationBookingActivity.this, "El conductor no acepto el viaje", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(NotificationBookingActivity.this, MapDriverActivity.class);
                        startActivity(intent);
                        this.finish();
                    }
                });
    }
}
