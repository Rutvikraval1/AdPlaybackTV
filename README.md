# Android TV Ad Playback System

A comprehensive Android TV application for ad playback management with offline capabilities, geo-targeting, and auto-resume functionality.

## Features

✅ **ExoPlayer Integration**: Seamless video playback with adaptive streaming
✅ **Offline Caching**: Download and cache ads for offline playback
✅ **JSON Manifest Parsing**: Dynamic ad scheduling based on remote configuration
✅ **Auto-Start on Boot**: Automatically resume playback after device restart
✅ **Geo-Based Targeting**: Location-aware ad delivery
✅ **Resume Logic**: Remember playback position across app restarts
✅ **TV-Optimized UI**: Leanback-compatible interface for Android TV
✅ **Background Service**: Continuous operation and cache management

## Architecture

### Core Components

- **MainActivity**: Main TV launcher interface
- **PlayerActivity**: Full-screen video playback
- **AdManager**: Handles ad manifest and scheduling
- **CacheManager**: Local storage and cache management
- **DownloadManager**: Background ad downloading
- **LocationManager**: Geo-location services
- **PlaybackManager**: Playback state and analytics

### Services

- **AdPlaybackService**: Background service for continuous operation
- **DownloadService**: Handles background downloads
- **BootReceiver**: Auto-start on device boot

## Setup Instructions

1. **Clone the repository**
2. **Open in Android Studio**
3. **Configure API endpoint** in `RetrofitClient.java`
4. **Add your manifest URL** in the network configuration
5. **Build and deploy** to Android TV device

## API Integration

The app expects a JSON manifest in the following format:

```json
{
  "version": "1.0",
  "last_updated": "2024-01-15T10:00:00Z",
  "geo_location": "US-NY",
  "ads": [
    {
      "id": "ad_001",
      "title": "Sample Ad",
      "description": "Sample advertisement",
      "video_url": "https://example.com/ad.mp4",
      "duration": 30,
      "geo_location": "US-NY",
      "priority": 1,
      "schedule_time": "2024-01-15T12:00:00Z"
    }
  ]
}
```

## Permissions

- `INTERNET`: Network access for downloading ads
- `ACCESS_FINE_LOCATION`: Geo-targeting functionality
- `WRITE_EXTERNAL_STORAGE`: Local cache storage
- `RECEIVE_BOOT_COMPLETED`: Auto-start on boot
- `FOREGROUND_SERVICE`: Background service operation

## Key Features Implementation

### 1. Auto-Start on Boot
- `BootReceiver` handles device boot events
- Automatically starts the playback service
- Resumes last played ad if configured

### 2. Offline Playback
- Downloads ads to local storage
- Maintains cache with size limits
- Cleanup of old/unused content

### 3. Geo-Targeting
- Uses device location for ad selection
- Caches location for offline use
- Backend sync for location-based content

### 4. Resume Logic
- Saves playback state in SharedPreferences
- Remembers last played ad and position
- Automatic resume after app restart

## Development Notes

- **Java-based**: Full Java implementation for maximum compatibility
- **ExoPlayer**: Advanced video playback with format support
- **Retrofit**: Network layer for API communication
- **Gson**: JSON parsing for manifest data
- **TV-Optimized**: Leanback UI components for TV navigation

## Testing

1. **Install on Android TV emulator or device**
2. **Configure network settings**
3. **Test offline mode by disconnecting network**
4. **Verify auto-start by rebooting device**
5. **Check cache management and cleanup**

## Production Considerations

- **Error Handling**: Comprehensive error handling for network failures
- **Security**: Secure API endpoints and data storage
- **Performance**: Optimized for TV hardware constraints
- **Monitoring**: Add analytics and crash reporting
- **Updates**: OTA update mechanism for app and content

## Support

For technical support or feature requests, please create an issue in the repository.