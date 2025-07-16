package com.example.playback_tv.tv.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.config.UniqueIdManager;
import com.example.playback_tv.tv.database.AdDatabase;
import com.example.playback_tv.tv.database.AdEntity;
import com.example.playback_tv.tv.database.PlaybackMetricEntity;
import com.example.playback_tv.tv.manager.DownloadManager;
import com.example.playback_tv.tv.network.ApiService;
import com.example.playback_tv.tv.network.RetrofitClient;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundSyncService extends Service {
    private static final String TAG = "BackgroundSyncService";
    private static final String CHANNEL_ID = "background_sync_channel";
    private static final int NOTIFICATION_ID = 2;
    private static final long SYNC_INTERVAL = 5 * 60 * 1000; // 5 minutes
    
    private Handler syncHandler;
    private Runnable syncRunnable;
    private ExecutorService executor;
    private AdDatabase database;
    private ApiService apiService;
    private UniqueIdManager uniqueIdManager;
    private DownloadManager downloadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Background sync service created");
        
        database = AdDatabase.getInstance(this);
        apiService = RetrofitClient.getApiService();
        uniqueIdManager = new UniqueIdManager(this);
        downloadManager = new DownloadManager(this);
        executor = Executors.newCachedThreadPool();
        
        createNotificationChannel();
        setupSyncHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Background sync service started");
        
        startForeground(NOTIFICATION_ID, createNotification());
        startPeriodicSync();
        
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Background Sync Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Syncs ad content and metrics in background");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ad System Active")
                .setContentText("Syncing ad content and metrics")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    private void setupSyncHandler() {
        syncHandler = new Handler(Looper.getMainLooper());
        syncRunnable = new Runnable() {
            @Override
            public void run() {
                performSync();
                syncHandler.postDelayed(this, SYNC_INTERVAL);
            }
        };
    }

    private void startPeriodicSync() {
        syncHandler.post(syncRunnable);
    }

    private void performSync() {
        Log.d(TAG, "Starting periodic sync");
        
        executor.execute(() -> {
            try {
                // 1. Check for new ad content
                checkForNewAdContent();
                
                // 2. Download pending ads
                downloadPendingAds();
                
                // 3. Send playback metrics
                sendPlaybackMetrics();
                
                // 4. Cleanup old data
                cleanupOldData();
                
            } catch (Exception e) {
                Log.e(TAG, "Error during sync", e);
            }
        });
    }

    private void checkForNewAdContent() {
        try {
            String deviceId = uniqueIdManager.getUniqueId();
            
            // Call backend API to get new ad schedule
            // This would be implemented with actual API call
            Log.d(TAG, "Checking for new ad content for device: " + deviceId);
            
            // For now, simulate with temp data
            simulateNewAdContent();
            
        } catch (Exception e) {
            Log.e(TAG, "Error checking for new ad content", e);
        }
    }

    private void simulateNewAdContent() {
        // Simulate new ad content from backend
        List<AdEntity> newAds = createSampleAds();
        
        for (AdEntity ad : newAds) {
            AdEntity existingAd = database.adDao().getAdById(ad.id);
            if (existingAd == null) {
                database.adDao().insertAd(ad);
                Log.d(TAG, "Added new ad: " + ad.title);
            }
        }
    }

    private List<AdEntity> createSampleAds() {
        return List.of(
            new AdEntity("ad_morning_30", "Morning Coffee Ad", 
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4", 
                30, "09:00"),
            new AdEntity("ad_afternoon_60", "Lunch Special", 
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4", 
                60, "14:00"),
            new AdEntity("ad_evening_30", "Dinner Restaurant", 
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4", 
                30, "19:00")
        );
    }

    private void downloadPendingAds() {
        try {
            List<AdEntity> pendingAds = database.adDao().getPendingDownloads();
            
            for (AdEntity ad : pendingAds) {
                if (downloadManager.downloadAd(ad)) {
                    database.adDao().markAsDownloaded(
                        ad.id, 
                        ad.localFilePath, 
                        System.currentTimeMillis()
                    );
                    Log.d(TAG, "Downloaded ad: " + ad.title);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error downloading ads", e);
        }
    }

    private void sendPlaybackMetrics() {
        try {
            List<PlaybackMetricEntity> unsentMetrics = database.playbackMetricDao().getUnsentMetrics();
            
            for (PlaybackMetricEntity metric : unsentMetrics) {
                // Send to backend API
                if (sendMetricToBackend(metric)) {
                    database.playbackMetricDao().markAsSent(metric.id);
                    Log.d(TAG, "Sent metric for ad: " + metric.adId);
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending metrics", e);
        }
    }

    private boolean sendMetricToBackend(PlaybackMetricEntity metric) {
        try {
            // Simulate API call to send metrics
            Log.d(TAG, "Sending metric: Device=" + metric.uniqueDeviceId + 
                  ", Ad=" + metric.adId + ", Duration=" + metric.actualDuration);
            
            // In real implementation, use Retrofit to send to backend
            return true;
            
        } catch (Exception e) {
            Log.e(TAG, "Error sending metric to backend", e);
            return false;
        }
    }

    private void cleanupOldData() {
        try {
            // Clean up metrics older than 30 days
            long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
            database.playbackMetricDao().deleteOldMetrics(thirtyDaysAgo);
            
            Log.d(TAG, "Cleaned up old data");
            
        } catch (Exception e) {
            Log.e(TAG, "Error cleaning up old data", e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        
        if (syncHandler != null && syncRunnable != null) {
            syncHandler.removeCallbacks(syncRunnable);
        }
        
        if (executor != null) {
            executor.shutdown();
        }
        
        Log.d(TAG, "Background sync service destroyed");
    }
}