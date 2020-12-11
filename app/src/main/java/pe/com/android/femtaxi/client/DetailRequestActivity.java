package pe.com.android.femtaxi.client;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import pe.com.android.femtaxi.R;
import pe.com.android.femtaxi.databinding.ActivityDetailRequestBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.Info;
import pe.com.android.femtaxi.providers.GoogleApiProvider;
import pe.com.android.femtaxi.providers.InfoProvider;
import pe.com.android.femtaxi.utils.DecodePoints;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailRequestActivity extends AppCompatActivity implements OnMapReadyCallback {
    String TAG = DetailRequestActivity.class.getSimpleName();
    private Button mButtonRequest;
    private ActivityDetailRequestBinding binding;

    private GoogleMap nMap;
    private SupportMapFragment nMapFragment;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinoLat;
    private double mExtradestinoLng;
    private String mExtraOrigin;
    private String mExtraDestination;

    private LatLng mOriginLatLng;
    private LatLng mDestinationLatLng;
    private GoogleApiProvider mGoogleApiProvider;
    private InfoProvider mInfoProvider;
    private List<LatLng> mPolyLinesList;
    private PolylineOptions mPolylineOptions;
    private LatLngBounds.Builder builder = LatLngBounds.builder();
    private LatLngBounds bounds = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mButtonRequest = findViewById(R.id.btn_solicitar);
        mButtonRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRequestDriver();

            }
        });

        nMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        nMapFragment.getMapAsync(this);

        mGoogleApiProvider = new GoogleApiProvider(DetailRequestActivity.this);
        mInfoProvider = new InfoProvider();

        mExtraOrigin = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN);
        mExtraOriginLat = getIntent().getDoubleExtra(Constants.Extras.EXTRA_ORIGIN_LAT, 0);
        mExtraOriginLng = getIntent().getDoubleExtra(Constants.Extras.EXTRA_ORIGIN_LONG, 0);
        mExtraDestination = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO);
        mExtraDestinoLat = getIntent().getDoubleExtra(Constants.Extras.EXTRA_DESTINO_LAT, 0);
        mExtradestinoLng = getIntent().getDoubleExtra(Constants.Extras.EXTRA_DESTINO_LONG, 0);

        mOriginLatLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        mDestinationLatLng = new LatLng(mExtraDestinoLat, mExtradestinoLng);

        binding.txtOrigin.setText(mExtraOrigin);
        binding.txtDestino.setText(mExtraDestination);

        binding.btnBackPresset.setOnClickListener(view -> {
            this.finish();
        });
    }

    private void goToRequestDriver() {
        Intent intent = new Intent(DetailRequestActivity.this, RequestDriverActivity.class);
        intent.putExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN, mExtraOrigin);
        intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LAT, mExtraOriginLat);
        intent.putExtra(Constants.Extras.EXTRA_ORIGIN_LONG, mExtraOriginLng);
        intent.putExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO, mExtraDestination);
        intent.putExtra(Constants.Extras.EXTRA_DESTINO_LAT, mExtraDestinoLat);
        intent.putExtra(Constants.Extras.EXTRA_DESTINO_LONG, mExtradestinoLng);
        startActivity(intent);
        this.finish();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;
        nMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        nMap.getUiSettings().setZoomControlsEnabled(false);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        nMap.setMyLocationEnabled(false);

        nMap.addMarker(new MarkerOptions()
                .position(mOriginLatLng)
                .title("Origen")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.rojoiconomarker)));
        nMap.addMarker(new MarkerOptions()
                .position(mDestinationLatLng)
                .title("Destino")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.azuliconomarker)));

        builder.include(mOriginLatLng);
        builder.include(mDestinationLatLng);
        bounds = builder.build();
        CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds, 100);
        nMap.animateCamera(camera);
        drawRoute();
    }

    private void drawRoute() {
        mGoogleApiProvider.getDirections(mOriginLatLng, mDestinationLatLng)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response.body());
                            Log.d(TAG, "drawRoute jsonObject: " + jsonObject);
                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                            Log.d(TAG, "drawRoute jsonArray: " + jsonArray);
                            JSONObject route = jsonArray.getJSONObject(0);
                            Log.d(TAG, "drawRoute route: " + route);
                            JSONObject polyLines = route.getJSONObject("overview_polyline");
                            Log.d(TAG, "drawRoute polyLines: " + polyLines);
                            String points = polyLines.getString("points");
                            Log.d(TAG, "drawRoute points: " + points);
                            mPolyLinesList = DecodePoints.decodePoly(points);
                            mPolylineOptions = new PolylineOptions();
                            mPolylineOptions.color(Color.DKGRAY);
                            mPolylineOptions.width(13f);
                            mPolylineOptions.startCap(new SquareCap());
                            mPolylineOptions.jointType(JointType.ROUND);
                            mPolylineOptions.addAll(mPolyLinesList);
                            nMap.addPolyline(mPolylineOptions);

                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);
                            JSONObject distance = leg.getJSONObject("distance");
                            JSONObject duration = leg.getJSONObject("duration");
                            String distanceText = distance.getString("text");
                            String durationText = duration.getString("text");
                            binding.txtTimeAndDistance.setText(durationText + " , " + distanceText);
                            //mTextViewDistance.setText(distanceText);

                            String[] distanceAndKM = distanceText.split(" ");
                            double distanceValor = Double.parseDouble(distanceAndKM[0]);
                            String[] durationAnMins = distanceText.split(" ");
                            double minutosValor = Double.parseDouble(durationAnMins[0]);

                            calcularPrice(distanceValor, minutosValor);
                        } catch (Exception e) {
                            Log.d(TAG, "drawRoute Error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(TAG, "drawRoute onFailure Error: " + t.getMessage());
                    }
                });
    }

    private void calcularPrice(double distanceValor, double minutosValor) {
        mInfoProvider.getInfo()
                .addOnSuccessListener(snapshots -> {
                    if (snapshots.exists()) {
                        Info info = snapshots.toObject(Info.class);
                        double totalDistance = distanceValor * info.getKm();
                        double totalMinutes = minutosValor * info.getMin();
                        double total = totalDistance + totalMinutes;
                        double minTotal = total - 0.50;
                        double maxTotal = total + 0.50;
                        binding.txtPrice.setText("S/ " + String.format("%.2f", minTotal) + " - " + String.format("%.2f", maxTotal));
                    }
                });
    }
}


