package com.example.playback_tv.tv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.playback_tv.tv.service.AdPlaybackService;
import com.example.playback_tv.tv.ui.PlayerActivity;
import com.example.playback_tv.tv.utils.SharedPreferencesHelper;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || 
            "android.intent.action.QUICKBOOT_POWERON".equals(action)) {
            
            Log.d(TAG, "Device boot completed, starting ad playback");
            
            SharedPreferencesHelper prefsHelper = new SharedPreferencesHelper(context);
            
            // Start background service
            Intent serviceIntent = new Intent(context, AdPlaybackService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            }

            // Auto-start player if enabled
            if (prefsHelper.shouldAutoResume()) {
                Intent playerIntent = new Intent(context, PlayerActivity.class);
                playerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(playerIntent);
            }
        }
    }
}