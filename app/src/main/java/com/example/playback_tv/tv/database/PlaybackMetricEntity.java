package com.example.playback_tv.tv.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "playback_metrics")
public class PlaybackMetricEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String uniqueDeviceId;
    public String adId;
    public long playbackTimestamp;
    public int actualDuration; // actual playback duration
    public int scheduledDuration; // expected duration
    public boolean playbackCompleted;
    public String playbackStatus; // "started", "completed", "interrupted"
    public boolean sentToBackend;
    public long createdAt;
    
    public PlaybackMetricEntity() {
        this.createdAt = System.currentTimeMillis();
        this.sentToBackend = false;
    }
}