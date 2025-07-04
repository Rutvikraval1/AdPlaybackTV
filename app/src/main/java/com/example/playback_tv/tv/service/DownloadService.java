package com.example.playback_tv.tv.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.example.playback_tv.tv.manager.DownloadManager;

public class DownloadService extends Service {
    private static final String TAG = "DownloadService";
    
    private DownloadManager downloadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = new DownloadManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Download service started");
        
        // Handle download tasks
        
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}