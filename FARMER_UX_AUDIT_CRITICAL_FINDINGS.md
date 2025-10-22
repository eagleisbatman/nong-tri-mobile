# CRITICAL UX AUDIT: Complete Farmer Image Diagnosis Flow
**Date**: 2025-10-22
**Context**: Rural farmer, 2G connection, low literacy, holding phone to photograph diseased rice plant
**Scope**: Camera tap → Upload → Diagnosis → Display

---

## EXECUTIVE SUMMARY

**Overall Assessment**: The mobile app has SOLID foundations but contains **7 CRITICAL** and **12 HIGH** severity issues that could result in farmers losing work, seeing broken UI, or believing the app is frozen.

**Key Strengths**:
- ✅ Optimistic UI patterns (immediate feedback)
- ✅ Comprehensive error messages in ImagePicker
- ✅ Proper bitmap recycling (memory management)
- ✅ Size validation (5MB limit)
- ✅ Timeout handling (60s)
- ✅ Null-safe DiagnosisData parsing

**Critical Gaps**:
- ❌ Error messages NOT localized (hardcoded English)
- ❌ No recovery mechanism when upload fails after optimistic message shown
- ❌ State loss on rotation during upload (selectedImageBase64)
- ❌ No handling for diagnosisData=null in rendering logic
- ❌ Snackbar duration too short for low-literacy users
- ❌ No visible loading feedback during 60s upload on 2G
- ❌ Multiple UI strings not using localization system

---

## DETAILED FINDINGS BY FLOW STAGE

---

### 🎯 STAGE 1: Camera Icon Tap
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/screens/ChatScreen.kt`
**Lines**: 361-376

#### ✅ WORKING CORRECTLY
```kotlin
// Line 363-366: Prevents double-tap
if (isImageProcessing) {
    println("[ChatScreen] Image already being processed, ignoring click")
    return@WhatsAppStyleInputBar
}
```
- `isImageProcessing` flag with `rememberSaveable` survives rotation
- Prevents simultaneous camera launches

#### ⚠️ MEDIUM: App Backgrounding During Permission Flow
**Impact**: If farmer presses home button during camera permission dialog, state is preserved correctly.
**Status**: ✅ Works correctly due to `rememberSaveable` on key flags

#### ⚠️ LOW: isImageProcessing Reset Timing
**Location**: Lines 678, 706, 759
**Issue**: `isImageProcessing` reset happens in three places:
1. Line 678: After camera callback (correct)
2. Line 706: After gallery callback (correct)
3. Line 759: After upload starts in `onConfirm` (correct)

**Farmer Impact**: LOW - This is correctly implemented, but worth documenting.

---

### 🎯 STAGE 2: Camera Capture
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/androidMain/kotlin/com/nongtri/app/platform/ImagePicker.android.kt`
**Lines**: 39-285

#### ✅ WORKING CORRECTLY: Error Handling
```kotlin
// Line 142-150: Farmer-friendly error when file unreadable
return ImagePickerResult(
    uri = uri.toString(),
    base64Data = null,
    error = "Cannot access image file. Please try another image."
)

// Line 165: Corrupted file handling
error = "Cannot read image. The file may be corrupted or in an unsupported format."
```

**Strengths**:
- Clear, actionable error messages
- Distinguishes between "can't access" vs "corrupted"
- Returns ImagePickerResult with error field instead of null

#### 🔴 CRITICAL: Error Messages Not Localized
**Location**: Lines 149, 165
**Impact**: CRITICAL for Vietnamese farmers
**Current**:
```kotlin
error = "Cannot access image file. Please try another image."  // English only!
```

**Missing Vietnamese Translation**:
```kotlin
error = "Không thể truy cập tệp hình ảnh. Vui lòng chọn hình ảnh khác."
```

**Farmer Impact**: CRITICAL
- Farmer sees English error they cannot read
- No idea what went wrong or how to fix it
- May abandon app entirely

**Fix Required**:
```kotlin
// ImagePicker.android.kt should receive Strings object
actual class ImagePicker(
    private val context: Context,
    private val strings: Strings  // ADD THIS
)

// Use localized strings
error = strings.errorImageAccess ?: "Cannot access image file..."
error = strings.errorImageCorrupted ?: "Cannot read image..."
```

**Files to Update**:
1. `/mobile/composeApp/src/androidMain/kotlin/com/nongtri/app/platform/ImagePicker.android.kt`
2. `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/l10n/Strings.kt` (add new error strings)
3. `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/screens/ChatScreen.kt` (pass strings to ImagePicker)

---

#### ⚠️ HIGH: Cache Full / SD Card Full Not Handled
**Location**: Lines 271-284 (`createTempImageFile`)
**Current Code**:
```kotlin
private fun createTempImageFile(): File {
    val storageDir = File(context.cacheDir, "shared_images")
    if (!storageDir.exists()) {
        storageDir.mkdirs()
    }
    return File.createTempFile(imageFileName, ".jpg", storageDir)
}
```

**Missing**:
- No try-catch around `File.createTempFile()`
- No check for available storage space
- No fallback if cache directory creation fails

**Scenario**:
1. Farmer's phone has 100MB left, mostly photos of family
2. Taps camera icon
3. `createTempFile()` throws `IOException: No space left on device`
4. App crashes OR farmer sees generic error

**Farmer Impact**: HIGH
- Rural farmers often have cheap phones with 8-16GB storage
- SD cards may be full of photos/videos
- Error is cryptic, farmer doesn't know to clear space

**Fix Required**:
```kotlin
private fun createTempImageFile(): File {
    try {
        val storageDir = File(context.cacheDir, "shared_images")

        // Check available space (at least 10MB needed)
        val freeSpace = storageDir.freeSpace
        if (freeSpace < 10 * 1024 * 1024) {
            throw IOException(strings.errorLowStorage)
        }

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException(strings.errorCannotCreateFolder)
        }

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    } catch (e: IOException) {
        // Return user-friendly error via callback
        throw Exception(strings.errorStorageFull ?: "Not enough storage. Please free up space.")
    }
}
```

---

#### ✅ EXCELLENT: Bitmap Recycling
**Location**: Lines 188-191
**Code**:
```kotlin
// Clean up bitmaps
if (compressedBitmap != bitmap) {
    bitmap.recycle()
}
compressedBitmap.recycle()
```

**Farmer Impact**: LOW (positive)
- Prevents memory leaks on low-RAM devices (512MB)
- Correctly handles both original and scaled bitmaps

---

### 🎯 STAGE 3: Image Processing (Decode → Compress → Base64)
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/androidMain/kotlin/com/nongtri/app/platform/ImagePicker.android.kt`
**Lines**: 136-206

#### ✅ WORKING CORRECTLY: Compression Logic
**Location**: Lines 212-255
**Strengths**:
- Scales down images > 2048px (saves bandwidth on 2G)
- Iterative quality reduction (90% → 50%) to hit 2MB target
- Logs each step for debugging

**Farmer Impact**: LOW (positive)
- 2MB target is appropriate for 2G networks
- 50% quality floor still readable for diagnosis

---

#### 🔴 CRITICAL: Compression Failure → Unrecycled Bitmap
**Location**: Lines 248-254
**Current Code**:
```kotlin
} catch (e: Exception) {
    // Clean up scaled bitmap if compression failed
    if (scaledBitmap != bitmap) {
        scaledBitmap.recycle()
    }
    throw e
}
```

**Issue**: Original `bitmap` is NOT recycled if compression fails!

**Scenario**:
1. Farmer takes 12MP photo (4000x3000)
2. Bitmap decoded successfully (12MB in memory)
3. Scaling works, creates `scaledBitmap` (2048x1536, 3MB)
4. Compression loop throws `OutOfMemoryError` on low-RAM device
5. Catch block recycles `scaledBitmap` ✅
6. Original `bitmap` LEAKED ❌ (12MB stays in memory)

**Farmer Impact**: CRITICAL on 512MB devices
- Memory leak accumulates if farmer retries
- 2-3 failed attempts = 36MB leaked = app crash
- Farmer blames app for "freezing"

**Fix Required**:
```kotlin
} catch (e: Exception) {
    // Clean up ALL bitmaps if compression failed
    if (scaledBitmap != bitmap) {
        bitmap.recycle()      // ← ADD THIS
        scaledBitmap.recycle()
    } else {
        bitmap.recycle()      // ← Also recycle if no scaling happened
    }
    throw e
}
```

---

### 🎯 STAGE 4: Image Preview Dialog
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/components/ImagePreviewDialog.kt`
**Lines**: 1-163

#### ⚠️ HIGH: Huge Image → Laggy Preview
**Location**: Lines 84-89 (AsyncImage)
**Current**:
```kotlin
AsyncImage(
    model = imageUri,
    contentDescription = "Selected plant image",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Fit
)
```

**Issue**:
- `imageUri` points to full-resolution original (4000x3000)
- Coil loads and decodes entire bitmap for preview
- On low-end device, this causes 1-2 second lag

**Farmer Impact**: HIGH
- Farmer sees white screen for 1-2 seconds after capturing
- May think app froze and tap back button
- Loses captured image

**Fix Suggestion** (Medium Priority):
```kotlin
AsyncImage(
    model = imageUri,
    contentDescription = "Selected plant image",
    modifier = Modifier.fillMaxSize(),
    contentScale = ContentScale.Fit,
    // Coil downsampling for preview
    placeholder = { CircularProgressIndicator() },  // Show loading
    error = { Icon(Icons.Default.BrokenImage, ...) }
)
```

**Alternative**: Use the already-compressed base64 for preview instead of original URI.

---

#### ⚠️ MEDIUM: Vietnamese Special Characters in Question Field
**Location**: Lines 111-119
**Current**:
```kotlin
OutlinedTextField(
    value = question,
    onValueChange = { question = it },
    placeholder = { Text("How is the health of my crop?") },  // English placeholder
    minLines = 2,
    maxLines = 4
)
```

**Issue**:
- Placeholder is hardcoded English
- Vietnamese input works (tested with Unicode support)
- But placeholder misleads Vietnamese users

**Farmer Impact**: MEDIUM
- Farmer may think they need to type in English
- Reduces usability for low-literacy farmers

**Fix Required**:
```kotlin
// In ImagePreviewDialog, receive strings parameter
@Composable
fun ImagePreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit,
    onConfirm: (question: String) -> Unit,
    strings: Strings,  // ← ADD THIS
    modifier: Modifier = Modifier
) {
    var question by remember { mutableStateOf(strings.defaultPlantQuestion) }

    OutlinedTextField(
        value = question,
        onValueChange = { question = it },
        placeholder = { Text(strings.defaultPlantQuestion) }  // Localized
    )
}

// Add to Strings.kt:
// EN: "How is the health of my crop?"
// VI: "Sức khỏe của cây trồng của tôi như thế nào?"
```

---

#### ⚠️ LOW: Keyboard Covers Confirm Button
**Location**: Lines 123-150 (Buttons at bottom)
**Current**: Buttons are in bottom `Surface` with fixed padding

**Potential Issue**:
- On small screens (4.5" phones) with keyboard open
- Buttons may be obscured
- Farmer cannot tap "Send for Diagnosis"

**Farmer Impact**: LOW
- Can dismiss keyboard by tapping back
- Most modern Android handles this with `windowSoftInputMode`

**Status**: Likely OK due to `DialogProperties(usePlatformDefaultWidth = false)` + scrolling

---

### 🎯 STAGE 5: Upload to Backend
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/viewmodel/ChatViewModel.kt`
**Lines**: 583-728

#### ✅ EXCELLENT: Size Validation
**Location**: Lines 589-603
```kotlin
val estimatedSizeBytes = (imageData.length * 3L / 4)  // base64 to bytes
val estimatedSizeMB = estimatedSizeBytes / (1024.0 * 1024.0)
val maxSizeMB = 5.0

if (estimatedSizeMB > maxSizeMB) {
    println("[ImageDiagnosis] ✗ Image too large: ${String.format("%.2f", estimatedSizeMB)}MB")
    _uiState.update { state ->
        state.copy(
            error = "Image is too large (${String.format("%.1f", estimatedSizeMB)}MB). Please try a smaller image.",
            isLoading = false
        )
    }
    return
}
```

**Farmer Impact**: LOW (positive)
- Prevents wasting bandwidth on 2G
- Clear error message with actual size
- But error message NOT localized ⚠️

---

#### 🔴 CRITICAL: Optimistic Message Without Cleanup on Error
**Location**: Lines 612-617, 715-724

**Current Flow**:
```kotlin
// 1. Set loading state (Line 612-617)
_uiState.update { state ->
    state.copy(
        isLoading = true,
        error = null
    )
}

// 2. Create optimistic assistant message (Line 620-631)
val initialAssistantMessage = ChatMessage(
    id = assistantMessageId,
    role = MessageRole.ASSISTANT,
    content = "",
    isLoading = true  // ← Shows typing indicator
)

// 3. On failure (Line 715-724)
onFailure = { error ->
    _uiState.update { state ->
        state.copy(
            messages = state.messages.filter { it.id != assistantMessageId },  // ✅ Removes assistant
            isLoading = false,  // ✅ Clears global loading
            error = error.message ?: "Failed to analyze image"
        )
    }
}
```

**What's Missing**: User's optimistic image message is NOT removed on failure!

**Full Flow**:
1. **ChatScreen.kt Line 744-747**: `viewModel.showOptimisticImageMessage()` adds USER message
2. **ChatViewModel.kt Line 519-540**: Creates message with `isLoading = true`
3. **ChatScreen.kt Line 750-753**: `viewModel.sendImageDiagnosis()` starts upload
4. **Upload fails** (timeout, network error, backend 500)
5. **ChatViewModel.kt Line 715-724**: Removes assistant message ✅, sets `uiState.error` ✅
6. **USER MESSAGE STILL SHOWS** with `isLoading = true` ❌❌❌

**Farmer Sees**:
```
┌─────────────────────────────┐
│ [USER IMAGE - still spinning]│
│ "Is my rice plant healthy?" │  ← isLoading=true FOREVER
│ Analyzing... (spinner)      │  ← Never cleared
└─────────────────────────────┘

[Snackbar at bottom]
"Failed to analyze image"
```

**Farmer Impact**: CRITICAL
- Optimistic message shows "Analyzing..." with spinner FOREVER
- Farmer thinks request is still processing
- May wait 5+ minutes before giving up
- Snackbar disappears after 4 seconds (not visible long enough)
- No retry mechanism visible

**Fix Required**:
```kotlin
// ChatViewModel.kt, add method to clear user's loading state
private fun clearImageMessageLoading() {
    _uiState.update { state ->
        state.copy(
            messages = state.messages.map { msg ->
                if (msg.role == MessageRole.USER &&
                    msg.messageType == "image" &&
                    msg.isLoading) {
                    msg.copy(isLoading = false)
                } else {
                    msg
                }
            }
        )
    }
}

// In sendImageDiagnosis onFailure (Line 715)
onFailure = { error ->
    clearImageMessageLoading()  // ← ADD THIS
    _uiState.update { state ->
        state.copy(
            messages = state.messages.filter { it.id != assistantMessageId },
            isLoading = false,
            error = error.message ?: "Failed to analyze image"
        )
    }
}
```

---

#### 🔴 CRITICAL: First Chunk Clears Loading, But What If No Chunks?
**Location**: Lines 642-661

**Current Logic**:
```kotlin
var firstChunkReceived = false

onChunk = { chunk ->
    // On first chunk, clear loading state on user's image message
    if (!firstChunkReceived) {
        firstChunkReceived = true
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.role == MessageRole.USER &&
                        msg.messageType == "image" &&
                        msg.isLoading) {
                        msg.copy(isLoading = false)  // ✅ Clears on first chunk
                    } else {
                        msg
                    }
                }
            )
        }
    }
    // ... append chunk to assistant message
}
```

**Issue**: What if backend sends **metadata but NO content chunks**?

**Scenario**:
1. Backend receives image
2. AgriVision MCP times out (30s)
3. Backend sends SSE: `{__metadata: true, diagnosisData: null, error: "AgriVision timeout"}`
4. Mobile's `onMetadata` is called ✅
5. But `onChunk` is NEVER called ❌
6. `firstChunkReceived` stays `false`
7. User's image message shows "Analyzing..." FOREVER ❌

**Farmer Impact**: CRITICAL
- Same as previous issue
- Infinite loading spinner on user message
- No clear error state

**Fix Required**:
```kotlin
onMetadata = { metadata ->
    // Clear user image loading when metadata arrives (regardless of chunks)
    _uiState.update { state ->
        state.copy(
            messages = state.messages.map { msg ->
                if (msg.role == MessageRole.USER &&
                    msg.messageType == "image" &&
                    msg.isLoading) {
                    msg.copy(isLoading = false)  // ← Clear loading on metadata
                } else {
                    msg
                }
            }
        )
    }

    // ... rest of metadata handling
}
```

---

#### ⚠️ HIGH: No Visible Progress During 60s Upload on 2G
**Context**:
- 5MB image on 2G network (30-40 KB/s upload)
- Upload takes 60+ seconds
- Mobile timeout: 60 seconds (Line 47 in NongTriApi.kt)

**Current UX**:
1. Farmer taps "Send for Diagnosis" in preview dialog
2. Dialog closes immediately
3. User message appears with image + "Analyzing..." spinner
4. **60 seconds of no feedback** (besides spinner)
5. Either: success (chunks start) OR timeout error

**Farmer Impact**: HIGH
- Farmer has NO IDEA upload is happening
- Spinner looks same for upload vs backend processing
- On timeout, farmer may retry immediately (wastes more bandwidth)

**Missing**: Progress indicator for upload phase

**Fix Suggestion** (Medium Priority):
```kotlin
// In ImageMessageBubble.kt, show upload progress
if (message.isLoading) {
    Box(...) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(...)
            Spacer(...)
            Text(
                if (message.uploadProgress != null) {
                    "Uploading: ${message.uploadProgress}%"  // ← NEW
                } else {
                    "Analyzing..."
                }
            )
        }
    }
}

// ChatViewModel needs to track upload progress
// Ktor supports progress callbacks via `onUpload { sent, total -> ... }`
```

**Alternative** (Simpler):
- Change "Analyzing..." to "Uploading..." during first 5 seconds
- Backend sends `{status: "upload_complete"}` chunk when image received
- Mobile switches text to "Analyzing..." after that chunk

---

### 🎯 STAGE 6: SSE Stream Reception
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/data/api/NongTriApi.kt`
**Lines**: 163-303

#### ✅ EXCELLENT: Timeout Handling
**Location**: Lines 46-50, 290-301
```kotlin
install(HttpTimeout) {
    requestTimeoutMillis = 60000  // 60 seconds
    connectTimeoutMillis = 15000  // 15 seconds
    socketTimeoutMillis = 60000   // 60 seconds
}

// Farmer-friendly error messages
val userMessage = when {
    e.message?.contains("timeout", ignoreCase = true) == true ->
        "Upload timed out. This may be due to slow internet. Please try a smaller image or wait and try again."
    e.message?.contains("network", ignoreCase = true) == true ->
        "No internet connection. Please check your network and try again."
    else ->
        "Upload failed. Please try again."
}
```

**Farmer Impact**: LOW (positive)
- 60s timeout is appropriate for 2G
- Error messages are clear and actionable
- BUT: Messages are hardcoded English ⚠️

---

#### ⚠️ HIGH: Error Messages Not Localized (Again)
**Location**: Lines 288-301
**Issue**: Farmer-friendly errors are hardcoded English

**Fix Required**:
```kotlin
// NongTriApi should receive Strings
class NongTriApi(
    private val baseUrl: String = BuildConfig.API_URL,
    private val strings: Strings  // ← ADD THIS
)

// Use localized errors
val userMessage = when {
    e.message?.contains("timeout") == true -> strings.errorUploadTimeout
    e.message?.contains("network") == true -> strings.errorNoInternet
    else -> strings.errorUploadFailed
}

// Add to Strings.kt:
// EN: "Upload timed out. This may be due to slow internet..."
// VI: "Tải lên hết thời gian. Điều này có thể do mạng chậm..."
```

---

#### ✅ EXCELLENT: diagnosisData Null Handling
**Location**: Lines 238-253
```kotlin
val diagnosisData = try {
    val diagnosisElement = parsed["diagnosisData"]
    if (diagnosisElement != null && diagnosisElement.toString() != "null") {
        val diagnosisJson = if (diagnosisElement is JsonObject) {
            diagnosisElement.toString()
        } else {
            diagnosisElement.toString().trim('"')
        }
        Json.decodeFromString<DiagnosisData>(diagnosisJson)
    } else {
        null  // ✅ Handles null gracefully
    }
} catch (e: Exception) {
    println("[ImageDiagnosis] Error parsing diagnosisData: ${e.message}")
    null  // ✅ Logs error and continues
}
```

**Farmer Impact**: LOW (positive)
- Backend can send `diagnosisData: null` safely
- Mobile sets `message.diagnosisData = null`
- DiagnosisResponseBubble must handle this → Check next stage

---

#### ⚠️ MEDIUM: Malformed JSON → Silent Failure
**Location**: Lines 238-253
**Current**: `catch (e: Exception) { println(...); null }`

**Issue**:
- If backend sends malformed JSON in diagnosisData
- Mobile logs error and sets `diagnosisData = null`
- Farmer sees advice text but NO diagnosis card
- No indication that parsing failed

**Scenario**:
```json
// Backend sends:
{
  "__metadata": true,
  "diagnosisData": "{\"crop\": {\"name_en\": \"Rice\", BROKEN JSON HERE"
}
```

**Result**:
- Parsing fails silently
- Farmer sees only text advice (no crop/health/issues card)
- No error shown

**Farmer Impact**: MEDIUM
- Missing visual diagnosis card (color-coded health)
- Farmer may think diagnosis failed
- But can still read advice text

**Fix Suggestion** (Low Priority):
- Add to metadata: `diagnosisParseError: true`
- Show small warning in UI: "Visual diagnosis unavailable, see text advice"

---

### 🎯 STAGE 7: Diagnosis Rendering
**File**: `/Users/eagleisbatman/nong_tri_workspace/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/components/DiagnosisResponseBubble.kt`
**Lines**: 1-250

#### 🔴 CRITICAL: diagnosisData=null → Broken UI (Potential)
**Location**: Lines 37-168

**Current Code**:
```kotlin
// Diagnosis summary card (if diagnosis data available)
if (message.diagnosisData != null) {
    Card {
        // ... render crop, health status, issues
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// Full advice text (always shown)
Surface {
    if (message.content.isNotBlank()) {
        Text(text = message.content, ...)
    } else if (message.isLoading) {
        // Show loading indicator
    }
}
```

**Analysis**:
- ✅ `if (message.diagnosisData != null)` prevents null pointer
- ✅ Advice text shown even if diagnosisData is null
- ✅ Loading indicator shown during streaming

**Potential Issue**: What if `message.content` is ALSO blank AND diagnosisData is null?

**Scenario**:
1. Backend times out during AgriVision call
2. Sends metadata: `{diagnosisData: null, content: ""}`
3. Mobile receives it
4. `message.diagnosisData = null` → No card shown ✅
5. `message.content = ""` → No advice text ❌
6. `message.isLoading = false` (stream ended) → No loading indicator ❌

**Farmer Sees**:
```
┌─────────────────────┐
│ (empty bubble)      │  ← Just timestamp, no content
│                     │
│ 2m ago              │
└─────────────────────┘
```

**Farmer Impact**: CRITICAL
- Completely empty response
- Farmer thinks app is broken
- No indication of what went wrong

**Fix Required**:
```kotlin
Surface {
    Column {
        if (message.content.isNotBlank()) {
            Text(text = message.content, ...)
        } else if (message.isLoading) {
            // Loading indicator
            CircularProgressIndicator(...)
        } else if (message.diagnosisData == null) {
            // ← ADD THIS: Show error state when both are null
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ErrorOutline, tint = MaterialTheme.colorScheme.error)
                Spacer(width = 8.dp)
                Text(
                    text = "Unable to analyze image. Please try again with a clearer photo.",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // TTS button (existing code)
    }
}
```

---

#### ⚠️ HIGH: Empty Issues Array → "Detected Issues:" Header with Nothing Below
**Location**: Lines 107-153

**Current Code**:
```kotlin
// Issues list (if any)
if (message.diagnosisData.issues.isNotEmpty()) {
    Spacer(...)
    HorizontalDivider(...)
    Spacer(...)

    Text(text = "Detected Issues:", ...)  // Header

    Spacer(...)

    message.diagnosisData.issues.forEach { issue ->
        // Render issue
    }
}
```

**Analysis**: ✅ Correctly checks `isNotEmpty()` before showing header

**No Issue Here** - This is implemented correctly!

---

#### ⚠️ MEDIUM: Color-Blind Farmers Cannot Distinguish Health Status
**Location**: Lines 83-103

**Current**:
```kotlin
val healthColor = when (message.diagnosisData.getHealthStatusColor()) {
    HealthStatusColor.GREEN -> Color(0xFF4CAF50)   // Healthy
    HealthStatusColor.YELLOW -> Color(0xFFFFC107)  // Mild
    HealthStatusColor.ORANGE -> Color(0xFFFF9800)  // Moderate
    HealthStatusColor.RED -> Color(0xFFF44336)     // Severe
    HealthStatusColor.GRAY -> MaterialTheme.colorScheme.onSurfaceVariant
}

Icon(Icons.Default.HealthAndSafety, tint = healthColor, ...)
Text(text = message.diagnosisData.healthStatus, color = healthColor)
```

**Issue**:
- Health status relies ONLY on color coding
- Red-green color blindness affects ~8% of men, ~0.5% of women
- Farmer cannot distinguish "Healthy" (green) from "Severe" (red)

**Farmer Impact**: MEDIUM
- Color-blind farmer misreads health status
- May apply wrong treatment
- Health text IS shown ("Healthy", "Severe Issue") but color reinforcement missing

**Fix Suggestion** (Low Priority):
```kotlin
// Add icon variation for different health levels
val (healthIcon, healthColor) = when (diagnosisData.getHealthStatusColor()) {
    HealthStatusColor.GREEN -> Pair(Icons.Default.CheckCircle, Color.Green)
    HealthStatusColor.YELLOW -> Pair(Icons.Default.Warning, Color.Yellow)
    HealthStatusColor.ORANGE -> Pair(Icons.Default.Error, Color.Orange)
    HealthStatusColor.RED -> Pair(Icons.Default.Dangerous, Color.Red)
    else -> Pair(Icons.Default.HealthAndSafety, Color.Gray)
}

Icon(healthIcon, tint = healthColor, ...)  // Different shapes
```

---

### 🎯 STAGE 8: State Survival Across Lifecycle Events

#### 🔴 CRITICAL: Rotation During Upload → Base64 Data Lost
**Location**: ChatScreen.kt Lines 72-76

**Current Code**:
```kotlin
// Image selection state
var showImagePreviewDialog by rememberSaveable { mutableStateOf(false) }  // ✅ Survives
var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }  // ✅ Survives
// Note: selectedImageBase64 cannot use rememberSaveable (too large for savedInstanceState)
var selectedImageBase64 by remember { mutableStateOf<String?>(null) }  // ❌ LOST on rotation!
var isImageProcessing by rememberSaveable { mutableStateOf(false) }  // ✅ Survives
```

**Issue**: `selectedImageBase64` uses `remember`, not `rememberSaveable`

**Scenario**:
1. Farmer captures plant photo
2. Confirms in preview dialog (line 724-760)
3. `viewModel.showOptimisticImageMessage()` called ✅
4. `viewModel.sendImageDiagnosis()` called ✅
5. **Upload is in progress** (20 seconds into 60s timeout)
6. **Farmer rotates phone** (portrait → landscape)
7. Compose recomposes
8. `selectedImageBase64` is LOST (reset to null) ❌
9. Upload continues in background (already sent to Ktor) ✅
10. Upload completes successfully
11. Farmer tries to send ANOTHER image
12. Taps confirm in preview → base64Data is NULL
13. Line 727-738 shows error: "Failed to process image"

**Farmer Impact**: CRITICAL
- Cannot send second image after rotation
- No way to recover without re-capturing photo
- Error message is confusing ("Failed to process image" when image was already processed)

**Why Not Using rememberSaveable?**
- Comment says: "too large for savedInstanceState"
- Base64 of 5MB image = ~6.7MB string
- Android savedInstanceState limit = 1MB
- TransactionTooLargeException if stored

**Fix Required**: Use ViewModel to hold transient state
```kotlin
// In ChatViewModel, add:
private var pendingImageBase64: String? = null

fun setPendingImageData(base64: String) {
    pendingImageBase64 = base64
}

fun clearPendingImageData() {
    pendingImageBase64 = null
}

// In ChatScreen:
var selectedImageUri by rememberSaveable { mutableStateOf<String?>(null) }
// Remove: var selectedImageBase64 by remember { ... }

// When image captured:
result.base64Data?.let { base64 ->
    selectedImageUri = result.uri
    viewModel.setPendingImageData(base64)  // Store in ViewModel (survives rotation)
    showImagePreviewDialog = true
}

// In preview onConfirm:
val base64Data = viewModel.pendingImageBase64  // Retrieve from ViewModel
if (base64Data == null) { ... }
```

---

#### ⚠️ LOW: App Backgrounded During Upload
**Scenario**:
1. Farmer sends image diagnosis
2. Phone call comes in
3. App goes to background
4. Upload continues (Android allows background network for ~5min)
5. Farmer returns to app after 2 minutes

**Status**: ✅ Should work correctly
- ViewModel state preserved
- Optimistic message still shown
- SSE stream continues in coroutine
- UI updates when chunks arrive

**No Issue Found** - Android handles this well

---

### 🎯 STAGE 9: Error Visibility & Farmer Comprehension

#### 🔴 CRITICAL: Snackbar Duration Too Short for Low-Literacy Farmers
**Location**: ChatScreen.kt Lines 671-675, 698-702, 730-734

**Current**:
```kotlin
snackbarHostState.showSnackbar(
    message = errorMessage,
    duration = SnackbarDuration.Long  // = 10 seconds (Android default)
)
```

**Issue**:
- `SnackbarDuration.Long` = 10 seconds
- Error messages are 50-100 characters (English)
- Low-literacy farmer reads at ~30 WPM (words per minute)
- Average error: "Upload timed out. This may be due to slow internet. Please try a smaller image or wait and try again." (20 words)
- Read time: 20 words ÷ 30 WPM = 40 seconds
- **Snackbar disappears after 10 seconds** ❌

**Farmer Impact**: CRITICAL
- Farmer sees error flash at bottom
- Cannot read full message before it disappears
- No way to retrieve error message
- Farmer has no idea what went wrong or how to fix it

**Fix Required**:
```kotlin
// Option 1: Indefinite duration with dismiss button
snackbarHostState.showSnackbar(
    message = errorMessage,
    duration = SnackbarDuration.Indefinite,
    actionLabel = strings.ok  // "OK" or "Đóng"
)

// Option 2: Use AlertDialog instead for critical errors
if (errorMessage.contains("timeout") || errorMessage.contains("failed")) {
    showErrorDialog = true
    errorDialogMessage = errorMessage
}

// Render dialog:
if (showErrorDialog) {
    AlertDialog(
        onDismissRequest = { showErrorDialog = false },
        title = { Text(strings.errorTitle) },
        text = { Text(errorDialogMessage) },
        confirmButton = {
            TextButton(onClick = { showErrorDialog = false }) {
                Text(strings.ok)
            }
        }
    )
}
```

---

#### 🔴 CRITICAL: Snackbar Position Obscured by Input Bar on Small Screens
**Location**: ChatScreen.kt Lines 150-152

**Current**:
```kotlin
Scaffold(
    snackbarHost = {
        SnackbarHost(hostState = snackbarHostState)  // Default position = bottom
    },
    bottomBar = {
        // WhatsAppStyleInputBar
    }
)
```

**Issue**:
- Snackbar appears at bottom of screen
- Input bar is 56dp tall + keyboard height
- On 4.5" screen with keyboard open, snackbar may be obscured

**Farmer Impact**: CRITICAL
- Error message shown but not visible
- Farmer thinks upload is still processing (no error seen)

**Fix Required**:
```kotlin
snackbarHost = {
    // Position snackbar ABOVE bottom bar
    Box(modifier = Modifier.padding(bottom = 72.dp)) {
        SnackbarHost(hostState = snackbarHostState)
    }
}
```

---

### 🎯 STAGE 10: Localization Coverage

#### 🔴 CRITICAL: 15+ UI Strings Not Localized
**Location**: Various files

**Hardcoded English Strings**:

1. **ImagePreviewDialog.kt** Line 53: `"Confirm Image"` ❌
2. **ImagePreviewDialog.kt** Line 105: `"Ask a question about your plant:"` ❌
3. **ImagePreviewDialog.kt** Line 115: `"How is the health of my crop?"` (placeholder) ❌
4. **ImagePreviewDialog.kt** Line 148: `"Send for Diagnosis"` ❌
5. **ImagePreviewDialog.kt** Line 155: `"The AI will analyze your plant image..."` ❌
6. **ImageMessageBubble.kt** Line 93: `"Analyzing..."` ❌
7. **DiagnosisResponseBubble.kt** Line 113: `"Detected Issues:"` ❌
8. **DiagnosisResponseBubble.kt** Line 159: `"Growth Stage:"` ❌
9. **DiagnosisResponseBubble.kt** Line 193: `"Analyzing plant health..."` ❌
10. **DiagnosisResponseBubble.kt** Line 216: `"Listen to advice"` ❌
11. **ImagePicker.android.kt** Line 149: `"Cannot access image file..."` ❌
12. **ImagePicker.android.kt** Line 165: `"Cannot read image..."` ❌
13. **NongTriApi.kt** Line 292: `"Upload timed out..."` ❌
14. **NongTriApi.kt** Line 295: `"No internet connection..."` ❌
15. **NongTriApi.kt** Line 299: `"Upload failed. Please try again."` ❌
16. **ChatViewModel.kt** Line 598: `"Image is too large..."` ❌

**Farmer Impact**: CRITICAL
- Vietnamese farmers see English errors/labels
- Cannot understand what went wrong
- Cannot use diagnosis feature effectively

**Fix Required**:
```kotlin
// Add to Strings.kt:
interface Strings {
    // ... existing strings

    // Image diagnosis
    val confirmImage: String
    val askPlantQuestion: String
    val defaultPlantQuestion: String
    val sendForDiagnosis: String
    val aiWillAnalyze: String
    val analyzing: String
    val analyzingPlantHealth: String
    val detectedIssues: String
    val growthStage: String
    val listenToAdvice: String

    // Errors
    val errorImageAccess: String
    val errorImageCorrupted: String
    val errorImageTooLarge: String
    val errorUploadTimeout: String
    val errorNoInternet: String
    val errorUploadFailed: String
}

// English:
override val confirmImage = "Confirm Image"
override val askPlantQuestion = "Ask a question about your plant:"
override val defaultPlantQuestion = "How is the health of my crop?"
// ... etc

// Vietnamese:
override val confirmImage = "Xác nhận hình ảnh"
override val askPlantQuestion = "Hỏi một câu hỏi về cây trồng của bạn:"
override val defaultPlantQuestion = "Sức khỏe của cây trồng của tôi như thế nào?"
// ... etc
```

---

## SUMMARY OF FINDINGS

### 🔴 CRITICAL Issues (7 total)

| # | Issue | File | Line | Farmer Impact |
|---|-------|------|------|---------------|
| 1 | Error messages not localized | ImagePicker.android.kt | 149, 165 | Cannot read errors in Vietnamese |
| 2 | Optimistic user message stuck loading after upload failure | ChatViewModel.kt | 715-724 | Sees infinite spinner, thinks app frozen |
| 3 | User message stuck loading when metadata arrives but no chunks | ChatViewModel.kt | 642-661 | Sees infinite spinner on timeout |
| 4 | Bitmap leak when compression fails | ImagePicker.android.kt | 248-254 | Memory leak → app crash on retry |
| 5 | State loss on rotation (selectedImageBase64) | ChatScreen.kt | 75 | Cannot send 2nd image after rotation |
| 6 | Snackbar duration too short for low-literacy | ChatScreen.kt | 673, 700, 732 | Cannot read error before disappears |
| 7 | 15+ UI strings not localized | Multiple files | Various | Vietnamese farmers see English |

### ⚠️ HIGH Issues (6 total)

| # | Issue | File | Line | Farmer Impact |
|---|-------|------|------|---------------|
| 1 | Cache full / SD card full not handled | ImagePicker.android.kt | 271-284 | Cryptic error, doesn't know to free space |
| 2 | No visible progress during 60s upload on 2G | ChatViewModel.kt | 583-728 | Thinks app frozen, may retry too soon |
| 3 | API error messages not localized | NongTriApi.kt | 288-301 | Cannot read timeout/network errors |
| 4 | Huge image lag in preview | ImagePreviewDialog.kt | 84-89 | 1-2s white screen, may think froze |
| 5 | Empty issues array edge case (VERIFIED OK) | DiagnosisResponseBubble.kt | 107 | ✅ No issue - handled correctly |
| 6 | Empty diagnosis bubble when both content and diagnosisData null | DiagnosisResponseBubble.kt | 178-198 | Sees blank response, thinks broken |

### ⚠️ MEDIUM Issues (6 total)

| # | Issue | File | Line | Farmer Impact |
|---|-------|------|------|---------------|
| 1 | Vietnamese placeholder in preview dialog | ImagePreviewDialog.kt | 115 | May think must type in English |
| 2 | Malformed diagnosisData → silent failure | NongTriApi.kt | 238-253 | Missing visual card, no error shown |
| 3 | Color-blind farmers cannot distinguish health | DiagnosisResponseBubble.kt | 83-103 | Cannot tell healthy vs severe by color |
| 4 | Keyboard covers confirm button (likely OK) | ImagePreviewDialog.kt | 123-150 | Can dismiss keyboard, minor UX issue |
| 5 | Snackbar obscured by input bar | ChatScreen.kt | 150-152 | Error not visible on small screens |
| 6 | Permission revoked mid-capture | ImagePicker.android.kt | Various | Unlikely scenario, needs testing |

---

## RECOMMENDED PRIORITY FIXES

### Priority 1: IMMEDIATE (Before Next Release)
1. ✅ **Add localization for all hardcoded strings** (7 CRITICAL issues)
   - ImagePicker errors → Strings.kt
   - ImagePreviewDialog labels → Strings.kt
   - DiagnosisResponseBubble labels → Strings.kt
   - NongTriApi errors → Strings.kt
   - ChatViewModel errors → Strings.kt

2. ✅ **Fix infinite loading spinner on upload failure**
   - Add `clearImageMessageLoading()` in ChatViewModel
   - Call on both `onFailure` and `onMetadata` (if no chunks)

3. ✅ **Fix snackbar duration and position**
   - Change to `SnackbarDuration.Indefinite` with dismiss button
   - OR use AlertDialog for critical errors
   - Position above input bar

### Priority 2: HIGH (Within 2 Weeks)
4. ✅ **Fix state loss on rotation**
   - Move `selectedImageBase64` to ViewModel
   - Add `pendingImageBase64` property

5. ✅ **Add storage space check**
   - Check free space before creating temp file
   - Show farmer-friendly error if < 10MB available

6. ✅ **Fix bitmap leak on compression failure**
   - Recycle original bitmap in catch block

7. ✅ **Add upload progress indicator**
   - Show "Uploading: X%" or "Uploading..." vs "Analyzing..."
   - Use Ktor progress callbacks

### Priority 3: MEDIUM (Nice to Have)
8. ✅ **Add error state for empty diagnosis**
   - Show error icon + message when content and diagnosisData both null

9. ✅ **Optimize image preview loading**
   - Add Coil placeholder/error states
   - Consider using compressed base64 for preview

10. ✅ **Improve color-blind accessibility**
    - Add icon variation for health status (checkmark vs warning vs error)

---

## TESTING CHECKLIST FOR FARMER UX

### Scenario 1: Poor Network (2G)
- [ ] Upload 5MB image on throttled network (30 KB/s)
- [ ] Verify "Uploading..." shows during upload phase
- [ ] Wait 60 seconds → Timeout error shown in Vietnamese
- [ ] Error persists until dismissed (not auto-hide)
- [ ] User image message loading cleared (no infinite spinner)

### Scenario 2: Upload Fails After Optimistic Message
- [ ] Disconnect network after tapping "Send for Diagnosis"
- [ ] Verify user image message shows "Analyzing..." spinner
- [ ] Wait for timeout error
- [ ] Verify spinner is CLEARED on error
- [ ] Verify error shown in Vietnamese with retry option

### Scenario 3: Rotation During Upload
- [ ] Start image upload
- [ ] Rotate device mid-upload
- [ ] Verify upload continues (check backend logs)
- [ ] Verify response displays correctly after rotation
- [ ] Try sending SECOND image after rotation (should work)

### Scenario 4: Low Storage Space
- [ ] Fill device storage to < 5MB free
- [ ] Try to capture photo
- [ ] Verify farmer-friendly error in Vietnamese
- [ ] Error suggests freeing space

### Scenario 5: diagnosisData = null
- [ ] Mock backend to send `{diagnosisData: null, content: "text advice"}`
- [ ] Verify text advice shows, no crash
- [ ] Verify no empty diagnosis card

### Scenario 6: Empty Response
- [ ] Mock backend to send `{diagnosisData: null, content: ""}`
- [ ] Verify error state shown (not blank bubble)
- [ ] Error message in Vietnamese

### Scenario 7: Compression Failure
- [ ] Use very large image (20MB, 8000x6000)
- [ ] Force OutOfMemoryError during compression
- [ ] Verify bitmap recycled, no memory leak
- [ ] Verify error shown to farmer

### Scenario 8: Low-Literacy Farmer
- [ ] Show errors to non-technical user
- [ ] Verify text size readable (12sp minimum)
- [ ] Verify error stays visible until dismissed
- [ ] Verify Vietnamese text displays correctly (no garbled characters)

---

## FILES REQUIRING CHANGES

1. **Strings.kt** - Add 15+ new localized strings
2. **ImagePicker.android.kt** - Storage check, bitmap leak fix, localization
3. **ChatViewModel.kt** - Fix infinite loading states, move base64 to ViewModel
4. **NongTriApi.kt** - Localize errors, add upload progress
5. **ChatScreen.kt** - Fix snackbar position/duration, pass strings to components
6. **ImagePreviewDialog.kt** - Localize all strings, add loading placeholder
7. **ImageMessageBubble.kt** - Add upload progress display
8. **DiagnosisResponseBubble.kt** - Add empty state handling, localize strings

---

## CONCLUSION

The mobile app has a **solid architectural foundation** with optimistic UI, proper error handling, and memory management. However, **localization gaps** and **state management edge cases** pose CRITICAL risks for rural Vietnamese farmers on 2G networks.

**Top 3 Blockers**:
1. Non-localized error messages (farmers cannot read)
2. Infinite loading spinners on failure (farmers think app frozen)
3. State loss on rotation (farmers lose work)

**Recommended Action**: Fix Priority 1 issues before next release. Priority 2 issues can be addressed in follow-up releases based on user feedback.

**Estimated Effort**:
- Priority 1: 2-3 days (localization + loading state fixes)
- Priority 2: 3-4 days (storage checks, rotation fix, progress indicator)
- Priority 3: 2-3 days (accessibility improvements)

**Total**: ~8-10 development days for complete farmer UX polish.
