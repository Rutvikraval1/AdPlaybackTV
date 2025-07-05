package com.example.playback_tv.tv.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

//import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.manager.AdManager;
import com.example.playback_tv.tv.manager.CacheManager;
import com.example.playback_tv.tv.manager.LocationManager;
import com.example.playback_tv.tv.service.AdPlaybackService;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    
    private AdManager adManager;
    private CacheManager cacheManager;
    private LocationManager locationManager;
    private SharedPreferencesHelper prefsHelper;
    
    private TextView statusText;
    private Button playButton;
    private Button downloadButton;
    private Button refreshButton;
    private Button loadShortAdsButton;

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
        startAdPlaybackService();
    }

    private void initializeComponents() {
        adManager = new AdManager(this);
        cacheManager = new CacheManager(this);
        locationManager = new LocationManager(this);
        prefsHelper = new SharedPreferencesHelper(this);
    }

    private void setupViews() {
        statusText = findViewById(R.id.status_text);
        playButton = findViewById(R.id.play_button);
        downloadButton = findViewById(R.id.download_button);
        refreshButton = findViewById(R.id.refresh_button);
        loadShortAdsButton = findViewById(R.id.load_short_ads_button);
        
        updateUI();
    }

    private void setupListeners() {
        playButton.setOnClickListener(v -> startPlayback());
        downloadButton.setOnClickListener(v -> downloadAds());
        refreshButton.setOnClickListener(v -> refreshManifest());
        loadShortAdsButton.setOnClickListener(v -> loadShortTestAds());
    }

    private void loadTempData() {
        statusText.setText("Loading temporary ads...");
        
        // Load temp manifest immediately
        adManager.refreshManifest("temp", new AdManager.ManifestCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    statusText.setText("Temporary ads loaded successfully");
                    updateUI();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    statusText.setText("Failed to load temp ads: " + error);
                });
            }
        });
    }

    private void loadShortTestAds() {
        statusText.setText("Loading short test ads...");
        
        adManager.loadShortTestAds();
        statusText.setText("Short test ads loaded (15 seconds each)");
        updateUI();
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
                        statusText.setText("Manifest refreshed");
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

    private void startAdPlaybackService() {
        Intent serviceIntent = new Intent(this, AdPlaybackService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        }
    }

    private void updateUI() {
        int cachedAdsCount = cacheManager.getCachedAdsCount();
        String currentLocation = locationManager.getCachedLocation();
        
        // Get current playlist info
        var currentPlaylist = adManager.getCurrentPlaylist();
        int totalAds = currentPlaylist != null ? currentPlaylist.size() : 0;
        
        statusText.setText(String.format(
            "Total ads: %d\nCached ads: %d\nLocation: %s\n\nReady to play!", 
            totalAds,
            cachedAdsCount, 
            currentLocation != null ? currentLocation : "Unknown"
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }
}