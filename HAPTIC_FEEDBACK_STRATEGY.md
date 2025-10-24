# Haptic Feedback Strategy for Nông Trí

## Overview

Haptic feedback enhances user experience by providing tactile confirmation of actions, improving perceived responsiveness, and creating a more premium feel. This document outlines strategic haptic patterns for the Nông Trí agricultural AI assistant.

---

## Android Haptic API

### HapticFeedbackConstants (Standard)

```kotlin
import android.view.HapticFeedbackConstants

view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
```

### Vibrator API (Custom Patterns)

```kotlin
import android.os.VibrationEffect
import android.os.Vibrator

val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

// Simple vibration
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
} else {
    vibrator.vibrate(50)
}

// Pattern: on-off-on-off (in milliseconds)
val pattern = longArrayOf(0, 50, 100, 50)
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))

// Predefined effects (Android 10+)
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
}
```

---

## Haptic Strategy by Feature

### 1. **Message Sending**

#### **Text Message Sent**
```kotlin
// Location: ChatScreen.kt, when user taps send button
// Pattern: Light tick (confirmation without being intrusive)

view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
// OR
vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
```

**Why:** Confirms message was sent, similar to WhatsApp/Telegram behavior.

**Timing:** Immediately when send button is tapped (optimistic)

---

#### **Voice Message Sent**
```kotlin
// Location: VoiceRecordingViewModel.kt, after transcription completes
// Pattern: Double tap (success confirmation for multi-step action)

val pattern = longArrayOf(0, 30, 50, 30)
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
// OR
vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
```

**Why:** Voice recording is a more complex interaction requiring stronger confirmation.

**Timing:** After successful transcription, before AI response starts.

---

#### **Image Diagnosis Submitted**
```kotlin
// Location: ChatViewModel.kt, after upload completes
// Pattern: Success pattern (medium strength)

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
```

**Why:** Confirms image upload succeeded before diagnosis starts.

**Timing:** After backend confirms job submission (`submitDiagnosisJob` onSuccess).

---

### 2. **Voice Recording**

#### **Recording Started**
```kotlin
// Location: VoiceRecordingViewModel.kt, when recording begins
// Pattern: Strong single tap (clear start signal)

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
```

**Why:** Clear tactile signal that recording started (important for eyes-off use).

**Timing:** Immediately when `startRecording()` succeeds.

---

#### **Recording Stopped**
```kotlin
// Location: VoiceRecordingViewModel.kt, when user releases button
// Pattern: Light tap (gentler than start)

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
```

**Why:** Confirms recording stopped without being jarring.

**Timing:** Immediately when `stopRecording()` called.

---

#### **Recording Error**
```kotlin
// Location: VoiceRecordingViewModel.kt, on recording failure
// Pattern: Error pattern (three short pulses)

val pattern = longArrayOf(0, 50, 100, 50, 100, 50)
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
```

**Why:** Distinct error pattern alerts user something went wrong.

**Timing:** When recording fails (too short, failed to start, etc.).

---

### 3. **AI Response Streaming**

#### **Response Starts**
```kotlin
// Location: ChatViewModel.kt, when first chunk arrives
// Pattern: Subtle tick (acknowledges AI started responding)

vibrator.vibrate(VibrationEffect.createOneShot(20, 50)) // Very gentle
```

**Why:** Subtle confirmation that AI received the question and is responding.

**Timing:** When first SSE chunk arrives (`onChunk` first call).

**Trade-off:** Optional - might be too frequent. Consider only for first message in session.

---

#### **Response Complete**
```kotlin
// Location: ChatViewModel.kt, when streaming ends
// Pattern: Completion tap

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
```

**Why:** Signals user can now read the complete response or ask follow-up.

**Timing:** In `onSuccess` handler after `flushChunkBuffer()`.

---

### 4. **TTS (Text-to-Speech)**

#### **TTS Started**
```kotlin
// Location: MessageActionButtons.kt, when TTS begins
// Pattern: Light tap (audio starting)

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
```

**Why:** Confirms audio is about to play (especially useful if volume is low).

**Timing:** When TTS state transitions from IDLE → LOADING → PLAYING.

---

#### **TTS Paused**
```kotlin
// Location: MessageActionButtons.kt, when user pauses TTS
// Pattern: Double light tap (different from start/stop)

val pattern = longArrayOf(0, 20, 40, 20)
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
```

**Why:** Distinguishes pause from stop/complete.

**Timing:** When TTS state transitions to PAUSED.

---

#### **TTS Completed**
```kotlin
// Location: TextToSpeechManager.android.kt, when playback ends
// Pattern: Single medium tap

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
```

**Why:** Confirms entire message was read aloud.

**Timing:** When TTS naturally completes (not user-interrupted).

---

### 5. **Follow-Up Questions**

#### **Follow-Up Chip Tapped**
```kotlin
// Location: MessageBubble.kt, when SuggestionChip is clicked
// Pattern: Light tap (selection confirmation)

view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
```

**Why:** Confirms selection, feels like a button press.

**Timing:** Immediately when chip `onClick` is triggered.

---

### 6. **Location Sharing**

#### **GPS Location Shared**
```kotlin
// Location: LocationViewModel.kt, after GPS location sent to backend
// Pattern: Success pattern (confirms location captured)

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
```

**Why:** Provides confirmation that location was successfully shared with AI.

**Timing:** After `shareGpsLocation()` succeeds.

---

### 7. **Errors & Warnings**

#### **Network Error**
```kotlin
// Location: ChatViewModel.kt, on API failure
// Pattern: Error buzz (longer, distinct from success)

val pattern = longArrayOf(0, 80, 150, 80)
vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
```

**Why:** Strong tactile feedback that something went wrong.

**Timing:** In `onFailure` handlers for API calls.

---

#### **Permission Denied**
```kotlin
// Location: Permission ViewModels, when user denies permission
// Pattern: Warning pattern (medium buzz)

vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
```

**Why:** Alerts user that permission is required for feature to work.

**Timing:** After permission denial callback.

---

### 8. **Navigation & UI**

#### **Pull-to-Refresh**
```kotlin
// Location: Conversations screen (if implemented)
// Pattern: Light tick when refresh starts

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
```

**Why:** Confirms refresh action triggered.

---

#### **Conversation Deleted**
```kotlin
// Location: ConversationsViewModel.kt, after delete succeeds
// Pattern: Heavy click (confirms destructive action)

vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
```

**Why:** Strong confirmation for destructive action.

**Timing:** After delete API call succeeds.

---

#### **Bottom Navigation Tab Switch**
```kotlin
// Location: ChatScreen.kt or App.kt, on tab change
// Pattern: Very light tap (subtle navigation feedback)

vibrator.vibrate(VibrationEffect.createOneShot(15, 40)) // Extremely gentle
```

**Why:** Subtle tactile feedback for navigation (optional, can be distracting).

---

## Implementation Guidelines

### 1. **Create a Haptic Helper Class**

```kotlin
// File: composeApp/src/androidMain/kotlin/com/nongtri/app/platform/HapticFeedback.android.kt

package com.nongtri.app.platform

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

class HapticFeedback(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Light tap for subtle confirmations
    fun tick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(20, 50))
        }
    }

    // Medium tap for standard actions
    fun click() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    // Strong tap for important actions
    fun heavyClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createOneShot(80, 200))
        }
    }

    // Double tap for success/completion
    fun doubleTap() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 30, 50, 30)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    // Error pattern (3 short buzzes)
    fun error() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 50, 100, 50, 100, 50)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }

    // Success pattern (short-long)
    fun success() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 30, 100, 60)
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
        }
    }
}

// Compose helper for View-based haptics
fun View.hapticClick() {
    performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
}

fun View.hapticTick() {
    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
}
```

### 2. **Add to MainActivity.kt**

```kotlin
class MainActivity : ComponentActivity() {
    private lateinit var hapticFeedback: HapticFeedback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hapticFeedback = HapticFeedback(applicationContext)

        setContent {
            CompositionLocalProvider(
                // ... existing providers
                LocalHapticFeedback provides hapticFeedback
            ) {
                App()
            }
        }
    }
}

// Composition local
val LocalHapticFeedback = staticCompositionLocalOf<HapticFeedback> {
    error("HapticFeedback not provided")
}
```

### 3. **Usage in Composables**

```kotlin
@Composable
fun ChatScreen() {
    val haptic = LocalHapticFeedback.current

    Button(onClick = {
        haptic.click()
        viewModel.sendMessage(message)
    }) {
        Text("Send")
    }
}
```

### 4. **Usage in ViewModels**

```kotlin
class ChatViewModel(
    private val hapticFeedback: HapticFeedback? = null
) : ViewModel() {

    fun sendMessage(message: String) {
        // ... send logic
        hapticFeedback?.click()
    }
}
```

---

## Best Practices

### ✅ DO:

1. **Use subtle haptics for frequent actions** (typing, scrolling)
2. **Use strong haptics for important/rare actions** (delete, error)
3. **Respect user preferences** - Allow disabling in settings
4. **Test on real devices** - Emulator haptics don't represent reality
5. **Match platform conventions** - Android users expect certain patterns
6. **Consider battery impact** - Haptics use power, don't overuse

### ❌ DON'T:

1. **Don't vibrate on every UI interaction** - Overwhelming and annoying
2. **Don't use long vibrations** - Drains battery and feels unresponsive
3. **Don't vibrate during streaming** - One per message is enough
4. **Don't vibrate for background events** - Only for user-initiated actions
5. **Don't ignore accessibility** - Some users rely on haptics for feedback

---

## Priority Implementation Order

### Phase 1: Essential Haptics (High Impact)
1. ✅ **Voice recording start/stop** - Critical for eyes-free use
2. ✅ **Message sent confirmation** - Standard messaging behavior
3. ✅ **Error feedback** - Alert users to problems

### Phase 2: Quality of Life (Medium Impact)
4. ✅ **TTS start/stop** - Audio playback confirmation
5. ✅ **Image upload success** - Multi-step action confirmation
6. ✅ **Follow-up chip tap** - Selection feedback

### Phase 3: Polish (Nice to Have)
7. ⚠️ **Response start/complete** - Can be distracting
8. ⚠️ **Navigation feedback** - Very subtle, optional
9. ⚠️ **Location shared** - Confirms GPS action

---

## User Settings (Recommended)

Add haptic preferences to UserPreferences:

```kotlin
// In UserPreferences.kt
private val HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
private val HAPTIC_STRENGTH = stringPreferencesKey("haptic_strength") // "light", "medium", "strong"

fun setHapticEnabled(enabled: Boolean)
fun getHapticEnabled(): Boolean = true // Default enabled

fun setHapticStrength(strength: String) // "light", "medium", "strong"
fun getHapticStrength(): String = "medium"
```

Update HapticFeedback class:

```kotlin
class HapticFeedback(
    private val context: Context,
    private val preferences: UserPreferences
) {

    private fun shouldVibrate(): Boolean {
        return preferences.getHapticEnabled()
    }

    private fun getStrengthMultiplier(): Float {
        return when (preferences.getHapticStrength()) {
            "light" -> 0.5f
            "strong" -> 1.5f
            else -> 1.0f
        }
    }

    fun click() {
        if (!shouldVibrate()) return
        // ... existing logic with strength adjustment
    }
}
```

---

## Analytics Tracking

Track haptic usage for optimization:

```kotlin
// Add to Events.kt
fun logHapticFeedback(
    hapticType: String, // "click", "tick", "error", etc.
    trigger: String     // "message_sent", "voice_start", etc.
) {
    AnalyticsService.logEvent("haptic_feedback", mapOf(
        "haptic_type" to hapticType,
        "trigger" to trigger
    ))
}
```

---

## Accessibility Considerations

- ✅ Haptics help users with visual impairments navigate
- ✅ Provide distinct patterns for success/error/warning
- ✅ Respect system accessibility settings
- ✅ Allow complete disabling in app settings
- ✅ Don't rely solely on haptics - always have visual feedback

---

## Testing Checklist

- [ ] Test on multiple devices (high-end, mid-range)
- [ ] Test with haptic settings disabled
- [ ] Test with different strength settings
- [ ] Verify battery impact (shouldn't be noticeable)
- [ ] Check haptics don't lag behind UI actions
- [ ] Ensure patterns are distinct and recognizable
- [ ] Test during actual usage scenarios (farm field, noisy environment)

---

## Device Compatibility

| Android Version | Features Available |
|-----------------|-------------------|
| API 26+ (Oreo) | VibrationEffect with patterns |
| API 29+ (Android 10) | Predefined effects (CLICK, TICK, etc.) |
| API 31+ (Android 12) | VibratorManager for better control |

**Minimum Target:** API 26 (you're on API 24, will need to handle fallbacks)

---

## Estimated Implementation Time

- **Haptic Helper Class:** 2 hours
- **Phase 1 (Essential):** 3-4 hours
- **Phase 2 (Quality of Life):** 2-3 hours
- **Phase 3 (Polish):** 2 hours
- **Settings UI:** 2 hours
- **Testing & Refinement:** 4 hours

**Total:** ~15-17 hours for full implementation

---

## Conclusion

Haptic feedback significantly improves perceived app quality and usability, especially for:
- 🎤 **Voice recording** - Critical for eyes-off operation
- 📸 **Image capture** - Confirms camera shutter
- ⚠️ **Errors** - Alerts user without being intrusive
- ✅ **Success actions** - Positive reinforcement

**Recommendation:** Start with Phase 1 (voice recording + errors), measure user feedback, then expand to Phase 2/3.
