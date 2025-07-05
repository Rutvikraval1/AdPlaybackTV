package com.example.playback_tv.tv.data;

import com.example.playback_tv.tv.model.AdBreak;
import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.model.AdManifest;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TempAdData {
    
    public static AdManifest createTempManifest() {
        AdManifest manifest = new AdManifest();
        manifest.setVersion("1.0.0");
        manifest.setLastUpdated("2025-01-04T10:00:00Z");
        manifest.setGeoLocation("US-NY");
        manifest.setAds(createTempAds());
        manifest.setAdBreaks(createTempAdBreaks());
        manifest.setBreakIntervalMinutes(2); // 2-minute break system
        manifest.setDefaultBreakDuration(90); // 90 seconds default break
        return manifest;
    }
    
    public static List<AdItem> createTempAds() {
        List<AdItem> ads = new ArrayList<>();
        
        // Main content videos (longer duration)
        ads.add(createAdItem(
            "content_001",
            "Big Buck Bunny",
            "Blender Foundation's Big Buck Bunny - Main Content",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            596, // ~10 minutes
            "US-NY",
            1,
            "content"
        ));
        
        ads.add(createAdItem(
            "content_002", 
            "Elephant Dream",
            "Blender Foundation's Elephant Dream - Main Content",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            653, // ~11 minutes
            "US-NY",
            2,
            "content"
        ));
        
        ads.add(createAdItem(
            "content_003",
            "Sintel",
            "Blender Foundation's Sintel - Main Content",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            888, // ~15 minutes
            "US-NY",
            3,
            "content"
        ));
        
        ads.add(createAdItem(
            "content_004",
            "Tears of Steel",
            "Blender Foundation's Tears of Steel - Main Content",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            734, // ~12 minutes
            "US-NY",
            4,
            "content"
        ));
        
        // Short advertisement videos
        ads.add(createAdItem(
            "ad_001",
            "For Bigger Blazes",
            "Short promotional video - 15 seconds",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            15,
            "US-NY",
            5,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_002",
            "For Bigger Escape",
            "Adventure themed video - 15 seconds",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            15,
            "US-NY",
            6,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_003",
            "For Bigger Fun",
            "Entertainment video - 60 seconds",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            60,
            "US-NY",
            7,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_004",
            "For Bigger Joyrides",
            "Action packed video - 15 seconds",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            15,
            "US-NY",
            8,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_005",
            "For Bigger Meltdowns",
            "Dramatic video content - 15 seconds",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            15,
            "US-NY",
            9,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_006",
            "Subaru Outback",
            "Car commercial sample - 15 seconds",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            15,
            "US-NY",
            10,
            "advertisement"
        ));
        
        return ads;
    }
    
    public static List<AdBreak> createTempAdBreaks() {
        List<AdBreak> adBreaks = new ArrayList<>();
        
        // Morning break
        AdBreak morningBreak = new AdBreak();
        morningBreak.setId("morning_break");
        morningBreak.setBreakTime("09:00");
        morningBreak.setBreakDuration(120); // 2 minutes
        morningBreak.setBreakType("scheduled");
        morningBreak.setPriority(1);
        morningBreak.setRepeatDaily(true);
        morningBreak.setAds(createBreakAds("morning"));
        adBreaks.add(morningBreak);
        
        // Afternoon break
        AdBreak afternoonBreak = new AdBreak();
        afternoonBreak.setId("afternoon_break");
        afternoonBreak.setBreakTime("14:30");
        afternoonBreak.setBreakDuration(90); // 1.5 minutes
        afternoonBreak.setBreakType("scheduled");
        afternoonBreak.setPriority(2);
        afternoonBreak.setRepeatDaily(true);
        afternoonBreak.setAds(createBreakAds("afternoon"));
        adBreaks.add(afternoonBreak);
        
        // Evening break
        AdBreak eveningBreak = new AdBreak();
        eveningBreak.setId("evening_break");
        eveningBreak.setBreakTime("19:00");
        eveningBreak.setBreakDuration(150); // 2.5 minutes
        eveningBreak.setBreakType("scheduled");
        eveningBreak.setPriority(3);
        eveningBreak.setRepeatDaily(true);
        eveningBreak.setAds(createBreakAds("evening"));
        adBreaks.add(eveningBreak);
        
        // Immediate test break (30 seconds from now for immediate testing)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 30); // 30 seconds from now
        String testTime = String.format("%02d:%02d", 
            calendar.get(Calendar.HOUR_OF_DAY), 
            calendar.get(Calendar.MINUTE));
        
        AdBreak testBreak = new AdBreak();
        testBreak.setId("immediate_test_break");
        testBreak.setBreakTime(testTime);
        testBreak.setBreakDuration(60); // 1 minute
        testBreak.setBreakType("scheduled");
        testBreak.setPriority(10);
        testBreak.setRepeatDaily(false);
        testBreak.setAds(createBreakAds("test"));
        adBreaks.add(testBreak);
        
        return adBreaks;
    }
    
    private static List<AdItem> createBreakAds(String breakType) {
        List<AdItem> ads = new ArrayList<>();
        
        switch (breakType) {
            case "morning":
                ads.add(createAdItem(
                    "morning_ad_1",
                    "Morning Coffee Ad",
                    "Start your day right",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    15,
                    "US-NY",
                    1,
                    "break_ad"
                ));
                ads.add(createAdItem(
                    "morning_ad_2",
                    "Morning News Promo",
                    "Stay informed",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    15,
                    "US-NY",
                    2,
                    "break_ad"
                ));
                break;
                
            case "afternoon":
                ads.add(createAdItem(
                    "afternoon_ad_1",
                    "Lunch Special",
                    "Delicious lunch options",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
                    15,
                    "US-NY",
                    1,
                    "break_ad"
                ));
                ads.add(createAdItem(
                    "afternoon_ad_2",
                    "Energy Drink",
                    "Afternoon energy boost",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    60,
                    "US-NY",
                    2,
                    "break_ad"
                ));
                break;
                
            case "evening":
                ads.add(createAdItem(
                    "evening_ad_1",
                    "Dinner Restaurant",
                    "Perfect evening dining",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
                    15,
                    "US-NY",
                    1,
                    "break_ad"
                ));
                ads.add(createAdItem(
                    "evening_ad_2",
                    "Movie Theater",
                    "Tonight's entertainment",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
                    15,
                    "US-NY",
                    2,
                    "break_ad"
                ));
                break;
                
            case "test":
                ads.add(createAdItem(
                    "test_ad_1",
                    "Immediate Test Break Ad",
                    "This is an immediate test break advertisement",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    15,
                    "US-NY",
                    1,
                    "break_ad"
                ));
                break;
        }
        
        return ads;
    }
    
    private static AdItem createAdItem(String id, String title, String description, 
                                     String videoUrl, int duration, String geoLocation, 
                                     int priority, String type) {
        AdItem ad = new AdItem();
        ad.setId(id);
        ad.setTitle(title);
        ad.setDescription(description);
        ad.setVideoUrl(videoUrl);
        ad.setDuration(duration);
        ad.setGeoLocation(geoLocation);
        ad.setPriority(priority);
        ad.setScheduleTime("2025-01-04T12:00:00Z");
        return ad;
    }
    
    private static AdItem createAdItem(String id, String title, String description, 
                                     String videoUrl, int duration, String geoLocation, int priority) {
        return createAdItem(id, title, description, videoUrl, duration, geoLocation, priority, "general");
    }
    
    // Alternative local video URLs for testing
    public static List<AdItem> createLocalTestAds() {
        List<AdItem> ads = new ArrayList<>();
        
        // These would be local files you can add to assets or raw folder
        ads.add(createAdItem(
            "local_001",
            "Local Test Ad 1",
            "First local test advertisement",
            "android.resource://com.example.playback_tv/raw/test_ad_1",
            30,
            "US-NY",
            1
        ));
        
        ads.add(createAdItem(
            "local_002",
            "Local Test Ad 2", 
            "Second local test advertisement",
            "android.resource://com.example.playback_tv/raw/test_ad_2",
            15,
            "US-NY",
            2
        ));
        
        return ads;
    }
    
    // Short test videos for quick testing (2-minute break system)
    public static List<AdItem> createShortTestAds() {
        List<AdItem> ads = new ArrayList<>();
        
        ads.add(createAdItem(
            "short_001",
            "Short Test 1",
            "15 second test video for 2-min breaks",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            15,
            "US-NY",
            1
        ));
        
        ads.add(createAdItem(
            "short_002",
            "Short Test 2",
            "15 second test video for 2-min breaks",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            15,
            "US-NY",
            2
        ));
        
        ads.add(createAdItem(
            "short_003",
            "Short Test 3",
            "15 second test video for 2-min breaks",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            15,
            "US-NY",
            3
        ));
        
        return ads;
    }
    
    // Content-only playlist (no ads) with 2-minute breaks
    public static List<AdItem> createContentOnlyPlaylist() {
        List<AdItem> content = new ArrayList<>();
        
        content.add(createAdItem(
            "content_big_buck",
            "Big Buck Bunny",
            "Full length animated short with 2-min breaks",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            596,
            "US-NY",
            1,
            "content"
        ));
        
        content.add(createAdItem(
            "content_sintel",
            "Sintel",
            "Fantasy animated short with 2-min breaks",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            888,
            "US-NY",
            2,
            "content"
        ));
        
        return content;
    }
    
    // Ad-only playlist (no content, no 2-minute breaks)
    public static List<AdItem> createAdOnlyPlaylist() {
        List<AdItem> ads = new ArrayList<>();
        
        ads.add(createAdItem(
            "ad_blazes",
            "For Bigger Blazes",
            "Action advertisement",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            15,
            "US-NY",
            1,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_escapes",
            "For Bigger Escapes",
            "Adventure advertisement",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            15,
            "US-NY",
            2,
            "advertisement"
        ));
        
        ads.add(createAdItem(
            "ad_fun",
            "For Bigger Fun",
            "Entertainment advertisement",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            60,
            "US-NY",
            3,
            "advertisement"
        ));
        
        return ads;
    }
}