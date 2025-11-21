# Running Nông Trí App from Command Line

This guide shows you how to run the app completely outside of Android Studio using command line tools.

## Prerequisites

- Android SDK installed (usually at `~/Library/Android/sdk` on macOS)
- `emulator` and `adb` commands available in your PATH
- At least one Android Virtual Device (AVD) created

## Quick Start

### Option 1: Full Automated Script (Recommended)

The `run-emulator.sh` script handles everything:

```bash
# Start emulator, build, install, and launch
./run-emulator.sh

# Or specify a specific AVD
./run-emulator.sh Pixel_6a_API_34
```

### Option 2: Manual Steps

#### 1. List Available Emulators
```bash
emulator -list-avds
```

#### 2. Start an Emulator
```bash
# Start in background (recommended)
emulator -avd Pixel_8_API_34 &

# Or start in foreground (you'll need another terminal)
emulator -avd Pixel_8_API_34
```

#### 3. Wait for Emulator to Boot
```bash
# Check when emulator is ready (should show "device" status)
adb devices

# Wait until you see something like:
# emulator-5554    device
```

#### 4. Build the App
```bash
./gradlew :composeApp:assembleDebug
```

#### 5. Install the App
```bash
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

#### 6. Launch the App
```bash
adb shell am start -n com.nongtri.app/.MainActivity
```

### Option 3: Quick Rebuild & Reinstall

If emulator is already running, use the quick script:

```bash
./quick-install.sh
```

This will rebuild, reinstall, and relaunch the app.

## Useful Commands

### Emulator Management
```bash
# List all AVDs
emulator -list-avds

# Start emulator
emulator -avd <AVD_NAME>

# Stop emulator
adb emu kill

# Check running devices
adb devices
```

### App Management
```bash
# Install app
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Uninstall app
adb uninstall com.nongtri.app

# Launch app
adb shell am start -n com.nongtri.app/.MainActivity

# Stop app
adb shell am force-stop com.nongtri.app

# View app logs
adb logcat | grep -i nongtri
```

### Build Commands
```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew :composeApp:assembleDebug

# Build and install (if emulator is running)
./gradlew :composeApp:installDebug
```

## Troubleshooting

### Emulator Won't Start
- Check if another emulator is already running: `adb devices`
- Kill existing emulator: `adb emu kill`
- Restart ADB: `adb kill-server && adb start-server`

### App Won't Install
- Make sure emulator is fully booted (check `adb devices` shows "device")
- Try uninstalling first: `adb uninstall com.nongtri.app`
- Check APK exists: `ls -lh composeApp/build/outputs/apk/debug/composeApp-debug.apk`

### ADB Connection Issues
```bash
# Restart ADB server
adb kill-server
adb start-server

# Reconnect to device
adb reconnect
```

## Available AVDs

Based on your setup, you have these AVDs available:
- `Medium_Phone_API_36`
- `Pixel_6a_API_34`
- `Pixel_8_API_34` (recommended)
- `Pixel_Tablet_API_34`

## Tips

1. **Keep emulator running**: Once started, keep the emulator running to speed up development
2. **Use quick-install.sh**: For faster iteration during development
3. **Check logs**: Use `adb logcat` to debug issues
4. **Multiple emulators**: You can run multiple emulators simultaneously with different AVDs

