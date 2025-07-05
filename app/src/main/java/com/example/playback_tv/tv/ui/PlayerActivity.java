package com.example.playback_tv.tv.ui;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.manager.AdManager;
import com.example.playback_tv.tv.manager.PlaybackManager;
import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.List;

public class PlayerActivity extends FragmentActivity {
    private static final String TAG = "PlayerActivity";
    
    private ExoPlayer player;
    private StyledPlayerView playerView;
    private PlaybackManager playbackManager;
    private AdManager adManager;
    private SharedPreferencesHelper prefsHelper;
    
    private List<AdItem> currentPlaylist;
    private int currentAdIndex = 0;
    private boolean isPlaying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Keep screen on and hide system UI
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        
        setContentView(R.layout.activity_player);
        
        initializeComponents();
        setupPlayer();
        loadPlaylist();
    }

    private void initializeComponents() {
        playerView = findViewById(R.id.player_view);
        playbackManager = new PlaybackManager(this);
        adManager = new AdManager(this);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        
        // Hide player controls for TV experience
        playerView.setUseController(false);
        
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                handlePlaybackStateChange(state);
            }

            @Override
            public void onMediaItemTransition(MediaItem mediaItem, int reason) {
                handleMediaTransition(mediaItem, reason);
            }
        });
    }

    private void loadPlaylist() {
        currentPlaylist = adManager.getCurrentPlaylist();
        
        if (currentPlaylist != null && !currentPlaylist.isEmpty()) {
            Log.d(TAG, "Loaded playlist with " + currentPlaylist.size() + " ads");
            
            // Resume from last position if available
            currentAdIndex = prefsHelper.getLastPlayedAdIndex();
            if (currentAdIndex >= currentPlaylist.size()) {
                currentAdIndex = 0;
            }
            
            // Show toast with playlist info
            Toast.makeText(this, 
                "Playing " + currentPlaylist.size() + " ads", 
                Toast.LENGTH_SHORT).show();
            
            playNextAd();
        } else {
            Log.e(TAG, "No ads available for playback");
            Toast.makeText(this, "No ads available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void playNextAd() {
        if (currentPlaylist == null || currentAdIndex >= currentPlaylist.size()) {
            // Loop back to beginning
            currentAdIndex = 0;
        }
        
        AdItem adItem = currentPlaylist.get(currentAdIndex);
        
        if (adItem != null) {
            // For temp data, we'll play directly from URL
            // Check if we have a local cached version first
            if (adItem.getLocalPath() != null) {
                playAd(adItem);
            } else {
                // Play from URL directly (for temp data)
                playAdFromUrl(adItem);
            }
        } else {
            Log.w(TAG, "Ad item is null, skipping");
            currentAdIndex++;
            playNextAd();
        }
    }

    private void playAd(AdItem adItem) {
        Log.d(TAG, "Playing cached ad: " + adItem.getTitle());
        
        // Save current position
        prefsHelper.saveLastPlayedAdIndex(currentAdIndex);
        prefsHelper.saveLastPlayedAdId(adItem.getId());
        
        // Create media source from local file
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(adItem.getLocalPath())));
        
        // Set media source and play
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        isPlaying = true;
        
        // Update analytics
        playbackManager.trackAdPlayback(adItem);
        
        // Show current ad info
        Toast.makeText(this, 
            "Playing: " + adItem.getTitle() + " (" + (currentAdIndex + 1) + "/" + currentPlaylist.size() + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void playAdFromUrl(AdItem adItem) {
        Log.d(TAG, "Playing ad from URL: " + adItem.getTitle());
        
        // Save current position
        prefsHelper.saveLastPlayedAdIndex(currentAdIndex);
        prefsHelper.saveLastPlayedAdId(adItem.getId());
        
        // Create media source from URL
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(adItem.getVideoUrl())));
        
        // Set media source and play
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        isPlaying = true;
        
        // Update analytics
        playbackManager.trackAdPlayback(adItem);
        
        // Show current ad info
        Toast.makeText(this, 
            "Streaming: " + adItem.getTitle() + " (" + (currentAdIndex + 1) + "/" + currentPlaylist.size() + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void handlePlaybackStateChange(int state) {
        switch (state) {
            case Player.STATE_ENDED:
                Log.d(TAG, "Ad playback ended");
                currentAdIndex++;
                playNextAd();
                break;
            case Player.STATE_READY:
                Log.d(TAG, "Player ready");
                break;
            case Player.STATE_BUFFERING:
                Log.d(TAG, "Player buffering");
                break;
            case Player.STATE_IDLE:
                Log.d(TAG, "Player idle");
                break;
        }
    }

    private void handleMediaTransition(MediaItem mediaItem, int reason) {
        Log.d(TAG, "Media transition: " + reason);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && isPlaying) {
            player.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
            player = null;
        }
    }
}