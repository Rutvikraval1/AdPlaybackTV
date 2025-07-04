package com.example.playback_tv.tv.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesHelper {
    private static final String PREFS_NAME = "ad_playback_prefs";
    private static final String KEY_MANIFEST = "manifest";
    private static final String KEY_LOCATION = "location";
    private static final String KEY_CACHED_AD_IDS = "cached_ad_ids";
    private static final String KEY_LAST_PLAYED_AD_ID = "last_played_ad_id";
    private static final String KEY_LAST_PLAYED_AD_INDEX = "last_played_ad_index";
    private static final String KEY_LAST_PLAYBACK_TIME = "last_playback_time";
    private static final String KEY_AUTO_RESUME = "auto_resume";
    private static final String KEY_PLAYBACK_POSITION = "playback_position_";
    
    private final SharedPreferences prefs;
    private final Gson gson;

    public SharedPreferencesHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveManifest(String manifestJson) {
        prefs.edit().putString(KEY_MANIFEST, manifestJson).apply();
    }

    public String getCachedManifest() {
        return prefs.getString(KEY_MANIFEST, null);
    }

    public void saveLocation(String location) {
        prefs.edit().putString(KEY_LOCATION, location).apply();
    }

    public String getLocation() {
        return prefs.getString(KEY_LOCATION, null);
    }

    public void saveCachedAdIds(List<String> adIds) {
        String json = gson.toJson(adIds);
        prefs.edit().putString(KEY_CACHED_AD_IDS, json).apply();
    }

    public List<String> getCachedAdIds() {
        String json = prefs.getString(KEY_CACHED_AD_IDS, null);
        if (json != null) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            return gson.fromJson(json, listType);
        }
        return new ArrayList<>();
    }

    public void saveLastPlayedAdId(String adId) {
        prefs.edit().putString(KEY_LAST_PLAYED_AD_ID, adId).apply();
    }

    public String getLastPlayedAdId() {
        return prefs.getString(KEY_LAST_PLAYED_AD_ID, null);
    }

    public void saveLastPlayedAdIndex(int index) {
        prefs.edit().putInt(KEY_LAST_PLAYED_AD_INDEX, index).apply();
    }

    public int getLastPlayedAdIndex() {
        return prefs.getInt(KEY_LAST_PLAYED_AD_INDEX, 0);
    }

    public void saveLastPlaybackTime(long time) {
        prefs.edit().putLong(KEY_LAST_PLAYBACK_TIME, time).apply();
    }

    public long getLastPlaybackTime() {
        return prefs.getLong(KEY_LAST_PLAYBACK_TIME, 0);
    }

    public void setAutoResume(boolean autoResume) {
        prefs.edit().putBoolean(KEY_AUTO_RESUME, autoResume).apply();
    }

    public boolean shouldAutoResume() {
        return prefs.getBoolean(KEY_AUTO_RESUME, true);
    }

    public void savePlaybackPosition(String adId, long position) {
        prefs.edit().putLong(KEY_PLAYBACK_POSITION + adId, position).apply();
    }

    public long getPlaybackPosition(String adId) {
        return prefs.getLong(KEY_PLAYBACK_POSITION + adId, 0);
    }
}