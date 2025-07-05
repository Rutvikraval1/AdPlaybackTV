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
import com.example.playback_tv.tv.manager.BreakTimeManager;
import com.example.playback_tv.tv.manager.PlaybackManager;
import com.example.playback_tv.tv.model.AdBreak;
import com.example.playback_tv.tv.model.AdItem;
import com.example.playback_tv.tv.model.AdManifest;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.util.List;

public class PlayerActivity extends FragmentActivity implements BreakTimeManager.BreakTimeCallback {
    private static final String TAG = "PlayerActivity";
    
    private ExoPlayer player;
    private StyledPlayerView playerView;
    private PlaybackManager playbackManager;
    private AdManager adManager;
    private BreakTimeManager breakTimeManager;
    private SharedPreferencesHelper prefsHelper;
    
    private List<AdItem> currentPlaylist;
    private List<AdItem> breakAds;
    private int currentAdIndex = 0;
    private boolean isPlaying = false;
    private boolean isInBreak = false;
    private int breakAdIndex = 0;
    
    private AdItem pausedMainContent;
    private long pausedPosition = 0;

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
        setupBreakTimeManager();
    }

    private void initializeComponents() {
        playerView = findViewById(R.id.player_view);
        playbackManager = new PlaybackManager(this);
        adManager = new AdManager(this);
        breakTimeManager = new BreakTimeManager(this);
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
            Log.d(TAG, "Loaded playlist with " + currentPlaylist.size() + " items");
            
            // Resume from last position if available
            currentAdIndex = prefsHelper.getLastPlayedAdIndex();
            if (currentAdIndex >= currentPlaylist.size()) {
                currentAdIndex = 0;
            }
            
            // Show toast with playlist info
            Toast.makeText(this, 
                "Playing " + currentPlaylist.size() + " items", 
                Toast.LENGTH_SHORT).show();
            
            playNextItem();
        } else {
            Log.e(TAG, "No content available for playback");
            Toast.makeText(this, "No content available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBreakTimeManager() {
        breakTimeManager.setBreakTimeCallback(this);
        
        // Get manifest to setup break scheduling
        AdManifest manifest = adManager.getCurrentManifest();
        if (manifest != null && manifest.getAdBreaks() != null) {
            breakTimeManager.scheduleBreaks(
                manifest.getAdBreaks(), 
                manifest.getBreakIntervalMinutes()
            );
            
            Log.d(TAG, "Scheduled " + manifest.getAdBreaks().size() + " ad breaks");
            Toast.makeText(this, 
                "Break schedule active: " + manifest.getAdBreaks().size() + " breaks", 
                Toast.LENGTH_SHORT).show();
        }
    }

    private void playNextItem() {
        if (isInBreak) {
            playNextBreakAd();
            return;
        }
        
        if (currentPlaylist == null || currentAdIndex >= currentPlaylist.size()) {
            // Loop back to beginning
            currentAdIndex = 0;
        }
        
        AdItem item = currentPlaylist.get(currentAdIndex);
        
        if (item != null) {
            // Check if we have a local cached version first
            if (item.getLocalPath() != null) {
                playItem(item);
            } else {
                // Play from URL directly (for temp data)
                playItemFromUrl(item);
            }
        } else {
            Log.w(TAG, "Item is null, skipping");
            currentAdIndex++;
            playNextItem();
        }
    }

    private void playNextBreakAd() {
        if (breakAds == null || breakAdIndex >= breakAds.size()) {
            // End of break ads, return to main content
            endBreak();
            return;
        }
        
        AdItem breakAd = breakAds.get(breakAdIndex);
        if (breakAd != null) {
            playBreakAd(breakAd);
        } else {
            breakAdIndex++;
            playNextBreakAd();
        }
    }

    private void playItem(AdItem item) {
        Log.d(TAG, "Playing cached item: " + item.getTitle());
        
        // Save current position
        prefsHelper.saveLastPlayedAdIndex(currentAdIndex);
        prefsHelper.saveLastPlayedAdId(item.getId());
        
        // Create media source from local file
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(item.getLocalPath())));
        
        // Set media source and play
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        isPlaying = true;
        
        // Update analytics
        playbackManager.trackAdPlayback(item);
        
        // Show current item info
        String itemType = item.getDuration() > 300 ? "Content" : "Ad";
        Toast.makeText(this, 
            "Playing: " + item.getTitle() + " (" + itemType + " " + (currentAdIndex + 1) + "/" + currentPlaylist.size() + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void playItemFromUrl(AdItem item) {
        Log.d(TAG, "Playing item from URL: " + item.getTitle());
        
        // Save current position
        prefsHelper.saveLastPlayedAdIndex(currentAdIndex);
        prefsHelper.saveLastPlayedAdId(item.getId());
        
        // Create media source from URL
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(item.getVideoUrl())));
        
        // Set media source and play
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        isPlaying = true;
        
        // Update analytics
        playbackManager.trackAdPlayback(item);
        
        // Show current item info
        String itemType = item.getDuration() > 300 ? "Content" : "Ad";
        Toast.makeText(this, 
            "Streaming: " + item.getTitle() + " (" + itemType + " " + (currentAdIndex + 1) + "/" + currentPlaylist.size() + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void playBreakAd(AdItem breakAd) {
        Log.d(TAG, "Playing break ad: " + breakAd.getTitle());
        
        // Create media source from URL
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(breakAd.getVideoUrl())));
        
        // Set media source and play
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        isPlaying = true;
        
        // Update analytics
        playbackManager.trackAdPlayback(breakAd);
        
        // Show break ad info
        Toast.makeText(this, 
            "Break Ad: " + breakAd.getTitle() + " (" + (breakAdIndex + 1) + "/" + breakAds.size() + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void handlePlaybackStateChange(int state) {
        switch (state) {
            case Player.STATE_ENDED:
                Log.d(TAG, "Playback ended");
                if (isInBreak) {
                    breakAdIndex++;
                    playNextBreakAd();
                } else {
                    currentAdIndex++;
                    playNextItem();
                }
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

    // BreakTimeManager.BreakTimeCallback implementation
    @Override
    public void onBreakTimeTriggered(AdBreak adBreak) {
        Log.d(TAG, "Break time triggered: " + adBreak.getId());
        
        if (isInBreak) {
            Log.d(TAG, "Already in break, ignoring new break");
            return;
        }
        
        // Pause current content if playing
        if (player != null && player.isPlaying()) {
            pausedPosition = player.getCurrentPosition();
            pausedMainContent = currentPlaylist.get(currentAdIndex);
            player.pause();
        }
        
        // Start break
        startBreak(adBreak);
    }

    @Override
    public void onBreakTimeEnded(AdBreak adBreak) {
        Log.d(TAG, "Break time ended: " + adBreak.getId());
        
        if (!isInBreak) {
            Log.d(TAG, "Not in break, ignoring break end");
            return;
        }
        
        // Force end break if still playing break ads
        endBreak();
    }

    private void startBreak(AdBreak adBreak) {
        Log.d(TAG, "Starting ad break: " + adBreak.getId());
        
        isInBreak = true;
        breakAdIndex = 0;
        breakAds = adBreak.getAds();
        
        Toast.makeText(this, 
            "Ad Break Started: " + adBreak.getId(), 
            Toast.LENGTH_LONG).show();
        
        if (breakAds != null && !breakAds.isEmpty()) {
            playNextBreakAd();
        } else {
            // No break ads, end break immediately
            endBreak();
        }
    }

    private void endBreak() {
        Log.d(TAG, "Ending ad break");
        
        isInBreak = false;
        breakAdIndex = 0;
        breakAds = null;
        
        Toast.makeText(this, 
            "Ad Break Ended - Resuming Content", 
            Toast.LENGTH_SHORT).show();
        
        // Resume main content if we paused it
        if (pausedMainContent != null) {
            resumePausedContent();
        } else {
            // Continue with next item
            playNextItem();
        }
    }

    private void resumePausedContent() {
        Log.d(TAG, "Resuming paused content: " + pausedMainContent.getTitle());
        
        // Play the paused content
        if (pausedMainContent.getLocalPath() != null) {
            playItem(pausedMainContent);
        } else {
            playItemFromUrl(pausedMainContent);
        }
        
        // Seek to paused position
        if (pausedPosition > 0) {
            player.seekTo(pausedPosition);
        }
        
        // Clear paused state
        pausedMainContent = null;
        pausedPosition = 0;
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
        
        // Stop break time manager
        if (breakTimeManager != null) {
            breakTimeManager.stopAllTimers();
        }
        
        // Release player
        if (player != null) {
            player.release();
            player = null;
        }
    }
}