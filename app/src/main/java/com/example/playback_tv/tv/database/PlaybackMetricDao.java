package com.example.playback_tv.tv.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PlaybackMetricDao {
    
    @Insert
    long insertMetric(PlaybackMetricEntity metric);
    
    @Update
    void updateMetric(PlaybackMetricEntity metric);
    
    @Query("SELECT * FROM playback_metrics WHERE sentToBackend = 0")
    List<PlaybackMetricEntity> getUnsentMetrics();
    
    @Query("UPDATE playback_metrics SET sentToBackend = 1 WHERE id = :metricId")
    void markAsSent(long metricId);
    
    @Query("SELECT * FROM playback_metrics WHERE adId = :adId ORDER BY playbackTimestamp DESC")
    List<PlaybackMetricEntity> getMetricsForAd(String adId);
    
    @Query("SELECT COUNT(*) FROM playback_metrics WHERE playbackTimestamp >= :timestamp")
    int getMetricsCountSince(long timestamp);
    
    @Query("DELETE FROM playback_metrics WHERE createdAt < :timestamp")
    void deleteOldMetrics(long timestamp);
}