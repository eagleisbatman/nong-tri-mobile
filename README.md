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

### Building

**Android:**
```bash
./gradlew :composeApp:assembleDebug
```

**iOS:**
Open `iosApp/iosApp.xcworkspace` in Xcode and build

## Backend

This app connects to the Nông Trí backend API hosted on Railway:
- Base URL: `https://nong-tri.up.railway.app`
- API Docs: See backend repo's `API.md`

## Running

### Android
1. Open project in Android Studio
2. Select Android device/emulator
3. Click Run

### iOS
1. Open `iosApp/iosApp.xcworkspace` in Xcode
2. Select iOS device/simulator
3. Click Run

## Configuration

To change the backend URL, edit:
`composeApp/src/commonMain/kotlin/com/nongtri/app/data/api/NongTriApi.kt`

```kotlin
class NongTriApi(
    private val baseUrl: String = "YOUR_BACKEND_URL"
)
```

## Architecture

```
UI Layer (Compose)
    ↓
ViewModel (State Management)
    ↓
Repository (API Client)
    ↓
Backend API (Railway)
    ↓
OpenAI + Database
```

## Features Roadmap

- [x] Chat interface
- [x] AI responses
- [x] Conversation history
- [ ] Offline support
- [ ] Voice input
- [ ] Image upload for crop disease detection
- [ ] Push notifications
- [ ] Multi-language support
- [ ] User authentication

## License

MIT

## Related Repositories

- Backend: [nong-tri](https://github.com/eagleisbatman/nong-tri)
