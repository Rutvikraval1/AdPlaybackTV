package com.example.playback_tv.tv.database;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(
    entities = {AdEntity.class, PlaybackMetricEntity.class},
    version = 1,
    exportSchema = false
)
public abstract class AdDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "ad_database";
    private static volatile AdDatabase INSTANCE;

    public abstract AdDao adDao();
    public abstract PlaybackMetricDao playbackMetricDao();

    public static AdDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AdDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AdDatabase.class,
                        DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}