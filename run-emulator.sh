#!/bin/bash

# Script to start emulator, build, install, and launch the Nông Trí app
# Usage: ./run-emulator.sh [AVD_NAME]

set -e

# Get the project directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Default AVD name (you can change this)
DEFAULT_AVD="Pixel_8_API_34"
AVD_NAME="${1:-$DEFAULT_AVD}"

echo "🚀 Starting Nông Trí Mobile App"
echo "================================"
echo ""

# Step 1: Check if emulator is already running
echo "📱 Checking for running emulators..."
if adb devices | grep -q "device$"; then
    echo "✅ Emulator already running"
    DEVICE=$(adb devices | grep "device$" | awk '{print $1}' | head -1)
    echo "   Using device: $DEVICE"
else
    echo "🔧 Starting emulator: $AVD_NAME"
    echo "   (This may take a minute...)"
    
    # Start emulator in background
    emulator -avd "$AVD_NAME" > /dev/null 2>&1 &
    EMULATOR_PID=$!
    
    echo "   Waiting for emulator to boot..."
    
    # Wait for emulator to be ready (check every 2 seconds, max 120 seconds)
    TIMEOUT=120
    ELAPSED=0
    while [ $ELAPSED -lt $TIMEOUT ]; do
        if adb devices | grep -q "device$"; then
            echo "✅ Emulator is ready!"
            break
        fi
        sleep 2
        ELAPSED=$((ELAPSED + 2))
        echo -n "."
    done
    echo ""
    
    if [ $ELAPSED -ge $TIMEOUT ]; then
        echo "❌ Timeout waiting for emulator to start"
        kill $EMULATOR_PID 2>/dev/null || true
        exit 1
    fi
fi

# Step 2: Build the app
echo ""
echo "🔨 Building debug APK..."
./gradlew :composeApp:assembleDebug

# Step 3: Install the app
echo ""
echo "📦 Installing app on emulator..."
APK_PATH="composeApp/build/outputs/apk/debug/composeApp-debug.apk"
adb install -r "$APK_PATH"

# Step 4: Launch the app
echo ""
echo "🚀 Launching Nông Trí app..."
adb shell am start -n com.nongtri.app/.MainActivity

echo ""
echo "✅ Done! The app should now be running on your emulator."
echo ""
echo "💡 Tips:"
echo "   - To stop the emulator: adb emu kill"
echo "   - To see available AVDs: emulator -list-avds"
echo "   - To use a different AVD: ./run-emulator.sh Pixel_6a_API_34"

