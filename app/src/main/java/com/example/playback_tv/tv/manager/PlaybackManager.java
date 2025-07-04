package com.example.playback_tv.tv.manager;

import android.content.Context;
import android.util.Log;

import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

import java.util.Date;

public class PlaybackManager {
    private static final String TAG = "PlaybackManager";
    
    private final Context context;
    private final SharedPreferencesHelper prefsHelper;

    public PlaybackManager(Context context) {
        this.context = context;
        this.prefsHelper = new SharedPreferencesHelper(context);
    }

    public void trackAdPlayback(AdItem adItem) {
        Log.d(TAG, "Tracking playback for ad: " + adItem.getId());
        
        // Update last played information
        prefsHelper.saveLastPlayedAdId(adItem.getId());
        prefsHelper.saveLastPlaybackTime(new Date().getTime());
        
        // Could send analytics to server here
        // sendAnalytics(adItem);
    }

    public void savePlaybackState(String adId, long position) {
        prefsHelper.savePlaybackPosition(adId, position);
    }

    public long getPlaybackPosition(String adId) {
        return prefsHelper.getPlaybackPosition(adId);
    }

    public boolean shouldResumePlayback() {
        long lastPlaybackTime = prefsHelper.getLastPlaybackTime();
        long currentTime = new Date().getTime();
        
        // Resume if last playback was within 24 hours
        return (currentTime - lastPlaybackTime) < (24 * 60 * 60 * 1000);
    }
}