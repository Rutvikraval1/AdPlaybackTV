package com.example.playback_tv.tv.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.playback_tv.tv.model.AdBreak;
import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BreakTimeManager {
    private static final String TAG = "BreakTimeManager";
    
    private final Context context;
    private final SharedPreferencesHelper prefsHelper;
    private final Handler mainHandler;
    
    private Timer breakTimer;
    private Timer intervalTimer;
    private List<AdBreak> scheduledBreaks;
    private BreakTimeCallback callback;
    
    private long lastBreakTime = 0;
    private boolean isBreakActive = false;

    public BreakTimeManager(Context context) {
        this.context = context;
        this.prefsHelper = new SharedPreferencesHelper(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.scheduledBreaks = new ArrayList<>();
    }

    public void setBreakTimeCallback(BreakTimeCallback callback) {
        this.callback = callback;
    }

    public void scheduleBreaks(List<AdBreak> adBreaks, int intervalMinutes) {
        Log.d(TAG, "Scheduling " + adBreaks.size() + " ad breaks");
        
        this.scheduledBreaks = adBreaks;
        
        // Cancel existing timers
        stopAllTimers();
        
        // Schedule time-based breaks
        scheduleTimeBasedBreaks();
        
        // Schedule interval-based breaks
        scheduleIntervalBreaks(intervalMinutes);
    }

    private void scheduleTimeBasedBreaks() {
        if (scheduledBreaks == null || scheduledBreaks.isEmpty()) {
            return;
        }

        breakTimer = new Timer("BreakTimer", true);
        
        for (AdBreak adBreak : scheduledBreaks) {
            if (!adBreak.isEnabled() || !"scheduled".equals(adBreak.getBreakType())) {
                continue;
            }
            
            Date breakTime = parseBreakTime(adBreak.getBreakTime());
            if (breakTime != null && breakTime.after(new Date())) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        triggerAdBreak(adBreak);
                        
                        // Reschedule for next day if repeat daily
                        if (adBreak.isRepeatDaily()) {
                            scheduleNextDayBreak(adBreak);
                        }
                    }
                };
                
                breakTimer.schedule(task, breakTime);
                Log.d(TAG, "Scheduled break: " + adBreak.getId() + " at " + breakTime);
            }
        }
    }

    private void scheduleIntervalBreaks(int intervalMinutes) {
        if (intervalMinutes <= 0) {
            return;
        }

        intervalTimer = new Timer("IntervalTimer", true);
        long intervalMillis = intervalMinutes * 60 * 1000L;
        
        TimerTask intervalTask = new TimerTask() {
            @Override
            public void run() {
                // Check if enough time has passed since last break
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBreakTime >= intervalMillis && !isBreakActive) {
                    triggerIntervalBreak();
                }
            }
        };
        
        // Check every minute for interval breaks
        intervalTimer.scheduleAtFixedRate(intervalTask, 60000, 60000);
        Log.d(TAG, "Scheduled interval breaks every " + intervalMinutes + " minutes");
    }

    private void scheduleNextDayBreak(AdBreak adBreak) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        
        Date nextBreakTime = parseBreakTimeForDate(adBreak.getBreakTime(), calendar.getTime());
        if (nextBreakTime != null) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    triggerAdBreak(adBreak);
                    if (adBreak.isRepeatDaily()) {
                        scheduleNextDayBreak(adBreak);
                    }
                }
            };
            
            if (breakTimer != null) {
                breakTimer.schedule(task, nextBreakTime);
                Log.d(TAG, "Scheduled next day break: " + adBreak.getId() + " at " + nextBreakTime);
            }
        }
    }

    private void triggerAdBreak(AdBreak adBreak) {
        Log.d(TAG, "Triggering ad break: " + adBreak.getId());
        
        isBreakActive = true;
        lastBreakTime = System.currentTimeMillis();
        
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onBreakTimeTriggered(adBreak);
            }
        });
        
        // Auto-end break after duration
        mainHandler.postDelayed(() -> {
            isBreakActive = false;
            if (callback != null) {
                callback.onBreakTimeEnded(adBreak);
            }
        }, adBreak.getBreakDuration() * 1000L);
    }

    private void triggerIntervalBreak() {
        Log.d(TAG, "Triggering interval break");
        
        // Create a generic interval break
        AdBreak intervalBreak = createIntervalBreak();
        triggerAdBreak(intervalBreak);
    }

    private AdBreak createIntervalBreak() {
        AdBreak intervalBreak = new AdBreak();
        intervalBreak.setId("interval_break_" + System.currentTimeMillis());
        intervalBreak.setBreakType("interval");
        intervalBreak.setBreakDuration(120); // 2 minutes default
        intervalBreak.setPriority(5);
        
        // Use default ads for interval breaks
        List<AdItem> defaultAds = getDefaultIntervalAds();
        intervalBreak.setAds(defaultAds);
        
        return intervalBreak;
    }

    private List<AdItem> getDefaultIntervalAds() {
        List<AdItem> ads = new ArrayList<>();
        
        // Create short interval ads
        AdItem ad1 = new AdItem();
        ad1.setId("interval_ad_1");
        ad1.setTitle("Break Time Ad 1");
        ad1.setDescription("Short break advertisement");
        ad1.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4");
        ad1.setDuration(15);
        ads.add(ad1);
        
        AdItem ad2 = new AdItem();
        ad2.setId("interval_ad_2");
        ad2.setTitle("Break Time Ad 2");
        ad2.setDescription("Another break advertisement");
        ad2.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4");
        ad2.setDuration(15);
        ads.add(ad2);
        
        return ads;
    }

    private Date parseBreakTime(String timeString) {
        return parseBreakTimeForDate(timeString, new Date());
    }

    private Date parseBreakTimeForDate(String timeString, Date targetDate) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date time = timeFormat.parse(timeString);
            
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(targetDate);
            
            Calendar timeCalendar = Calendar.getInstance();
            timeCalendar.setTime(time);
            
            calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY));
            calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE));
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            
            return calendar.getTime();
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing break time: " + timeString, e);
            return null;
        }
    }

    public void stopAllTimers() {
        if (breakTimer != null) {
            breakTimer.cancel();
            breakTimer = null;
        }
        
        if (intervalTimer != null) {
            intervalTimer.cancel();
            intervalTimer = null;
        }
        
        Log.d(TAG, "All break timers stopped");
    }

    public boolean isBreakActive() {
        return isBreakActive;
    }

    public void forceBreak() {
        AdBreak manualBreak = createIntervalBreak();
        manualBreak.setId("manual_break_" + System.currentTimeMillis());
        manualBreak.setBreakType("manual");
        triggerAdBreak(manualBreak);
    }

    public interface BreakTimeCallback {
        void onBreakTimeTriggered(AdBreak adBreak);
        void onBreakTimeEnded(AdBreak adBreak);
    }
}