package com.example.playback_tv.tv.manager;

import android.content.Context;
import android.util.Log;

import com.example.playback_tv.tv.model.AdItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final int BUFFER_SIZE = 8192;
    private static final int TIMEOUT = 30000; // 30 seconds
    
    private final Context context;
    private final CacheManager cacheManager;

    public DownloadManager(Context context) {
        this.context = context;
        this.cacheManager = new CacheManager(context);
    }

    public boolean downloadAd(AdItem adItem) {
        try {
            Log.d(TAG, "Starting download for ad: " + adItem.getId());
            
            URL url = new URL(adItem.getVideoUrl());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(TIMEOUT);
            connection.setReadTimeout(TIMEOUT);
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error: " + responseCode);
                return false;
            }
            
            long fileSize = connection.getContentLength();
            InputStream inputStream = connection.getInputStream();
            
            File outputFile = new File(cacheManager.getCacheDir(), adItem.getId() + ".mp4");
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            
            byte[] buffer = new byte[BUFFER_SIZE];
            long totalBytesRead = 0;
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;
                
                // Update progress if needed
                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                    // Could notify UI about progress here
                }
            }
            
            outputStream.close();
            inputStream.close();
            connection.disconnect();
            
            // Set local path
            adItem.setLocalPath(outputFile.getAbsolutePath());
            
            Log.d(TAG, "Download completed for ad: " + adItem.getId());
            return true;
            
        } catch (IOException e) {
            Log.e(TAG, "Error downloading ad: " + adItem.getId(), e);
            return false;
        }
    }
}