package com.example.playback_tv.tv.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.playback_tv.R;
import com.example.playback_tv.tv.adapter.FileAdapter;
import com.example.playback_tv.tv.model.FileItem;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileBrowserActivity extends FragmentActivity implements FileAdapter.OnFileClickListener {
    private static final String TAG = "FileBrowserActivity";
    
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private File currentDirectory;
    private List<FileItem> fileItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_browser);
        
        setupViews();
        loadInitialDirectory();
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.files_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        fileItems = new ArrayList<>();
        fileAdapter = new FileAdapter(fileItems, this);
        recyclerView.setAdapter(fileAdapter);
    }

    private void loadInitialDirectory() {
        // Start with external storage or downloads folder
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (downloadsDir.exists()) {
            currentDirectory = downloadsDir;
        } else {
            currentDirectory = Environment.getExternalStorageDirectory();
        }
        
        loadDirectory(currentDirectory);
    }

    private void loadDirectory(File directory) {
        fileItems.clear();
        
        // Add parent directory option (except for root)
        if (directory.getParent() != null) {
            fileItems.add(new FileItem("..", directory.getParentFile(), true));
        }
        
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, (f1, f2) -> {
                if (f1.isDirectory() && !f2.isDirectory()) return -1;
                if (!f1.isDirectory() && f2.isDirectory()) return 1;
                return f1.getName().compareToIgnoreCase(f2.getName());
            });
            
            for (File file : files) {
                if (file.isDirectory() || isVideoFile(file)) {
                    fileItems.add(new FileItem(file.getName(), file, file.isDirectory()));
                }
            }
        }
        
        fileAdapter.notifyDataSetChanged();
        Log.d(TAG, "Loaded directory: " + directory.getAbsolutePath() + " with " + fileItems.size() + " items");
    }

    private boolean isVideoFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".mkv") || 
               name.endsWith(".mov") || name.endsWith(".wmv") || name.endsWith(".flv") ||
               name.endsWith(".webm") || name.endsWith(".m4v");
    }

    @Override
    public void onFileClick(FileItem fileItem) {
        if (fileItem.isDirectory()) {
            currentDirectory = fileItem.getFile();
            loadDirectory(currentDirectory);
        } else {
            // Play selected video file
            playVideoFile(fileItem.getFile());
        }
    }

    private void playVideoFile(File videoFile) {
        Log.d(TAG, "Playing video file: " + videoFile.getAbsolutePath());
        
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("playback_mode", "user_video");
        intent.putExtra("video_path", videoFile.getAbsolutePath());
        intent.putExtra("video_uri", Uri.fromFile(videoFile).toString());
        startActivity(intent);
        
        Toast.makeText(this, "Playing: " + videoFile.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (currentDirectory != null && currentDirectory.getParent() != null) {
            currentDirectory = currentDirectory.getParentFile();
            loadDirectory(currentDirectory);
        } else {
            super.onBackPressed();
        }
    }
}