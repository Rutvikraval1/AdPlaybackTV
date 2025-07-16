package com.example.playback_tv.tv.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.manager.AdManager;
import com.example.playback_tv.tv.manager.BreakTimeManager;
import com.example.playback_tv.tv.manager.CacheManager;
import com.example.playback_tv.tv.manager.LocationManager;
import com.example.playback_tv.tv.service.BackgroundSyncService;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    
    private AdManager adManager;
    private CacheManager cacheManager;
    private LocationManager locationManager;
    private BreakTimeManager breakTimeManager;
    private SharedPreferencesHelper prefsHelper;
    
    private TextView statusText;
    private Button playButton;
    private Button downloadButton;
    private Button refreshButton;
    private Button forceBreakButton;
    private Button loadShortAdsButton;
    private Button contentOnlyButton;
    private Button adsOnlyButton;
    private Button mixedContentButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        setupViews();
        setupListeners();
        
        // Load temp data immediately for testing
        loadTempData();
        
        // Check for auto-resume
        checkAutoResume();
        
        // Start background service
        startBackgroundService();
    }

    private void initializeComponents() {
        adManager = new AdManager(this);
        cacheManager = new CacheManager(this);
        locationManager = new LocationManager(this);
        breakTimeManager = new BreakTimeManager(this);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void setupViews() {
        statusText = findViewById(R.id.status_text);
        playButton = findViewById(R.id.play_button);
        downloadButton = findViewById(R.id.download_button);
        refreshButton = findViewById(R.id.refresh_button);
        forceBreakButton = findViewById(R.id.force_break_button);
        loadShortAdsButton = findViewById(R.id.load_short_ads_button);
        contentOnlyButton = findViewById(R.id.content_only_button);
        adsOnlyButton = findViewById(R.id.ads_only_button);
        mixedContentButton = findViewById(R.id.mixed_content_button);
        
        updateUI();
    }

    private void setupListeners() {
        playButton.setOnClickListener(v -> startPlayback());
        downloadButton.setOnClickListener(v -> downloadAds());
        refreshButton.setOnClickListener(v -> refreshManifest());
        forceBreakButton.setOnClickListener(v -> forceBreak());
        loadShortAdsButton.setOnClickListener(v -> loadShortTestAds());
        contentOnlyButton.setOnClickListener(v -> loadContentOnly());
        adsOnlyButton.setOnClickListener(v -> loadAdsOnly());
        mixedContentButton.setOnClickListener(v -> loadMixedContent());
    }

    private void loadTempData() {
        statusText.setText("Loading temporary content with break schedule...");
        
        // Load temp manifest immediately
        adManager.refreshManifest("temp", new AdManager.ManifestCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    statusText.setText("Content loaded with break schedule active");
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusText.setText("Failed to load content: " + error);
                });
            }
        });
    }

    private void loadShortTestAds() {
        statusText.setText("Loading short test ads (15 seconds each)...");
        
        adManager.loadShortTestAds();
        statusText.setText("Short test ads loaded - 5 minute break intervals");
        updateUI();
    }

    private void loadContentOnly() {
        statusText.setText("Loading content-only playlist...");
        
        adManager.loadContentOnlyPlaylist();
        statusText.setText("Content-only playlist loaded - 10 minute break intervals");
        updateUI();
    }

    private void loadAdsOnly() {
        statusText.setText("Loading ads-only playlist...");
        
        adManager.loadAdOnlyPlaylist();
        statusText.setText("Ads-only playlist loaded - no scheduled breaks");
        updateUI();
    }

    private void loadMixedContent() {
        statusText.setText("Loading mixed content playlist...");
        
        // Load the default temp manifest (mixed content)
        adManager.refreshManifest("temp", new AdManager.ManifestCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    statusText.setText("Mixed content loaded - full break schedule active");
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusText.setText("Failed to load mixed content: " + error);
                });
            }
        });
    }

    private void forceBreak() {
        statusText.setText("Forcing ad break...");
        
        if (breakTimeManager != null) {
            breakTimeManager.forceBreak();
            statusText.setText("Manual ad break triggered");
        } else {
            statusText.setText("Break manager not available");
        }
    }

    private void startPlayback() {
        Intent intent = new Intent(this, PlayerActivity.class);
        startActivity(intent);
    }

    private void downloadAds() {
        statusText.setText("Downloading ads...");
        downloadButton.setEnabled(false);
        
        adManager.downloadAds(new AdManager.DownloadCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    statusText.setText("Ads downloaded successfully");
                    downloadButton.setEnabled(true);
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusText.setText("Download failed: " + error);
                    downloadButton.setEnabled(true);
                });
            }
        });
    }

    private void refreshManifest() {
        statusText.setText("Refreshing manifest...");
        refreshButton.setEnabled(false);
        
        locationManager.getCurrentLocation(location -> {
            adManager.refreshManifest(location, new AdManager.ManifestCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        statusText.setText("Manifest refreshed with break schedule");
                        refreshButton.setEnabled(true);
                        updateUI();
                    });
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        statusText.setText("Refresh failed: " + error);
                        refreshButton.setEnabled(true);
                    });
                }
            });
        });
    }

    private void checkAutoResume() {
        if (prefsHelper.shouldAutoResume()) {
            // Auto-start playback after device boot
            startPlayback();
        }
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, BackgroundSyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

    private void updateUI() {
        int cachedAdsCount = cacheManager.getCachedAdsCount();
        String currentLocation = locationManager.getCachedLocation();
        
        // Get current playlist info
        var currentPlaylist = adManager.getCurrentPlaylist();
        var currentManifest = adManager.getCurrentManifest();
        
        int totalItems = currentPlaylist != null ? currentPlaylist.size() : 0;
        int totalBreaks = (currentManifest != null && currentManifest.getAdBreaks() != null) 
                         ? currentManifest.getAdBreaks().size() : 0;
        int breakInterval = currentManifest != null ? currentManifest.getBreakIntervalMinutes() : 0;
        
        statusText.setText(String.format(
            "Content: %d items | Cached: %d | Breaks: %d scheduled\n" +
            "Interval: %d min | Location: %s\n\n" +
            "Ready to play with break time management!", 
            totalItems,
            cachedAdsCount,
            totalBreaks,
            breakInterval,
            currentLocation != null ? currentLocation : "Unknown"
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (breakTimeManager != null) {
            breakTimeManager.stopAllTimers();
        }
    }
}