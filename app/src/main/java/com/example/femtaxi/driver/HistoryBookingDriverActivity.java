package com.example.femtaxi.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.femtaxi.client.MapClienteActivity;
import com.example.femtaxi.client.adapter.HistoryBookingClientAdapter;
import com.example.femtaxi.databinding.ActivityHistoryBookingClientBinding;
import com.example.femtaxi.databinding.ActivityHistoryBookingDriverBinding;
import com.example.femtaxi.driver.adapter.HistoryBookingDriverAdapter;
import com.example.femtaxi.models.HistoryBooking;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HistoryBookingDriverActivity extends AppCompatActivity {

    String TAG = HistoryBookingDriverActivity.class.getSimpleName();

    private ActivityHistoryBookingDriverBinding binding;

    private AuthProvider mAuthProvider;
    private HistoryBookingProvider mHistoryBookingProvider;

    private HistoryBookingDriverAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBookingDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuthProvider = new AuthProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        setupRecyclerView();

        binding.btnBackPresset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryBookingDriverActivity.this, MapClienteActivity.class));
                HistoryBookingDriverActivity.this.finish();
            }
        });

        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupRecyclerView() {
        mAdapter = new HistoryBookingDriverAdapter(this);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        binding.recycler.setHasFixedSize(true);
        binding.recycler.setLayoutManager(mLayoutManager);
        binding.recycler.setAdapter(mAdapter);
        mAdapter.setListener((view, item, position, longPress) -> {
            if (!longPress) {

            }
            return false;
        });
    }

    private void loadData() {
        mHistoryBookingProvider.getListHistoryBookingDriver(mAuthProvider.getId())
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot snapshots) {
                        if (snapshots != null) {
                            processQuerySnapshot(snapshots);
                        }
                    }
                });
    }

    private void processQuerySnapshot(QuerySnapshot documentSnapshot) {
        if (documentSnapshot != null) {
            if (documentSnapshot.getDocuments().size() > 0) {
                ArrayList<HistoryBooking> tmpListHistory = new ArrayList<>();
                for (DocumentSnapshot snapshot : documentSnapshot) {
                    HistoryBooking historyBooking = snapshot.toObject(HistoryBooking.class);
                    historyBooking.setIdHistory(snapshot.getId());
                    tmpListHistory.add(historyBooking);
                }
                mAdapter.setData(tmpListHistory);
            }
        }
    }
}