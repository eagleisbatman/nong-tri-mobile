# Farmer Image Diagnosis UX Flow - Visual Diagram

## Complete Flow with Critical Failure Points

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         FARMER USER JOURNEY                              │
│                    (Rural Vietnam, 2G, Low Literacy)                     │
└─────────────────────────────────────────────────────────────────────────┘

1. CAMERA ICON TAP (ChatScreen.kt:361-376)
   ┌────────────────────────────────────┐
   │  Farmer taps camera icon           │
   │  isImageProcessing = true          │
   └────────────────┬───────────────────┘
                    │
                    ├─ ✅ Double-tap prevented
                    ├─ ✅ Rotation safe (rememberSaveable)
                    ├─ ❌ CRITICAL: Home button during permission → recovers OK
                    │
                    v
   ┌────────────────────────────────────┐
   │  Check camera + storage permission │
   └────────────────┬───────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
     NOT GRANTED           GRANTED
          │                   │
          v                   v
   ┌──────────────┐    ┌──────────────┐
   │ Show bottom  │    │ Show source  │
   │ sheet for    │    │ selector:    │
   │ permission   │    │ Camera/      │
   │ request      │    │ Gallery      │
   └──────────────┘    └──────┬───────┘
                              │
════════════════════════════════════════════════════════════════════════════

2. CAMERA CAPTURE (ImagePicker.android.kt:39-107)
                              │
                              v
   ┌────────────────────────────────────┐
   │  Create temp file in cache/        │
   │  shared_images/                    │
   └────────────────┬───────────────────┘
                    │
                    ├─ ⚠️ HIGH: No storage space check!
                    │   If < 5MB free → IOException → Crash
                    │
                    v
   ┌────────────────────────────────────┐
   │  Launch camera with FileProvider   │
   │  URI                               │
   └────────────────┬───────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
      SUCCESS             CANCELLED/FAILED
          │                   │
          v                   v
   ┌──────────────┐    ┌──────────────┐
   │ Process      │    │ Show error   │
   │ image URI    │    │ snackbar     │
   └──────┬───────┘    └──────────────┘
          │                   │
          │            ❌ CRITICAL: Error not localized
          │               "Cannot access image file..." (English only)
          │               Vietnamese farmer cannot read
          │
════════════════════════════════════════════════════════════════════════════

3. IMAGE PROCESSING (ImagePicker.android.kt:136-206)
          │
          v
   ┌────────────────────────────────────┐
   │  1. Open input stream from URI     │
   └────────────────┬───────────────────┘
                    │
                    ├─ ✅ Null check on inputStream
                    ├─ ✅ Farmer-friendly error if null
                    │
                    v
   ┌────────────────────────────────────┐
   │  2. BitmapFactory.decodeStream()   │
   │     Original: 4000x3000 (~12MB)    │
   └────────────────┬───────────────────┘
                    │
                    ├─ ❌ CRITICAL: OutOfMemoryError on 512MB device?
                    │   No try-catch here, may crash
                    │
                    v
   ┌────────────────────────────────────┐
   │  3. Compress bitmap if > 2MB       │
   │     - Scale down to 2048px max     │
   │     - Quality 90% → 50%            │
   └────────────────┬───────────────────┘
                    │
                    ├─ ❌ CRITICAL: Bitmap leak if compression fails!
                    │   catch block recycles scaledBitmap but NOT original bitmap
                    │   12MB leaked → retry = 24MB → crash
                    │
                    v
   ┌────────────────────────────────────┐
   │  4. Convert to base64 data URL     │
   │     "data:image/jpeg;base64,..."   │
   └────────────────┬───────────────────┘
                    │
                    ├─ ✅ Bitmap recycled after conversion
                    │
                    v
   ┌────────────────────────────────────┐
   │  Return ImagePickerResult          │
   │  - uri: "file:///..."              │
   │  - base64Data: "data:image/..."    │
   │  - sizeBytes: 2.3MB                │
   │  - error: null                     │
   └────────────────┬───────────────────┘
                    │
════════════════════════════════════════════════════════════════════════════

4. IMAGE PREVIEW DIALOG (ImagePreviewDialog.kt:27-163)
          │
          v
   ┌────────────────────────────────────┐
   │  Store in state:                   │
   │  - selectedImageUri (saveable) ✅  │
   │  - selectedImageBase64 (NOT saveable!) ❌ │
   └────────────────┬───────────────────┘
                    │
                    ├─ 🔴 CRITICAL: selectedImageBase64 lost on rotation!
                    │   Uses remember{} not rememberSaveable{}
                    │   If farmer rotates device → base64 = null
                    │   Cannot send 2nd image after rotation
                    │
                    v
   ┌────────────────────────────────────┐
   │  Show dialog:                      │
   │  - AsyncImage (imageUri)           │
   │  - Question input field            │
   │  - "Send for Diagnosis" button     │
   └────────────────┬───────────────────┘
                    │
                    ├─ ⚠️ HIGH: Large image → 1-2s lag loading preview
                    │   Farmer sees white screen, may think froze
                    │
                    ├─ ⚠️ MEDIUM: Placeholder "How is the health of my crop?" (English)
                    │   Should be localized to Vietnamese
                    │
                    v
          Farmer types question & taps "Send"
                    │
════════════════════════════════════════════════════════════════════════════

5. UPLOAD STARTS (ChatViewModel.kt:583-728, ChatScreen.kt:724-760)
                    │
                    v
   ┌────────────────────────────────────┐
   │  Validate base64Data exists        │
   └────────────────┬───────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
       NULL                NOT NULL
          │                   │
          v                   v
   ┌──────────────┐    ┌──────────────┐
   │ Show error   │    │ Validate size│
   │ snackbar     │    │ < 5MB        │
   └──────────────┘    └──────┬───────┘
          │                   │
          │            ┌──────┴──────┐
          │            │             │
          │         > 5MB          OK
          │            │             │
          │            v             v
          │     ┌──────────────┐    │
          │     │ Show error   │    │
          │     │ "Image too   │    │
          │     │ large..."    │    │
          │     └──────────────┘    │
          │            │             │
          │     ❌ CRITICAL: Not localized!
          │                         │
                                    v
   ┌────────────────────────────────────┐
   │  1. Show optimistic USER message   │
   │     - Image preview                │
   │     - Question text                │
   │     - isLoading = true ✅          │
   │     - "Analyzing..." overlay       │
   └────────────────┬───────────────────┘
                    │
                    ├─ ✅ Immediate feedback (good UX)
                    │
                    v
   ┌────────────────────────────────────┐
   │  2. Set global loading state       │
   │     uiState.isLoading = true       │
   └────────────────┬───────────────────┘
                    │
                    v
   ┌────────────────────────────────────┐
   │  3. Create optimistic ASSISTANT    │
   │     message (empty, isLoading=true)│
   └────────────────┬───────────────────┘
                    │
                    v
   ┌────────────────────────────────────┐
   │  4. Send POST to /api/chat/stream  │
   │     - userId                       │
   │     - message (question)           │
   │     - imageData (base64)           │
   │     - deviceInfo                   │
   └────────────────┬───────────────────┘
                    │
                    ├─ ⚠️ HIGH: No progress indicator!
                    │   On 2G: 5MB upload takes 60+ seconds
                    │   Farmer sees only "Analyzing..." spinner
                    │   No way to tell upload vs processing
                    │
                    v
          ┌─────────┴─────────┐
          │                   │
      UPLOAD FAILS        UPLOAD SUCCESS
          │                   │
          v                   │
   ┌──────────────────────┐  │
   │ Ktor timeout (60s)   │  │
   │ Network error        │  │
   │ Connection lost      │  │
   └──────┬───────────────┘  │
          │                  │
          v                  │
   ┌──────────────────────┐  │
   │ onFailure callback   │  │
   │ - Remove assistant   │  │
   │   message ✅         │  │
   │ - Set uiState.error  │  │
   │   ✅                 │  │
   │ - Clear global       │  │
   │   loading ✅         │  │
   └──────┬───────────────┘  │
          │                  │
          │  🔴🔴🔴 CRITICAL BUG! 🔴🔴🔴
          │  User's image message STILL shows:
          │  - isLoading = true
          │  - "Analyzing..." spinner
          │  - NEVER CLEARED!!!
          │
          │  Farmer sees:
          │  ┌─────────────────────┐
          │  │ [IMAGE - spinning]  │
          │  │ "Is my rice OK?"    │
          │  │ Analyzing... ⏳     │ ← FOREVER!
          │  └─────────────────────┘
          │  [Snackbar: "Upload failed" (disappears in 10s)]
          │
          │  Farmer thinks:
          │  - "Still processing..."
          │  - Waits 5 minutes
          │  - Gives up, thinks app broken
          │
          ├─ ❌ CRITICAL: Error message not localized
          │   "Upload failed. Please try again." (English)
          │
          ├─ ❌ CRITICAL: Snackbar duration too short
          │   10 seconds for 20-word error message
          │   Farmer reads at 30 WPM → needs 40 seconds
          │
════════════════════════════════════════════════════════════════════════════

6. SSE STREAM RECEPTION (NongTriApi.kt:163-303)
                                 │
                                 v
   ┌────────────────────────────────────┐
   │  Backend receives image ✅         │
   │  Uploads to MinIO ✅               │
   │  Calls AgriVision MCP...           │
   └────────────────┬───────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
    AGRIVISION OK        AGRIVISION TIMEOUT
          │                   │
          v                   v
   ┌──────────────┐    ┌──────────────┐
   │ Start SSE    │    │ Send SSE:    │
   │ stream       │    │ {__metadata: │
   └──────┬───────┘    │  diagnosisData:null,│
          │            │  content:"" }│
          │            └──────┬───────┘
          │                   │
          v                   │
   ┌──────────────┐           │
   │ Send chunks: │           │
   │ {content:    │           │
   │  "Advice..."}│           │
   └──────┬───────┘           │
          │                   │
          ├─ ✅ On first chunk: Clear user image isLoading
          │                   │
          v                   │
   ┌──────────────┐           │
   │ Send metadata│           │
   │ {__metadata: │           │
   │  true,       │           │
   │  diagnosisData:{...},    │
   │  conversationId:123}     │
   └──────┬───────┘           │
          │                   │
          ├─ ✅ Parse diagnosisData
          ├─ ✅ Handle null gracefully
          │                   │
          v                   v
   ┌────────────────────────────────────┐
   │  Update assistant message:         │
   │  - content = full advice text      │
   │  - diagnosisData = parsed object   │
   │  - isLoading = false               │
   └────────────────┬───────────────────┘
                    │
                    │  🔴 CRITICAL BUG!
                    │  If NO chunks sent (only metadata):
                    │  - onChunk never called
                    │  - firstChunkReceived = false
                    │  - User image isLoading NEVER cleared!
                    │  - Infinite spinner again!
                    │
════════════════════════════════════════════════════════════════════════════

7. DIAGNOSIS RENDERING (DiagnosisResponseBubble.kt:25-250)
                    │
                    v
   ┌────────────────────────────────────┐
   │  Check if diagnosisData != null    │
   └────────────────┬───────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
       NULL              NOT NULL
          │                   │
          v                   v
   ┌──────────────┐    ┌──────────────┐
   │ Show only    │    │ Show card:   │
   │ advice text  │    │ - Crop info  │
   │              │    │ - Health     │
   │              │    │   status     │
   │              │    │ - Issues list│
   │              │    │ + Advice text│
   └──────┬───────┘    └──────┬───────┘
          │                   │
          │                   ├─ ✅ Color-coded health (green/yellow/orange/red)
          │                   ├─ ⚠️ MEDIUM: Color-blind farmers can't distinguish
          │                   │
          ├──────────┬────────┘
                    │
                    v
   ┌────────────────────────────────────┐
   │  Check if content.isNotBlank()     │
   └────────────────┬───────────────────┘
                    │
          ┌─────────┴─────────┐
          │                   │
       BLANK               NOT BLANK
          │                   │
          v                   v
   Check isLoading?    ┌──────────────┐
          │            │ Show advice  │
          ├─ Yes →    │ text         │
          │   Show    │              │
          │   spinner │              │
          │            └──────────────┘
          v
   ┌──────────────┐
   │ 🔴 CRITICAL: │
   │ If BOTH      │
   │ diagnosisData│
   │ = null AND   │
   │ content = "" │
   │ AND isLoading│
   │ = false:     │
   │              │
   │ → EMPTY      │
   │   BUBBLE! ❌ │
   │              │
   │ Farmer sees: │
   │ (nothing)    │
   │ Just         │
   │ timestamp    │
   └──────────────┘

════════════════════════════════════════════════════════════════════════════

8. FARMER SEES RESULT
                    │
                    v
   ┌────────────────────────────────────┐
   │  SUCCESS PATH:                     │
   │  ┌──────────────────────────┐     │
   │  │ [IMAGE]                  │     │
   │  │ "Is my rice healthy?"    │     │
   │  └──────────────────────────┘     │
   │  ┌──────────────────────────┐     │
   │  │ 🌾 Lúa (Rice)            │     │
   │  │    Oryza sativa          │     │
   │  │                          │     │
   │  │ 🟡 Moderate Issue        │     │
   │  │                          │     │
   │  │ Detected Issues:         │     │
   │  │ ⚠️⚠️ Bệnh đạo ôn        │     │
   │  │   Fungal Disease         │     │
   │  │   Severity: Moderate     │     │
   │  │   Affected: Leaves       │     │
   │  └──────────────────────────┘     │
   │  ┌──────────────────────────┐     │
   │  │ [Vietnamese advice text] │     │
   │  │ "Cần xử lý ngay..."      │     │
   │  │                          │     │
   │  │ [🔊 Listen to advice]    │     │
   │  └──────────────────────────┘     │
   └────────────────────────────────────┘

   ┌────────────────────────────────────┐
   │  FAILURE PATH (Current Bugs):      │
   │  ┌──────────────────────────┐     │
   │  │ [IMAGE - spinning ⏳]    │  ← STUCK FOREVER!
   │  │ "Is my rice healthy?"    │     │
   │  │ Analyzing...             │     │
   │  └──────────────────────────┘     │
   │                                    │
   │  [Snackbar flashes for 10s:]      │
   │  "Upload failed. Please try again."│  ← NOT LOCALIZED!
   │                                    │  ← DISAPPEARS TOO FAST!
   │  Farmer waits 5 minutes...         │
   │  Thinks: "App is broken" 😞        │
   └────────────────────────────────────┘

════════════════════════════════════════════════════════════════════════════

9. LIFECYCLE EVENTS (Rotation, Backgrounding)

   ROTATION SCENARIO:
   ┌────────────────────────────────────┐
   │  Before rotation:                  │
   │  - selectedImageUri ✅ (saveable)  │
   │  - selectedImageBase64 ✅ (in memory)│
   │  - Upload in progress...           │
   └────────────────┬───────────────────┘
                    │
                    v  📱 ROTATE
   ┌────────────────────────────────────┐
   │  After rotation:                   │
   │  - selectedImageUri ✅ (restored)  │
   │  - selectedImageBase64 ❌ (LOST!)  │
   │  - Upload completes ✅             │
   │  - Response displays ✅            │
   │                                    │
   │  Try to send 2nd image:            │
   │  - base64Data = null ❌            │
   │  - Error: "Failed to process image"│
   │  - Farmer must re-capture photo 😞 │
   └────────────────────────────────────┘

   BACKGROUNDING SCENARIO:
   ┌────────────────────────────────────┐
   │  Phone call comes in...            │
   │  - App goes to background          │
   │  - Upload continues ✅ (5min grace)│
   │  - ViewModel state preserved ✅    │
   │                                    │
   │  Farmer returns after 2min:        │
   │  - Response displays correctly ✅  │
   │  - No issues found 👍              │
   └────────────────────────────────────┘

════════════════════════════════════════════════════════════════════════════

SUMMARY OF CRITICAL FAILURE POINTS:

🔴 1. INFINITE LOADING SPINNER (2 occurrences)
     - Upload fails → User image stuck at "Analyzing..."
     - Metadata arrives but no chunks → Same issue
     - Farmer Impact: Thinks app frozen, waits forever

🔴 2. NOT LOCALIZED (15+ strings)
     - All errors in English
     - Vietnamese farmer cannot read
     - Farmer Impact: Cannot understand what went wrong

🔴 3. SNACKBAR TOO SHORT
     - 10 seconds for 20-word error
     - Farmer reads at 30 WPM → needs 40 seconds
     - Farmer Impact: Cannot read error before disappears

🔴 4. STATE LOSS ON ROTATION
     - selectedImageBase64 lost
     - Cannot send 2nd image
     - Farmer Impact: Must re-capture photo

🔴 5. BITMAP MEMORY LEAK
     - Original bitmap not recycled on compression failure
     - 12MB leaked per retry
     - Farmer Impact: App crashes after 2-3 retries

🔴 6. EMPTY DIAGNOSIS BUBBLE
     - If diagnosisData=null AND content="" AND isLoading=false
     - Shows blank response
     - Farmer Impact: Thinks app broken

🔴 7. NO STORAGE SPACE CHECK
     - createTempFile() fails if < 5MB free
     - IOException → Crash or cryptic error
     - Farmer Impact: Doesn't know to free up space

════════════════════════════════════════════════════════════════════════════
```

## Recommended Fixes Priority

### 🔴 P1: IMMEDIATE (Ship Blockers)
1. Fix infinite loading spinner on errors
2. Localize ALL error messages and UI strings
3. Increase snackbar duration or use AlertDialog
4. Add empty diagnosis error state

### 🟡 P2: HIGH (2 Weeks)
5. Fix state loss on rotation (move base64 to ViewModel)
6. Add storage space check before camera capture
7. Fix bitmap memory leak in compression
8. Add upload progress indicator

### 🟢 P3: MEDIUM (Nice to Have)
9. Optimize image preview loading
10. Add color-blind accessibility (icon variation)
11. Improve error recovery UX (retry button)

**Estimated Total Effort**: 8-10 development days
