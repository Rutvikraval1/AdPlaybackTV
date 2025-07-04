package com.example.playback_tv.tv.manager;

import android.content.Context;
import android.util.Log;

import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.model.AdManifest;
import com.example.playback_tv.tv.network.ApiService;
import com.example.playback_tv.tv.network.RetrofitClient;
import com.example.playback_tv.tv.utils.JsonParser;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdManager {
    private static final String TAG = "AdManager";
    
    private final Context context;
    private final ApiService apiService;
    private final CacheManager cacheManager;
    private final DownloadManager downloadManager;
    private final SharedPreferencesHelper prefsHelper;
    private final ExecutorService executor;
    
    private AdManifest currentManifest;

    public AdManager(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getApiService();
        this.cacheManager = new CacheManager(context);
        this.downloadManager = new DownloadManager(context);
        this.prefsHelper = new SharedPreferencesHelper(context);
        this.executor = Executors.newCachedThreadPool();
    }

    public void refreshManifest(String location, ManifestCallback callback) {
        Log.d(TAG, "Refreshing manifest for location: " + location);
        
        executor.execute(() -> {
            try {
                Call<AdManifest> call = apiService.getAdManifest(location);
                call.enqueue(new Callback<AdManifest>() {
                    @Override
                    public void onResponse(Call<AdManifest> call, Response<AdManifest> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            currentManifest = response.body();
                            prefsHelper.saveManifest(JsonParser.toJson(currentManifest));
                            callback.onSuccess();
                        } else {
                            callback.onError("Failed to fetch manifest");
                        }
                    }

                    @Override
                    public void onFailure(Call<AdManifest> call, Throwable t) {
                        Log.e(TAG, "Error fetching manifest", t);
                        callback.onError(t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Exception in refreshManifest", e);
                callback.onError(e.getMessage());
            }
        });
    }

    public void downloadAds(DownloadCallback callback) {
        if (currentManifest == null) {
            loadCachedManifest();
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
        
        if (currentManifest != null) {
            return currentManifest.getAds();
        }
        
        return null;
    }

    private void loadCachedManifest() {
        String manifestJson = prefsHelper.getCachedManifest();
        if (manifestJson != null) {
            currentManifest = JsonParser.fromJson(manifestJson, AdManifest.class);
        }
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