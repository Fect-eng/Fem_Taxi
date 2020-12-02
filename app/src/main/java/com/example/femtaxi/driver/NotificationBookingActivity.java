package com.example.femtaxi.driver;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.femtaxi.databinding.ActivityNotificationBookingBinding;
import com.example.femtaxi.helpers.Constans;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.ClientBookingProvider;
import com.example.femtaxi.providers.GeofireProvider;

public class NotificationBookingActivity extends AppCompatActivity {
    private ActivityNotificationBookingBinding binding;

    private GeofireProvider mGeofireProvider;
    private AuthProvider mAuthProvider;
    private ClientBookingProvider mClientBookingProvider;
    private String mExtraIdClient;
    private String mExtraAddressOrigin;
    private String mExtraAddressDetino;
    private String mExtraMinutes;
    private String mExtraKM;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBookingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuthProvider = new AuthProvider();
        mGeofireProvider = new GeofireProvider(Constans.DRIVER_ACTIVE);

        mExtraIdClient = getIntent().getStringExtra(Constans.Extras.EXTRA_CLIENT_ID);
        mExtraAddressOrigin = getIntent().getStringExtra(Constans.Extras.EXTRA_ADDRESS_ORIGIN);
        mExtraAddressDetino = getIntent().getStringExtra(Constans.Extras.EXTRA_ADDRESS_DESTINO);
        mExtraMinutes = getIntent().getStringExtra(Constans.Extras.EXTRA_MINUT);
        mExtraKM = getIntent().getStringExtra(Constans.Extras.EXTRA_KM);

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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
    }

    private void initTimer() {
        mHandler = new Handler();
        mHandler.postDelayed(runnable, 1000);
    }

    private void acceptBooking() {
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.getUpdateStatus(mExtraIdClient, "Aceptado");

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(2);

        Intent intent1 = new Intent(NotificationBookingActivity.this, MapDriveBookingActivity.class);
        intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent1.setAction(Intent.ACTION_RUN);
        intent1.putExtra(Constans.Extras.EXTRA_CLIENT_ID, mExtraIdClient);
        startActivity(intent1);
    }

    private void cancelBooking() {
        if (mHandler != null)
            mHandler.removeCallbacks(runnable);
        mAuthProvider = new AuthProvider();
        mClientBookingProvider = new ClientBookingProvider();
        mClientBookingProvider.getUpdateStatus(mExtraIdClient, "Cancelado");
        mGeofireProvider = new GeofireProvider(Constans.DRIVER_ACTIVE);
        mGeofireProvider.removeLocation(mAuthProvider.getId());
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2);
        startActivity(new Intent(this, MapDriverActivity.class));
        finish();
    }
}
