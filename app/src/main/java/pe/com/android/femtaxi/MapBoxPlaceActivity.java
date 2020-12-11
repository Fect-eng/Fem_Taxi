package pe.com.android.femtaxi;

import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.Arrays;

public class MapBoxPlaceActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap nMap;
    private static final String TAG = "info: ";
    private Object SupportMapFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_box_place);

        /*
       SupportMapFragment mapFragment;
        mapFragment = (pe.com.android.femtaxi.SupportMapFragment) SupportMapFragment;
        getSupportFragmentManager()
        .findFragmentById(R.id.mapa);*/

        //mapFragment.getMapAsync(this);

        String apikey = getString(R.string.google_api_key);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),apikey);
        }

        PlacesClient placesClient = Places.createClient(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        //agregado
        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
                new LatLng(0.313611, 32.581111),
                new LatLng(0.313611, 32.581111)
        ));
        //finde agregado

        autocompleteFragment.setCountries("UG");
        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
            }


            @Override
            public void onError(@NotNull Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        nMap = googleMap;

        LatLng kampala = new LatLng(0.313611,32.581111);
        nMap.addMarker(new MarkerOptions().position(kampala).title("Marker"));
        nMap.moveCamera(CameraUpdateFactory.newLatLng(kampala));
    }
}