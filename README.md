# YouTube Video Downloader Android App

An Android application that allows users to download videos and audio from YouTube. Built with Java and the Android SDK.

## Features

- Download YouTube videos in various qualities
- Extract audio from videos
- Support for both progressive and adaptive streams
- Real-time download progress tracking
- Video format conversion support
- Custom video quality selection
- Thumbnail preview
- Video metadata display (title, length, etc.)

## Technical Details

The app is built using:

- JavaTube <https://github.com/felipeucelli/JavaTube>
- Java
- Android SDK (minimum SDK version 29)
- FFmpeg for media processing

Key components:

- `Youtube.java` - Core YouTube interaction logic
- `Stream.java` - Handles video/audio stream downloading
- `Cipher.java` - Manages YouTube signature decryption
- `Request.java` - Network request handling

## Project Structure

The project follows standard Android application architecture:

```bash
app/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/projekat/
│   │   │       ├── javatube/           # Core YouTube functionality
│   │   │       ├── MainActivity.java    # Entry point
│   │   │       └── DownloadActivity.java # Download handling
│   │   └── res/                        # Android resources
└── build.gradle                        # Project configuration
```

## Dependencies

This app uses the following key libraries:

- **AndroidX & Material Design** - For modern Android UI components and layouts
- **FFmpeg** - For processing and converting video/audio files
- **Glide** - For loading and displaying video thumbnails
- **Rhino** - For JavaScript processing

## Permissions

The app requires the following permissions to function properly:

- **Internet Access** - To download videos from YouTube
- **Storage Access** - To save downloaded videos and audio files to your device

Note: When you first launch the app, you'll be asked to grant these permissions. The app cannot function without them.

## Building

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on your device/emulator

## Note

This application is for educational purposes only. Please respect YouTube's terms of service and content creators' rights when using this application.
