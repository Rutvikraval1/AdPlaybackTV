package com.example.playback_tv.tv.model;

import com.google.gson.annotations.SerializedName;

public class AdItem {
    @SerializedName("id")
    private String id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("description")
    private String description;
    
    @SerializedName("video_url")
    private String videoUrl;
    
    @SerializedName("duration")
    private int duration;
    
    @SerializedName("geo_location")
    private String geoLocation;
    
    @SerializedName("priority")
    private int priority;
    
    @SerializedName("schedule_time")
    private String scheduleTime;
    
    private String localPath;

    public AdItem() {}

    public AdItem(String id, String title, String videoUrl) {
        this.id = id;
        this.title = title;
        this.videoUrl = videoUrl;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public String getGeoLocation() { return geoLocation; }
    public void setGeoLocation(String geoLocation) { this.geoLocation = geoLocation; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getScheduleTime() { return scheduleTime; }
    public void setScheduleTime(String scheduleTime) { this.scheduleTime = scheduleTime; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    @Override
    public String toString() {
        return "AdItem{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", videoUrl='" + videoUrl + '\'' +
                ", duration=" + duration +
                ", geoLocation='" + geoLocation + '\'' +
                ", priority=" + priority +
                '}';
    }
}