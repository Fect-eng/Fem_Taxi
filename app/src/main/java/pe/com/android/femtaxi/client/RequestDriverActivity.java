package pe.com.android.femtaxi.client;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.ListenerRegistration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import pe.com.android.femtaxi.annotation.ServiceType;
import pe.com.android.femtaxi.databinding.ActivityRequestDriverBinding;
import pe.com.android.femtaxi.helpers.Constants;
import pe.com.android.femtaxi.models.ClientBooking;
import pe.com.android.femtaxi.models.FCMResponse;
import pe.com.android.femtaxi.models.FieldNotification;
import pe.com.android.femtaxi.models.PushNotification;
import pe.com.android.femtaxi.models.ServiceNotification;
import pe.com.android.femtaxi.providers.AuthProvider;
import pe.com.android.femtaxi.providers.ClientBookingProvider;
import pe.com.android.femtaxi.providers.GeofireProvider;
import pe.com.android.femtaxi.providers.GoogleApiProvider;
import pe.com.android.femtaxi.providers.NotificationProvider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestDriverActivity extends AppCompatActivity {
    String TAG = RequestDriverActivity.class.getSimpleName();

    private ActivityRequestDriverBinding binding;

    private GeofireProvider mGeofireProvider;

    private double mExtraOriginLat;
    private double mExtraOriginLng;
    private double mExtraDestinoLat;
    private double mExtradestinoLng;
    private String mExtraOrigin;
    private String mExtraDestination;

    private double mRadius = 0.1;
    private boolean mDriverFound = false;
    private String mIdDriverFound = "";
    private NotificationProvider mNotificacionProvider;

    private ClientBookingProvider mClientBookingProvider;
    private AuthProvider mAuthProvider;
    private GoogleApiProvider mGoogleApiProvider;
    private ListenerRegistration mListener;
    @ServiceType
    private int mServiceType;
    private double mPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestDriverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.animation.playAnimation();
        mGeofireProvider = new GeofireProvider(Constants.Firebase.Nodo.DRIVER_ACTIVE);
        mExtraOrigin = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_ORIGIN);
        mExtraOriginLat = getIntent().getDoubleExtra(Constants.Extras.EXTRA_ORIGIN_LAT, 0);
        mExtraOriginLng = getIntent().getDoubleExtra(Constants.Extras.EXTRA_ORIGIN_LONG, 0);
        mExtraDestination = getIntent().getStringExtra(Constants.Extras.EXTRA_ADDRESS_DESTINO);
        mExtraDestinoLat = getIntent().getDoubleExtra(Constants.Extras.EXTRA_DESTINO_LAT, 0);
        mExtradestinoLng = getIntent().getDoubleExtra(Constants.Extras.EXTRA_DESTINO_LONG, 0);
        mServiceType = getIntent().getIntExtra(Constants.Extras.EXTRA_SERVICE_TYPE, 0);
        mPrice = getIntent().getDoubleExtra(Constants.Extras.EXTRA_PRICE, 0);
        mClientBookingProvider = new ClientBookingProvider();
        mAuthProvider = new AuthProvider();
        mGoogleApiProvider = new GoogleApiProvider(RequestDriverActivity.this);
        mNotificacionProvider = new NotificationProvider();
        binding.btnCancelViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Cancelar viaje: ");
                cancelRequest();
            }
        });
        LatLng latLng = new LatLng(mExtraOriginLat, mExtraOriginLng);
        if (latLng != null) {
            getClosestDriver(latLng);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mListener != null) mListener.remove();
    }

    private void getClosestDriver(LatLng LatLng) {
        mGeofireProvider.getActiveDrivers(LatLng, mRadius)
                .addGeoQueryEventListener(new GeoQueryEventListener() {
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        if (!mDriverFound) {
                            mDriverFound = true;
                            mIdDriverFound = key;
                            binding.textViewLookingFor.setText("CONDUCTOR ENCONTRADO\nESPERANDO RESPUESTA");
                            Log.d(TAG, "getClosestDriver onKeyEntered: " + mIdDriverFound);
                            createClientBooking();
                        }
                    }

                    @Override
                    public void onKeyExited(String key) {
                        Log.d(TAG, "getClosestDriver onKeyExited: " + mIdDriverFound + ", key: " + key);
                    }

                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Log.d(TAG, "getClosestDriver onKeyMoved key: " + key);
                    }

                    @Override
                    public void onGeoQueryReady() {
                        if (!mDriverFound) {
                            mRadius = mRadius + 0.1f;
                            if (mRadius > 5) {
                                binding.textViewLookingFor.setText("NO SE ENCONTRO UN CONDUCTOR");
                                moveToMapClient("No se encontro choferes disponibles");
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

    private void sendNotification(String time, String distance) {
        Log.i(TAG, "sendNotification");
        if (!TextUtils.isEmpty(mIdDriverFound)) {
            String title, body;
            switch (mServiceType) {
                case ServiceType.INTRA_URBANO:
                    title = "SOLICITUD DE SERVICIO DE INTRA-URBANO A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de intra-urbano a una distancia de " + distance + " KM";
                    break;
                case ServiceType.DELIVERY:
                    title = "SOLICITUD DE SERVICIO DE DELIVERY A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de delivery a una distancia de " + distance + " KM";
                    break;
                case ServiceType.MESSAGING:
                    title = "SOLICITUD DE SERVICIO DE MENSAJERIA A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de mensajeria a una distancia de " + distance + " KM";
                    break;
                case ServiceType.CARGA:
                    title = "SOLICITUD DE SERVICIO DE CARGA A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de carga a una distancia de " + distance + " KM";
                    break;
                case ServiceType.PET:
                    title = "SOLICITUD DE SERVICIO DE MASCOTAS A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de mascotas a una distancia de " + distance + " KM";
                    break;
                case ServiceType.FRIEND:
                    title = "SOLICITUD DE SERVICIO DE AMIGA ELEGIDA A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de amiga elegida a una distancia de " + distance + " KM";
                    break;
                case ServiceType.TAXI:
                default:
                    title = "SOLICITUD DE SERVICIO DE TAXI A " + time + " DE TU POSICION";
                    body = "Un cliente esta solicitando un servicio de taxi a una distancia de " + distance + " KM";
                    break;
            }

            ServiceNotification serviceNotification = new ServiceNotification(
                    title,
                    body,
                    mAuthProvider.getId(),
                    mExtraOrigin,
                    mExtraDestination,
                    time,
                    distance + " KM"
            );
            FieldNotification fieldNotification = new FieldNotification(
                    "/topics/" + mIdDriverFound,
                    title,
                    body,
                    mPrice,
                    "high",
                    "4500s",
                    serviceNotification
            );
            PushNotification pushNotification = new PushNotification(
                    "/topics/" + mIdDriverFound,
                    fieldNotification
            );

            mNotificacionProvider.sendNotification(pushNotification)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                            Log.d(TAG, "sendNotification onResponse: " + response);
                            if (response.body() != null) {
                                if (!response.body().getMessage_id().isEmpty()) {
                                    Toast.makeText(RequestDriverActivity.this, "Notificacion enviada con exito", Toast.LENGTH_SHORT).show();
                                    String idHistory = new SimpleDateFormat("HHmmssddmmyyyy").format(
                                            Calendar.getInstance(Locale.getDefault()).getTime());
                                    ClientBooking clientBooking = new ClientBooking(
                                            idHistory,
                                            mExtraDestination,
                                            mExtraDestinoLat,
                                            mExtradestinoLng,
                                            mAuthProvider.getId(),
                                            mIdDriverFound,
                                            distance,
                                            mExtraOrigin,
                                            mExtraOriginLat,
                                            mExtraOriginLng,
                                            "create",
                                            time,
                                            mPrice
                                    );
                                    mClientBookingProvider.createClentBooking(clientBooking)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(RequestDriverActivity.this, "Peticion creada con exito", Toast.LENGTH_SHORT).show();
                                                    checkStatusClientBooking();
                                                }
                                            });

                                } else {
                                    Toast.makeText(RequestDriverActivity.this, "error al enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Log.d(TAG, "sendNotification onFailure: " + t.getMessage());
                        }
                    });
        } else {
            Toast.makeText(RequestDriverActivity.this, "El conductor no cuenta con token de notificacion", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendNotificationCancel() {
        Log.i(TAG, "sendNotification");
        if (!TextUtils.isEmpty(mIdDriverFound)) {
            Log.d(TAG, "sendNotificationCancel mIdDriverFound: " + mIdDriverFound);
            String title = "VIAJE CANCELADO";
            String body = "Un cliente cancelo la solicitud";
            ServiceNotification serviceNotification = new ServiceNotification(
                    title,
                    body,
                    mAuthProvider.getId(),
                    null,
                    null,
                    null,
                    null
            );
            Log.d(TAG, "sendNotificationCancel serviceNotification: " + serviceNotification);
            FieldNotification fieldNotification = new FieldNotification(
                    "/topics/" + mIdDriverFound,
                    title,
                    body,
                    mPrice,
                    "high",
                    "4500s",
                    serviceNotification
            );
            Log.d(TAG, "sendNotificationCancel fieldNotification: " + fieldNotification);
            PushNotification pushNotification = new PushNotification(
                    "/topics/" + mIdDriverFound,
                    fieldNotification
            );
            Log.d(TAG, "sendNotificationCancel pushNotification: " + pushNotification);
            mNotificacionProvider.sendNotification(pushNotification)
                    .enqueue(new Callback<FCMResponse>() {
                        @Override
                        public void onResponse(Call<FCMResponse> call,
                                               Response<FCMResponse> response) {
                            Log.d(TAG, "sendNotification onResponse: " + response);
                            if (response.body() != null) {
                                if (!response.body().getMessage_id().isEmpty()) {
                                    moveToMapClient("Viaje cancelado con exito");
                                } else {
                                    Toast.makeText(RequestDriverActivity.this, "error al enviar la notificacion", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                            Toast.makeText(RequestDriverActivity.this, "No existe el token", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Log.d(TAG, "sendNotificationCancel:");
            moveToMapClient("Viaje cancelado");
        }
    }

    private void moveToMapClient(String s) {
        Toast.makeText(RequestDriverActivity.this, s, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RequestDriverActivity.this, MapClienteActivity.class);
        startActivity(intent);
        RequestDriverActivity.this.finish();
    }

    private void createClientBooking() {
        Log.i(TAG, "createClientBooking");
        mGoogleApiProvider.getDirections(new LatLng(mExtraOriginLat, mExtraOriginLng),
                new LatLng(mExtraDestinoLat, mExtradestinoLng))
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

                            JSONArray legs = route.getJSONArray("legs");
                            JSONObject leg = legs.getJSONObject(0);
                            JSONObject distance = leg.getJSONObject("distance");
                            JSONObject duration = leg.getJSONObject("duration");
                            String distanceText = distance.getString("text");
                            String durationText = duration.getString("text");
                            sendNotification(durationText, distanceText);
                        } catch (Exception e) {
                            Log.d(TAG, "drawRoute Error: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Log.d(TAG, "createClientBooking onFailure Error: " + t.getMessage());
                    }
                });
    }

    private void checkStatusClientBooking() {
        mListener = mClientBookingProvider.getClientBooking(mAuthProvider.getId())
                .addSnapshotListener((value, error) -> {
                    if (value.exists()) {
                        ClientBooking clientBooking = value.toObject(ClientBooking.class);
                        Log.d(TAG, "sendNotification clientBooking: " + clientBooking);
                        if (clientBooking.getStatus().equals("accept")) {
                            Intent intent = new Intent(RequestDriverActivity.this, MapClientBookingActivity.class);
                            startActivity(intent);
                            this.finish();
                        } else if (clientBooking.getStatus().equals("cancel")) {
                            moveToMapClient("El conductor no acepto el viaje");
                        }
                    }
                });
    }

    private void cancelRequest() {
        Log.d(TAG, "cancelRequest: ");
        mClientBookingProvider.deleteClientBooking(mAuthProvider.getId())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "cancelRequest onSuccess: ");
                        sendNotificationCancel();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "cancelRequest addOnFailureListener: " + e.getMessage());
                    sendNotificationCancel();
                });
    }
}