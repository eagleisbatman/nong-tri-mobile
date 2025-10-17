# Testing Nông Trí Mobile App

## Prerequisites

- Android Studio (latest version - Arctic Fox or newer)
- JDK 11 or higher
- Android SDK with at least API 24

## Setup Steps

### 1. Configure API URL

Edit `composeApp/src/commonMain/kotlin/com/nongtri/app/BuildConfig.kt`:

```kotlin
object BuildConfig {
    const val API_URL = "https://nong-tri.up.railway.app"
}
```

### 2. Open in Android Studio

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to `/Users/eagleisbatman/nong-tri-mobile`
4. Click "Open"
5. Wait for Gradle sync to complete

### 3. Run on Android

#### Using Emulator:
1. In Android Studio, go to **Tools > Device Manager**
2. Create a new device (or use existing):
   - Device: Pixel 5 or newer
   - System Image: Android 13 (API 33) or higher
   - Click "Create Virtual Device"
3. Click the **Run** button (green play icon) or press `Shift + F10`
4. Select your emulator from the device list
5. Wait for the app to build and launch

#### Using Physical Device:
1. Enable **Developer Options** on your Android device:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
2. Enable **USB Debugging**:
   - Go to Settings > Developer Options
   - Turn on "USB Debugging"
3. Connect device via USB
4. Click the **Run** button in Android Studio
5. Select your device from the list

## Testing the UI Flow

### 1. Language Selection (First Launch)
- [ ] App opens to language selection screen
- [ ] Green gradient background visible
- [ ] "🌾 Nông Trí" logo animates in
- [ ] Two language cards: English and Vietnamese
- [ ] Tap English card - should scale up and show checkmark
- [ ] "Continue" button appears with animation
- [ ] Tap Continue button

### 2. Chat Screen
- [ ] Smooth transition to chat screen
- [ ] Welcome card displays with gradient
- [ ] Message: "Hello! I'm your AI farming assistant"
- [ ] Description about crop, livestock, pest management
- [ ] Input bar at bottom with placeholder "Type your message..."
- [ ] Profile button (👤) in top-right

### 3. Send First Message
- [ ] Type: "How do I grow tomatoes?"
- [ ] Send button (➤) appears as you type
- [ ] Send button is green and circular
- [ ] Tap send button
- [ ] Message appears in green bubble on right side
- [ ] Message animates in (fade + slide)
- [ ] Timestamp appears below message

### 4. Receive AI Response
- [ ] Typing indicator appears (3 animated dots in gray bubble)
- [ ] AI response arrives
- [ ] Response appears in gray bubble on left side
- [ ] Response animates in (fade + slide)
- [ ] Timestamp appears below
- [ ] List auto-scrolls to show full response

### 5. Continue Conversation
- [ ] Send multiple messages
- [ ] Check auto-scroll works for each new message
- [ ] Verify message order (user on right, AI on left)
- [ ] Check timestamps are correct

### 6. Dark Mode (Test manually)
- Change device theme to dark mode in Settings
- Reopen app
- [ ] Background is dark (#121212)
- [ ] User bubbles are light green
- [ ] AI bubbles are dark gray (#2C2C2C)
- [ ] Text is legible
- [ ] All colors adjusted properly

### 7. Test Tags (For Automated Testing)
All these elements have test tags for UI testing:
- `language_selection_screen`
- `language_card_en`, `language_card_vi`
- `continue_button`
- `chat_screen`
- `message_list`
- `welcome_card`
- `text_field`
- `send_button`
- `message_bubble_0`, `message_bubble_1`, etc.
- `loading_indicator`

## Common Issues

### Gradle Sync Failed
- **Solution**: File > Invalidate Caches > Invalidate and Restart

### Cannot resolve BuildConfig
- **Solution**: Make sure you created the file at the correct path

### Network Error
- **Solution**: Check that the backend API is running at https://nong-tri.up.railway.app
- Test with: `curl https://nong-tri.up.railway.app/health`

### Emulator Won't Start
- **Solution**: In Device Manager, wipe data and cold boot the emulator

### App Crashes on Launch
- **Solution**: Check Logcat in Android Studio for error details
- Look for stack traces with "com.nongtri.app" package name

## Backend Health Check

Before testing, verify the backend is running:

```bash
curl https://nong-tri.up.railway.app/health
```

Expected response:
```json
{
  "status": "ok",
  "message": "Nông Trí - AI Assistant for Vietnamese Farmers (Mobile API)",
  "services": {
    "database": true,
    "redis": true,
    "openai": true
  }
}
```

## Expected User Experience

### Language Selection
- **Visual**: Clean, modern, with green agricultural theme
- **Animation**: Smooth fade-ins, scale animations on cards
- **Duration**: Should feel quick and responsive (<300ms per animation)

### Chat Interface
- **Visual**: WhatsApp-like message bubbles, clean typography
- **Animation**: Messages slide in from bottom, typing indicator pulses
- **Performance**: Smooth scrolling, no lag when typing

### Dark Mode
- **Visual**: Easy on eyes, proper contrast ratios
- **Colors**: Green accents remain visible, backgrounds properly dark

## Next Steps After Testing

Once text chat works well:
1. Add voice input support (speech-to-text)
2. Add image upload (for plant disease detection)
3. Add profile screen with settings
4. Implement persistent storage for preferences

## Automated UI Testing

To run automated UI tests (to be created):

```bash
./gradlew :composeApp:connectedAndroidTest
```

This will run tests using the test tags we've defined.

---

**Note**: This is a Kotlin Multiplatform project. The same code runs on both Android and iOS, but we're focusing on Android testing first.
