package com.example.playback_tv.tv.model;

import java.io.File;

public class FileItem {
    private String name;
    private File file;
    private boolean isDirectory;

    public FileItem(String name, File file, boolean isDirectory) {
        this.name = name;
        this.file = file;
        this.isDirectory = isDirectory;
    }

    public String getName() { return name; }
    public File getFile() { return file; }
    public boolean isDirectory() { return isDirectory; }
    
    public String getDisplayName() {
        if (isDirectory) {
            return "📁 " + name;
        } else {
            return "🎬 " + name;
        }
    }
}