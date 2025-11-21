# 🌾 Nông Trí Mobile App

<div align="center">

**AI-powered farming assistant for Vietnamese smallholder farmers**

Built with Jetpack Compose Multiplatform for Android & iOS

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Compose-Multiplatform-green.svg)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📱 About

**Nông Trí** (Smart Farming) is a mobile application designed to empower Vietnamese smallholder farmers with AI-powered agricultural guidance. The app provides:

- 🤖 **AI-Powered Chat**: Get instant farming advice powered by OpenAI GPT-4
- 📸 **Plant Disease Diagnosis**: Upload photos for AI-powered crop disease detection (2-3 minute turnaround)
- 🎤 **Voice Input**: Speak in Vietnamese and receive voice responses (low-literacy friendly)
- 📍 **Location-Aware**: Contextual advice based on GPS location and local weather
- 🌐 **Bilingual Support**: Seamless Vietnamese ↔ English switching
- 💬 **Conversation History**: Save and revisit past conversations
- 🔔 **Push Notifications**: Receive farming tips and updates

### Target Users

- **Primary**: Vietnamese smallholder farmers (age 30-55, low-to-medium literacy)
- **Secondary**: Young farmers (age 20-35) seeking modern agricultural guidance
- **Tertiary**: Agricultural extension workers recommending the app to farmers

---

## 🚀 Features

### Current Features ✅

- ✅ Chat interface with Material 3 design
- ✅ AI-powered responses via OpenAI GPT-4
- ✅ Conversation history with persistent storage
- ✅ Auto-scrolling message list
- ✅ Loading states and error handling
- ✅ Image upload for plant diagnosis
- ✅ Voice input/output support
- ✅ Location-based services
- ✅ Firebase Analytics integration
- ✅ PostHog analytics (dual tracking)
- ✅ Cloudinary image hosting
- ✅ Push notifications (FCM)
- ✅ Multi-language support (Vietnamese/English)
- ✅ Offline conversation history

### Roadmap 🗺️

- [ ] Offline mode with local database
- [ ] Advanced crop management features
- [ ] Weather integration
- [ ] Dark mode
- [ ] User authentication
- [ ] Premium subscription features

---

## 🛠️ Tech Stack

- **Kotlin Multiplatform** - Shared code across Android & iOS
- **Jetpack Compose** - Modern declarative UI framework
- **Ktor Client** - HTTP networking
- **Kotlinx Serialization** - JSON parsing
- **Kotlinx Coroutines** - Async operations
- **Material 3** - Modern UI components
- **Firebase** - Analytics, Crashlytics, Cloud Messaging
- **PostHog** - Product analytics
- **Cloudinary** - Image hosting and optimization

---

## 📋 Prerequisites

- **Android Studio** (latest version) - [Download](https://developer.android.com/studio)
- **JDK 11 or higher** - Required for Android builds
- **Android SDK** - Installed via Android Studio
- **For iOS**: Xcode (macOS only) - [Download](https://developer.apple.com/xcode/)

---

## ⚙️ Setup

### 1. Clone the Repository

```bash
git clone https://github.com/eagleisbatman/nong-tri-mobile.git
cd nong-tri-mobile
```

### 2. Configure Backend API URL

Edit `composeApp/src/commonMain/kotlin/com/nongtri/app/BuildConfig.kt`:

```kotlin
object BuildConfig {
    const val API_URL = "https://nong-tri.up.railway.app"  // Your backend URL
    const val VERSION_NAME = "1.0.0"
}
```

### 3. Configure PostHog Analytics (Optional)

Create or edit `local.properties` in the project root:

```properties
# PostHog Analytics (optional)
posthog.api.key=your_posthog_api_key_here
posthog.api.host=https://us.i.posthog.com
```

**Note**: `local.properties` is gitignored and won't be committed.

### 4. Configure Firebase (Required for Push Notifications)

1. Download `google-services.json` from Firebase Console
2. Place it in `composeApp/` directory
3. The file is gitignored for security

---

## 🏗️ Building

### Android

**Build debug APK:**
```bash
./gradlew :composeApp:assembleDebug
```

**Build release APK:**
```bash
./gradlew :composeApp:assembleRelease
```

**Install on connected device/emulator:**
```bash
./gradlew :composeApp:installDebug
```

### iOS

1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Select your target device/simulator
3. Click Run (⌘R)

---

## 🚀 Running

### Option 1: Android Studio (Recommended for Development)

1. Open project in Android Studio
2. Set your API URL in `BuildConfig.kt`
3. Select Android device/emulator
4. Click Run ▶️

### Option 2: Command Line

**Full automated setup:**
```bash
./run-emulator.sh
```

This script will:
- Start an emulator (if not running)
- Build the app
- Install on emulator
- Launch the app

**Quick rebuild (if emulator already running):**
```bash
./quick-install.sh
```

**Manual steps:**
```bash
# 1. Start emulator
emulator -avd Pixel_8_API_34 &

# 2. Build and install
./gradlew :composeApp:installDebug

# 3. Launch app
adb shell am start -n com.nongtri.app/.MainActivity
```

See [COMMAND_LINE_SETUP.md](COMMAND_LINE_SETUP.md) for detailed command-line instructions.

---

## 📁 Project Structure

```
composeApp/
├── src/
│   ├── androidMain/          # Android-specific code
│   │   ├── analytics/        # Firebase Analytics implementation
│   │   ├── platform/         # Android platform services
│   │   └── ui/               # Android-specific UI
│   │
│   ├── commonMain/           # Shared Kotlin code
│   │   ├── data/
│   │   │   ├── api/          # API clients (Ktor)
│   │   │   ├── model/        # Data models
│   │   │   └── preferences/  # User preferences storage
│   │   ├── ui/
│   │   │   ├── screens/      # Main screens (Chat, Conversations, etc.)
│   │   │   ├── components/   # Reusable UI components
│   │   │   └── viewmodel/    # State management
│   │   ├── analytics/        # Analytics service interface
│   │   ├── l10n/             # Localization strings
│   │   └── platform/         # Platform abstractions
│   │
│   └── iosMain/              # iOS-specific code
│       └── platform/         # iOS platform implementations
│
├── build.gradle.kts          # Build configuration
└── google-services.json      # Firebase config (gitignored)
```

---

## 🏛️ Architecture

```
┌─────────────────────────────────────────┐
│         UI Layer (Compose)              │
│  ┌──────────┐  ┌──────────┐           │
│  │  Screens │  │Components│           │
│  └────┬─────┘  └────┬─────┘           │
└───────┼─────────────┼─────────────────┘
        │             │
┌───────▼─────────────▼─────────────────┐
│      ViewModel (State Management)      │
│  ┌──────────┐  ┌──────────┐           │
│  │   Chat   │  │Location  │           │
│  └────┬─────┘  └────┬─────┘           │
└───────┼─────────────┼─────────────────┘
        │             │
┌───────▼─────────────▼─────────────────┐
│      Data Layer                        │
│  ┌──────────┐  ┌──────────┐           │
│  │API Client│  │Preferences│           │
│  └────┬─────┘  └───────────┘           │
└───────┼────────────────────────────────┘
        │
┌───────▼───────────────────────────────┐
│      Backend API (Railway)             │
│  ┌──────────┐  ┌──────────┐           │
│  │  OpenAI  │  │PostgreSQL│           │
│  └──────────┘  └──────────┘           │
└────────────────────────────────────────┘
```

---

## 🔐 Security

### ✅ Security Best Practices

- ✅ No hardcoded API keys or secrets
- ✅ Environment variables for sensitive config
- ✅ `local.properties` gitignored (contains local SDK paths)
- ✅ `google-services.json` gitignored (contains Firebase keys)
- ✅ PostHog API keys stored in `local.properties` (not committed)
- ✅ HTTPS-only API communication
- ✅ Secure storage for user preferences

### ⚠️ Important Notes

- **Never commit** `local.properties` or `google-services.json`
- Use environment variables or `local.properties` for API keys
- Rotate API keys regularly
- Review `.gitignore` before committing

---

## 🧪 Testing

### Run Tests

```bash
# Unit tests
./gradlew test

# Android instrumentation tests
./gradlew connectedAndroidTest
```

### Debug Build

```bash
./gradlew :composeApp:assembleDebug
```

APK location: `composeApp/build/outputs/apk/debug/composeApp-debug.apk`

---

## 🐛 Troubleshooting

### Build Errors

**Clean build:**
```bash
./gradlew clean
./gradlew :composeApp:assembleDebug
```

**Invalidate caches in Android Studio:**
- File → Invalidate Caches → Invalidate and Restart

**JDK version issues:**
- Ensure JDK 11+ is installed
- Set `JAVA_HOME` environment variable

### API Connection Issues

1. Verify `BuildConfig.API_URL` is set correctly
2. Check backend is deployed and running
3. Test backend health: `curl https://nong-tri.up.railway.app/health`
4. Check network permissions in `AndroidManifest.xml`

### Emulator Issues

- **Emulator won't start**: Check available AVDs: `emulator -list-avds`
- **ADB connection issues**: Restart ADB: `adb kill-server && adb start-server`
- **App won't install**: Uninstall existing app: `adb uninstall com.nongtri.app`

### iOS Build Issues

- Run `pod install` in `iosApp` directory
- Check Xcode version is up to date
- Verify code signing is configured
- Check iOS deployment target matches requirements

---

## 📚 Documentation

- **[Command Line Setup](COMMAND_LINE_SETUP.md)** - Run app from command line
- **[Backend API](https://github.com/eagleisbatman/nong_tri)** - Backend repository
- **[Project Context](../PROJECT_CONTEXT.md)** - Complete project overview
- **[API Documentation](https://nong-tri.up.railway.app/api-docs)** - Interactive API docs

---

## 🤝 Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable names
- Add comments for complex logic
- Test on both Android and iOS
- Update documentation as needed

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🔗 Related Projects

- **[Backend API](https://github.com/eagleisbatman/nong_tri)** - Node.js backend with OpenAI integration
- **[Agriculture API](https://github.com/eagleisbatman/nong-tri-agriculture-api)** - Crops and livestock data API
- **[MCP Servers](https://github.com/eagleisbatman/nong-tri-mcp-servers)** - Model Context Protocol servers

---

## 📞 Support

For issues and questions:

1. Check [Troubleshooting](#-troubleshooting) section
2. Review [API Documentation](https://nong-tri.up.railway.app/api-docs)
3. Create a [GitHub Issue](https://github.com/eagleisbatman/nong-tri-mobile/issues)

---

## 🙏 Acknowledgments

- Built for Vietnamese smallholder farmers
- Powered by OpenAI GPT-4
- Deployed on Railway
- Analytics by Firebase & PostHog

---

<div align="center">

**Made with ❤️ for Vietnamese farmers**

[Website](https://nong-tri.up.railway.app) • [Documentation](https://nong-tri.up.railway.app/api-docs) • [Issues](https://github.com/eagleisbatman/nong-tri-mobile/issues)

</div>
