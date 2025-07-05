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
    
    // 2-minute break system configuration
    private static final long BREAK_INTERVAL_MS = 2 * 60 * 1000L; // 2 minutes
    private static final int BREAK_DURATION_SECONDS = 90; // 90 seconds break
    private static final long MIN_CONTENT_PLAY_TIME = 30 * 1000L; // 30 seconds minimum content play
    
    private final Context context;
    private final SharedPreferencesHelper prefsHelper;
    private final Handler mainHandler;
    
    private Timer breakTimer;
    private Timer intervalTimer;
    private Timer twoMinuteTimer;
    private List<AdBreak> scheduledBreaks;
    private BreakTimeCallback callback;
    
    private long lastBreakTime = 0;
    private long contentStartTime = 0;
    private boolean isBreakActive = false;
    private boolean isContentPlaying = false;
    private int breakCounter = 0;

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
        Log.d(TAG, "Scheduling " + adBreaks.size() + " ad breaks with 2-minute system");
        
        this.scheduledBreaks = adBreaks;
        
        // Cancel existing timers
        stopAllTimers();
        
        // Schedule time-based breaks
        scheduleTimeBasedBreaks();
        
        // Schedule interval-based breaks (original system)
        scheduleIntervalBreaks(intervalMinutes);
        
        // Start 2-minute break system
        start2MinuteBreakSystem();
    }

    /**
     * Start the 2-minute break system that triggers breaks every 2 minutes
     */
    private void start2MinuteBreakSystem() {
        Log.d(TAG, "Starting 2-minute break system");
        
        twoMinuteTimer = new Timer("TwoMinuteBreakTimer", true);
        
        TimerTask twoMinuteTask = new TimerTask() {
            @Override
            public void run() {
                checkFor2MinuteBreak();
            }
        };
        
        // Check every 10 seconds for precise timing
        twoMinuteTimer.scheduleAtFixedRate(twoMinuteTask, 10000, 10000);
        
        Log.d(TAG, "2-minute break system started - breaks every 2 minutes");
    }

    /**
     * Check if it's time for a 2-minute break
     */
    private void checkFor2MinuteBreak() {
        long currentTime = System.currentTimeMillis();
        
        // Don't interrupt if already in break
        if (isBreakActive) {
            return;
        }
        
        // Check if content has been playing for at least minimum time
        if (!isContentPlaying || (currentTime - contentStartTime) < MIN_CONTENT_PLAY_TIME) {
            return;
        }
        
        // Check if 2 minutes have passed since last break or content start
        long timeSinceLastEvent = Math.max(
            currentTime - lastBreakTime,
            currentTime - contentStartTime
        );
        
        if (timeSinceLastEvent >= BREAK_INTERVAL_MS) {
            Log.d(TAG, "2-minute interval reached - triggering break");
            trigger2MinuteBreak();
        }
    }

    /**
     * Trigger a 2-minute interval break
     */
    private void trigger2MinuteBreak() {
        breakCounter++;
        
        AdBreak twoMinuteBreak = create2MinuteBreak();
        triggerAdBreak(twoMinuteBreak);
        
        Log.d(TAG, "2-minute break #" + breakCounter + " triggered");
    }

    /**
     * Create a 2-minute break with appropriate ads
     */
    private AdBreak create2MinuteBreak() {
        AdBreak breakAd = new AdBreak();
        breakAd.setId("two_minute_break_" + breakCounter);
        breakAd.setBreakType("2_minute_interval");
        breakAd.setBreakDuration(BREAK_DURATION_SECONDS);
        breakAd.setPriority(10);
        breakAd.setRepeatDaily(false);
        
        // Create ads for 2-minute break
        List<AdItem> breakAds = create2MinuteBreakAds();
        breakAd.setAds(breakAds);
        
        return breakAd;
    }

    /**
     * Create ads specifically for 2-minute breaks
     */
    private List<AdItem> create2MinuteBreakAds() {
        List<AdItem> ads = new ArrayList<>();
        
        // Rotate through different ads based on break counter
        String[] adUrls = {
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
        };
        
        String[] adTitles = {
            "2-Min Break: Action Ad",
            "2-Min Break: Adventure Ad", 
            "2-Min Break: Joyride Ad",
            "2-Min Break: Drama Ad",
            "2-Min Break: Car Ad"
        };
        
        // Select ads based on break counter to provide variety
        int adIndex1 = (breakCounter - 1) % adUrls.length;
        int adIndex2 = breakCounter % adUrls.length;
        
        // First ad (15 seconds)
        AdItem ad1 = new AdItem();
        ad1.setId("2min_break_ad_1_" + breakCounter);
        ad1.setTitle(adTitles[adIndex1] + " #" + breakCounter);
        ad1.setDescription("2-minute break advertisement " + breakCounter + "/1");
        ad1.setVideoUrl(adUrls[adIndex1]);
        ad1.setDuration(15);
        ad1.setPriority(1);
        ads.add(ad1);
        
        // Second ad (15 seconds)
        AdItem ad2 = new AdItem();
        ad2.setId("2min_break_ad_2_" + breakCounter);
        ad2.setTitle(adTitles[adIndex2] + " #" + breakCounter);
        ad2.setDescription("2-minute break advertisement " + breakCounter + "/2");
        ad2.setVideoUrl(adUrls[adIndex2]);
        ad2.setDuration(15);
        ad2.setPriority(2);
        ads.add(ad2);
        
        // Third ad (60 seconds) - longer ad for variety
        if (breakCounter % 3 == 0) { // Every 3rd break gets a longer ad
            AdItem ad3 = new AdItem();
            ad3.setId("2min_break_ad_3_" + breakCounter);
            ad3.setTitle("2-Min Break: Extended Ad #" + breakCounter);
            ad3.setDescription("Extended 2-minute break advertisement");
            ad3.setVideoUrl("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4");
            ad3.setDuration(60);
            ad3.setPriority(3);
            ads.add(ad3);
        }
        
        Log.d(TAG, "Created " + ads.size() + " ads for 2-minute break #" + breakCounter);
        return ads;
    }

    /**
     * Notify that content playback has started
     */
    public void onContentStarted() {
        isContentPlaying = true;
        contentStartTime = System.currentTimeMillis();
        
        Log.d(TAG, "Content started - 2-minute timer begins");
        
        // If this is the first content, set last break time to now
        if (lastBreakTime == 0) {
            lastBreakTime = contentStartTime;
        }
    }

    /**
     * Notify that content playback has paused
     */
    public void onContentPaused() {
        isContentPlaying = false;
        Log.d(TAG, "Content paused - 2-minute timer paused");
    }

    /**
     * Notify that content playback has resumed
     */
    public void onContentResumed() {
        isContentPlaying = true;
        Log.d(TAG, "Content resumed - 2-minute timer resumed");
    }

    /**
     * Get time until next 2-minute break
     */
    public long getTimeUntilNext2MinuteBreak() {
        if (!isContentPlaying || isBreakActive) {
            return -1;
        }
        
        long currentTime = System.currentTimeMillis();
        long timeSinceLastEvent = Math.max(
            currentTime - lastBreakTime,
            currentTime - contentStartTime
        );
        
        return Math.max(0, BREAK_INTERVAL_MS - timeSinceLastEvent);
    }

    /**
     * Get formatted time until next break
     */
    public String getFormattedTimeUntilNextBreak() {
        long timeMs = getTimeUntilNext2MinuteBreak();
        
        if (timeMs <= 0) {
            return "Break due now";
        }
        
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }

    // Original break system methods (preserved)
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
        Log.d(TAG, "Triggering ad break: " + adBreak.getId() + " (Type: " + adBreak.getBreakType() + ")");
        
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
            
            // Reset content start time for next 2-minute cycle
            if ("2_minute_interval".equals(adBreak.getBreakType())) {
                contentStartTime = System.currentTimeMillis();
            }
            
            if (callback != null) {
                callback.onBreakTimeEnded(adBreak);
            }
            
            Log.d(TAG, "Break ended: " + adBreak.getId() + " - Next 2-min break in 2 minutes");
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
        
        if (twoMinuteTimer != null) {
            twoMinuteTimer.cancel();
            twoMinuteTimer = null;
        }
        
        Log.d(TAG, "All break timers stopped (including 2-minute system)");
    }

    public boolean isBreakActive() {
        return isBreakActive;
    }

    public void forceBreak() {
        AdBreak manualBreak = create2MinuteBreak();
        manualBreak.setId("manual_break_" + System.currentTimeMillis());
        manualBreak.setBreakType("manual");
        triggerAdBreak(manualBreak);
    }

    public int getBreakCounter() {
        return breakCounter;
    }

    public interface BreakTimeCallback {
        void onBreakTimeTriggered(AdBreak adBreak);
        void onBreakTimeEnded(AdBreak adBreak);
    }
}