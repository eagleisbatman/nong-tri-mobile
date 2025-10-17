# Nông Trí Mobile App

AI-powered farming assistant for Vietnamese smallholder farmers. Built with Jetpack Compose Multiplatform.

## Features

- 🌾 AI-powered agricultural advice
- 💬 Real-time chat interface
- 📱 Native Android & iOS apps
- 🔄 Conversation history
- 🌐 Connects to Railway backend

## Tech Stack

- **Kotlin Multiplatform** - Shared code across Android & iOS
- **Jetpack Compose** - Modern UI framework
- **Ktor Client** - HTTP networking
- **Kotlinx Serialization** - JSON parsing
- **Kotlinx Coroutines** - Async operations
- **Material 3** - UI components

## Project Structure

```
composeApp/
├── src/
│   ├── androidMain/      # Android-specific code
│   ├── iosMain/          # iOS-specific code
│   └── commonMain/       # Shared code
│       ├── data/
│       │   ├── api/      # API client
│       │   └── model/    # Data models
│       └── ui/           # UI components
```

## Setup

### Prerequisites

- Android Studio (latest version)
- JDK 11 or higher
- For iOS: Xcode (macOS only)

### Configuration

**Set your backend API URL:**

Edit [BuildConfig.kt](composeApp/src/commonMain/kotlin/com/nongtri/app/BuildConfig.kt):

```kotlin
object BuildConfig {
    const val API_URL = "YOUR_RAILWAY_API_URL"  // Replace with your Railway deployment URL
}
```

### Building

**Android:**
```bash
./gradlew :composeApp:assembleDebug
```

**iOS:**
Open `iosApp/iosApp.xcworkspace` in Xcode and build

## Backend

This app connects to the Nông Trí backend API:
- Backend repo: [nong_tri](https://github.com/eagleisbatman/nong_tri)
- Deploy backend to Railway and set the API_URL in BuildConfig.kt

## Running

### Android
1. Open project in Android Studio
2. Set your API URL in BuildConfig.kt
3. Select Android device/emulator
4. Click Run

### iOS
1. Set your API URL in BuildConfig.kt
2. Open `iosApp/iosApp.xcworkspace` in Xcode
3. Select iOS device/simulator
4. Click Run

## Architecture

```
UI Layer (Compose)
    ↓
 ViewModel (State Management)
    ↓
API Client (Ktor)
    ↓
Backend API (Railway)
    ↓
OpenAI + PostgreSQL
```

## Features

### Current
- ✅ Chat interface with Material 3 design
- ✅ AI-powered responses
- ✅ Conversation history
- ✅ Auto-scrolling message list
- ✅ Loading states
- ✅ Error handling

### Roadmap
- [ ] Offline support with local database
- [ ] Image upload for crop disease diagnosis
- [ ] Voice input support
- [ ] Push notifications for farming tips
- [ ] Multi-language support
- [ ] Dark mode
- [ ] User authentication

## Development

### Project Files

- **[BuildConfig.kt](composeApp/src/commonMain/kotlin/com/nongtri/app/BuildConfig.kt)** - API configuration
- **[NongTriApi.kt](composeApp/src/commonMain/kotlin/com/nongtri/app/data/api/NongTriApi.kt)** - HTTP client
- **[ChatViewModel.kt](composeApp/src/commonMain/kotlin/com/nongtri/app/ui/ChatViewModel.kt)** - State management
- **[ChatScreen.kt](composeApp/src/commonMain/kotlin/com/nongtri/app/ui/ChatScreen.kt)** - UI components

### Adding Dependencies

Edit [gradle/libs.versions.toml](gradle/libs.versions.toml) to add new libraries.

## Troubleshooting

### Build Errors
- Clean build: `./gradlew clean`
- Invalidate caches in Android Studio
- Check JDK version (must be 11+)

### API Connection Issues
- Verify BuildConfig.API_URL is set correctly
- Check backend is deployed and running on Railway
- Test backend health endpoint: `curl YOUR_API_URL/health`

### iOS Build Issues
- Run `pod install` in iosApp directory
- Check Xcode version is up to date
- Verify code signing is configured

## Contributing

Contributions welcome! Please:
- Follow Kotlin coding conventions
- Test on both Android and iOS
- Update documentation as needed

## License

MIT License

## Related Projects

- **Backend API**: [nong_tri](https://github.com/eagleisbatman/nong_tri) - Node.js backend with OpenAI integration

---

**Built for Vietnamese smallholder farmers**
