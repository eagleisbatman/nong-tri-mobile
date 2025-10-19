# Nông Trí Mobile App

## Overview

Kotlin Multiplatform mobile application providing AI-powered agricultural advice for Vietnamese smallholder farmers. Built with Jetpack Compose and Material 3 design system.

**Platforms**: Android (primary), iOS (planned)
**Repository**: https://github.com/eagleisbatman/nong-tri-mobile

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│         UI Layer (Jetpack Compose)              │
│   ChatScreen | LanguageSelectionScreen         │
│   MessageBubble | InputBar | Components        │
└────────────┬────────────────────────────────────┘
             │
┌────────────▼────────────────────────────────────┐
│        ViewModel Layer (State Management)       │
│              ChatViewModel                      │
│         (StateFlow + Coroutines)               │
└────────────┬────────────────────────────────────┘
             │
┌────────────▼────────────────────────────────────┐
│           Data Layer (Repositories)             │
│  NongTriApi | UserPreferences | Platform       │
└────────────┬────────────────────────────────────┘
             │
┌────────────▼────────────────────────────────────┐
│          External Services                      │
│  Backend API | TTS | Share | System Services   │
└─────────────────────────────────────────────────┘
```

**Pattern**: MVVM (Model-View-ViewModel) with unidirectional data flow

---

## Directory Structure

```
mobile/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/kotlin/com/nongtri/app/
│   │   │   ├── App.kt                              # Application root
│   │   │   ├── BuildConfig.kt                      # API configuration
│   │   │   ├── l10n/
│   │   │   │   └── Strings.kt                      # Localization (EN/VI)
│   │   │   ├── data/
│   │   │   │   ├── api/
│   │   │   │   │   ├── NongTriApi.kt               # HTTP client (Ktor)
│   │   │   │   │   └── ApiModels.kt                # API DTOs
│   │   │   │   ├── model/
│   │   │   │   │   └── ChatMessage.kt              # Message data model
│   │   │   │   └── preferences/
│   │   │   │       └── UserPreferences.kt          # App preferences
│   │   │   ├── platform/
│   │   │   │   ├── TextToSpeechManager.kt          # TTS interface
│   │   │   │   ├── ShareManager.kt                 # Share interface
│   │   │   │   ├── LocalTextToSpeechManager.kt     # CompositionLocal
│   │   │   │   └── LocalShareManager.kt            # CompositionLocal
│   │   │   └── ui/
│   │   │       ├── theme/
│   │   │       │   ├── Color.kt                    # Color palette
│   │   │       │   ├── Typography.kt               # Typography system
│   │   │       │   └── Theme.kt                    # Material 3 theme
│   │   │       ├── components/
│   │   │       │   ├── TestTags.kt                 # Test identifiers
│   │   │       │   ├── MessageBubble.kt            # Message display
│   │   │       │   ├── MarkdownText.kt             # Markdown renderer
│   │   │       │   ├── WhatsAppStyleInputBar.kt    # Input component
│   │   │       │   ├── MessageActionButtons.kt     # Copy/Share/TTS/Feedback
│   │   │       │   ├── ShareBottomSheet.kt         # Share dialog
│   │   │       │   └── WelcomeCard.kt              # Welcome UI
│   │   │       ├── screens/
│   │   │       │   ├── ChatScreen.kt               # Main chat UI
│   │   │       │   └── LanguageSelectionScreen.kt  # Onboarding
│   │   │       └── viewmodel/
│   │   │           └── ChatViewModel.kt            # State management
│   │   └── androidMain/kotlin/com/nongtri/app/
│   │       ├── MainActivity.kt                     # Android entry point
│   │       └── platform/
│   │           ├── TextToSpeechManager.android.kt  # Android TTS impl
│   │           └── ShareManager.android.kt         # Android Share impl
│   └── build.gradle.kts                            # Build configuration
├── gradle/                                         # Gradle wrapper
├── build.gradle.kts                                # Project build script
├── settings.gradle.kts                             # Project settings
├── local.properties                                # SDK paths
├── README.md                                       # Setup guide
├── TESTING.md                                      # Testing procedures
├── IMPLEMENTATION_PLAN.md                          # Feature roadmap
└── CLAUDE.md                                       # This file
```

---

## Key Features

### ✅ Implemented

**Core Chat**:
- Real-time streaming AI responses (SSE via Ktor)
- Message bubbles with timestamps
- Markdown rendering of AI responses
- Auto-scrolling message list
- Loading indicators and animations
- Error handling with retry

**Multilingual Support**:
- English and Vietnamese UI strings
- Language selection on first launch
- Dropdown menu for language switching
- Localized welcome messages
- Dynamic string updates

**User Interface**:
- Material 3 design system
- Dark/Light theme with system detection
- Smooth animations (fade + slide)
- WhatsApp-style input bar
- Responsive layout

**Message Actions**:
- Copy to clipboard
- Share via system share sheet
- Text-to-Speech with OpenAI TTS
- Thumbs up/down feedback buttons
- Action confirmation toasts

**Platform Integration**:
- Native Android share functionality
- Text-to-Speech with tone control
- System theme detection
- Permission handling

**Testing Infrastructure**:
- 39 test tags defined
- Manual testing guide (TESTING.md)
- Test scenarios documented

### ⚠️ Partially Implemented

**User Preferences**:
- Language selection (works)
- Theme selection (works)
- Onboarding flag (works)
- **ISSUE**: No persistent storage (in-memory only)

**Location Sharing**:
- UI dialog exists in ChatScreen
- **ISSUE**: Non-functional (TODO comments)

**Test Tags**:
- Defined in TestTags.kt
- Applied to some components
- **ISSUE**: Not applied to all UI elements

### ❌ Not Implemented

**Critical Missing Features**:
- Persistent local storage (SharedPreferences/UserDefaults)
- User profile management
- Crop/livestock selection
- Location tracking (GPS + IP-based)
- Automated UI tests
- Settings screen (settings in dropdown menu only)
- Conversation history persistence
- Offline support

---

## Data Models

### ChatMessage
```kotlin
@Serializable
data class ChatMessage(
    val id: String,
    val role: MessageRole,              // USER, ASSISTANT, SYSTEM
    val content: String,
    val timestamp: Instant,
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val conversationId: Int? = null     // Backend DB ID for feedback
)

enum class MessageRole { USER, ASSISTANT, SYSTEM }
```

### API Models

**Request**:
```kotlin
@Serializable
data class ChatRequest(
    val userId: String,
    val message: String,
    val userName: String? = null
)

@Serializable
data class FeedbackRequest(
    val userId: String,
    val conversationId: Int,
    val isPositive: Boolean,
    val feedbackText: String? = null
)
```

**Response**:
```kotlin
@Serializable
data class ChatResponse(
    val success: Boolean,
    val response: String? = null,
    val timestamp: String? = null,
    val error: String? = null
)

@Serializable
data class HistoryResponse(
    val success: Boolean,
    val history: List<HistoryItem> = emptyList()
)

@Serializable
data class HistoryItem(
    val role: String,
    val content: String,
    val timestamp: String
)
```

---

## State Management

### ChatViewModel

**UI State**:
```kotlin
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

**State Flow**:
```kotlin
private val _uiState = MutableStateFlow(ChatUiState())
val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
```

**Key Operations**:
- `sendMessage(message: String)` - Sends user message, streams AI response
- `clearHistory()` - Clears conversation (UI + backend)
- `retryLastMessage()` - Retries failed message
- `submitFeedback(conversationId: Int, isPositive: Boolean)` - Sends feedback

### UserPreferences (Singleton)

```kotlin
object UserPreferences {
    private val _language = MutableStateFlow(Language.VIETNAMESE)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    fun setLanguage(language: Language)
    fun setThemeMode(mode: ThemeMode)
    fun setHasCompletedOnboarding(completed: Boolean)
}

enum class ThemeMode { LIGHT, DARK, SYSTEM }
```

**ISSUE**: No persistence - all preferences reset on app restart

---

## Localization System

### Language Support
```kotlin
enum class Language(val code: String, val displayName: String, val flag: String) {
    ENGLISH("en", "English", "🇬🇧"),
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳")
}
```

### String Interface
```kotlin
interface Strings {
    val appName: String
    val appTagline: String
    val languageSelectionTitle: String
    val continueButton: String
    val chatTitle: String
    val typeMessage: String
    val send: String
    // ... 61 total string constants
}
```

### Usage
```kotlin
val strings = when (UserPreferences.language.value) {
    Language.ENGLISH -> EnglishStrings
    Language.VIETNAMESE -> VietnameseStrings
}

Text(text = strings.appName)
```

**Coverage**:
- ✅ App branding
- ✅ Chat interface
- ✅ Language selection
- ✅ Error messages
- ✅ Common actions
- ✅ Voice/media controls
- ❌ Profile screen strings (missing)
- ❌ Crop/livestock terminology (missing)
- ❌ Location strings (missing)

---

## Testing

### Test Tags (TestTags.kt)

**Defined Test Tags** (39 tags):
```kotlin
object TestTags {
    // Language Selection
    const val LANGUAGE_SELECTION_SCREEN = "language_selection_screen"
    const val LANGUAGE_CARD_PREFIX = "language_card_"  // + language code
    const val CONTINUE_BUTTON = "continue_button"

    // Chat Screen
    const val CHAT_SCREEN = "chat_screen"
    const val MESSAGE_LIST = "message_list"
    const val MESSAGE_BUBBLE_PREFIX = "message_bubble_"  // + index
    const val TEXT_FIELD = "text_field"
    const val SEND_BUTTON = "send_button"
    const val VOICE_BUTTON = "voice_button"

    // ... more tags
}
```

**Usage**:
```kotlin
// Apply test tag
Text(
    text = "Hello",
    modifier = Modifier.testTag(TestTags.MESSAGE_TEXT_PREFIX + index)
)
```

### Manual Testing (TESTING.md)

**Test Scenarios**:
1. Language selection on first launch
2. Send message and receive streaming response
3. Copy AI response
4. Share AI response
5. Text-to-Speech playback
6. Feedback submission
7. Clear conversation history
8. Theme switching
9. Language switching

### Automated Testing (Not Implemented)

**Planned**:
```kotlin
// Example UI test (not implemented)
@Test
fun testSendMessage() {
    composeTestRule.onNodeWithTag(TestTags.TEXT_FIELD)
        .performTextInput("How do I grow rice?")

    composeTestRule.onNodeWithTag(TestTags.SEND_BUTTON)
        .performClick()

    composeTestRule.onNodeWithTag(TestTags.MESSAGE_BUBBLE_PREFIX + "0")
        .assertExists()
}
```

**Required Setup**:
- Add Compose UI Test dependency
- Create `androidTest` directory
- Implement test classes
- Set up test runner

---

## API Integration (NongTriApi.kt)

### Configuration
```kotlin
object BuildConfig {
    const val API_URL = "https://nong-tri.up.railway.app"
}
```

### HTTP Client (Ktor)
```kotlin
private val client = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(SSE)  // Server-Sent Events for streaming
    install(Logging) {
        level = LogLevel.INFO
    }
}
```

### Endpoints

**Chat Stream** (SSE):
```kotlin
suspend fun chatStream(
    userId: String,
    message: String,
    userName: String? = null
): Flow<String> = flow {
    client.preparePost("$baseUrl/api/chat/stream") {
        contentType(ContentType.Application.Json)
        setBody(ChatRequest(userId, message, userName))
    }.execute { response ->
        val channel: ByteReadChannel = response.body()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: continue
            if (line.startsWith("data: ")) {
                val json = line.removePrefix("data: ")
                // Parse and emit chunks
            }
        }
    }
}
```

**Get History**:
```kotlin
suspend fun getHistory(userId: String, limit: Int = 20): Result<List<HistoryItem>>
```

**Clear History**:
```kotlin
suspend fun clearHistory(userId: String): Result<Boolean>
```

**Submit Feedback**:
```kotlin
suspend fun submitFeedback(
    userId: String,
    conversationId: Int,
    isPositive: Boolean,
    feedbackText: String? = null
): Result<Boolean>
```

---

## Platform Services

### Text-to-Speech (Android)

```kotlin
class AndroidTextToSpeechManager(context: Context) : TextToSpeechManager {
    private val tts = TextToSpeech(context) { status -> ... }

    override suspend fun speak(text: String, tone: String) {
        // Option 1: Use Android TTS (offline)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)

        // Option 2: Use OpenAI TTS API (current implementation)
        val audioBytes = fetchTTSFromAPI(text, tone)
        playAudio(audioBytes)
    }

    override fun stop() {
        tts.stop()
    }
}
```

**Features**:
- Tone-based speech modulation
- Background audio playback
- Pause/resume controls

### Share Manager (Android)

```kotlin
class AndroidShareManager(private val context: Context) : ShareManager {
    override fun shareText(text: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }
}
```

---

## Theme System

### Color Palette
```kotlin
val GreenPrimary = Color(0xFF4CAF50)      // Agricultural green
val GreenSecondary = Color(0xFF8BC34A)    // Light green
val BrownAccent = Color(0xFF795548)       // Earth tone

val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    // ...
)

val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    // ...
)
```

### Typography
```kotlin
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    // ... Material 3 typography scale
)
```

### Theme Application
```kotlin
@Composable
fun NongTriTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
```

---

## Dependencies

### Kotlin Multiplatform
```kotlin
kotlin("multiplatform") version "2.0.0"
```

### Compose Multiplatform
```kotlin
compose = "1.6.11"
androidx.compose.ui = "1.6.8"
androidx.material3 = "1.2.1"
```

### Networking
```kotlin
ktor = "2.3.12"
implementation("io.ktor:ktor-client-core")
implementation("io.ktor:ktor-client-content-negotiation")
implementation("io.ktor:ktor-serialization-kotlinx-json")
implementation("io.ktor:ktor-client-okhttp")  // Android
implementation("io.ktor:ktor-client-logging")
```

### Serialization
```kotlin
kotlinx-serialization = "1.6.3"
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")
```

### Coroutines
```kotlin
kotlinx-coroutines = "1.8.1"
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")
```

### DateTime
```kotlin
kotlinx-datetime = "0.6.0"
implementation("org.jetbrains.kotlinx:kotlinx-datetime")
```

### Markdown Rendering
```kotlin
multiplatform-markdown-renderer = "0.25.0"
implementation("com.mikepenz:multiplatform-markdown-renderer")
```

### Android-Specific
```kotlin
androidx-activity-compose = "1.9.0"
androidx-lifecycle = "2.8.0"
implementation("androidx.activity:activity-compose")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose")
implementation("androidx.lifecycle:lifecycle-runtime-compose")
```

---

## Build Configuration

### Android Target
```kotlin
android {
    namespace = "com.nongtri.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.nongtri.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(...)
        }
    }
}
```

### Gradle Tasks
```bash
./gradlew tasks                  # List all tasks
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew installDebug           # Install debug on device
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests
```

---

## Development Workflow

### Setup
```bash
cd mobile
# Open in Android Studio
# Or build from command line:
./gradlew build
```

### Run on Emulator/Device
1. Open Android Studio
2. Select device from dropdown
3. Click Run (green play button)
4. App installs and launches

### Debug Logging
```kotlin
// View logs in Logcat
println("Debug: $message")

// Ktor HTTP logging
install(Logging) {
    level = LogLevel.ALL  // For debugging
}
```

---

## Known Issues & Limitations

### Critical Issues
1. **No Persistent Storage**
   - User preferences reset on app restart
   - Language/theme selection not saved
   - **Fix**: Implement SharedPreferences (Android) / UserDefaults (iOS)

2. **Missing Test Coverage**
   - Test tags defined but not all applied
   - No automated tests implemented
   - **Fix**: Complete testTag application, write UI tests

3. **Location Non-Functional**
   - UI exists but doesn't work
   - No GPS permission handling
   - **Fix**: Implement location services

### Minor Issues
- Some message action buttons need visual polish
- Timestamp format not localized
- No offline support (crashes without internet)

---

## Planned Features

### Phase 1: Foundation (Weeks 1-2)
- [ ] Persistent storage for preferences
- [ ] Complete test tag application
- [ ] Automated UI tests
- [ ] Settings screen (move from dropdown menu)

### Phase 2: User Context (Weeks 3-4)
- [ ] User profile screen
- [ ] Location tracking (IP + GPS)
- [ ] Profile persistence to backend

### Phase 3: Agriculture Core (Weeks 5-8)
- [ ] Crop selection UI
- [ ] Livestock selection UI
- [ ] Master data integration
- [ ] Context injection in AI prompts

### Phase 4: Advanced (Weeks 9-12)
- [ ] RAG memory system
- [ ] Weather integration
- [ ] Offline support with local DB
- [ ] Voice input (speech-to-text)

---

## Performance Considerations

### Optimizations Implemented
- LazyColumn for message list (virtualized scrolling)
- remember() for composable stability
- State hoisting to prevent recomposition
- Coroutine-based async operations
- Ktor connection pooling

### Potential Improvements
- Add paging for conversation history
- Implement local caching
- Optimize image loading (if image upload added)
- Reduce APK size with R8/ProGuard

---

## Accessibility

### Current Support
- Material 3 accessibility defaults
- Semantic content descriptions (partial)
- Theme support (dark mode for low vision)

### Planned Improvements
- Complete content descriptions
- Screen reader testing
- Larger touch targets
- High contrast mode
- Font scaling support

---

## Troubleshooting

### Build Errors
```bash
# Clean and rebuild
./gradlew clean
./gradlew build --refresh-dependencies
```

### Gradle Sync Issues
1. Invalidate Caches (Android Studio)
2. File → Sync Project with Gradle Files
3. Check SDK path in local.properties

### Runtime Errors
- Check API_URL in BuildConfig.kt
- Verify backend is running
- Check internet connection
- Review Logcat for stack traces

---

**Last Updated**: October 2025
**Status**: Active Development
**Version**: 1.0.0 (Pre-release)
**Target SDK**: Android 14 (API 34)
**Min SDK**: Android 7.0 (API 24)
