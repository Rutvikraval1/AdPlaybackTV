package com.example.playback_tv.tv.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdBreak {
    @SerializedName("id")
    private String id;
    
    @SerializedName("break_time")
    private String breakTime; // Format: "HH:mm" or "HH:mm:ss"
    
    @SerializedName("break_duration")
    private int breakDuration; // Duration in seconds
    
    @SerializedName("break_type")
    private String breakType; // "scheduled", "interval", "content_break"
    
    @SerializedName("ads")
    private List<AdItem> ads;
    
    @SerializedName("priority")
    private int priority;
    
    @SerializedName("repeat_daily")
    private boolean repeatDaily;
    
    @SerializedName("enabled")
    private boolean enabled;

    public AdBreak() {
        this.enabled = true;
        this.repeatDaily = true;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBreakTime() { return breakTime; }
    public void setBreakTime(String breakTime) { this.breakTime = breakTime; }

    public int getBreakDuration() { return breakDuration; }
    public void setBreakDuration(int breakDuration) { this.breakDuration = breakDuration; }

    public String getBreakType() { return breakType; }
    public void setBreakType(String breakType) { this.breakType = breakType; }

    public List<AdItem> getAds() { return ads; }
    public void setAds(List<AdItem> ads) { this.ads = ads; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public boolean isRepeatDaily() { return repeatDaily; }
    public void setRepeatDaily(boolean repeatDaily) { this.repeatDaily = repeatDaily; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public String toString() {
        return "AdBreak{" +
                "id='" + id + '\'' +
                ", breakTime='" + breakTime + '\'' +
                ", breakType='" + breakType + '\'' +
                ", ads=" + (ads != null ? ads.size() : 0) +
                ", enabled=" + enabled +
                '}';
    }
}