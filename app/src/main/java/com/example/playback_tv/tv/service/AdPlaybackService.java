package com.example.playback_tv.tv.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.manager.AdManager;
import com.example.playback_tv.tv.manager.CacheManager;
import com.example.playback_tv.tv.manager.LocationManager;

public class AdPlaybackService extends Service {
    private static final String TAG = "AdPlaybackService";
    private static final String CHANNEL_ID = "ad_playback_channel";
    private static final int NOTIFICATION_ID = 1;
    
    private AdManager adManager;
    private CacheManager cacheManager;
    private LocationManager locationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        
        adManager = new AdManager(this);
        cacheManager = new CacheManager(this);
        locationManager = new LocationManager(this);
        
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");
        
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Perform background tasks
        performBackgroundTasks();
        
        return START_STICKY; // Restart service if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Ad Playback Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background service for ad playback management");
            
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Ad Playback Service")
                .setContentText("Managing ad playback in background")
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void performBackgroundTasks() {
        // Clean up old cache
        cacheManager.cleanupOldCache();
        
        // Update location and refresh manifest
        locationManager.getCurrentLocation(location -> {
            if (location != null) {
                adManager.refreshManifest(location, new AdManager.ManifestCallback() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Manifest refreshed successfully");
                        // Download new ads if available
                        adManager.downloadAds(new AdManager.DownloadCallback() {
                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "Ads downloaded successfully");
                            }

                            @Override
                            public void onError(String error) {
                                Log.w(TAG, "Failed to download ads: " + error);
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Log.w(TAG, "Failed to refresh manifest: " + error);
                    }
                });
            }
        });
    }
}