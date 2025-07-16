package com.example.playback_tv.tv.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ads")
public class AdEntity {
    @PrimaryKey
    public String id;
    
    public String title;
    public String description;
    public String videoUrl;
    public String localFilePath;
    public int duration; // in seconds
    public String scheduleTime; // HH:mm format
    public int priority;
    public String geoLocation;
    public boolean isDownloaded;
    public boolean isPlayed;
    public long downloadTimestamp;
    public long lastPlayedTimestamp;
    public String adType; // "30s" or "60s"
    
    public AdEntity() {}
    
    public AdEntity(String id, String title, String videoUrl, int duration, String scheduleTime) {
        this.id = id;
        this.title = title;
        this.videoUrl = videoUrl;
        this.duration = duration;
        this.scheduleTime = scheduleTime;
        this.isDownloaded = false;
        this.isPlayed = false;
        this.priority = 1;
        this.adType = duration <= 30 ? "30s" : "60s";
    }
}