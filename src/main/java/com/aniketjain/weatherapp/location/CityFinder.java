package com.aniketjain.weatherapp.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

import java.util.List;
import java.util.Locale;

public class CityFinder {

    public static void setLongitudeLatitude(Location location) {
        try {
            LocationCord.lat = String.valueOf(location.getLatitude());
            LocationCord.lon = String.valueOf(location.getLongitude());
            Log.d("location_lat", LocationCord.lat);
            Log.d("location_lon", LocationCord.lon);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public static String getCityNameUsingNetwork(Context context, Location location) {
        String city = "";
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                city = addresses.get(0).getLocality();
                Log.d("city", city);
            } else {
                Log.d("city", "City not found");
            }
        } catch (Exception e) {
            Log.d("city", "Error to find the city.");
        }
        return city;
    }
}
