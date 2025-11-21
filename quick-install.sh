#!/bin/bash

# Quick script to rebuild and reinstall the app on a running emulator
# Usage: ./quick-install.sh

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

echo "🔨 Building debug APK..."
./gradlew :composeApp:assembleDebug

echo ""
echo "📦 Installing app..."
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

echo ""
echo "🚀 Launching app..."
adb shell am start -n com.nongtri.app/.MainActivity

echo ""
echo "✅ Done!"

