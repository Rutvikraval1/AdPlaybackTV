package com.example.playback_tv.tv.manager;

import android.content.Context;
import android.util.Log;

import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CacheManager {
    private static final String TAG = "CacheManager";
    private static final String CACHE_DIR = "ads_cache";
    private static final long MAX_CACHE_SIZE = 500 * 1024 * 1024; // 500MB
    
    private final Context context;
    private final SharedPreferencesHelper prefsHelper;
    private final File cacheDir;

    public CacheManager(Context context) {
        this.context = context;
        this.prefsHelper = new SharedPreferencesHelper(context);
        this.cacheDir = new File(context.getExternalFilesDir(null), CACHE_DIR);
        
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public boolean isAdCached(String adId) {
        File adFile = new File(cacheDir, adId + ".mp4");
        return adFile.exists() && adFile.length() > 0;
    }

    public void cacheAd(AdItem adItem) {
        try {
            // Update cache metadata
            List<String> cachedAds = getCachedAdIds();
            if (!cachedAds.contains(adItem.getId())) {
                cachedAds.add(adItem.getId());
                prefsHelper.saveCachedAdIds(cachedAds);
            }
            
            // Update ad item with local path
            File adFile = new File(cacheDir, adItem.getId() + ".mp4");
            adItem.setLocalPath(adFile.getAbsolutePath());
            
            Log.d(TAG, "Ad cached: " + adItem.getId());
        } catch (Exception e) {
            Log.e(TAG, "Error caching ad", e);
        }
    }

    public void removeAdFromCache(String adId) {
        try {
            File adFile = new File(cacheDir, adId + ".mp4");
            if (adFile.exists()) {
                adFile.delete();
            }
            
            List<String> cachedAds = getCachedAdIds();
            cachedAds.remove(adId);
            prefsHelper.saveCachedAdIds(cachedAds);
            
            Log.d(TAG, "Ad removed from cache: " + adId);
        } catch (Exception e) {
            Log.e(TAG, "Error removing ad from cache", e);
        }
    }

    public int getCachedAdsCount() {
        return getCachedAdIds().size();
    }

    public List<String> getCachedAdIds() {
        List<String> cachedIds = prefsHelper.getCachedAdIds();
        return cachedIds != null ? cachedIds : new ArrayList<>();
    }

    public File getCacheDir() {
        return cacheDir;
    }

    public void cleanupOldCache() {
        try {
            long totalSize = calculateCacheSize();
            
            if (totalSize > MAX_CACHE_SIZE) {
                // Remove oldest files first
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    // Sort by last modified time
                    java.util.Arrays.sort(files, (f1, f2) -> 
                        Long.compare(f1.lastModified(), f2.lastModified()));
                    
                    for (File file : files) {
                        if (calculateCacheSize() <= MAX_CACHE_SIZE * 0.8) {
                            break;
                        }
                        
                        String adId = file.getName().replace(".mp4", "");
                        removeAdFromCache(adId);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up cache", e);
        }
    }

    private long calculateCacheSize() {
        long totalSize = 0;
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                totalSize += file.length();
            }
        }
        return totalSize;
    }
}