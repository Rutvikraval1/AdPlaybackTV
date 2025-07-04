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

    public AdManifest() {}

    // Getters and setters
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getGeoLocation() { return geoLocation; }
    public void setGeoLocation(String geoLocation) { this.geoLocation = geoLocation; }

    public List<AdItem> getAds() { return ads; }
    public void setAds(List<AdItem> ads) { this.ads = ads; }

    @Override
    public String toString() {
        return "AdManifest{" +
                "version='" + version + '\'' +
                ", lastUpdated='" + lastUpdated + '\'' +
                ", geoLocation='" + geoLocation + '\'' +
                ", ads=" + (ads != null ? ads.size() : 0) +
                '}';
    }
}