package com.example.playback_tv.tv.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AdDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAd(AdEntity ad);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAds(List<AdEntity> ads);
    
    @Update
    void updateAd(AdEntity ad);
    
    @Query("SELECT * FROM ads WHERE id = :adId")
    AdEntity getAdById(String adId);
    
    @Query("SELECT * FROM ads WHERE isDownloaded = 1")
    List<AdEntity> getDownloadedAds();
    
    @Query("SELECT * FROM ads WHERE scheduleTime = :time AND isDownloaded = 1")
    List<AdEntity> getAdsForTime(String time);
    
    @Query("SELECT * FROM ads WHERE duration = :duration AND isDownloaded = 1 ORDER BY priority ASC")
    List<AdEntity> getAdsByDuration(int duration);
    
    @Query("SELECT * FROM ads WHERE isDownloaded = 0")
    List<AdEntity> getPendingDownloads();
    
    @Query("UPDATE ads SET isDownloaded = 1, localFilePath = :filePath, downloadTimestamp = :timestamp WHERE id = :adId")
    void markAsDownloaded(String adId, String filePath, long timestamp);
    
    @Query("UPDATE ads SET isPlayed = 1, lastPlayedTimestamp = :timestamp WHERE id = :adId")
    void markAsPlayed(String adId, long timestamp);
    
    @Query("SELECT COUNT(*) FROM ads WHERE isDownloaded = 1")
    int getDownloadedAdsCount();
    
    @Query("DELETE FROM ads WHERE id = :adId")
    void deleteAd(String adId);
    
    @Query("SELECT * FROM ads ORDER BY priority ASC, scheduleTime ASC")
    List<AdEntity> getAllAds();
}