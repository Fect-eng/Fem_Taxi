package com.example.femtaxi.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.femtaxi.client.adapter.HistoryBookingClientAdapter;
import com.example.femtaxi.databinding.ActivityHistoryBookingClientBinding;
import com.example.femtaxi.models.HistoryBooking;
import com.example.femtaxi.providers.AuthProvider;
import com.example.femtaxi.providers.HistoryBookingProvider;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class HistoryBookingClientActivity extends AppCompatActivity {

    String TAG = HistoryBookingClientActivity.class.getSimpleName();

    private ActivityHistoryBookingClientBinding binding;

    private AuthProvider mAuthProvider;
    private HistoryBookingProvider mHistoryBookingProvider;

    private HistoryBookingClientAdapter mAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBookingClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mAuthProvider = new AuthProvider();
        mHistoryBookingProvider = new HistoryBookingProvider();
        setupRecyclerView();

        binding.btnBackPresset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HistoryBookingClientActivity.this, MapClienteActivity.class));
                HistoryBookingClientActivity.this.finish();
            }
        });

        loadData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupRecyclerView() {
        mAdapter = new HistoryBookingClientAdapter(this);
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
        mHistoryBookingProvider.getListHistoryBookingClient(mAuthProvider.getId())
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
