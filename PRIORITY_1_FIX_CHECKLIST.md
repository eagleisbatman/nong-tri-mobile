# Priority 1 Fixes - Developer Checklist
**MUST BE COMPLETED BEFORE RELEASE**
**Estimated Time**: 2-3 days for 1 developer

---

## ✅ TASK 1: Localize All Hardcoded Strings (4-5 hours)

### 1.1 Add New Strings to Strings.kt (1 hour)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/l10n/Strings.kt`

```kotlin
interface Strings {
    // ... existing strings ...

    // Image diagnosis UI
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

    // Image picker errors
    val errorImageAccess: String
    val errorImageCorrupted: String
    val errorImageTooLarge: String
    val errorLowStorage: String
    val errorCannotCreateFolder: String

    // Upload/network errors
    val errorUploadTimeout: String
    val errorNoInternet: String
    val errorUploadFailed: String
    val errorConnectionLost: String

    // Diagnosis errors
    val errorDiagnosisFailed: String
    val errorNoDiagnosisData: String
}

// English implementation
object EnglishStrings : Strings {
    // ... existing strings ...

    override val confirmImage = "Confirm Image"
    override val askPlantQuestion = "Ask a question about your plant:"
    override val defaultPlantQuestion = "How is the health of my crop?"
    override val sendForDiagnosis = "Send for Diagnosis"
    override val aiWillAnalyze = "The AI will analyze your plant image and provide health diagnosis with treatment recommendations."
    override val analyzing = "Analyzing..."
    override val analyzingPlantHealth = "Analyzing plant health..."
    override val detectedIssues = "Detected Issues:"
    override val growthStage = "Growth Stage"
    override val listenToAdvice = "Listen to advice"

    override val errorImageAccess = "Cannot access image file. Please try another image."
    override val errorImageCorrupted = "Cannot read image. The file may be corrupted or in an unsupported format."
    override val errorImageTooLarge = "Image is too large ({size}MB). Please try a smaller image."
    override val errorLowStorage = "Not enough storage space. Please free up at least 10MB."
    override val errorCannotCreateFolder = "Cannot create image folder. Please check app permissions."

    override val errorUploadTimeout = "Upload timed out. This may be due to slow internet. Please try a smaller image or wait and try again."
    override val errorNoInternet = "No internet connection. Please check your network and try again."
    override val errorUploadFailed = "Upload failed. Please try again."
    override val errorConnectionLost = "Connection lost. Please check your internet connection."

    override val errorDiagnosisFailed = "Unable to analyze image. Please try again with a clearer photo."
    override val errorNoDiagnosisData = "No diagnosis data available. Please try again."
}

// Vietnamese implementation
object VietnameseStrings : Strings {
    // ... existing strings ...

    override val confirmImage = "Xác nhận hình ảnh"
    override val askPlantQuestion = "Hỏi một câu hỏi về cây trồng của bạn:"
    override val defaultPlantQuestion = "Sức khỏe của cây trồng của tôi như thế nào?"
    override val sendForDiagnosis = "Gửi để chẩn đoán"
    override val aiWillAnalyze = "AI sẽ phân tích hình ảnh cây trồng của bạn và cung cấp chẩn đoán sức khỏe với khuyến nghị điều trị."
    override val analyzing = "Đang phân tích..."
    override val analyzingPlantHealth = "Đang phân tích sức khỏe cây trồng..."
    override val detectedIssues = "Vấn đề phát hiện:"
    override val growthStage = "Giai đoạn sinh trưởng"
    override val listenToAdvice = "Nghe lời khuyên"

    override val errorImageAccess = "Không thể truy cập tệp hình ảnh. Vui lòng chọn hình ảnh khác."
    override val errorImageCorrupted = "Không thể đọc hình ảnh. Tệp có thể bị hỏng hoặc định dạng không được hỗ trợ."
    override val errorImageTooLarge = "Hình ảnh quá lớn ({size}MB). Vui lòng thử hình ảnh nhỏ hơn."
    override val errorLowStorage = "Không đủ dung lượng lưu trữ. Vui lòng giải phóng ít nhất 10MB."
    override val errorCannotCreateFolder = "Không thể tạo thư mục hình ảnh. Vui lòng kiểm tra quyền ứng dụng."

    override val errorUploadTimeout = "Tải lên hết thời gian. Điều này có thể do mạng chậm. Vui lòng thử hình ảnh nhỏ hơn hoặc đợi và thử lại."
    override val errorNoInternet = "Không có kết nối internet. Vui lòng kiểm tra mạng của bạn và thử lại."
    override val errorUploadFailed = "Tải lên thất bại. Vui lòng thử lại."
    override val errorConnectionLost = "Mất kết nối. Vui lòng kiểm tra kết nối internet của bạn."

    override val errorDiagnosisFailed = "Không thể phân tích hình ảnh. Vui lòng thử lại với ảnh rõ hơn."
    override val errorNoDiagnosisData = "Không có dữ liệu chẩn đoán. Vui lòng thử lại."
}
```

**Test**:
```bash
# Verify Vietnamese characters display correctly
./gradlew assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
# Open app, switch to Vietnamese, trigger errors
```

---

### 1.2 Update ImagePicker.android.kt (30 min)

**File**: `/mobile/composeApp/src/androidMain/kotlin/com/nongtri/app/platform/ImagePicker.android.kt`

**Changes**:
```kotlin
// Add strings parameter
actual class ImagePicker(
    private val context: Context,
    private val strings: Strings  // ← ADD THIS
)

// Line 149: Replace hardcoded error
return ImagePickerResult(
    uri = uri.toString(),
    base64Data = null,
    error = strings.errorImageAccess  // ← CHANGED
)

// Line 165: Replace hardcoded error
return ImagePickerResult(
    uri = uri.toString(),
    base64Data = null,
    error = strings.errorImageCorrupted  // ← CHANGED
)

// createTempImageFile: Add storage check
private fun createTempImageFile(): File {
    try {
        val storageDir = File(context.cacheDir, "shared_images")

        // Check available space
        if (storageDir.freeSpace < 10 * 1024 * 1024) {
            throw IOException(strings.errorLowStorage)  // ← NEW
        }

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw IOException(strings.errorCannotCreateFolder)  // ← NEW
        }

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    } catch (e: IOException) {
        throw Exception(e.message ?: strings.errorLowStorage)
    }
}
```

**Update rememberImagePicker**:
```kotlin
@Composable
actual fun rememberImagePicker(strings: Strings): ImagePicker {  // ← ADD PARAM
    val context = LocalContext.current
    val imagePicker = remember { ImagePicker(context, strings) }
    // ... rest
}
```

---

### 1.3 Update ChatScreen.kt to Pass Strings (30 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/screens/ChatScreen.kt`

**Changes**:
```kotlin
// Line 50: Pass strings to imagePicker
val imagePicker = com.nongtri.app.platform.rememberImagePicker(strings)  // ← ADD strings

// Line 671-675: Use localized error
coroutineScope.launch {
    snackbarHostState.showSnackbar(
        message = result?.error ?: strings.errorImageCorrupted,  // ← CHANGED
        duration = SnackbarDuration.Indefinite,  // ← CHANGED
        actionLabel = strings.ok  // ← CHANGED
    )
}

// Line 698-702: Use localized error
coroutineScope.launch {
    snackbarHostState.showSnackbar(
        message = result.error ?: strings.errorImageCorrupted,  // ← CHANGED
        duration = SnackbarDuration.Indefinite,  // ← CHANGED
        actionLabel = strings.ok  // ← CHANGED
    )
}

// Line 730-734: Use localized error
coroutineScope.launch {
    snackbarHostState.showSnackbar(
        message = strings.errorImageCorrupted,  // ← CHANGED
        duration = SnackbarDuration.Indefinite,  // ← CHANGED
        actionLabel = strings.ok  // ← CHANGED
    )
}
```

---

### 1.4 Update ImagePreviewDialog.kt (30 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/components/ImagePreviewDialog.kt`

**Changes**:
```kotlin
@Composable
fun ImagePreviewDialog(
    imageUri: String,
    onDismiss: () -> Unit,
    onConfirm: (question: String) -> Unit,
    strings: Strings,  // ← ADD THIS
    modifier: Modifier = Modifier
) {
    var question by remember { mutableStateOf(strings.defaultPlantQuestion) }  // ← CHANGED

    Dialog(...) {
        Column {
            TopAppBar(
                title = {
                    Text(
                        strings.confirmImage,  // ← CHANGED
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                // ... rest
            )

            // ... image preview ...

            Surface {
                Column {
                    Text(
                        text = strings.askPlantQuestion,  // ← CHANGED
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = question,
                        onValueChange = { question = it },
                        placeholder = { Text(strings.defaultPlantQuestion) },  // ← CHANGED
                        // ... rest
                    )

                    // ... buttons ...

                    Button(
                        onClick = {
                            val finalQuestion = question.ifBlank { strings.defaultPlantQuestion }  // ← CHANGED
                            onConfirm(finalQuestion)
                        },
                        // ... rest
                    ) {
                        // ... icon ...
                        Text(strings.sendForDiagnosis)  // ← CHANGED
                    }

                    Text(
                        text = strings.aiWillAnalyze,  // ← CHANGED
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

**Update ChatScreen.kt call**:
```kotlin
// Line 716: Pass strings
ImagePreviewDialog(
    imageUri = selectedImageUri!!,
    onDismiss = { ... },
    onConfirm = { ... },
    strings = strings  // ← ADD THIS
)
```

---

### 1.5 Update Other Components (1 hour)

**ImageMessageBubble.kt**:
```kotlin
// Line 93
Text(
    strings.analyzing,  // ← CHANGED from "Analyzing..."
    color = Color.White,
    // ... rest
)
```

**DiagnosisResponseBubble.kt**:
```kotlin
// Line 113
Text(
    text = strings.detectedIssues,  // ← CHANGED
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.onSecondaryContainer
)

// Line 159
Text(
    text = "${strings.growthStage}: $stage",  // ← CHANGED
    style = MaterialTheme.typography.bodySmall,
    // ... rest
)

// Line 193
Text(
    text = strings.analyzingPlantHealth,  // ← CHANGED
    style = MaterialTheme.typography.bodyMedium,
    // ... rest
)

// Line 216
Text(strings.listenToAdvice)  // ← CHANGED
```

**Pass strings to components in ChatScreen.kt**:
```kotlin
// Line 486-491
ImageMessageBubble(
    message = message,
    onImageClick = { imageUrl -> showFullscreenImage = imageUrl },
    strings = strings  // ← ADD THIS
)

// Line 497-508
DiagnosisResponseBubble(
    message = message,
    onTtsClick = { ... },
    strings = strings  // ← ADD THIS
)
```

---

### 1.6 Update NongTriApi.kt (1 hour)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/data/api/NongTriApi.kt`

**Changes**:
```kotlin
class NongTriApi(
    private val baseUrl: String = BuildConfig.API_URL,
    private val strings: Strings  // ← ADD THIS (inject via DI or singleton)
)

// Line 288-301: Replace hardcoded errors
val userMessage = when {
    e::class.simpleName?.contains("Timeout", ignoreCase = true) == true ||
    e.message?.contains("timeout", ignoreCase = true) == true ->
        strings.errorUploadTimeout  // ← CHANGED

    e.message?.contains("host", ignoreCase = true) == true ||
    e.message?.contains("network", ignoreCase = true) == true ->
        strings.errorNoInternet  // ← CHANGED

    e.message?.contains("connection", ignoreCase = true) == true ->
        strings.errorConnectionLost  // ← CHANGED

    else ->
        strings.errorUploadFailed  // ← CHANGED
}
```

**Update instantiation in ChatViewModel**:
```kotlin
class ChatViewModel(
    private val api: NongTriApi = NongTriApi(strings = LocalizationProvider.getStrings(Language.VIETNAMESE))  // TODO: Get from user prefs
) : ViewModel()
```

---

### 1.7 Update ChatViewModel.kt (30 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/viewmodel/ChatViewModel.kt`

**Changes**:
```kotlin
// Line 598: Use localized error
_uiState.update { state ->
    state.copy(
        error = strings.errorImageTooLarge.replace("{size}", String.format("%.1f", estimatedSizeMB)),  // ← CHANGED
        isLoading = false
    )
}
```

**Note**: ChatViewModel needs access to Strings. Options:
1. Pass via constructor (dependency injection)
2. Use singleton LocalizationProvider with language from UserPreferences
3. Make error messages parameters to sendImageDiagnosis()

**Recommended**: Option 2 (use UserPreferences to get language, then get strings)

---

**TOTAL TIME FOR TASK 1**: 4-5 hours

---

## ✅ TASK 2: Fix Infinite Loading Spinner (30 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/viewmodel/ChatViewModel.kt`

### 2.1 Add Helper Method (5 min)

```kotlin
/**
 * Clear loading state on user's image message
 * Called when upload fails or metadata arrives without chunks
 */
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
```

### 2.2 Update onFailure Handler (10 min)

**Location**: Line 715-724

```kotlin
onFailure = { error ->
    // Clear loading state on user's image message
    clearImageMessageLoading()  // ← ADD THIS

    // Remove the placeholder message and show error
    println("[ImageDiagnosis] ✗ Error: ${error.message}")
    _uiState.update { state ->
        state.copy(
            messages = state.messages.filter { it.id != assistantMessageId },
            isLoading = false,
            error = error.message ?: "Failed to analyze image"
        )
    }
}
```

### 2.3 Update onMetadata Handler (10 min)

**Location**: Line 676-697

```kotlin
onMetadata = { metadata ->
    // ALWAYS clear user image loading when metadata arrives
    // (even if no chunks sent)
    clearImageMessageLoading()  // ← ADD THIS

    // Update the assistant message with metadata
    println("[ImageDiagnosis] Metadata received: conversationId=${metadata.conversationId}, hasDiagnosisData=${metadata.diagnosisData != null}")
    _uiState.update { state ->
        state.copy(
            messages = state.messages.map { msg ->
                if (msg.id == assistantMessageId) {
                    msg.copy(
                        responseType = metadata.responseType,
                        followUpQuestions = metadata.followUpQuestions,
                        isGenericResponse = metadata.isGenericResponse,
                        language = metadata.language,
                        conversationId = metadata.conversationId,
                        diagnosisData = metadata.diagnosisData
                    )
                } else {
                    msg
                }
            }
        )
    }
}
```

### 2.4 Remove Redundant Code (5 min)

**Delete**: Lines 642-661 (firstChunkReceived logic is now redundant)

```kotlin
// DELETE THIS ENTIRE BLOCK:
var firstChunkReceived = false

onChunk = { chunk ->
    // On first chunk, clear loading state on user's image message
    if (!firstChunkReceived) {
        firstChunkReceived = true
        _uiState.update { ... }  // DELETE
    }

    // Keep only this:
    _uiState.update { state ->
        state.copy(
            messages = state.messages.map { msg ->
                if (msg.id == assistantMessageId) {
                    msg.copy(content = msg.content + chunk)
                } else {
                    msg
                }
            }
        )
    }
}
```

**Test**:
```bash
# Simulate timeout
adb shell settings put global captive_portal_detection_enabled 0
# Open app, upload image, disconnect WiFi after tapping "Send"
# Expected: Spinner clears after 60s timeout, error shown
```

---

## ✅ TASK 3: Fix Snackbar Duration & Position (1 hour)

### 3.1 Increase Snackbar Duration (30 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/screens/ChatScreen.kt`

**Option A: Indefinite with Dismiss** (Recommended)
```kotlin
// Lines 671-675, 698-702, 730-734
coroutineScope.launch {
    snackbarHostState.showSnackbar(
        message = errorMessage,
        duration = SnackbarDuration.Indefinite,  // ← CHANGED
        actionLabel = strings.ok  // ← ADD THIS
    )
}
```

**Option B: AlertDialog for Critical Errors** (More invasive)
```kotlin
// Add state
var showErrorDialog by remember { mutableStateOf(false) }
var errorDialogMessage by remember { mutableStateOf("") }

// On error
if (result == null || result.base64Data == null) {
    errorDialogMessage = result?.error ?: strings.errorImageCorrupted
    showErrorDialog = true
}

// Render dialog
if (showErrorDialog) {
    AlertDialog(
        onDismissRequest = { showErrorDialog = false },
        title = { Text(strings.error) },
        text = { Text(errorDialogMessage) },
        confirmButton = {
            TextButton(onClick = { showErrorDialog = false }) {
                Text(strings.ok)
            }
        }
    )
}
```

### 3.2 Fix Snackbar Position (30 min)

**Location**: Lines 150-152

```kotlin
snackbarHost = {
    // Position snackbar ABOVE bottom bar to prevent obscuring
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 80.dp)  // ← ADD: Above 72dp input bar + 8dp margin
    ) {
        SnackbarHost(hostState = snackbarHostState)
    }
}
```

**Test**:
```bash
# Open app in Vietnamese
# Trigger camera error (deny permission then accept)
# Expected: Error stays visible until dismissed, positioned above input bar
```

---

## ✅ TASK 4: Fix State Loss on Rotation (1 hour)

### 4.1 Add ViewModel State (15 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/viewmodel/ChatViewModel.kt`

```kotlin
class ChatViewModel(
    private val api: NongTriApi = NongTriApi()
) : ViewModel() {

    // ... existing state ...

    // Temporary storage for image base64 during preview/upload
    // Survives rotation (ViewModel is not recreated)
    private var pendingImageBase64: String? = null

    fun setPendingImageData(base64: String) {
        pendingImageBase64 = base64
    }

    fun getPendingImageData(): String? {
        return pendingImageBase64
    }

    fun clearPendingImageData() {
        pendingImageBase64 = null
    }
}
```

### 4.2 Update ChatScreen.kt (45 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/screens/ChatScreen.kt`

**Remove**:
```kotlin
// DELETE Line 75:
// var selectedImageBase64 by remember { mutableStateOf<String?>(null) }
```

**Update camera callback** (Line 660-679):
```kotlin
imagePicker.launchCamera { result ->
    if (result != null && result.base64Data != null) {
        println("[ChatScreen] Camera image captured: ${result.width}x${result.height}, ${result.sizeBytes / 1024}KB")
        selectedImageUri = result.uri
        viewModel.setPendingImageData(result.base64Data)  // ← CHANGED: Store in ViewModel
        showImagePreviewDialog = true
    } else {
        // ... error handling ...
    }
    isImageProcessing = false
}
```

**Update gallery callback** (Line 684-707):
```kotlin
imagePicker.launchGallery { result ->
    if (result != null && result.base64Data != null) {
        println("[ChatScreen] Gallery image selected: ${result.width}x${result.height}, ${result.sizeBytes / 1024}KB")
        selectedImageUri = result.uri
        viewModel.setPendingImageData(result.base64Data)  // ← CHANGED: Store in ViewModel
        showImagePreviewDialog = true
    } else {
        // ... error handling ...
    }
    isImageProcessing = false
}
```

**Update preview onConfirm** (Line 724-760):
```kotlin
onConfirm = { question ->
    // Retrieve base64 from ViewModel (survives rotation)
    val base64Data = viewModel.getPendingImageData()  // ← CHANGED
    if (base64Data == null) {
        println("[ChatScreen] Error: base64Data is null, cannot send image")
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = strings.errorImageCorrupted,
                duration = SnackbarDuration.Indefinite,
                actionLabel = strings.ok
            )
        }
        showImagePreviewDialog = false
        selectedImageUri = null
        viewModel.clearPendingImageData()  // ← ADD: Cleanup
        return@ImagePreviewDialog
    }

    println("[ChatScreen] Sending image for diagnosis: $question")

    // Show optimistic user message
    viewModel.showOptimisticImageMessage(
        imageData = selectedImageUri!!,
        question = question
    )

    // Send image to backend
    viewModel.sendImageDiagnosis(
        imageData = base64Data,
        question = question
    )

    // Cleanup
    showImagePreviewDialog = false
    selectedImageUri = null
    viewModel.clearPendingImageData()  // ← CHANGED: Clear from ViewModel
    isImageProcessing = false
}
```

**Test**:
```bash
# Open app, capture image, confirm preview
# Rotate device before upload completes
# Wait for response
# Try to send ANOTHER image (should work)
```

---

## ✅ TASK 5: Fix Bitmap Memory Leak (5 min)

**File**: `/mobile/composeApp/src/androidMain/kotlin/com/nongtri/app/platform/ImagePicker.android.kt`

**Location**: Lines 248-254

```kotlin
} catch (e: Exception) {
    // Clean up ALL bitmaps if compression failed
    if (scaledBitmap != bitmap) {
        bitmap.recycle()        // ← ADD THIS
        scaledBitmap.recycle()
    } else {
        bitmap.recycle()        // ← ADD THIS (if no scaling occurred)
    }
    throw e
}
```

**Test**:
```bash
# Use huge image (20MB, 8000x6000)
# Force OutOfMemoryError during compression (fill memory)
# Retry 3 times
# Expected: No crash (bitmaps recycled)
```

---

## ✅ TASK 6: Add Empty Diagnosis Error State (30 min)

**File**: `/mobile/composeApp/src/commonMain/kotlin/com/nongtri/app/ui/components/DiagnosisResponseBubble.kt`

**Location**: Lines 176-198

```kotlin
Surface {
    Column {
        if (message.content.isNotBlank()) {
            Text(text = message.content, ...)
        } else if (message.isLoading) {
            // Loading indicator
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(...)
                Spacer(...)
                Text(text = strings.analyzingPlantHealth, ...)
            }
        } else if (message.diagnosisData == null && message.content.isBlank()) {
            // ← ADD THIS: Error state when both are null
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = strings.errorDiagnosisFailed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // TTS button (existing code)
        // ...
    }
}
```

**Test**:
```bash
# Mock backend to return: {diagnosisData: null, content: ""}
# Expected: Error icon + message shown, not blank bubble
```

---

## ✅ TASK 7: Add Storage Space Check (30 min)

**Already implemented in TASK 1.2** (ImagePicker.android.kt, createTempImageFile)

**Additional**: Add error handling in ChatScreen.kt for storage errors

```kotlin
// In camera/gallery callbacks
if (result == null || result.base64Data == null) {
    val errorMessage = result?.error ?: strings.errorImageCorrupted

    // Check if it's a storage error
    if (errorMessage.contains("storage", ignoreCase = true) ||
        errorMessage.contains("space", ignoreCase = true)) {
        // Show more prominent error for storage issues
        errorDialogMessage = errorMessage
        showErrorDialog = true  // Use AlertDialog instead of Snackbar
    } else {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Indefinite,
                actionLabel = strings.ok
            )
        }
    }
}
```

---

## FINAL CHECKLIST

- [ ] **TASK 1**: All 15+ strings localized in Strings.kt ✅
- [ ] **TASK 1**: ImagePicker uses localized errors ✅
- [ ] **TASK 1**: ChatScreen passes strings to all components ✅
- [ ] **TASK 1**: ImagePreviewDialog fully localized ✅
- [ ] **TASK 1**: DiagnosisResponseBubble fully localized ✅
- [ ] **TASK 1**: NongTriApi uses localized errors ✅
- [ ] **TASK 1**: ChatViewModel uses localized errors ✅
- [ ] **TASK 2**: clearImageMessageLoading() method added ✅
- [ ] **TASK 2**: onFailure calls clearImageMessageLoading() ✅
- [ ] **TASK 2**: onMetadata calls clearImageMessageLoading() ✅
- [ ] **TASK 2**: firstChunkReceived logic removed ✅
- [ ] **TASK 3**: Snackbar duration changed to Indefinite ✅
- [ ] **TASK 3**: Snackbar positioned above input bar ✅
- [ ] **TASK 4**: ViewModel has pendingImageBase64 property ✅
- [ ] **TASK 4**: ChatScreen stores base64 in ViewModel ✅
- [ ] **TASK 4**: Preview retrieves base64 from ViewModel ✅
- [ ] **TASK 4**: Cleanup calls clearPendingImageData() ✅
- [ ] **TASK 5**: Bitmap recycled in compression catch block ✅
- [ ] **TASK 6**: Empty diagnosis shows error state ✅
- [ ] **TASK 7**: Storage space checked before createTempFile ✅

---

## TESTING

### Manual Testing (1 hour)
```bash
# Build and install
cd /Users/eagleisbatman/nong_tri_workspace/mobile
./gradlew assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# Test 1: Upload timeout (Vietnamese)
1. Open app, switch to Vietnamese
2. Throttle network to 2G (Chrome DevTools)
3. Capture image, confirm
4. Disconnect WiFi after 10 seconds
5. ✅ Expected: Spinner clears after timeout, error in Vietnamese, stays visible

# Test 2: Rotation during upload
1. Capture image, confirm
2. Rotate device during upload
3. Wait for response
4. Capture ANOTHER image (should work)
5. ✅ Expected: Both uploads succeed, no state loss

# Test 3: Low storage
1. Fill device storage to < 5MB
2. Try to capture image
3. ✅ Expected: Friendly error in Vietnamese: "Không đủ dung lượng..."

# Test 4: Empty diagnosis
1. Mock backend: {diagnosisData: null, content: ""}
2. ✅ Expected: Error state shown, not blank bubble

# Test 5: Compression failure (memory leak)
1. Use 20MB image on low-RAM device
2. Retry 3 times
3. ✅ Expected: No crash (bitmaps recycled)
```

### Automated Tests (Optional)
```kotlin
// ChatViewModelTest.kt
@Test
fun `upload failure clears user message loading`() {
    // Mock API to fail
    // Call sendImageDiagnosis()
    // Assert user message isLoading = false
}

@Test
fun `rotation preserves pending image data`() {
    // Set pendingImageBase64
    // Simulate rotation (recreate ViewModel? No, survives)
    // Assert getPendingImageData() returns data
}
```

---

## DEPLOYMENT

```bash
# 1. Run tests
./gradlew test

# 2. Build release
./gradlew assembleRelease

# 3. Sign APK (if needed)
# ...

# 4. Deploy to Railway (backend should already be deployed)
# Mobile app: Upload to Play Store Internal Testing

# 5. Monitor metrics
# - Upload success rate (should be > 95%)
# - Error frequency by type
# - Session duration (should increase)
# - Language distribution (should be ~95% Vietnamese)
```

---

## ROLLBACK PLAN

If critical bug discovered post-deployment:

1. **Revert to previous APK** (Play Store rollback)
2. **Hotfix branch**:
   ```bash
   git checkout e4affde  # Last known good commit
   git checkout -b hotfix/farmer-ux-fixes
   # Cherry-pick only stable fixes
   git cherry-pick <commit-hash>
   ```
3. **Emergency backend fix** (if needed):
   - Add feature flag to disable image diagnosis
   - Send degraded response without AgriVision call

---

**TOTAL ESTIMATED TIME**: 7-8 hours (1 day for experienced developer)
**RISK LEVEL**: 🟢 LOW (surgical changes, high test coverage)
**SHIP CONFIDENCE**: 🟢 HIGH (fixes verified issues, low regression risk)
