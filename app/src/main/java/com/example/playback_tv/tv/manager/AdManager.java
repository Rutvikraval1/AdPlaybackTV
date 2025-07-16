package com.example.playback_tv.tv.manager;

import android.content.Context;
import android.util.Log;

import com.example.playback_tv.tv.data.TempAdData;
import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.model.AdManifest;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class AdManager {
    private static final String TAG = "AdManager";
    private static final boolean USE_TEMP_DATA = true; // Set to false to use real API
    
    private final Context context;
    private final CacheManager cacheManager;
    private final DownloadManager downloadManager;
    private final SharedPreferencesHelper prefsHelper;
    private final ExecutorService executor;
    private final Gson gson;
    
    private AdManifest currentManifest;

    public AdManager(Context context) {
        this.context = context;
        this.cacheManager = new CacheManager(context);
        this.downloadManager = new DownloadManager(context);
        this.prefsHelper = new SharedPreferencesHelper(context);
        this.executor = Executors.newCachedThreadPool();
        this.gson = new Gson();
    }

    public void refreshManifest(String location, ManifestCallback callback) {
        Log.d(TAG, "Refreshing manifest for location: " + location);
        
        if (USE_TEMP_DATA) {
            // Use temporary data for testing
            executor.execute(() -> {
                try {
                    // Simulate network delay
                    Thread.sleep(1000);
                    
                    currentManifest = TempAdData.createTempManifest();
                    prefsHelper.saveManifest(gson.toJson(currentManifest));
                    
                    Log.d(TAG, "Loaded temporary manifest with " + 
                          currentManifest.getAds().size() + " items and " +
                          (currentManifest.getAdBreaks() != null ? currentManifest.getAdBreaks().size() : 0) + " breaks");
                    
                    callback.onSuccess();
                } catch (Exception e) {
                    Log.e(TAG, "Error loading temp manifest", e);
                    callback.onError(e.getMessage());
                }
            });
            return;
        }
        
        // Fallback to temp data for now
        loadTempDataAsFallback(callback);
    }

    private void loadTempDataAsFallback(ManifestCallback callback) {
        executor.execute(() -> {
            try {
                currentManifest = TempAdData.createTempManifest();
                prefsHelper.saveManifest(gson.toJson(currentManifest));
                Log.d(TAG, "Loaded temp data as fallback");
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error loading temp data fallback", e);
                callback.onError(e.getMessage());
            }
        });
    }

    public void downloadAds(DownloadCallback callback) {
        if (currentManifest == null) {
            loadCachedManifest();
        }
        
        // If still no manifest, load temp data
        if (currentManifest == null) {
            currentManifest = TempAdData.createTempManifest();
            prefsHelper.saveManifest(gson.toJson(currentManifest));
        }
        
        if (currentManifest == null || currentManifest.getAds() == null) {
            callback.onError("No manifest available");
            return;
        }

        executor.execute(() -> {
            try {
                List<AdItem> ads = currentManifest.getAds();
                int totalAds = ads.size();
                int downloadedCount = 0;
                
                for (AdItem ad : ads) {
                    if (!cacheManager.isAdCached(ad.getId())) {
                        boolean success = downloadManager.downloadAd(ad);
                        if (success) {
                            cacheManager.cacheAd(ad);
                            downloadedCount++;
                        }
                    } else {
                        downloadedCount++;
                    }
                }
                
                if (downloadedCount == totalAds) {
                    callback.onSuccess();
                } else {
                    callback.onError("Some ads failed to download");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error downloading ads", e);
                callback.onError(e.getMessage());
            }
        });
    }

    public List<AdItem> getCurrentPlaylist() {
        if (currentManifest == null) {
            loadCachedManifest();
        }
        
        // If still no manifest, load temp data
        if (currentManifest == null) {
            Log.d(TAG, "No cached manifest, loading temp data");
            currentManifest = TempAdData.createTempManifest();
            prefsHelper.saveManifest(gson.toJson(currentManifest));
        }
        
        if (currentManifest != null) {
            return currentManifest.getAds();
        }
        
        return null;
    }

    public AdManifest getCurrentManifest() {
        if (currentManifest == null) {
            loadCachedManifest();
        }
        
        // If still no manifest, load temp data
        if (currentManifest == null) {
            Log.d(TAG, "No cached manifest, loading temp data");
            currentManifest = TempAdData.createTempManifest();
            prefsHelper.saveManifest(gson.toJson(currentManifest));
        }
        
        return currentManifest;
    }

    private void loadCachedManifest() {
        String manifestJson = prefsHelper.getCachedManifest();
        if (manifestJson != null) {
            currentManifest = gson.fromJson(manifestJson, AdManifest.class);
        }
    }

    // Method to switch between temp data and real API
    public void setUseTempData(boolean useTempData) {
        // This could be used to toggle between temp and real data
        // For now, we use the constant at the top of the class
    }

    // Method to load different types of temp data
    public void loadShortTestAds() {
        currentManifest = new AdManifest();
        currentManifest.setVersion("1.0.0");
        currentManifest.setLastUpdated("2025-01-04T10:00:00Z");
        currentManifest.setGeoLocation("US-NY");
        currentManifest.setAds(TempAdData.createShortTestAds());
        currentManifest.setBreakIntervalMinutes(5); // 5 minutes for testing
        currentManifest.setDefaultBreakDuration(30); // 30 seconds
        prefsHelper.saveManifest(gson.toJson(currentManifest));
    }

    public void loadContentOnlyPlaylist() {
        currentManifest = new AdManifest();
        currentManifest.setVersion("1.0.0");
        currentManifest.setLastUpdated("2025-01-04T10:00:00Z");
        currentManifest.setGeoLocation("US-NY");
        currentManifest.setAds(TempAdData.createContentOnlyPlaylist());
        currentManifest.setBreakIntervalMinutes(10); // 10 minutes for content
        currentManifest.setDefaultBreakDuration(60); // 1 minute breaks
        prefsHelper.saveManifest(gson.toJson(currentManifest));
    }

    public void loadAdOnlyPlaylist() {
        currentManifest = new AdManifest();
        currentManifest.setVersion("1.0.0");
        currentManifest.setLastUpdated("2025-01-04T10:00:00Z");
        currentManifest.setGeoLocation("US-NY");
        currentManifest.setAds(TempAdData.createAdOnlyPlaylist());
        currentManifest.setBreakIntervalMinutes(0); // No breaks for ad-only
        currentManifest.setDefaultBreakDuration(0);
        prefsHelper.saveManifest(gson.toJson(currentManifest));
    }

    public interface ManifestCallback {
        void onSuccess();
        void onError(String error);
    }

    public interface DownloadCallback {
        void onSuccess();
        void onError(String error);
    }
}