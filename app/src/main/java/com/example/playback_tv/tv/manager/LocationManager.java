package com.example.playback_tv.tv.manager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.playback_tv.tv.utils.SharedPreferencesHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationManager {
    private static final String TAG = "LocationManager";
    
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final SharedPreferencesHelper prefsHelper;

    public LocationManager(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.prefsHelper = new SharedPreferencesHelper(context);
    }

    public void getCurrentLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            callback.onLocationReceived(getCachedLocation());
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        String locationString = location.getLatitude() + "," + location.getLongitude();
                        prefsHelper.saveLocation(locationString);
                        callback.onLocationReceived(locationString);
                    } else {
                        callback.onLocationReceived(getCachedLocation());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting location", e);
                    callback.onLocationReceived(getCachedLocation());
                });
    }

    public String getCachedLocation() {
        return prefsHelper.getLocation();
    }

    public interface LocationCallback {
        void onLocationReceived(String location);
    }
}