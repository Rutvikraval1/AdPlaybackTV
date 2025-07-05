package com.example.playback_tv.tv.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AdManifest {
    @SerializedName("version")
    private String version;
    
    @SerializedName("last_updated")
    private String lastUpdated;
    
    @SerializedName("geo_location")
    private String geoLocation;
    
    @SerializedName("ads")
    private List<AdItem> ads;
    
    @SerializedName("ad_breaks")
    private List<AdBreak> adBreaks;
    
    @SerializedName("break_interval_minutes")
    private int breakIntervalMinutes; // Auto break every X minutes
    
    @SerializedName("default_break_duration")
    private int defaultBreakDuration; // Default break duration in seconds

    public AdManifest() {
        this.breakIntervalMinutes = 30; // Default 30 minutes
        this.defaultBreakDuration = 120; // Default 2 minutes
    }

    // Getters and setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getGeoLocation() { return geoLocation; }
    public void setGeoLocation(String geoLocation) { this.geoLocation = geoLocation; }

    public List<AdItem> getAds() { return ads; }
    public void setAds(List<AdItem> ads) { this.ads = ads; }

    public List<AdBreak> getAdBreaks() { return adBreaks; }
    public void setAdBreaks(List<AdBreak> adBreaks) { this.adBreaks = adBreaks; }

    public int getBreakIntervalMinutes() { return breakIntervalMinutes; }
    public void setBreakIntervalMinutes(int breakIntervalMinutes) { this.breakIntervalMinutes = breakIntervalMinutes; }

    public int getDefaultBreakDuration() { return defaultBreakDuration; }
    public void setDefaultBreakDuration(int defaultBreakDuration) { this.defaultBreakDuration = defaultBreakDuration; }

    @Override
    public String toString() {
        return "AdManifest{" +
                "version='" + version + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", geoLocation='" + geoLocation + '\'' +
                ", ads=" + (ads != null ? ads.size() : 0) +
                ", adBreaks=" + (adBreaks != null ? adBreaks.size() : 0) +
                ", breakIntervalMinutes=" + breakIntervalMinutes +
                '}';
    }
}