# Mobile Image Diagnosis Implementation Checklist

**Status**: 🚧 IN PROGRESS
**Created**: October 21, 2025
**Based On**: IMAGE_DIAGNOSIS_STRATEGY.md v1.0 (FROZEN)

---

## ✅ Completion Criteria

This implementation is **COMPLETE** when:
- [ ] All 35 checklist items below are ✅ checked
- [ ] All edge cases are handled
- [ ] End-to-end flow tested on physical device
- [ ] No crashes or ANR errors
- [ ] Follows existing mobile app patterns (voice, location, TTS)

---

## 📋 Implementation Checklist

### **Phase 1: Permission Handling** (6 items)

- [ ] **1.1** Create `ImagePermissionViewModel.kt`
  - State: `hasCameraPermission`, `hasStoragePermission`, `shouldShowSettings`
  - Methods: `checkPermissionState()`, `requestCameraPermission()`, `requestStoragePermission()`, `openSettings()`
  - **Pattern**: Follow `VoicePermissionViewModel.kt` exactly
  - **Acceptance**: ViewModel compiles, state flows correctly, permission requests work

- [ ] **1.2** Create `ImagePermissionBottomSheet.kt`
  - Shows "Camera Permission" or "Storage Permission" header
  - Explanation: "Take photos of your crops for AI health diagnosis"
  - Button: "Grant Permission" or "Open Settings" (conditional)
  - **Pattern**: Follow `VoicePermissionBottomSheet.kt` design
  - **Acceptance**: Bottom sheet displays, dismisses on permission grant, opens settings when needed

- [ ] **1.3** Add Android permissions to `AndroidManifest.xml`
  - `android.permission.CAMERA`
  - `android.permission.READ_MEDIA_IMAGES` (Android 13+)
  - `android.permission.READ_EXTERNAL_STORAGE` (Android 12-)
  - **Acceptance**: Manifest includes all required permissions

- [ ] **1.4** Add permission types to `PermissionType` enum
  - `CAMERA`
  - `READ_MEDIA_IMAGES`
  - **Acceptance**: Permission types compile, PermissionHandler recognizes them

- [ ] **1.5** Integrate permission flow in `ChatScreen.kt`
  - Show permission bottom sheet when camera icon tapped without permission
  - Auto-check permission state while bottom sheet visible (500ms interval)
  - Auto-dismiss bottom sheet when permission granted
  - Auto-open image source selector after permission granted
  - **Acceptance**: Permission flow works smoothly, no manual dismissal needed

- [ ] **1.6** Handle "Open Settings" flow
  - Detect when user returns from settings
  - Auto-check permission state on resume
  - Auto-proceed to image source selector if granted
  - **Acceptance**: User can grant permission from settings and continue seamlessly

---

### **Phase 2: Image Source Selection & Capture** (7 items)

- [ ] **2.1** Create `ImageSourceBottomSheet.kt`
  - Two options: "Take Photo" (camera) and "Choose from Gallery" (gallery)
  - Material Design 3 cards with icons and descriptions
  - **Pattern**: Same bottom sheet style as permission sheet
  - **Acceptance**: Bottom sheet displays both options, dismisses on selection

- [ ] **2.2** Implement camera capture intent
  - Use `ActivityResultContract` for camera
  - Save photo to temporary file
  - Return URI to `ChatScreen`
  - **Acceptance**: Camera opens, photo captured, URI returned

- [ ] **2.3** Implement gallery selection intent
  - Use `ActivityResultContract` for gallery
  - Filter to images only (JPEG, PNG, WebP)
  - Return URI to `ChatScreen`
  - **Acceptance**: Gallery opens, image selected, URI returned

- [ ] **2.4** Create `ImageValidator.kt`
  - Constants: `MAX_SIZE_MB = 5`, `TARGET_SIZE_MB = 2`, `MIN_DIMENSION = 200`, `MAX_DIMENSION = 4096`
  - Method: `validateImage(uri, context)` returns `ValidationResult`
  - Checks: format, dimensions, file size
  - **Acceptance**: Validator correctly accepts valid images, rejects invalid ones

- [ ] **2.5** Implement image compression
  - Method: `compressImage(bitmap, targetSizeMB)` in `ImageValidator`
  - Algorithm: Iteratively reduce JPEG quality from 90% until size ≤ target
  - Minimum quality: 50%
  - **Acceptance**: Images > 2MB compressed to < 2MB, quality maintained

- [ ] **2.6** Handle camera/gallery errors
  - No camera available → Show "No camera found" error
  - Gallery permission denied → Show permission sheet
  - Image file not found → Show "Failed to load image" error
  - **Acceptance**: All error cases handled gracefully with user-friendly messages

- [ ] **2.7** Add camera icon to `WhatsAppStyleInputBar`
  - Location: Left of microphone icon
  - Visibility: Only when `value.isBlank()` (hide when typing)
  - Icon: `Icons.Outlined.CameraAlt`
  - Enabled state: Disabled during loading or voice recording
  - **Acceptance**: Camera icon appears, hides when typing, responds to taps

---

### **Phase 3: Image Preview & Confirmation** (6 items)

- [ ] **3.1** Create `ImagePreviewDialog.kt`
  - Full-screen dialog with top bar ("Plant Diagnosis" title, close button)
  - Zoomable image preview (middle section)
  - Default question chip: "How is the health of my crop?"
  - Optional text input with voice button
  - "Analyze Plant" confirmation button
  - **Acceptance**: Dialog displays image, default question, text input, and confirm button

- [ ] **3.2** Implement zoomable image
  - Use `AsyncImage` with `contentScale = ContentScale.Fit`
  - Add pinch-to-zoom gesture (optional for v1)
  - **Acceptance**: Image loads and displays correctly, fills available space

- [ ] **3.3** Implement default question logic
  - Method: `getDefaultDiagnosisQuestion(language)`
  - English: "How is the health of my crop?"
  - Vietnamese: "Cây trồng của tôi có khỏe không?"
  - **Acceptance**: Default question displays in correct language

- [ ] **3.4** Implement text input extension
  - User can type additional details
  - Placeholder: "Add more details (optional)"
  - Final question: `"$defaultQuestion $additionalText"`
  - **Acceptance**: User can type, text appended to default question

- [ ] **3.5** Implement voice input extension
  - Mic button in text field trailing icon
  - Starts voice recording when tapped
  - Shows recording indicator (red icon or animation)
  - Stops recording and transcribes on second tap
  - Appends transcription to text field
  - **Acceptance**: Voice recording works, transcription appends to text

- [ ] **3.6** Implement confirm/cancel actions
  - Cancel: Dismiss dialog, discard image
  - Confirm: Compose final question, call `uploadImage(uri, question)`
  - **Acceptance**: Cancel discards, confirm triggers upload

---

### **Phase 4: Image Upload & Diagnosis Flow** (8 items)

- [ ] **4.1** Create `uploadImage()` method in `ChatViewModel`
  - Convert URI to Base64
  - Add optimistic user message to UI
  - Call `repository.uploadImageForDiagnosis(imageUri, question)`
  - Update message with server response
  - **Acceptance**: Upload method works, optimistic UI updates correctly

- [ ] **4.2** Implement Base64 conversion
  - Read image from URI
  - Compress if > 2MB
  - Convert to Base64 data URL: `data:image/jpeg;base64,...`
  - **Acceptance**: Base64 conversion works, data URL format correct

- [ ] **4.3** Create `uploadImageForDiagnosis()` in repository
  - POST to `/api/chat/stream` with `imageData`, `message`, `userId`, `messageType: "image"`
  - **Acceptance**: API request sends correct payload

- [ ] **4.4** Implement upload progress indicator
  - States: `IDLE`, `COMPRESSING`, `UPLOADING`, `ANALYZING`, `COMPLETE`, `FAILED`
  - Show `LinearProgressIndicator` during upload
  - Show "Uploading..." text
  - **Acceptance**: Progress indicator displays during upload, updates correctly

- [ ] **4.5** Handle SSE streaming response
  - Parse SSE chunks: `data: {"type": "content", "text": "..."}`
  - Stream content to UI (append to assistant message)
  - Handle metadata chunks (diagnosis_data)
  - Handle "done" and "error" chunks
  - **Acceptance**: SSE streaming works, response displays in real-time

- [ ] **4.6** Implement upload error handling
  - Network error → Show retry dialog
  - Timeout (60s) → Show "Upload timed out" error
  - Server error (400/500) → Show error message from backend
  - MinIO error → Show "Failed to save image" error
  - **Acceptance**: All error types handled with retry option

- [ ] **4.7** Implement retry mechanism
  - Store image URI and question for retry
  - Show "Retry" button on error
  - Re-attempt upload with same data
  - **Acceptance**: Retry works, user can retry failed uploads

- [ ] **4.8** Handle partial response (connection lost during streaming)
  - If connection lost mid-stream, save partial response
  - Show warning: "Connection lost. This is a partial response."
  - Show retry button
  - **Acceptance**: Partial responses saved, warning shown, retry available

---

### **Phase 5: Message Display & Conversation History** (5 items)

- [ ] **5.1** Update `ChatMessage` data class
  - Add field: `imageUrl: String?`
  - Add field: `diagnosisData: DiagnosisData?`
  - **Acceptance**: Data class compiles, fields nullable

- [ ] **5.2** Create `DiagnosisData` data class
  - Fields: `crop`, `health_status`, `issues`, `growth_stage`, `image_quality`, `diagnostic_notes`
  - Nested classes: `Crop`, `Issue`
  - **Acceptance**: Data class matches backend JSON structure, serialization works

- [ ] **5.3** Create `ImageMessageBubble` composable
  - Display image thumbnail (200dp height, clickable)
  - Display question text below image
  - Show loading overlay during analysis
  - Timestamp
  - **Acceptance**: Image message displays correctly in chat

- [ ] **5.4** Create `DiagnosisResponseBubble` composable
  - Diagnosis summary card (crop name, health status, issues)
  - Full Vietnamese advice text
  - TTS button
  - Color-coded health status (Green/Yellow/Orange/Red)
  - **Acceptance**: Diagnosis response displays correctly, all data shown

- [ ] **5.5** Implement fullscreen image viewer
  - Create `FullscreenImageDialog.kt`
  - Zoomable image on black background
  - Top bar with close button
  - Bottom diagnosis summary card (if available)
  - **Acceptance**: Fullscreen viewer opens on tap, image zoomable, closes correctly

---

### **Phase 6: Edge Cases & Error Handling** (3 items)

- [ ] **6.1** Handle no internet connection
  - Check `isNetworkAvailable()` before upload
  - Show "No Internet Connection" dialog if offline
  - Don't add optimistic message
  - **Acceptance**: Offline state detected, user notified, no failed messages added

- [ ] **6.2** Handle internet lost during upload
  - Timeout after 60 seconds
  - Show timeout dialog with retry option
  - Remove optimistic message on failure
  - **Acceptance**: Timeout handled, user can retry, UI stays clean

- [ ] **6.3** Handle image quality issues from AI
  - Parse diagnostic_notes for quality keywords ("insufficient", "blurry", "no plant")
  - Show helpful dialog with camera tips
  - Offer retake option
  - **Acceptance**: Quality issues detected, tips shown, retake option available

---

## 🧪 End-to-End Testing Checklist

### **Happy Path Test**
- [ ] Tap camera icon → Permission granted → Image source selector shown
- [ ] Select camera → Camera opens → Photo captured → Preview shown
- [ ] Default question displayed → User can add text → Confirm button works
- [ ] Upload starts → Progress shown → SSE streaming works → Response displayed
- [ ] Diagnosis summary card shown → Image thumbnail in message → TTS button works
- [ ] Tap thumbnail → Fullscreen viewer opens → Image zoomable → Close works
- [ ] Reload conversation → Images and diagnosis data preserved

### **Permission Flow Test**
- [ ] First time: Request permission → User denies → Bottom sheet shown
- [ ] Second time: Request permission → User denies again → "Open Settings" shown
- [ ] User opens settings → Grants permission → Returns to app → Auto-proceeds to selector

### **Error Handling Test**
- [ ] No internet → Error shown, upload not attempted
- [ ] Upload timeout → Timeout error shown, retry available
- [ ] Server error → Error message shown, retry available
- [ ] Blurry image → AI detects, quality tips shown, retake available
- [ ] No plant in image → AI detects, error shown, retake available

### **Edge Case Test**
- [ ] Image > 5MB → Validation error shown
- [ ] Image < 200px → Validation error shown
- [ ] Image 2MB-5MB → Compression works, upload succeeds
- [ ] Voice input in preview → Recording works, transcription appends
- [ ] Connection lost during streaming → Partial response saved with warning
- [ ] Multiple rapid taps on camera icon → No crashes, UI stable

---

## 📊 Acceptance Criteria Summary

### **Functional Requirements**
✅ User can tap camera icon and select image source
✅ User can capture photo or select from gallery
✅ User sees preview with default question before sending
✅ User can extend question with text or voice
✅ Image uploads to backend with progress indicator
✅ AI diagnosis streams in real-time
✅ Diagnosis summary card displays crop, health, issues
✅ Images appear in conversation history
✅ Fullscreen image viewer works
✅ All errors handled gracefully with retry options

### **Non-Functional Requirements**
✅ Follows existing app patterns (voice, location, TTS)
✅ No crashes or ANR errors
✅ Smooth UI transitions and animations
✅ Accessible (content descriptions, TalkBack support)
✅ Works on Android 8.0+ (API 26+)
✅ Handles slow network gracefully (timeouts, partial responses)

### **Code Quality Requirements**
✅ All new files follow Kotlin coding standards
✅ ViewModels use StateFlow for reactive state
✅ Proper error logging with descriptive tags
✅ No hardcoded strings (use string resources)
✅ Serialization uses `@Serializable` with `@SerialName`
✅ Repository pattern maintained (separation of concerns)

---

## 📁 Files to Create/Modify

### **New Files** (8 files)
1. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/permissions/ImagePermissionViewModel.kt`
2. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/permissions/ImagePermissionBottomSheet.kt`
3. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/chat/ImageSourceBottomSheet.kt`
4. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/chat/ImagePreviewDialog.kt`
5. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/chat/ImageMessageBubble.kt`
6. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/chat/DiagnosisResponseBubble.kt`
7. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/ui/chat/FullscreenImageDialog.kt`
8. `composeApp/src/androidMain/kotlin/com/nongtri/mobile/utils/ImageValidator.kt`

### **Modified Files** (6 files)
1. `composeApp/src/androidMain/AndroidManifest.xml` - Add camera/storage permissions
2. `composeApp/src/commonMain/kotlin/com/nongtri/mobile/data/model/ChatMessage.kt` - Add imageUrl, diagnosisData
3. `composeApp/src/commonMain/kotlin/com/nongtri/mobile/data/model/DiagnosisData.kt` - NEW model for diagnosis JSON
4. `composeApp/src/commonMain/kotlin/com/nongtri/mobile/data/repository/ChatRepository.kt` - Add uploadImageForDiagnosis()
5. `composeApp/src/commonMain/kotlin/com/nongtri/mobile/ui/chat/ChatScreen.kt` - Integrate camera icon, permission flow, image display
6. `composeApp/src/commonMain/kotlin/com/nongtri/mobile/ui/chat/WhatsAppStyleInputBar.kt` - Add camera icon

---

## 🚀 Implementation Order

**Follow this order for smooth implementation:**

1. **Phase 1: Permissions** (Day 1)
   - Establish permission handling foundation
   - Test permission flows thoroughly

2. **Phase 2: Image Selection** (Day 1-2)
   - Camera and gallery integration
   - Image validation and compression

3. **Phase 3: Preview UI** (Day 2)
   - Preview dialog with question customization
   - Voice/text input integration

4. **Phase 4: Upload & Diagnosis** (Day 3)
   - Backend integration
   - SSE streaming
   - Error handling

5. **Phase 5: Display & History** (Day 4)
   - Message bubbles
   - Conversation history
   - Fullscreen viewer

6. **Phase 6: Testing & Polish** (Day 5)
   - End-to-end testing
   - Edge case handling
   - Bug fixes

---

**READY TO BEGIN IMPLEMENTATION**

*This checklist is based on IMAGE_DIAGNOSIS_STRATEGY.md v1.0 (FROZEN)*
*All implementations must follow the frozen strategy exactly*
