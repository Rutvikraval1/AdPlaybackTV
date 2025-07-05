package com.example.playback_tv.tv.data;

import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.model.AdManifest;

import java.util.ArrayList;
import java.util.List;

public class TempAdData {
    
    public static AdManifest createTempManifest() {
        AdManifest manifest = new AdManifest();
        manifest.setVersion("1.0.0");
        manifest.setLastUpdated("2025-01-04T10:00:00Z");
        manifest.setGeoLocation("US-NY");
        manifest.setAds(createTempAds());
        return manifest;
    }
    
    public static List<AdItem> createTempAds() {
        List<AdItem> ads = new ArrayList<>();
        
        // Sample video URLs - using publicly available test videos
        ads.add(createAdItem(
            "ad_001",
            "Big Buck Bunny",
            "Blender Foundation's Big Buck Bunny",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            596, // duration in seconds
            "US-NY",
            1
        ));
        
        ads.add(createAdItem(
            "ad_002", 
            "Elephant Dream",
            "Blender Foundation's Elephant Dream",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            653,
            "US-NY",
            2
        ));
        
        ads.add(createAdItem(
            "ad_003",
            "For Bigger Blazes",
            "Sample promotional video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            15,
            "US-NY",
            3
        ));
        
        ads.add(createAdItem(
            "ad_004",
            "For Bigger Escape",
            "Adventure themed video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            15,
            "US-NY",
            4
        ));
        
        ads.add(createAdItem(
            "ad_005",
            "For Bigger Fun",
            "Entertainment video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
            60,
            "US-NY",
            5
        ));
        
        ads.add(createAdItem(
            "ad_006",
            "For Bigger Joyrides",
            "Action packed video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            15,
            "US-NY",
            6
        ));
        
        ads.add(createAdItem(
            "ad_007",
            "For Bigger Meltdowns",
            "Dramatic video content",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
            15,
            "US-NY",
            7
        ));
        
        ads.add(createAdItem(
            "ad_008",
            "Sintel",
            "Blender Foundation's Sintel",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            888,
            "US-NY",
            8
        ));
        
        ads.add(createAdItem(
            "ad_009",
            "Subaru Outback",
            "Car commercial sample",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4",
            15,
            "US-NY",
            9
        ));
        
        ads.add(createAdItem(
            "ad_010",
            "Tears of Steel",
            "Blender Foundation's Tears of Steel",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
            734,
            "US-NY",
            10
        ));
        
        return ads;
    }
    
    private static AdItem createAdItem(String id, String title, String description, 
                                     String videoUrl, int duration, String geoLocation, int priority) {
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
    
    // Short test videos for quick testing
    public static List<AdItem> createShortTestAds() {
        List<AdItem> ads = new ArrayList<>();
        
        ads.add(createAdItem(
            "short_001",
            "Short Test 1",
            "15 second test video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
            15,
            "US-NY",
            1
        ));
        
        ads.add(createAdItem(
            "short_002",
            "Short Test 2",
            "15 second test video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
            15,
            "US-NY",
            2
        ));
        
        ads.add(createAdItem(
            "short_003",
            "Short Test 3",
            "15 second test video",
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4",
            15,
            "US-NY",
            3
        ));
        
        return ads;
    }
}