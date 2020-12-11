package com.example.femtaxi.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static String getStreet(Context context, double latitude, double longitude) {
        String street = "Ubicación no disponible";
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> direccion = geocoder.getFromLocation(latitude, longitude, 1);
            if (direccion.size() > 0) {
                String address = direccion.get(0).getAddressLine(0);
                String locality = direccion.get(0).getLocality();
                String adminArea = direccion.get(0).getAdminArea();
                String countryName = direccion.get(0).getCountryName();
                String featureName = direccion.get(0).getFeatureName();
                String countryCode = direccion.get(0).getCountryCode();
                String phone = direccion.get(0).getPhone();
                String subAdminArea = direccion.get(0).getSubAdminArea();
                String subLocality = direccion.get(0).getSubLocality();
                String premises = direccion.get(0).getPremises();
                String postalCode = direccion.get(0).getPostalCode();
                String subThoroughfare = direccion.get(0).getSubThoroughfare();
                String thoroughfare = direccion.get(0).getThoroughfare();
                String url = direccion.get(0).getUrl();

                street = address + " " + locality;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return street;
    }
}
