package com.example.playback_tv.tv.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.config.UniqueIdManager;
import com.example.playback_tv.tv.database.AdDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StandardScreenActivity extends FragmentActivity {
    private static final String TAG = "StandardScreenActivity";
    
    private TextView statusText;
    private TextView timeText;
    private TextView deviceIdText;
    private Button openFileBrowserButton;
    private Button playAdsButton;
    private Button settingsButton;
    
    private UniqueIdManager uniqueIdManager;
    private AdDatabase database;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standard_screen);
        
        initializeComponents();
        setupViews();
        setupListeners();
        updateDisplay();
    }

    private void initializeComponents() {
        uniqueIdManager = new UniqueIdManager(this);
        database = AdDatabase.getInstance(this);
        executor = Executors.newCachedThreadPool();
    }

    private void setupViews() {
        statusText = findViewById(R.id.status_text);
        timeText = findViewById(R.id.time_text);
        deviceIdText = findViewById(R.id.device_id_text);
        openFileBrowserButton = findViewById(R.id.open_file_browser_button);
        playAdsButton = findViewById(R.id.play_ads_button);
        settingsButton = findViewById(R.id.settings_button);
    }

    private void setupListeners() {
        openFileBrowserButton.setOnClickListener(v -> openFileBrowser());
        playAdsButton.setOnClickListener(v -> playAds());
        settingsButton.setOnClickListener(v -> openSettings());
    }

    private void updateDisplay() {
        // Update time
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        
        timeText.setText(timeFormat.format(new Date()));
        
        // Update device ID
        deviceIdText.setText("Device ID: " + uniqueIdManager.getUniqueId());
        
        // Update ad status
        executor.execute(() -> {
            int downloadedAds = database.adDao().getDownloadedAdsCount();
            
            runOnUiThread(() -> {
                statusText.setText(String.format(Locale.getDefault(),
                    "Android TV Ad System Ready\n\n" +
                    "📺 Standard Screen Active\n" +
                    "📁 Downloaded Ads: %d\n" +
                    "📅 %s\n\n" +
                    "Select an option below or use your remote control",
                    downloadedAds,
                    dateFormat.format(new Date())
                ));
            });
        });
    }

    private void openFileBrowser() {
        Intent intent = new Intent(this, FileBrowserActivity.class);
        startActivity(intent);
    }

    private void playAds() {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("playback_mode", "ads_only");
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}