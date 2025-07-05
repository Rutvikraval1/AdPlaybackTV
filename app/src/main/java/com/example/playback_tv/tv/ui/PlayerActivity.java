package com.example.playback_tv.tv.ui;

import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
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

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PlayerActivity extends FragmentActivity implements BreakTimeManager.BreakTimeCallback {
    private static final String TAG = "PlayerActivity";
    private static final int CONTROLS_HIDE_DELAY = 5000; // 5 seconds
    private static final int SEEK_INCREMENT = 10000; // 10 seconds
    private static final int VOLUME_INDICATOR_HIDE_DELAY = 2000; // 2 seconds
    
    // Player components
    private ExoPlayer player;
    private StyledPlayerView playerView;
    private PlaybackManager playbackManager;
    private AdManager adManager;
    private BreakTimeManager breakTimeManager;
    private SharedPreferencesHelper prefsHelper;
    private AudioManager audioManager;
    
    // UI components
    private LinearLayout controlsOverlay;
    private LinearLayout statusOverlay;
    private LinearLayout volumeIndicator;
    private Button playPauseButton;
    private Button previousButton;
    private Button nextButton;
    private Button rewindButton;
    private Button fastForwardButton;
    private Button restartButton;
    private Button stopButton;
    private Button forceBreakButton;
    private Button shuffleButton;
    private Button volumeDownButton;
    private Button volumeUpButton;
    private Button muteButton;
    private Button settingsButton;
    private Button exitButton;
    private SeekBar progressBar;
    private TextView currentTimeText;
    private TextView totalTimeText;
    private TextView currentItemTitle;
    private TextView playlistPosition;
    private TextView breakStatus;
    private TextView nextBreakInfo;
    private ProgressBar volumeProgress;
    
    // Playback state
    private List<AdItem> currentPlaylist;
    private List<AdItem> breakAds;
    private int currentAdIndex = 0;
    private boolean isPlaying = false;
    private boolean isInBreak = false;
    private boolean isShuffleMode = false;
    private boolean isMuted = false;
    private int breakAdIndex = 0;
    
    private AdItem pausedMainContent;
    private long pausedPosition = 0;
    
    // Handlers
    private Handler uiHandler;
    private Handler controlsHandler;
    private Runnable hideControlsRunnable;
    private Runnable updateProgressRunnable;
    private Runnable hideVolumeIndicatorRunnable;
    private Runnable updateBreakTimerRunnable;

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
        setupViews();
        setupPlayer();
        setupListeners();
        loadPlaylist();
        setupBreakTimeManager();
        startProgressUpdater();
        startBreakTimerUpdater();
    }

    private void initializeComponents() {
        playbackManager = new PlaybackManager(this);
        adManager = new AdManager(this);
        breakTimeManager = new BreakTimeManager(this);
        prefsHelper = new SharedPreferencesHelper(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
        uiHandler = new Handler(Looper.getMainLooper());
        controlsHandler = new Handler(Looper.getMainLooper());
        
        hideControlsRunnable = this::hideControls;
        updateProgressRunnable = this::updateProgress;
        hideVolumeIndicatorRunnable = this::hideVolumeIndicator;
        updateBreakTimerRunnable = this::updateBreakTimer;
    }

    private void setupViews() {
        playerView = findViewById(R.id.player_view);
        controlsOverlay = findViewById(R.id.player_controls_overlay);
        statusOverlay = findViewById(R.id.status_overlay);
        volumeIndicator = findViewById(R.id.volume_indicator);
        
        // Control buttons
        playPauseButton = findViewById(R.id.play_pause_button);
        previousButton = findViewById(R.id.previous_button);
        nextButton = findViewById(R.id.next_button);
        rewindButton = findViewById(R.id.rewind_button);
        fastForwardButton = findViewById(R.id.fast_forward_button);
        restartButton = findViewById(R.id.restart_button);
        stopButton = findViewById(R.id.stop_button);
        forceBreakButton = findViewById(R.id.force_break_button);
        shuffleButton = findViewById(R.id.shuffle_button);
        volumeDownButton = findViewById(R.id.volume_down_button);
        volumeUpButton = findViewById(R.id.volume_up_button);
        muteButton = findViewById(R.id.mute_button);
        settingsButton = findViewById(R.id.settings_button);
        exitButton = findViewById(R.id.exit_button);
        
        // Progress and info
        progressBar = findViewById(R.id.progress_bar);
        currentTimeText = findViewById(R.id.current_time);
        totalTimeText = findViewById(R.id.total_time);
        currentItemTitle = findViewById(R.id.current_item_title);
        playlistPosition = findViewById(R.id.playlist_position);
        breakStatus = findViewById(R.id.break_status);
        nextBreakInfo = findViewById(R.id.next_break_info);
        volumeProgress = findViewById(R.id.volume_progress);
        
        // Initially hide controls
        controlsOverlay.setVisibility(View.GONE);
        volumeIndicator.setVisibility(View.GONE);
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        
        // Hide default player controls since we have custom ones
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

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                PlayerActivity.this.isPlaying = isPlaying;
                updatePlayPauseButton();
                updateStatusOverlay();
                
                // Notify break time manager about content state
                if (isPlaying && !isInBreak) {
                    breakTimeManager.onContentStarted();
                } else if (!isPlaying && !isInBreak) {
                    breakTimeManager.onContentPaused();
                }
            }
        });
    }

    private void setupListeners() {
        // Main playback controls
        playPauseButton.setOnClickListener(v -> togglePlayPause());
        previousButton.setOnClickListener(v -> playPrevious());
        nextButton.setOnClickListener(v -> playNext());
        rewindButton.setOnClickListener(v -> seekBackward());
        fastForwardButton.setOnClickListener(v -> seekForward());
        
        // Additional controls
        restartButton.setOnClickListener(v -> restartCurrentItem());
        stopButton.setOnClickListener(v -> stopPlayback());
        forceBreakButton.setOnClickListener(v -> forceAdBreak());
        shuffleButton.setOnClickListener(v -> toggleShuffle());
        
        // Volume controls
        volumeDownButton.setOnClickListener(v -> adjustVolume(-1));
        volumeUpButton.setOnClickListener(v -> adjustVolume(1));
        muteButton.setOnClickListener(v -> toggleMute());
        
        // Other controls
        settingsButton.setOnClickListener(v -> showSettings());
        exitButton.setOnClickListener(v -> exitPlayer());
        
        // Progress bar
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && player != null) {
                    long duration = player.getDuration();
                    if (duration > 0) {
                        long position = (duration * progress) / 100;
                        player.seekTo(position);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                showControls();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                resetControlsTimer();
            }
        });
        
        // Click to show/hide controls
        playerView.setOnClickListener(v -> toggleControls());
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
            
            updatePlaylistInfo();
            playCurrentItem();
        } else {
            Log.e(TAG, "No content available for playback");
            Toast.makeText(this, "No content available", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBreakTimeManager() {
        breakTimeManager.setBreakTimeCallback(this);
        
        AdManifest manifest = adManager.getCurrentManifest();
        if (manifest != null && manifest.getAdBreaks() != null) {
            breakTimeManager.scheduleBreaks(
                manifest.getAdBreaks(), 
                manifest.getBreakIntervalMinutes()
            );
            
            Log.d(TAG, "Scheduled " + manifest.getAdBreaks().size() + " ad breaks with 2-minute system");
            updateBreakInfo();
        }
    }

    private void startBreakTimerUpdater() {
        updateBreakTimer();
    }

    private void updateBreakTimer() {
        if (breakTimeManager != null) {
            String timeUntilBreak = breakTimeManager.getFormattedTimeUntilNextBreak();
            int breakCount = breakTimeManager.getBreakCounter();
            
            if (isInBreak) {
                nextBreakInfo.setText("AD BREAK IN PROGRESS");
            } else {
                nextBreakInfo.setText("Next 2-min break: " + timeUntilBreak + " | Breaks: " + breakCount);
            }
        }
        
        // Update every second
        uiHandler.postDelayed(updateBreakTimerRunnable, 1000);
    }

    // Playback control methods
    private void togglePlayPause() {
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
                Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show();
            } else {
                player.play();
                Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show();
            }
        }
        resetControlsTimer();
    }

    private void playPrevious() {
        if (isInBreak) {
            if (breakAdIndex > 0) {
                breakAdIndex--;
                playCurrentBreakAd();
            }
        } else {
            if (currentAdIndex > 0) {
                currentAdIndex--;
            } else {
                currentAdIndex = currentPlaylist.size() - 1; // Loop to end
            }
            playCurrentItem();
        }
        resetControlsTimer();
    }

    private void playNext() {
        if (isInBreak) {
            breakAdIndex++;
            if (breakAdIndex >= breakAds.size()) {
                endBreak();
            } else {
                playCurrentBreakAd();
            }
        } else {
            currentAdIndex++;
            if (currentAdIndex >= currentPlaylist.size()) {
                currentAdIndex = 0; // Loop to beginning
            }
            playCurrentItem();
        }
        resetControlsTimer();
    }

    private void seekBackward() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long newPosition = Math.max(0, currentPosition - SEEK_INCREMENT);
            player.seekTo(newPosition);
            Toast.makeText(this, "⏪ -10s", Toast.LENGTH_SHORT).show();
        }
        resetControlsTimer();
    }

    private void seekForward() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            long newPosition = Math.min(duration, currentPosition + SEEK_INCREMENT);
            player.seekTo(newPosition);
            Toast.makeText(this, "⏩ +10s", Toast.LENGTH_SHORT).show();
        }
        resetControlsTimer();
    }

    private void restartCurrentItem() {
        if (player != null) {
            player.seekTo(0);
            player.play();
            Toast.makeText(this, "Restarted", Toast.LENGTH_SHORT).show();
        }
        resetControlsTimer();
    }

    private void stopPlayback() {
        if (player != null) {
            player.stop();
            Toast.makeText(this, "Stopped", Toast.LENGTH_SHORT).show();
        }
        resetControlsTimer();
    }

    private void forceAdBreak() {
        if (breakTimeManager != null) {
            breakTimeManager.forceBreak();
            Toast.makeText(this, "Forcing 2-minute ad break...", Toast.LENGTH_SHORT).show();
        }
        resetControlsTimer();
    }

    private void toggleShuffle() {
        isShuffleMode = !isShuffleMode;
        if (isShuffleMode) {
            Collections.shuffle(currentPlaylist);
            shuffleButton.setText("🔀");
            Toast.makeText(this, "Shuffle ON", Toast.LENGTH_SHORT).show();
        } else {
            shuffleButton.setText("Shuffle");
            Toast.makeText(this, "Shuffle OFF", Toast.LENGTH_SHORT).show();
        }
        resetControlsTimer();
    }

    // Volume control methods
    private void adjustVolume(int direction) {
        if (audioManager != null) {
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            
            int newVolume = Math.max(0, Math.min(maxVolume, currentVolume + direction));
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
            
            showVolumeIndicator(newVolume, maxVolume);
        }
        resetControlsTimer();
    }

    private void toggleMute() {
        if (audioManager != null) {
            if (isMuted) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                muteButton.setText("🔇");
                Toast.makeText(this, "Unmuted", Toast.LENGTH_SHORT).show();
            } else {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                muteButton.setText("🔊");
                Toast.makeText(this, "Muted", Toast.LENGTH_SHORT).show();
            }
            isMuted = !isMuted;
        }
        resetControlsTimer();
    }

    private void showVolumeIndicator(int currentVolume, int maxVolume) {
        int volumePercent = (currentVolume * 100) / maxVolume;
        volumeProgress.setProgress(volumePercent);
        volumeIndicator.setVisibility(View.VISIBLE);
        
        uiHandler.removeCallbacks(hideVolumeIndicatorRunnable);
        uiHandler.postDelayed(hideVolumeIndicatorRunnable, VOLUME_INDICATOR_HIDE_DELAY);
    }

    private void hideVolumeIndicator() {
        volumeIndicator.setVisibility(View.GONE);
    }

    // Settings and exit
    private void showSettings() {
        Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
        resetControlsTimer();
    }

    private void exitPlayer() {
        finish();
    }

    // UI control methods
    private void toggleControls() {
        if (controlsOverlay.getVisibility() == View.VISIBLE) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        controlsOverlay.setVisibility(View.VISIBLE);
        statusOverlay.setVisibility(View.VISIBLE);
        resetControlsTimer();
    }

    private void hideControls() {
        controlsOverlay.setVisibility(View.GONE);
        statusOverlay.setVisibility(View.VISIBLE); // Keep status visible
    }

    private void resetControlsTimer() {
        controlsHandler.removeCallbacks(hideControlsRunnable);
        controlsHandler.postDelayed(hideControlsRunnable, CONTROLS_HIDE_DELAY);
    }

    // Progress update methods
    private void startProgressUpdater() {
        updateProgress();
    }

    private void updateProgress() {
        if (player != null) {
            long currentPosition = player.getCurrentPosition();
            long duration = player.getDuration();
            
            if (duration > 0) {
                int progress = (int) ((currentPosition * 100) / duration);
                progressBar.setProgress(progress);
                
                currentTimeText.setText(formatTime(currentPosition));
                totalTimeText.setText(formatTime(duration));
            }
        }
        
        uiHandler.postDelayed(updateProgressRunnable, 1000);
    }

    private String formatTime(long timeMs) {
        long seconds = timeMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private void updatePlayPauseButton() {
        if (isPlaying) {
            playPauseButton.setText("⏸");
        } else {
            playPauseButton.setText("▶");
        }
    }

    private void updatePlaylistInfo() {
        if (currentPlaylist != null) {
            playlistPosition.setText(String.format(Locale.getDefault(), 
                "%d/%d", currentAdIndex + 1, currentPlaylist.size()));
            
            if (currentAdIndex < currentPlaylist.size()) {
                AdItem currentItem = currentPlaylist.get(currentAdIndex);
                currentItemTitle.setText(currentItem.getTitle());
            }
        }
    }

    private void updateStatusOverlay() {
        if (isInBreak) {
            breakStatus.setText("🔴 2-MINUTE AD BREAK");
        } else {
            breakStatus.setText("▶ Content Playing");
        }
    }

    private void updateBreakInfo() {
        // Update break information in status overlay
        AdManifest manifest = adManager.getCurrentManifest();
        if (manifest != null) {
            nextBreakInfo.setText(String.format(Locale.getDefault(),
                "2-min breaks active | Scheduled: %d", 
                manifest.getAdBreaks() != null ? manifest.getAdBreaks().size() : 0));
        }
    }

    // Playback implementation methods
    private void playCurrentItem() {
        if (currentPlaylist == null || currentAdIndex >= currentPlaylist.size()) {
            return;
        }
        
        AdItem item = currentPlaylist.get(currentAdIndex);
        if (item != null) {
            if (item.getLocalPath() != null) {
                playItem(item);
            } else {
                playItemFromUrl(item);
            }
            updatePlaylistInfo();
        }
    }

    private void playCurrentBreakAd() {
        if (breakAds == null || breakAdIndex >= breakAds.size()) {
            return;
        }
        
        AdItem breakAd = breakAds.get(breakAdIndex);
        if (breakAd != null) {
            playBreakAd(breakAd);
        }
    }

    private void playItem(AdItem item) {
        Log.d(TAG, "Playing cached item: " + item.getTitle());
        
        prefsHelper.saveLastPlayedAdIndex(currentAdIndex);
        prefsHelper.saveLastPlayedAdId(item.getId());
        
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(item.getLocalPath())));
        
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        playbackManager.trackAdPlayback(item);
        
        String itemType = item.getDuration() > 300 ? "Content" : "Ad";
        Toast.makeText(this, 
            "Playing: " + item.getTitle() + " (" + itemType + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void playItemFromUrl(AdItem item) {
        Log.d(TAG, "Playing item from URL: " + item.getTitle());
        
        prefsHelper.saveLastPlayedAdIndex(currentAdIndex);
        prefsHelper.saveLastPlayedAdId(item.getId());
        
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(item.getVideoUrl())));
        
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        playbackManager.trackAdPlayback(item);
        
        String itemType = item.getDuration() > 300 ? "Content" : "Ad";
        Toast.makeText(this, 
            "Streaming: " + item.getTitle() + " (" + itemType + ")", 
            Toast.LENGTH_SHORT).show();
    }

    private void playBreakAd(AdItem breakAd) {
        Log.d(TAG, "Playing 2-minute break ad: " + breakAd.getTitle());
        
        DefaultDataSourceFactory dataSourceFactory = 
                new DefaultDataSourceFactory(this, "AdPlayback");
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(Uri.parse(breakAd.getVideoUrl())));
        
        player.setMediaSource(mediaSource);
        player.prepare();
        player.play();
        
        playbackManager.trackAdPlayback(breakAd);
        
        Toast.makeText(this, 
            "2-Min Break: " + breakAd.getTitle() + " (" + (breakAdIndex + 1) + "/" + breakAds.size() + ")", 
            Toast.LENGTH_SHORT).show();
    }

    // ExoPlayer event handlers
    private void handlePlaybackStateChange(int state) {
        switch (state) {
            case Player.STATE_ENDED:
                Log.d(TAG, "Playback ended");
                if (isInBreak) {
                    breakAdIndex++;
                    if (breakAdIndex >= breakAds.size()) {
                        endBreak();
                    } else {
                        playCurrentBreakAd();
                    }
                } else {
                    currentAdIndex++;
                    if (currentAdIndex >= currentPlaylist.size()) {
                        currentAdIndex = 0; // Loop
                    }
                    playCurrentItem();
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
        updatePlaylistInfo();
    }

    // BreakTimeManager.BreakTimeCallback implementation
    @Override
    public void onBreakTimeTriggered(AdBreak adBreak) {
        Log.d(TAG, "2-minute break triggered: " + adBreak.getId());
        
        if (isInBreak) {
            Log.d(TAG, "Already in break, ignoring new break");
            return;
        }
        
        if (player != null && player.isPlaying()) {
            pausedPosition = player.getCurrentPosition();
            pausedMainContent = currentPlaylist.get(currentAdIndex);
            player.pause();
        }
        
        startBreak(adBreak);
    }

    @Override
    public void onBreakTimeEnded(AdBreak adBreak) {
        Log.d(TAG, "2-minute break ended: " + adBreak.getId());
        
        if (!isInBreak) {
            return;
        }
        
        endBreak();
    }

    private void startBreak(AdBreak adBreak) {
        Log.d(TAG, "Starting 2-minute ad break: " + adBreak.getId());
        
        isInBreak = true;
        breakAdIndex = 0;
        breakAds = adBreak.getAds();
        
        updateStatusOverlay();
        
        String breakType = "2_minute_interval".equals(adBreak.getBreakType()) ? 
            "2-Minute Break" : "Ad Break";
        
        Toast.makeText(this, 
            breakType + " Started: " + adBreak.getId(), 
            Toast.LENGTH_LONG).show();
        
        if (breakAds != null && !breakAds.isEmpty()) {
            playCurrentBreakAd();
        } else {
            endBreak();
        }
    }

    private void endBreak() {
        Log.d(TAG, "Ending 2-minute ad break");
        
        isInBreak = false;
        breakAdIndex = 0;
        breakAds = null;
        
        updateStatusOverlay();
        
        Toast.makeText(this, 
            "2-Minute Break Ended - Resuming Content", 
            Toast.LENGTH_SHORT).show();
        
        if (pausedMainContent != null) {
            resumePausedContent();
        } else {
            playCurrentItem();
        }
    }

    private void resumePausedContent() {
        Log.d(TAG, "Resuming paused content: " + pausedMainContent.getTitle());
        
        if (pausedMainContent.getLocalPath() != null) {
            playItem(pausedMainContent);
        } else {
            playItemFromUrl(pausedMainContent);
        }
        
        if (pausedPosition > 0) {
            player.seekTo(pausedPosition);
        }
        
        pausedMainContent = null;
        pausedPosition = 0;
        
        // Notify break time manager that content resumed
        breakTimeManager.onContentResumed();
    }

    // Key event handling for remote control
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                togglePlayPause();
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                seekBackward();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                seekForward();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                playPrevious();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                playNext();
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                adjustVolume(1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                adjustVolume(-1);
                return true;
            case KeyEvent.KEYCODE_VOLUME_MUTE:
                toggleMute();
                return true;
            case KeyEvent.KEYCODE_BACK:
                if (controlsOverlay.getVisibility() == View.VISIBLE) {
                    hideControls();
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_MENU:
                toggleControls();
                return true;
        }
        return super.onKeyDown(keyCode, event);
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
        
        // Stop all handlers
        uiHandler.removeCallbacks(updateProgressRunnable);
        uiHandler.removeCallbacks(updateBreakTimerRunnable);
        controlsHandler.removeCallbacks(hideControlsRunnable);
        uiHandler.removeCallbacks(hideVolumeIndicatorRunnable);
        
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