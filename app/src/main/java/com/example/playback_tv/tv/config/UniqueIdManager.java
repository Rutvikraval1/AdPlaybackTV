package com.example.playback_tv.tv.config;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

import java.util.UUID;

public class UniqueIdManager {
    private static final String TAG = "UniqueIdManager";
    private static final String UNIQUE_ID_KEY = "unique_device_id";
    
    private final Context context;
    private final SharedPreferencesHelper prefsHelper;
    private String uniqueId;

    public UniqueIdManager(Context context) {
        this.context = context;
        this.prefsHelper = new SharedPreferencesHelper(context);
        initializeUniqueId();
    }

    private void initializeUniqueId() {
        // Check if unique ID already exists
        uniqueId = prefsHelper.getUniqueId();
        
        if (uniqueId == null || uniqueId.isEmpty()) {
            // Generate new unique ID on first install
            uniqueId = generateUniqueId();
            prefsHelper.saveUniqueId(uniqueId);
            Log.d(TAG, "Generated new unique ID: " + uniqueId);
        } else {
            Log.d(TAG, "Using existing unique ID: " + uniqueId);
        }
    }

    private String generateUniqueId() {
        try {
            // Try to use Android ID first
            String androidId = Settings.Secure.getString(
                context.getContentResolver(), 
                Settings.Secure.ANDROID_ID
            );
            
            if (androidId != null && !androidId.equals("9774d56d682e549c")) {
                return "ATV_" + androidId;
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get Android ID", e);
        }
        
        // Fallback to UUID
        return "ATV_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public boolean isFirstTimeInstall() {
        return prefsHelper.isFirstTimeInstall();
    }

    public void markFirstTimeInstallComplete() {
        prefsHelper.setFirstTimeInstallComplete();
    }
}