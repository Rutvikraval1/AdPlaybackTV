package com.example.playback_tv.tv.scheduler;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.playback_tv.tv.config.UniqueIdManager;
import com.example.playback_tv.tv.database.AdDatabase;
import com.example.playback_tv.tv.database.AdEntity;
import com.example.playback_tv.tv.database.PlaybackMetricEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaybackScheduler {
    private static final String TAG = "PlaybackScheduler";
    private static final long CHECK_INTERVAL = 10 * 60 * 1000; // 10 minutes
    
    private final Context context;
    private final AdDatabase database;
    private final UniqueIdManager uniqueIdManager;
    private final ExecutorService executor;
    private final Handler mainHandler;
    
    private Timer schedulerTimer;
    private PlaybackCallback callback;
    private boolean isSchedulerActive = false;

    public interface PlaybackCallback {
        void onAdScheduled(AdEntity ad);
        void onNoAdsAvailable();
        void onSchedulerError(String error);
    }

    public PlaybackScheduler(Context context) {
        this.context = context;
        this.database = AdDatabase.getInstance(context);
        this.uniqueIdManager = new UniqueIdManager(context);
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void setPlaybackCallback(PlaybackCallback callback) {
        this.callback = callback;
    }

    public void startScheduler() {
        if (isSchedulerActive) {
            Log.w(TAG, "Scheduler already active");
            return;
        }
        
        Log.d(TAG, "Starting playback scheduler - checking every 10 minutes");
        
        schedulerTimer = new Timer("PlaybackScheduler", true);
        isSchedulerActive = true;
        
        TimerTask schedulerTask = new TimerTask() {
            @Override
            public void run() {
                checkForScheduledAds();
            }
        };
        
        // Check immediately, then every 10 minutes
        schedulerTimer.schedule(schedulerTask, 0);
        schedulerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkForScheduledAds();
            }
        }, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    public void stopScheduler() {
        if (schedulerTimer != null) {
            schedulerTimer.cancel();
            schedulerTimer = null;
        }
        isSchedulerActive = false;
        Log.d(TAG, "Playback scheduler stopped");
    }

    private void checkForScheduledAds() {
        executor.execute(() -> {
            try {
                String currentTime = getCurrentTimeString();
                Log.d(TAG, "Checking for ads scheduled at: " + currentTime);
                
                List<AdEntity> scheduledAds = database.adDao().getAdsForTime(currentTime);
                
                if (scheduledAds.isEmpty()) {
                    // Check for ads by duration (30s or 60s) based on current time
                    scheduledAds = getAdsByCurrentTime();
                }
                
                if (!scheduledAds.isEmpty()) {
                    AdEntity selectedAd = selectBestAd(scheduledAds);
                    playScheduledAd(selectedAd);
                } else {
                    Log.d(TAG, "No ads scheduled for current time");
                    notifyNoAdsAvailable();
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Error checking for scheduled ads", e);
                notifySchedulerError(e.getMessage());
            }
        });
    }

    private List<AdEntity> getAdsByCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        
        // Morning (6-12): 30s ads
        // Afternoon (12-18): 60s ads  
        // Evening (18-24): 30s ads
        // Night (0-6): 30s ads
        
        int preferredDuration = (hour >= 12 && hour < 18) ? 60 : 30;
        
        List<AdEntity> ads = database.adDao().getAdsByDuration(preferredDuration);
        
        // Fallback to any duration if preferred not available
        if (ads.isEmpty()) {
            preferredDuration = (preferredDuration == 30) ? 60 : 30;
            ads = database.adDao().getAdsByDuration(preferredDuration);
        }
        
        return ads;
    }

    private AdEntity selectBestAd(List<AdEntity> ads) {
        // Select ad based on priority and last played time
        AdEntity bestAd = null;
        long oldestPlayTime = Long.MAX_VALUE;
        
        for (AdEntity ad : ads) {
            if (bestAd == null) {
                bestAd = ad;
                oldestPlayTime = ad.lastPlayedTimestamp;
                continue;
            }
            
            // Prefer higher priority (lower number)
            if (ad.priority < bestAd.priority) {
                bestAd = ad;
                oldestPlayTime = ad.lastPlayedTimestamp;
            } else if (ad.priority == bestAd.priority) {
                // Same priority, prefer least recently played
                if (ad.lastPlayedTimestamp < oldestPlayTime) {
                    bestAd = ad;
                    oldestPlayTime = ad.lastPlayedTimestamp;
                }
            }
        }
        
        return bestAd;
    }

    private void playScheduledAd(AdEntity ad) {
        Log.d(TAG, "Playing scheduled ad: " + ad.title + " (" + ad.duration + "s)");
        
        // Create playback metric
        PlaybackMetricEntity metric = new PlaybackMetricEntity();
        metric.uniqueDeviceId = uniqueIdManager.getUniqueId();
        metric.adId = ad.id;
        metric.playbackTimestamp = System.currentTimeMillis();
        metric.scheduledDuration = ad.duration;
        metric.playbackStatus = "started";
        
        long metricId = database.playbackMetricDao().insertMetric(metric);
        
        // Mark ad as played
        database.adDao().markAsPlayed(ad.id, System.currentTimeMillis());
        
        // Notify callback
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onAdScheduled(ad);
            }
        });
        
        // Schedule completion tracking
        schedulePlaybackCompletion(ad, metricId);
    }

    private void schedulePlaybackCompletion(AdEntity ad, long metricId) {
        mainHandler.postDelayed(() -> {
            executor.execute(() -> {
                try {
                    // Update metric as completed
                    PlaybackMetricEntity metric = database.playbackMetricDao().getUnsentMetrics()
                        .stream()
                        .filter(m -> m.id == metricId)
                        .findFirst()
                        .orElse(null);
                    
                    if (metric != null) {
                        metric.actualDuration = ad.duration;
                        metric.playbackCompleted = true;
                        metric.playbackStatus = "completed";
                        database.playbackMetricDao().updateMetric(metric);
                        
                        Log.d(TAG, "Ad playback completed: " + ad.title);
                    }
                    
                } catch (Exception e) {
                    Log.e(TAG, "Error updating playback completion", e);
                }
            });
        }, ad.duration * 1000L);
    }

    private String getCurrentTimeString() {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return timeFormat.format(new Date());
    }

    private void notifyNoAdsAvailable() {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onNoAdsAvailable();
            }
        });
    }

    private void notifySchedulerError(String error) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onSchedulerError(error);
            }
        });
    }

    public boolean isActive() {
        return isSchedulerActive;
    }

    public void forceAdCheck() {
        if (isSchedulerActive) {
            executor.execute(this::checkForScheduledAds);
        }
    }
}