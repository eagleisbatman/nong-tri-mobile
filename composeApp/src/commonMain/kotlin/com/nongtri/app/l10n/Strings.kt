package com.nongtri.app.l10n

enum class Language(val code: String, val displayName: String, val flag: String) {
    VIETNAMESE("vi", "Tiếng Việt", "🇻🇳"),
    ENGLISH("en", "English", "🇬🇧")
}

interface Strings {
    // ============================================================================
    // APP IDENTITY
    // ============================================================================
    val appName: String
    val appTagline: String
    val aiAssistantName: String  // "Nông Trí AI"

    // ============================================================================
    // LANGUAGE SELECTION SCREEN
    // ============================================================================
    val selectLanguage: String
    val selectLanguageBilingual: String  // "Select Language / Chọn ngôn ngữ"
    val chooseYourLanguage: String
    val continue_: String

    // ============================================================================
    // CHAT SCREEN - TOP BAR & MENU
    // ============================================================================
    val chatTitle: String
    val menuConversations: String
    val menuNewChat: String
    val menuShareLocation: String
    val menuLanguageSection: String  // "Language" section header
    val menuThemeSection: String     // "Theme" section header

    // ============================================================================
    // NAVIGATION & COMMON ACTIONS
    // ============================================================================
    val back: String
    val close: String
    val cancel: String
    val ok: String
    val yes: String
    val no: String
    val retry: String
    val delete: String
    val send: String
    val confirm: String
    val dismiss: String

    // ============================================================================
    // WELCOME CARD
    // ============================================================================
    val welcomeMessage: String
    val welcomeDescription: String
    val buildingExperience: String  // "Building your experience..."

    // ============================================================================
    // STARTER QUESTIONS
    // ============================================================================
    val starterQuestionsHeaderVi: String  // "Gợi ý câu hỏi"
    val starterQuestionsHeaderEn: String  // "Suggested questions"

    // ============================================================================
    // INPUT BAR
    // ============================================================================
    val typeMessage: String
    val listening: String
    val processing: String
    val transcribing: String
    val tapToSpeak: String
    val releaseToSend: String
    val slideToCancelHint: String  // "< Slide to cancel"

    // ============================================================================
    // MESSAGE TYPES & LABELS
    // ============================================================================
    val textMessage: String
    val voiceMessage: String
    val imageMessage: String
    val userLabel: String      // "You"
    val aiLabel: String         // "Nông Trí AI"
    val followUpSectionHeader: String  // "💡 What's next?"

    // ============================================================================
    // MESSAGE ACTIONS
    // ============================================================================
    val actionCopy: String
    val actionShare: String
    val actionListen: String
    val actionListenError: String  // "Listen (Error - tap to retry)"
    val actionThumbsUp: String     // "Good response"
    val actionThumbsDown: String   // "Bad response"
    val shareAiResponse: String    // Share dialog title

    // ============================================================================
    // VOICE RECORDING
    // ============================================================================
    val recording: String          // "Recording..."
    val cancelRecording: String
    val sendRecording: String

    // ============================================================================
    // IMAGE ACTIONS
    // ============================================================================
    val attachImage: String
    val takePhoto: String
    val chooseFromGallery: String
    val camera: String
    val gallery: String
    val selectImageSource: String
    val captureNewPhoto: String
    val selectExistingPhoto: String
    val imageTipsText: String  // Photo tips for best results

    // ============================================================================
    // IMAGE PREVIEW & DIAGNOSIS
    // ============================================================================
    val confirmImage: String
    val askQuestionAboutPlant: String
    val defaultPlantQuestion: String  // "How is the health of my crop?"
    val sendForDiagnosis: String
    val diagnosisInfoText: String
    val analyzing: String
    val analyzingPlantHealth: String

    // ============================================================================
    // DIAGNOSIS RESPONSE
    // ============================================================================
    val healthStatus: String
    val detectedIssues: String
    val severityLabel: String
    val affectedLabel: String
    val growthStage: String
    val listenToAdvice: String

    // ============================================================================
    // DIAGNOSIS PENDING
    // ============================================================================
    val analyzingYourCrop: String
    val estimatedTime: String
    val notificationWhenReady: String
    val whatsHappening: String
    val diagnosisPendingExplanation: String
    val diagnosisPendingInfo: String
    val jobIdLabel: String
    val plantImageBeingAnalyzed: String  // Content description for diagnosis pending image

    // ============================================================================
    // TIMESTAMPS & RELATIVE TIME
    // ============================================================================
    val justNow: String
    val minutesAgo: String     // "m ago"
    val hoursAgo: String       // "h ago"
    val today: String
    val yesterday: String
    val daysAgo: String        // "days ago"
    val weeksAgo: String       // "weeks ago"
    val monthsAgo: String      // "months ago"

    // ============================================================================
    // CONVERSATIONS LIST SCREEN
    // ============================================================================
    val conversations: String
    val newConversation: String
    val noConversationsYet: String
    val noConversationsHint: String  // "Tap + to start a new conversation"
    val messageCountSuffix: String   // " messages"
    val deleteConversation: String
    val deleteConversationTitle: String
    val deleteConversationMessage: String
    val deleteConversationConfirm: String

    // ============================================================================
    // LOCATION
    // ============================================================================
    val location: String
    val shareLocation: String
    val updateLocation: String
    val shareGpsLocation: String
    val openSettings: String
    val detectedLocationIp: String    // "Detected Location (IP)"
    val mySharedLocationGps: String   // "My Shared Location (GPS)"
    val unableToDetectLocation: String
    val shareLocationDescription: String
    val locationHelpText: String

    // ============================================================================
    // PERMISSIONS - VOICE
    // ============================================================================
    val microphonePermission: String
    val voiceRecording: String
    val microphonePermissionSettingsPrompt: String
    val microphonePermissionPrompt: String
    val grantPermission: String
    val voiceMessageInfoText: String

    // ============================================================================
    // PERMISSIONS - CAMERA & PHOTOS
    // ============================================================================
    val cameraPhotoPermissions: String
    val cameraAccess: String
    val cameraPermissionSettingsPrompt: String
    val cameraPermissionGranted: String
    val cameraPermissionPrompt: String
    val photoLibraryAccess: String
    val photoLibraryPermissionSettingsPrompt: String
    val photoLibraryPermissionGranted: String
    val photoLibraryPermissionPrompt: String
    val imageUploadInfoText: String
    val permissionGrantedText: String

    // ============================================================================
    // THEME
    // ============================================================================
    val profile: String
    val settings: String
    val language: String
    val theme: String
    val light: String
    val dark: String
    val system: String
    val systemDefault: String
    val about: String
    val version: String

    // ============================================================================
    // SHARE
    // ============================================================================
    val shareResponse: String
    val shareAsText: String
    val shareAsImage: String
    val shareViaMessaging: String
    val saveOrShareScreenshot: String

    // ============================================================================
    // ERROR MESSAGES - GENERIC
    // ============================================================================
    val errorGeneric: String
    val errorNetwork: String
    val errorMicrophone: String
    val errorCamera: String
    val errorPermission: String
    val errorUnknown: String

    // ============================================================================
    // ERROR MESSAGES - SPECIFIC
    // ============================================================================
    val errorFailedToSendMessage: String
    val errorFailedToLoadConversations: String
    val errorFailedToCreateConversation: String
    val errorFailedToDeleteConversation: String
    val errorFailedToArchiveConversation: String
    val errorFailedToSubmitDiagnosis: String
    val errorImageTooLarge: String  // Needs size parameter
    val errorFailedToProcessImage: String
    val errorServerUpdating: String
    val errorConnectionFailed: String
    val errorLoadingConversations: String

    // Diagnosis errors
    val diagnosisCompleted: String
    val errorDiagnosisFailed: String
    val errorFailedToFetchDiagnosis: String
    val errorFetchingDiagnosis: String
    val diagnosisProcessingMessage: String
    val errorFailedToFetchStarterQuestions: String

    // Voice recording errors
    val errorRecordingTooShort: String
    val errorRecordingEmpty: String
    val errorFailedToStartRecording: String
    val errorFailedToStopRecording: String
    val errorTranscriptionFailed: String

    // Location errors
    val errorFailedToInitializeLocation: String
    val errorFailedToGetLocations: String
    val errorFailedToGetSavedLocations: String
    val errorFailedToSaveLocation: String
    val errorFailedToShareLocation: String
    val errorFailedToSetPrimaryLocation: String
    val errorFailedToDeleteLocation: String

    // API/Network errors
    val errorUploadTimeout: String
    val errorNoInternetConnection: String
    val errorCannotConnectToServer: String
    val errorUploadFailed: String
    val errorFailedToSaveVoiceMessage: String
    val errorFailedToUpdateAudioUrl: String
    val errorFailedToLoadHistory: String
    val errorFailedToLoadThreads: String
    val errorFailedToCreateThread: String
    val errorFailedToGetActiveThread: String
    val errorFailedToLoadThreadMessages: String
    val errorFailedToUpdateThread: String
    val errorFailedToDeleteThread: String
    val errorFailedToRegisterFcmToken: String

    // Image validation errors
    val errorImageTooLargeWithSize: String  // "Image is too large (X MB). Maximum size is Y MB."
    val errorImageFileEmpty: String
    val errorInvalidImageDimensions: String
    val errorImageDimensionsTooLarge: String  // "Image dimensions too large (WxH). Maximum is XxX."
    val errorUnsupportedImageFormatMimeType: String  // "Unsupported image format: X. Please use JPEG, PNG, or WebP."
    val errorUnsupportedImageFormatExtension: String  // "Unsupported image format: .X. Please use JPEG, PNG, or WebP."
    val errorCannotDetermineImageFormat: String

    // Platform-specific errors (Android)
    val errorCannotAccessImageFile: String
    val errorCannotReadImage: String
    val errorCouldNotOpenSettings: String
    val errorLocationPermissionNotGranted: String
    val errorCouldNotGetCurrentLocation: String

    // Toast messages (Android)
    val toastSpeechAlreadyPlaying: String
    val toastTtsError: String  // "TTS Error: X"

    // Permission messages (Android)
    val permissionMicrophoneDeniedSettings: String
    val permissionMicrophoneRationale: String
    val permissionCameraStorageDeniedSettings: String
    val permissionCameraDeniedSettings: String
    val permissionCameraRationale: String
    val permissionStorageDeniedSettings: String
    val permissionStorageRationale: String
    val permissionLocationDeniedSettings: String
    val permissionLocationRationale: String

    // Notification messages (Android)
    val notificationDiagnosisTitle: String
    val notificationDiagnosisBody: String
    val notificationChannelName: String
    val notificationChannelDescription: String

    // Share/UI strings
    val shareImageTitle: String

    // ============================================================================
    // VOICE MESSAGES
    // ============================================================================
    val audioNotAvailable: String
    val playVoiceMessage: String
    val pauseVoiceMessage: String
    val resumeVoiceMessage: String  // "Resume"

    // ============================================================================
    // FULLSCREEN IMAGE
    // ============================================================================
    val plantImage: String
    val plantImageFullscreen: String
    val healthLabel: String
    val issuesDetected: String
    val imageQualityLabel: String
    val noImage: String  // "No image"
    val cropLabel: String  // "Crop" for diagnosis response

    // ============================================================================
    // CONTENT DESCRIPTIONS (for accessibility)
    // ============================================================================
    val cdSettings: String
    val cdScrollToBottom: String
    val cdNewConversation: String
    val cdDeleteConversation: String
    val cdVoiceInput: String
    val cdAttachImage: String
    val cdCamera: String
    val cdGallery: String
    val cdSend: String
    val cdBack: String
    val cdClose: String
    val cdCancel: String
    val cdCopy: String
    val cdShare: String
    val cdPlay: String
    val cdPause: String
    val cdSelectedPlantImage: String
    val cdPlantImageFullscreen: String
    val cdCancelRecording: String
    val cdSendRecording: String
    val cdCrop: String

    // ============================================================================
    // FORMATTING FUNCTIONS for parameterized strings
    // ============================================================================

    /**
     * Format image size error with actual sizes
     * @param currentSize Current file size (formatted, e.g., "7 MB")
     * @param maxSize Maximum allowed size (formatted, e.g., "5 MB")
     */
    fun formatImageTooLargeWithSize(currentSize: String, maxSize: String): String

    /**
     * Format image dimensions error with actual dimensions
     * @param width Image width in pixels
     * @param height Image height in pixels
     * @param maxDimension Maximum allowed dimension
     */
    fun formatImageDimensionsTooLarge(width: Int, height: Int, maxDimension: Int): String

    /**
     * Format unsupported format error with MIME type
     * @param mimeType The unsupported MIME type
     */
    fun formatUnsupportedImageFormatMimeType(mimeType: String): String

    /**
     * Format unsupported format error with file extension
     * @param extension The unsupported file extension
     */
    fun formatUnsupportedImageFormatExtension(extension: String): String
}

object EnglishStrings : Strings {
    // ============================================================================
    // APP IDENTITY
    // ============================================================================
    override val appName = "Nông Trí"
    override val appTagline = "AI Farming Assistant"
    override val aiAssistantName = "Nông Trí AI"

    // ============================================================================
    // LANGUAGE SELECTION SCREEN
    // ============================================================================
    override val selectLanguage = "Select Language"
    override val selectLanguageBilingual = "Select Language / Chọn ngôn ngữ"
    override val chooseYourLanguage = "Choose your preferred language"
    override val continue_ = "Continue"

    // ============================================================================
    // CHAT SCREEN - TOP BAR & MENU
    // ============================================================================
    override val chatTitle = "Nông Trí"
    override val menuConversations = "Conversations"
    override val menuNewChat = "New Chat"
    override val menuShareLocation = "Share Location"
    override val menuLanguageSection = "Language"
    override val menuThemeSection = "Theme"

    // ============================================================================
    // NAVIGATION & COMMON ACTIONS
    // ============================================================================
    override val back = "Back"
    override val close = "Close"
    override val cancel = "Cancel"
    override val ok = "OK"
    override val yes = "Yes"
    override val no = "No"
    override val retry = "Retry"
    override val delete = "Delete"
    override val send = "Send"
    override val confirm = "Confirm"
    override val dismiss = "Dismiss"

    // ============================================================================
    // WELCOME CARD
    // ============================================================================
    override val welcomeMessage = "Hello! I'm your AI farming assistant"
    override val welcomeDescription = "Ask me anything about crops, livestock, pest management, or farming techniques. I'm here to help!"
    override val buildingExperience = "Building your experience..."

    // ============================================================================
    // STARTER QUESTIONS
    // ============================================================================
    override val starterQuestionsHeaderVi = "Gợi ý câu hỏi"
    override val starterQuestionsHeaderEn = "Suggested questions"

    // ============================================================================
    // INPUT BAR
    // ============================================================================
    override val typeMessage = "Type your message..."
    override val listening = "Listening..."
    override val processing = "Processing..."
    override val transcribing = "Transcribing..."
    override val tapToSpeak = "Tap to speak"
    override val releaseToSend = "Release to send"
    override val slideToCancelHint = "< Slide to cancel"

    // ============================================================================
    // MESSAGE TYPES & LABELS
    // ============================================================================
    override val textMessage = "Text message"
    override val voiceMessage = "Voice message"
    override val imageMessage = "Image message"
    override val userLabel = "You"
    override val aiLabel = "Nông Trí AI"
    override val followUpSectionHeader = "💡 What's next?"

    // ============================================================================
    // MESSAGE ACTIONS
    // ============================================================================
    override val actionCopy = "Copy"
    override val actionShare = "Share"
    override val actionListen = "Listen"
    override val actionListenError = "Listen (Error - tap to retry)"
    override val actionThumbsUp = "Good response"
    override val actionThumbsDown = "Bad response"
    override val shareAiResponse = "Share AI Response"

    // ============================================================================
    // VOICE RECORDING
    // ============================================================================
    override val recording = "Recording..."
    override val cancelRecording = "Cancel"
    override val sendRecording = "Send recording"

    // ============================================================================
    // IMAGE ACTIONS
    // ============================================================================
    override val attachImage = "Attach image"
    override val takePhoto = "Take Photo"
    override val chooseFromGallery = "Choose from Gallery"
    override val camera = "Camera"
    override val gallery = "Gallery"
    override val selectImageSource = "Select Image Source"
    override val captureNewPhoto = "Capture a new photo of your plant"
    override val selectExistingPhoto = "Select an existing photo"
    override val imageTipsText = "For best results, take clear photos in good lighting with the plant filling most of the frame."

    // ============================================================================
    // IMAGE PREVIEW & DIAGNOSIS
    // ============================================================================
    override val confirmImage = "Confirm Image"
    override val askQuestionAboutPlant = "Ask a question about your plant:"
    override val defaultPlantQuestion = "How is the health of my crop?"
    override val sendForDiagnosis = "Send for Diagnosis"
    override val diagnosisInfoText = "The AI will analyze your plant image and provide health diagnosis with treatment recommendations."
    override val analyzing = "Analyzing..."
    override val analyzingPlantHealth = "Analyzing plant health..."

    // ============================================================================
    // DIAGNOSIS RESPONSE
    // ============================================================================
    override val healthStatus = "Health status"
    override val detectedIssues = "Detected Issues:"
    override val severityLabel = " • Severity: "
    override val affectedLabel = "Affected: "
    override val growthStage = "Growth Stage: "
    override val listenToAdvice = "Listen to advice"

    // ============================================================================
    // DIAGNOSIS PENDING
    // ============================================================================
    override val analyzingYourCrop = "Analyzing your crop photo..."
    override val estimatedTime = "Estimated time: 2-3 minutes"
    override val notificationWhenReady = "We'll notify you when it's ready"
    override val whatsHappening = "What's happening?"
    override val diagnosisPendingExplanation = "Our AI is carefully analyzing your crop photo to identify:\n• Crop type\n• Health status\n• Any diseases or issues\n• Recommendations for care"
    override val diagnosisPendingInfo = "You can close this app and continue your work. We'll send you a notification when the diagnosis is ready!"
    override val jobIdLabel = "Job ID: "
    override val plantImageBeingAnalyzed = "Plant image being analyzed"

    // ============================================================================
    // TIMESTAMPS & RELATIVE TIME
    // ============================================================================
    override val justNow = "Just now"
    override val minutesAgo = "m ago"
    override val hoursAgo = "h ago"
    override val today = "Today"
    override val yesterday = "Yesterday"
    override val daysAgo = " days ago"
    override val weeksAgo = " weeks ago"
    override val monthsAgo = " months ago"

    // ============================================================================
    // CONVERSATIONS LIST SCREEN
    // ============================================================================
    override val conversations = "Conversations"
    override val newConversation = "New Conversation"
    override val noConversationsYet = "No conversations yet"
    override val noConversationsHint = "Tap + to start a new conversation"
    override val messageCountSuffix = " messages"
    override val deleteConversation = "Delete conversation"
    override val deleteConversationTitle = "Delete Conversation"
    override val deleteConversationMessage = "Are you sure you want to delete this conversation? This action cannot be undone."
    override val deleteConversationConfirm = "Delete"

    // ============================================================================
    // LOCATION
    // ============================================================================
    override val location = "Location"
    override val shareLocation = "Share Location"
    override val updateLocation = "Update Location"
    override val shareGpsLocation = "Share GPS Location"
    override val openSettings = "Open Settings"
    override val detectedLocationIp = "Detected Location (IP)"
    override val mySharedLocationGps = "My Shared Location (GPS)"
    override val unableToDetectLocation = "Unable to determine location"
    override val shareLocationDescription = "Share your precise location for more accurate weather forecasts and farming advice."
    override val locationHelpText = "Your location helps provide accurate weather forecasts and farming advice for your area."

    // ============================================================================
    // PERMISSIONS - VOICE
    // ============================================================================
    override val microphonePermission = "Microphone Permission"
    override val voiceRecording = "Voice Recording"
    override val microphonePermissionSettingsPrompt = "Microphone permission is required to record voice messages. Please enable it in Settings."
    override val microphonePermissionPrompt = "Grant microphone permission to record and send voice messages to the AI assistant."
    override val grantPermission = "Grant Permission"
    override val voiceMessageInfoText = "Voice messages are transcribed using AI and sent to the assistant for farming advice."

    // ============================================================================
    // PERMISSIONS - CAMERA & PHOTOS
    // ============================================================================
    override val cameraPhotoPermissions = "Camera & Photo Permissions"
    override val cameraAccess = "Camera Access"
    override val cameraPermissionSettingsPrompt = "Camera permission is required to capture plant photos. Please enable it in Settings."
    override val cameraPermissionGranted = "Camera access granted ✓"
    override val cameraPermissionPrompt = "Allow camera access to capture photos of your plants for AI diagnosis."
    override val photoLibraryAccess = "Photo Library Access"
    override val photoLibraryPermissionSettingsPrompt = "Photo library permission is required to select existing images. Please enable it in Settings."
    override val photoLibraryPermissionGranted = "Photo library access granted ✓"
    override val photoLibraryPermissionPrompt = "Allow photo library access to select existing plant images for diagnosis."
    override val imageUploadInfoText = "Images are sent to our AI for plant health diagnosis and treatment recommendations."
    override val permissionGrantedText = "Permission granted"

    // ============================================================================
    // THEME
    // ============================================================================
    override val profile = "Profile"
    override val settings = "Settings"
    override val language = "Language"
    override val theme = "Theme"
    override val light = "Light"
    override val dark = "Dark"
    override val system = "System"
    override val systemDefault = "System Default"
    override val about = "About"
    override val version = "Version"

    // ============================================================================
    // SHARE
    // ============================================================================
    override val shareResponse = "Share Response"
    override val shareAsText = "Share as Text"
    override val shareAsImage = "Share as Image"
    override val shareViaMessaging = "Share via messaging apps"
    override val saveOrShareScreenshot = "Save or share as screenshot"

    // ============================================================================
    // ERROR MESSAGES - GENERIC
    // ============================================================================
    override val errorGeneric = "Something went wrong. Please try again."
    override val errorNetwork = "No internet connection. Please check your network."
    override val errorMicrophone = "Microphone access denied"
    override val errorCamera = "Camera access denied"
    override val errorPermission = "Permission required"
    override val errorUnknown = "Unknown error"

    // ============================================================================
    // ERROR MESSAGES - SPECIFIC
    // ============================================================================
    override val errorFailedToSendMessage = "Failed to send message"
    override val errorFailedToLoadConversations = "Failed to load conversations"
    override val errorFailedToCreateConversation = "Failed to create conversation"
    override val errorFailedToDeleteConversation = "Failed to delete conversation"
    override val errorFailedToArchiveConversation = "Failed to archive conversation"
    override val errorFailedToSubmitDiagnosis = "Failed to submit diagnosis"
    override val errorServerUpdating = "Server is updating. Please try again in a few seconds."
    override val errorConnectionFailed = "Unable to connect to server. Check your network connection."
    override val errorImageTooLarge = "Image is too large. Please try a smaller image."
    override val errorFailedToProcessImage = "Failed to process image. Please try again."
    override val errorLoadingConversations = "Error loading conversations"

    // Diagnosis errors
    override val diagnosisCompleted = "Diagnosis completed"
    override val errorDiagnosisFailed = "Diagnosis failed"
    override val errorFailedToFetchDiagnosis = "Failed to fetch diagnosis"
    override val errorFetchingDiagnosis = "Error fetching diagnosis"
    override val diagnosisProcessingMessage = "Your diagnosis is being processed. We'll notify you when it's ready!"
    override val errorFailedToFetchStarterQuestions = "Failed to fetch starter questions"

    // Voice recording errors
    override val errorRecordingTooShort = "Recording too short. Please hold longer."
    override val errorRecordingEmpty = "Recording is empty. Please try again."
    override val errorFailedToStartRecording = "Failed to start recording"
    override val errorFailedToStopRecording = "Failed to stop recording"
    override val errorTranscriptionFailed = "Transcription failed"

    // Location errors
    override val errorFailedToInitializeLocation = "Failed to initialize location"
    override val errorFailedToGetLocations = "Failed to get locations"
    override val errorFailedToGetSavedLocations = "Failed to get saved locations"
    override val errorFailedToSaveLocation = "Failed to save location"
    override val errorFailedToShareLocation = "Failed to share location"
    override val errorFailedToSetPrimaryLocation = "Failed to set primary location"
    override val errorFailedToDeleteLocation = "Failed to delete location"

    // API/Network errors
    override val errorUploadTimeout = "Upload timed out. This may be due to slow internet. Please try a smaller image or wait and try again."
    override val errorNoInternetConnection = "No internet connection. Please check your network and try again."
    override val errorCannotConnectToServer = "Cannot connect to server. Please check your internet connection."
    override val errorUploadFailed = "Upload failed. Please try again."
    override val errorFailedToSaveVoiceMessage = "Failed to save voice message"
    override val errorFailedToUpdateAudioUrl = "Failed to update audio URL"
    override val errorFailedToLoadHistory = "Failed to load history"
    override val errorFailedToLoadThreads = "Failed to load threads"
    override val errorFailedToCreateThread = "Failed to create thread"
    override val errorFailedToGetActiveThread = "Failed to get active thread"
    override val errorFailedToLoadThreadMessages = "Failed to load thread messages"
    override val errorFailedToUpdateThread = "Failed to update thread"
    override val errorFailedToDeleteThread = "Failed to delete thread"
    override val errorFailedToRegisterFcmToken = "Failed to register FCM token"

    // Image validation errors
    override val errorImageTooLargeWithSize = "Image is too large. Maximum size is 10 MB."
    override val errorImageFileEmpty = "Image file is empty"
    override val errorInvalidImageDimensions = "Invalid image dimensions"
    override val errorImageDimensionsTooLarge = "Image dimensions too large. Maximum is 4096x4096."
    override val errorUnsupportedImageFormatMimeType = "Unsupported image format. Please use JPEG, PNG, or WebP."
    override val errorUnsupportedImageFormatExtension = "Unsupported image format. Please use JPEG, PNG, or WebP."
    override val errorCannotDetermineImageFormat = "Cannot determine image format"

    // Platform-specific errors (Android)
    override val errorCannotAccessImageFile = "Cannot access image file. Please try another image."
    override val errorCannotReadImage = "Cannot read image. The file may be corrupted or in an unsupported format."
    override val errorCouldNotOpenSettings = "Could not open settings"
    override val errorLocationPermissionNotGranted = "Location permission not granted"
    override val errorCouldNotGetCurrentLocation = "Could not get current location"

    // Toast messages (Android)
    override val toastSpeechAlreadyPlaying = "Speech already playing"
    override val toastTtsError = "TTS Error"

    // Permission messages (Android)
    override val permissionMicrophoneDeniedSettings = "Microphone permission denied. Please enable it in Settings to use voice messages."
    override val permissionMicrophoneRationale = "Microphone permission is needed to record voice messages"
    override val permissionCameraStorageDeniedSettings = "Camera and image permissions are needed. Please enable them in Settings."
    override val permissionCameraDeniedSettings = "Camera permission denied. Please enable it in Settings."
    override val permissionCameraRationale = "Camera permission is needed to capture plant images"
    override val permissionStorageDeniedSettings = "Storage permission denied. Please enable it in Settings."
    override val permissionStorageRationale = "Storage permission is needed to select images from gallery"
    override val permissionLocationDeniedSettings = "Please enable location permission in Settings to share your location"
    override val permissionLocationRationale = "Location permission is needed to share your exact location"

    // Notification messages (Android)
    override val notificationDiagnosisTitle = "Plant Diagnosis Ready"
    override val notificationDiagnosisBody = "Your plant diagnosis is complete. Tap to view results."
    override val notificationChannelName = "Diagnosis Notifications"
    override val notificationChannelDescription = "Notifications for plant diagnosis results"

    // Share/UI strings
    override val shareImageTitle = "Share Image"

    // ============================================================================
    // VOICE MESSAGES
    // ============================================================================
    override val audioNotAvailable = "⚠ Audio not available"
    override val playVoiceMessage = "Play"
    override val pauseVoiceMessage = "Pause"
    override val resumeVoiceMessage = "Resume"

    // ============================================================================
    // FULLSCREEN IMAGE
    // ============================================================================
    override val plantImage = "Plant image"
    override val plantImageFullscreen = "Plant image fullscreen"
    override val healthLabel = "Health: "
    override val issuesDetected = " issue(s) detected"
    override val imageQualityLabel = "Image Quality: "
    override val noImage = "No image"
    override val cropLabel = "Crop"

    // ============================================================================
    // CONTENT DESCRIPTIONS (for accessibility)
    // ============================================================================
    override val cdSettings = "Settings"
    override val cdScrollToBottom = "Scroll to bottom"
    override val cdNewConversation = "New Conversation"
    override val cdDeleteConversation = "Delete conversation"
    override val cdVoiceInput = "Voice input"
    override val cdAttachImage = "Attach image"
    override val cdCamera = "Camera"
    override val cdGallery = "Gallery"
    override val cdSend = "Send"
    override val cdBack = "Back"
    override val cdClose = "Close"
    override val cdCancel = "Cancel"
    override val cdCopy = "Copy"
    override val cdShare = "Share"
    override val cdPlay = "Play"
    override val cdPause = "Pause"
    override val cdSelectedPlantImage = "Selected plant image"
    override val cdPlantImageFullscreen = "Plant image fullscreen"
    override val cdCancelRecording = "Cancel recording"
    override val cdSendRecording = "Send recording"
    override val cdCrop = "Crop"

    // Formatting functions
    override fun formatImageTooLargeWithSize(currentSize: String, maxSize: String): String =
        "Image is too large ($currentSize). Maximum size is $maxSize."

    override fun formatImageDimensionsTooLarge(width: Int, height: Int, maxDimension: Int): String =
        "Image dimensions too large (${width}x${height}). Maximum is ${maxDimension}x${maxDimension}."

    override fun formatUnsupportedImageFormatMimeType(mimeType: String): String =
        "Unsupported image format: $mimeType. Please use JPEG, PNG, or WebP."

    override fun formatUnsupportedImageFormatExtension(extension: String): String =
        "Unsupported image format: .$extension. Please use JPEG, PNG, or WebP."
}

object VietnameseStrings : Strings {
    // ============================================================================
    // APP IDENTITY
    // ============================================================================
    override val appName = "Nông Trí"
    override val appTagline = "Trợ lý AI nông nghiệp"
    override val aiAssistantName = "Nông Trí AI"

    // ============================================================================
    // LANGUAGE SELECTION SCREEN
    // ============================================================================
    override val selectLanguage = "Chọn ngôn ngữ"
    override val selectLanguageBilingual = "Select Language / Chọn ngôn ngữ"
    override val chooseYourLanguage = "Chọn ngôn ngữ ưa thích của bạn"
    override val continue_ = "Tiếp tục"

    // ============================================================================
    // CHAT SCREEN - TOP BAR & MENU
    // ============================================================================
    override val chatTitle = "Nông Trí"
    override val menuConversations = "Cuộc trò chuyện"
    override val menuNewChat = "Trò chuyện mới"
    override val menuShareLocation = "Chia sẻ vị trí"
    override val menuLanguageSection = "Ngôn ngữ"
    override val menuThemeSection = "Giao diện"

    // ============================================================================
    // NAVIGATION & COMMON ACTIONS
    // ============================================================================
    override val back = "Quay lại"
    override val close = "Đóng"
    override val cancel = "Hủy"
    override val ok = "OK"
    override val yes = "Có"
    override val no = "Không"
    override val retry = "Thử lại"
    override val delete = "Xóa"
    override val send = "Gửi"
    override val confirm = "Xác nhận"
    override val dismiss = "Bỏ qua"

    // ============================================================================
    // WELCOME CARD
    // ============================================================================
    override val welcomeMessage = "Xin chào! Tôi là trợ lý AI nông nghiệp của bạn"
    override val welcomeDescription = "Hỏi tôi bất cứ điều gì về cây trồng, vật nuôi, quản lý sâu bệnh hoặc kỹ thuật canh tác. Tôi ở đây để giúp bạn!"
    override val buildingExperience = "Đang xây dựng trải nghiệm..."

    // ============================================================================
    // STARTER QUESTIONS
    // ============================================================================
    override val starterQuestionsHeaderVi = "Gợi ý câu hỏi"
    override val starterQuestionsHeaderEn = "Suggested questions"

    // ============================================================================
    // INPUT BAR
    // ============================================================================
    override val typeMessage = "Nhập tin nhắn của bạn..."
    override val listening = "Đang nghe..."
    override val processing = "Đang xử lý..."
    override val transcribing = "Đang phiên âm..."
    override val tapToSpeak = "Chạm để nói"
    override val releaseToSend = "Thả ra để gửi"
    override val slideToCancelHint = "< Vuốt để hủy"

    // ============================================================================
    // MESSAGE TYPES & LABELS
    // ============================================================================
    override val textMessage = "Tin nhắn văn bản"
    override val voiceMessage = "Tin nhắn thoại"
    override val imageMessage = "Tin nhắn hình ảnh"
    override val userLabel = "Bạn"
    override val aiLabel = "Nông Trí AI"
    override val followUpSectionHeader = "💡 Tiếp theo?"

    // ============================================================================
    // MESSAGE ACTIONS
    // ============================================================================
    override val actionCopy = "Sao chép"
    override val actionShare = "Chia sẻ"
    override val actionListen = "Nghe"
    override val actionListenError = "Nghe (Lỗi - chạm để thử lại)"
    override val actionThumbsUp = "Phản hồi tốt"
    override val actionThumbsDown = "Phản hồi không tốt"
    override val shareAiResponse = "Chia sẻ phản hồi AI"

    // ============================================================================
    // VOICE RECORDING
    // ============================================================================
    override val recording = "Đang ghi âm..."
    override val cancelRecording = "Hủy"
    override val sendRecording = "Gửi bản ghi"

    // ============================================================================
    // IMAGE ACTIONS
    // ============================================================================
    override val attachImage = "Đính kèm hình ảnh"
    override val takePhoto = "Chụp ảnh"
    override val chooseFromGallery = "Chọn từ thư viện"
    override val camera = "Máy ảnh"
    override val gallery = "Thư viện"
    override val selectImageSource = "Chọn nguồn hình ảnh"
    override val captureNewPhoto = "Chụp ảnh mới cây trồng của bạn"
    override val selectExistingPhoto = "Chọn ảnh có sẵn"
    override val imageTipsText = "Để có kết quả tốt nhất, hãy chụp ảnh rõ nét trong ánh sáng tốt với cây trồng lấp đầy phần lớn khung hình."

    // ============================================================================
    // IMAGE PREVIEW & DIAGNOSIS
    // ============================================================================
    override val confirmImage = "Xác nhận hình ảnh"
    override val askQuestionAboutPlant = "Đặt câu hỏi về cây trồng của bạn:"
    override val defaultPlantQuestion = "Tình trạng sức khỏe của cây trồng của tôi như thế nào?"
    override val sendForDiagnosis = "Gửi để chẩn đoán"
    override val diagnosisInfoText = "AI sẽ phân tích hình ảnh cây trồng của bạn và cung cấp chẩn đoán sức khỏe cùng khuyến nghị điều trị."
    override val analyzing = "Đang phân tích..."
    override val analyzingPlantHealth = "Đang phân tích sức khỏe cây trồng..."

    // ============================================================================
    // DIAGNOSIS RESPONSE
    // ============================================================================
    override val healthStatus = "Tình trạng sức khỏe"
    override val detectedIssues = "Vấn đề phát hiện:"
    override val severityLabel = " • Mức độ: "
    override val affectedLabel = "Bị ảnh hưởng: "
    override val growthStage = "Giai đoạn sinh trưởng: "
    override val listenToAdvice = "Nghe lời khuyên"

    // ============================================================================
    // DIAGNOSIS PENDING
    // ============================================================================
    override val analyzingYourCrop = "Đang phân tích ảnh cây trồng của bạn..."
    override val estimatedTime = "Thời gian ước tính: 2-3 phút"
    override val notificationWhenReady = "Chúng tôi sẽ thông báo khi sẵn sàng"
    override val whatsHappening = "Điều gì đang xảy ra?"
    override val diagnosisPendingExplanation = "AI của chúng tôi đang cẩn thận phân tích ảnh cây trồng của bạn để xác định:\n• Loại cây trồng\n• Tình trạng sức khỏe\n• Bệnh hoặc vấn đề nào\n• Khuyến nghị chăm sóc"
    override val diagnosisPendingInfo = "Bạn có thể đóng ứng dụng này và tiếp tục công việc. Chúng tôi sẽ gửi thông báo khi chẩn đoán sẵn sàng!"
    override val jobIdLabel = "Mã công việc: "
    override val plantImageBeingAnalyzed = "Hình ảnh cây trồng đang được phân tích"

    // ============================================================================
    // TIMESTAMPS & RELATIVE TIME
    // ============================================================================
    override val justNow = "Vừa xong"
    override val minutesAgo = " phút trước"
    override val hoursAgo = " giờ trước"
    override val today = "Hôm nay"
    override val yesterday = "Hôm qua"
    override val daysAgo = " ngày trước"
    override val weeksAgo = " tuần trước"
    override val monthsAgo = " tháng trước"

    // ============================================================================
    // CONVERSATIONS LIST SCREEN
    // ============================================================================
    override val conversations = "Cuộc trò chuyện"
    override val newConversation = "Cuộc trò chuyện mới"
    override val noConversationsYet = "Chưa có cuộc trò chuyện nào"
    override val noConversationsHint = "Nhấn + để bắt đầu cuộc trò chuyện mới"
    override val messageCountSuffix = " tin nhắn"
    override val deleteConversation = "Xóa cuộc trò chuyện"
    override val deleteConversationTitle = "Xóa cuộc trò chuyện"
    override val deleteConversationMessage = "Bạn có chắc chắn muốn xóa cuộc trò chuyện này? Hành động này không thể hoàn tác."
    override val deleteConversationConfirm = "Xóa"

    // ============================================================================
    // LOCATION
    // ============================================================================
    override val location = "Vị trí"
    override val shareLocation = "Chia sẻ vị trí"
    override val updateLocation = "Cập nhật vị trí"
    override val shareGpsLocation = "Chia sẻ vị trí GPS"
    override val openSettings = "Mở cài đặt"
    override val detectedLocationIp = "Vị trí phát hiện (IP)"
    override val mySharedLocationGps = "Vị trí chia sẻ của tôi (GPS)"
    override val unableToDetectLocation = "Không thể xác định vị trí"
    override val shareLocationDescription = "Chia sẻ vị trí chính xác của bạn để có dự báo thời tiết và lời khuyên canh tác chính xác hơn."
    override val locationHelpText = "Vị trí của bạn giúp cung cấp dự báo thời tiết và lời khuyên canh tác chính xác cho khu vực của bạn."

    // ============================================================================
    // PERMISSIONS - VOICE
    // ============================================================================
    override val microphonePermission = "Quyền microphone"
    override val voiceRecording = "Ghi âm giọng nói"
    override val microphonePermissionSettingsPrompt = "Cần có quyền microphone để ghi âm tin nhắn thoại. Vui lòng bật trong Cài đặt."
    override val microphonePermissionPrompt = "Cấp quyền microphone để ghi âm và gửi tin nhắn thoại cho trợ lý AI."
    override val grantPermission = "Cấp quyền"
    override val voiceMessageInfoText = "Tin nhắn thoại được phiên âm bằng AI và gửi đến trợ lý để nhận lời khuyên canh tác."

    // ============================================================================
    // PERMISSIONS - CAMERA & PHOTOS
    // ============================================================================
    override val cameraPhotoPermissions = "Quyền máy ảnh & ảnh"
    override val cameraAccess = "Truy cập máy ảnh"
    override val cameraPermissionSettingsPrompt = "Cần có quyền máy ảnh để chụp ảnh cây trồng. Vui lòng bật trong Cài đặt."
    override val cameraPermissionGranted = "Đã cấp quyền máy ảnh ✓"
    override val cameraPermissionPrompt = "Cho phép truy cập máy ảnh để chụp ảnh cây trồng của bạn cho chẩn đoán AI."
    override val photoLibraryAccess = "Truy cập thư viện ảnh"
    override val photoLibraryPermissionSettingsPrompt = "Cần có quyền thư viện ảnh để chọn hình ảnh có sẵn. Vui lòng bật trong Cài đặt."
    override val photoLibraryPermissionGranted = "Đã cấp quyền thư viện ảnh ✓"
    override val photoLibraryPermissionPrompt = "Cho phép truy cập thư viện ảnh để chọn hình ảnh cây trồng có sẵn cho chẩn đoán."
    override val imageUploadInfoText = "Hình ảnh được gửi đến AI của chúng tôi để chẩn đoán sức khỏe cây trồng và khuyến nghị điều trị."
    override val permissionGrantedText = "Đã cấp quyền"

    // ============================================================================
    // THEME
    // ============================================================================
    override val profile = "Hồ sơ"
    override val settings = "Cài đặt"
    override val language = "Ngôn ngữ"
    override val theme = "Giao diện"
    override val light = "Sáng"
    override val dark = "Tối"
    override val system = "Hệ thống"
    override val systemDefault = "Mặc định hệ thống"
    override val about = "Giới thiệu"
    override val version = "Phiên bản"

    // ============================================================================
    // SHARE
    // ============================================================================
    override val shareResponse = "Chia sẻ phản hồi"
    override val shareAsText = "Chia sẻ dưới dạng văn bản"
    override val shareAsImage = "Chia sẻ dưới dạng hình ảnh"
    override val shareViaMessaging = "Chia sẻ qua ứng dụng nhắn tin"
    override val saveOrShareScreenshot = "Lưu hoặc chia sẻ ảnh chụp màn hình"

    // ============================================================================
    // ERROR MESSAGES - GENERIC
    // ============================================================================
    override val errorGeneric = "Đã xảy ra lỗi. Vui lòng thử lại."
    override val errorNetwork = "Không có kết nối internet. Vui lòng kiểm tra mạng của bạn."
    override val errorMicrophone = "Quyền microphone bị từ chối"
    override val errorCamera = "Quyền máy ảnh bị từ chối"
    override val errorPermission = "Yêu cầu quyền"
    override val errorUnknown = "Lỗi không xác định"

    // ============================================================================
    // ERROR MESSAGES - SPECIFIC
    // ============================================================================
    override val errorFailedToSendMessage = "Không gửi được tin nhắn"
    override val errorFailedToLoadConversations = "Không tải được cuộc trò chuyện"
    override val errorFailedToCreateConversation = "Không tạo được cuộc trò chuyện"
    override val errorFailedToDeleteConversation = "Không xóa được cuộc trò chuyện"
    override val errorFailedToArchiveConversation = "Không lưu trữ được cuộc trò chuyện"
    override val errorFailedToSubmitDiagnosis = "Không gửi được chẩn đoán"
    override val errorServerUpdating = "Máy chủ đang cập nhật. Vui lòng thử lại sau vài giây."
    override val errorConnectionFailed = "Không thể kết nối với máy chủ. Kiểm tra kết nối mạng."
    override val errorImageTooLarge = "Hình ảnh quá lớn. Vui lòng thử hình ảnh nhỏ hơn."
    override val errorFailedToProcessImage = "Không xử lý được hình ảnh. Vui lòng thử lại."
    override val errorLoadingConversations = "Lỗi tải cuộc trò chuyện"

    // Diagnosis errors
    override val diagnosisCompleted = "Chẩn đoán hoàn tất"
    override val errorDiagnosisFailed = "Chẩn đoán thất bại"
    override val errorFailedToFetchDiagnosis = "Không lấy được chẩn đoán"
    override val errorFetchingDiagnosis = "Lỗi lấy chẩn đoán"
    override val diagnosisProcessingMessage = "Chẩn đoán của bạn đang được xử lý. Chúng tôi sẽ thông báo khi sẵn sàng!"
    override val errorFailedToFetchStarterQuestions = "Không tải được câu hỏi gợi ý"

    // Voice recording errors
    override val errorRecordingTooShort = "Ghi âm quá ngắn. Vui lòng giữ lâu hơn."
    override val errorRecordingEmpty = "Bản ghi âm trống. Vui lòng thử lại."
    override val errorFailedToStartRecording = "Không bắt đầu được ghi âm"
    override val errorFailedToStopRecording = "Không dừng được ghi âm"
    override val errorTranscriptionFailed = "Phiên âm thất bại"

    // Location errors
    override val errorFailedToInitializeLocation = "Không khởi tạo được vị trí"
    override val errorFailedToGetLocations = "Không lấy được vị trí"
    override val errorFailedToGetSavedLocations = "Không lấy được vị trí đã lưu"
    override val errorFailedToSaveLocation = "Không lưu được vị trí"
    override val errorFailedToShareLocation = "Không chia sẻ được vị trí"
    override val errorFailedToSetPrimaryLocation = "Không đặt được vị trí chính"
    override val errorFailedToDeleteLocation = "Không xóa được vị trí"

    // API/Network errors
    override val errorUploadTimeout = "Tải lên hết thời gian chờ. Có thể do internet chậm. Vui lòng thử hình ảnh nhỏ hơn hoặc đợi và thử lại."
    override val errorNoInternetConnection = "Không có kết nối internet. Vui lòng kiểm tra mạng và thử lại."
    override val errorCannotConnectToServer = "Không kết nối được máy chủ. Vui lòng kiểm tra kết nối internet."
    override val errorUploadFailed = "Tải lên thất bại. Vui lòng thử lại."
    override val errorFailedToSaveVoiceMessage = "Không lưu được tin nhắn thoại"
    override val errorFailedToUpdateAudioUrl = "Không cập nhật được URL âm thanh"
    override val errorFailedToLoadHistory = "Không tải được lịch sử"
    override val errorFailedToLoadThreads = "Không tải được cuộc trò chuyện"
    override val errorFailedToCreateThread = "Không tạo được cuộc trò chuyện"
    override val errorFailedToGetActiveThread = "Không lấy được cuộc trò chuyện đang hoạt động"
    override val errorFailedToLoadThreadMessages = "Không tải được tin nhắn"
    override val errorFailedToUpdateThread = "Không cập nhật được cuộc trò chuyện"
    override val errorFailedToDeleteThread = "Không xóa được cuộc trò chuyện"
    override val errorFailedToRegisterFcmToken = "Không đăng ký được FCM token"

    // Image validation errors
    override val errorImageTooLargeWithSize = "Hình ảnh quá lớn. Kích thước tối đa là 10 MB."
    override val errorImageFileEmpty = "Tệp hình ảnh trống"
    override val errorInvalidImageDimensions = "Kích thước hình ảnh không hợp lệ"
    override val errorImageDimensionsTooLarge = "Kích thước hình ảnh quá lớn. Tối đa là 4096x4096."
    override val errorUnsupportedImageFormatMimeType = "Định dạng hình ảnh không được hỗ trợ. Vui lòng sử dụng JPEG, PNG hoặc WebP."
    override val errorUnsupportedImageFormatExtension = "Định dạng hình ảnh không được hỗ trợ. Vui lòng sử dụng JPEG, PNG hoặc WebP."
    override val errorCannotDetermineImageFormat = "Không xác định được định dạng hình ảnh"

    // Platform-specific errors (Android)
    override val errorCannotAccessImageFile = "Không truy cập được tệp hình ảnh. Vui lòng thử hình ảnh khác."
    override val errorCannotReadImage = "Không đọc được hình ảnh. Tệp có thể bị hỏng hoặc định dạng không được hỗ trợ."
    override val errorCouldNotOpenSettings = "Không mở được cài đặt"
    override val errorLocationPermissionNotGranted = "Chưa cấp quyền vị trí"
    override val errorCouldNotGetCurrentLocation = "Không lấy được vị trí hiện tại"

    // Toast messages (Android)
    override val toastSpeechAlreadyPlaying = "Đang phát giọng nói"
    override val toastTtsError = "Lỗi TTS"

    // Permission messages (Android)
    override val permissionMicrophoneDeniedSettings = "Quyền microphone bị từ chối. Vui lòng bật trong Cài đặt để sử dụng tin nhắn thoại."
    override val permissionMicrophoneRationale = "Cần quyền microphone để ghi âm tin nhắn thoại"
    override val permissionCameraStorageDeniedSettings = "Cần quyền máy ảnh và ảnh. Vui lòng bật trong Cài đặt."
    override val permissionCameraDeniedSettings = "Quyền máy ảnh bị từ chối. Vui lòng bật trong Cài đặt."
    override val permissionCameraRationale = "Cần quyền máy ảnh để chụp ảnh cây trồng"
    override val permissionStorageDeniedSettings = "Quyền lưu trữ bị từ chối. Vui lòng bật trong Cài đặt."
    override val permissionStorageRationale = "Cần quyền lưu trữ để chọn hình ảnh từ thư viện"
    override val permissionLocationDeniedSettings = "Vui lòng bật quyền vị trí trong Cài đặt để chia sẻ vị trí"
    override val permissionLocationRationale = "Cần quyền vị trí để chia sẻ vị trí chính xác"

    // Notification messages (Android)
    override val notificationDiagnosisTitle = "Chẩn đoán cây trồng sẵn sàng"
    override val notificationDiagnosisBody = "Chẩn đoán cây trồng của bạn đã hoàn tất. Nhấn để xem kết quả."
    override val notificationChannelName = "Thông báo chẩn đoán"
    override val notificationChannelDescription = "Thông báo cho kết quả chẩn đoán cây trồng"

    // Share/UI strings
    override val shareImageTitle = "Chia sẻ hình ảnh"

    // ============================================================================
    // VOICE MESSAGES
    // ============================================================================
    override val audioNotAvailable = "⚠ Âm thanh không khả dụng"
    override val playVoiceMessage = "Phát"
    override val pauseVoiceMessage = "Tạm dừng"
    override val resumeVoiceMessage = "Tiếp tục"

    // ============================================================================
    // FULLSCREEN IMAGE
    // ============================================================================
    override val plantImage = "Hình ảnh cây trồng"
    override val plantImageFullscreen = "Hình ảnh cây trồng toàn màn hình"
    override val healthLabel = "Sức khỏe: "
    override val issuesDetected = " vấn đề phát hiện"
    override val imageQualityLabel = "Chất lượng hình ảnh: "
    override val noImage = "Không có hình ảnh"
    override val cropLabel = "Cây trồng"

    // ============================================================================
    // CONTENT DESCRIPTIONS (for accessibility)
    // ============================================================================
    override val cdSettings = "Cài đặt"
    override val cdScrollToBottom = "Cuộn xuống cuối"
    override val cdNewConversation = "Cuộc trò chuyện mới"
    override val cdDeleteConversation = "Xóa cuộc trò chuyện"
    override val cdVoiceInput = "Nhập giọng nói"
    override val cdAttachImage = "Đính kèm hình ảnh"
    override val cdCamera = "Máy ảnh"
    override val cdGallery = "Thư viện"
    override val cdSend = "Gửi"
    override val cdBack = "Quay lại"
    override val cdClose = "Đóng"
    override val cdCancel = "Hủy"
    override val cdCopy = "Sao chép"
    override val cdShare = "Chia sẻ"
    override val cdPlay = "Phát"
    override val cdPause = "Tạm dừng"
    override val cdSelectedPlantImage = "Hình ảnh cây trồng đã chọn"
    override val cdPlantImageFullscreen = "Hình ảnh cây trồng toàn màn hình"
    override val cdCancelRecording = "Hủy ghi âm"
    override val cdSendRecording = "Gửi bản ghi"
    override val cdCrop = "Cây trồng"

    // Formatting functions
    override fun formatImageTooLargeWithSize(currentSize: String, maxSize: String): String =
        "Hình ảnh quá lớn ($currentSize). Kích thước tối đa là $maxSize."

    override fun formatImageDimensionsTooLarge(width: Int, height: Int, maxDimension: Int): String =
        "Kích thước hình ảnh quá lớn (${width}x${height}). Tối đa là ${maxDimension}x${maxDimension}."

    override fun formatUnsupportedImageFormatMimeType(mimeType: String): String =
        "Định dạng hình ảnh không được hỗ trợ: $mimeType. Vui lòng sử dụng JPEG, PNG hoặc WebP."

    override fun formatUnsupportedImageFormatExtension(extension: String): String =
        "Định dạng hình ảnh không được hỗ trợ: .$extension. Vui lòng sử dụng JPEG, PNG hoặc WebP."
}

/**
 * Dynamic Strings implementation that merges API translations with hardcoded fallbacks
 */
class DynamicStrings(
    private val apiTranslations: Map<String, String>,
    private val fallbackStrings: Strings
) : Strings {
    // Helper function to get translation from API or fallback
    private fun t(key: String, fallback: String): String {
        return apiTranslations[key] ?: fallback
    }

    // ============================================================================
    // APP IDENTITY
    // ============================================================================
    override val appName get() = t("app_name", fallbackStrings.appName)
    override val appTagline get() = t("app_tagline", fallbackStrings.appTagline)
    override val aiAssistantName get() = t("ai_assistant_name", fallbackStrings.aiAssistantName)

    // ============================================================================
    // LANGUAGE SELECTION SCREEN
    // ============================================================================
    override val selectLanguage get() = t("select_language", fallbackStrings.selectLanguage)
    override val selectLanguageBilingual get() = t("select_language_bilingual", fallbackStrings.selectLanguageBilingual)
    override val chooseYourLanguage get() = t("choose_your_language", fallbackStrings.chooseYourLanguage)
    override val continue_ get() = t("continue", fallbackStrings.continue_)

    // ============================================================================
    // CHAT SCREEN - TOP BAR & MENU
    // ============================================================================
    override val chatTitle get() = t("chat_title", fallbackStrings.chatTitle)
    override val menuConversations get() = t("menu_conversations", fallbackStrings.menuConversations)
    override val menuNewChat get() = t("menu_new_chat", fallbackStrings.menuNewChat)
    override val menuShareLocation get() = t("menu_share_location", fallbackStrings.menuShareLocation)
    override val menuLanguageSection get() = t("menu_language_section", fallbackStrings.menuLanguageSection)
    override val menuThemeSection get() = t("menu_theme_section", fallbackStrings.menuThemeSection)

    // ============================================================================
    // NAVIGATION & COMMON ACTIONS
    // ============================================================================
    override val back get() = t("back", fallbackStrings.back)
    override val close get() = t("close", fallbackStrings.close)
    override val cancel get() = t("cancel", fallbackStrings.cancel)
    override val ok get() = t("ok", fallbackStrings.ok)
    override val yes get() = t("yes", fallbackStrings.yes)
    override val no get() = t("no", fallbackStrings.no)
    override val retry get() = t("retry", fallbackStrings.retry)
    override val delete get() = t("delete", fallbackStrings.delete)
    override val send get() = t("send", fallbackStrings.send)
    override val confirm get() = t("confirm", fallbackStrings.confirm)
    override val dismiss get() = t("dismiss", fallbackStrings.dismiss)

    // ============================================================================
    // WELCOME CARD
    // ============================================================================
    override val welcomeMessage get() = t("welcome_message", fallbackStrings.welcomeMessage)
    override val welcomeDescription get() = t("welcome_description", fallbackStrings.welcomeDescription)
    override val buildingExperience get() = t("building_experience", fallbackStrings.buildingExperience)

    // ============================================================================
    // STARTER QUESTIONS
    // ============================================================================
    override val starterQuestionsHeaderVi get() = t("starter_questions_header_vi", fallbackStrings.starterQuestionsHeaderVi)
    override val starterQuestionsHeaderEn get() = t("starter_questions_header_en", fallbackStrings.starterQuestionsHeaderEn)

    // ============================================================================
    // INPUT BAR
    // ============================================================================
    override val typeMessage get() = t("type_message", fallbackStrings.typeMessage)
    override val listening get() = t("listening", fallbackStrings.listening)
    override val processing get() = t("processing", fallbackStrings.processing)
    override val transcribing get() = t("transcribing", fallbackStrings.transcribing)
    override val tapToSpeak get() = t("tap_to_speak", fallbackStrings.tapToSpeak)
    override val releaseToSend get() = t("release_to_send", fallbackStrings.releaseToSend)
    override val slideToCancelHint get() = t("slide_to_cancel_hint", fallbackStrings.slideToCancelHint)

    // ============================================================================
    // MESSAGE TYPES & LABELS
    // ============================================================================
    override val textMessage get() = t("text_message", fallbackStrings.textMessage)
    override val voiceMessage get() = t("voice_message", fallbackStrings.voiceMessage)
    override val imageMessage get() = t("image_message", fallbackStrings.imageMessage)
    override val userLabel get() = t("user_label", fallbackStrings.userLabel)
    override val aiLabel get() = t("ai_label", fallbackStrings.aiLabel)
    override val followUpSectionHeader get() = t("follow_up_section_header", fallbackStrings.followUpSectionHeader)

    // ============================================================================
    // MESSAGE ACTIONS
    // ============================================================================
    override val actionCopy get() = t("action_copy", fallbackStrings.actionCopy)
    override val actionShare get() = t("action_share", fallbackStrings.actionShare)
    override val actionListen get() = t("action_listen", fallbackStrings.actionListen)
    override val actionListenError get() = t("action_listen_error", fallbackStrings.actionListenError)
    override val actionThumbsUp get() = t("action_thumbs_up", fallbackStrings.actionThumbsUp)
    override val actionThumbsDown get() = t("action_thumbs_down", fallbackStrings.actionThumbsDown)
    override val shareAiResponse get() = t("share_ai_response", fallbackStrings.shareAiResponse)

    // ============================================================================
    // VOICE RECORDING
    // ============================================================================
    override val recording get() = t("recording", fallbackStrings.recording)
    override val cancelRecording get() = t("cancel_recording", fallbackStrings.cancelRecording)
    override val sendRecording get() = t("send_recording", fallbackStrings.sendRecording)

    // ============================================================================
    // IMAGE ACTIONS
    // ============================================================================
    override val attachImage get() = t("attach_image", fallbackStrings.attachImage)
    override val takePhoto get() = t("take_photo", fallbackStrings.takePhoto)
    override val chooseFromGallery get() = t("choose_from_gallery", fallbackStrings.chooseFromGallery)
    override val camera get() = t("camera", fallbackStrings.camera)
    override val gallery get() = t("gallery", fallbackStrings.gallery)
    override val selectImageSource get() = t("select_image_source", fallbackStrings.selectImageSource)
    override val captureNewPhoto get() = t("capture_new_photo", fallbackStrings.captureNewPhoto)
    override val selectExistingPhoto get() = t("select_existing_photo", fallbackStrings.selectExistingPhoto)
    override val imageTipsText get() = t("image_tips_text", fallbackStrings.imageTipsText)

    // ============================================================================
    // IMAGE PREVIEW & DIAGNOSIS
    // ============================================================================
    override val confirmImage get() = t("confirm_image", fallbackStrings.confirmImage)
    override val askQuestionAboutPlant get() = t("ask_question_about_plant", fallbackStrings.askQuestionAboutPlant)
    override val defaultPlantQuestion get() = t("default_plant_question", fallbackStrings.defaultPlantQuestion)
    override val sendForDiagnosis get() = t("send_for_diagnosis", fallbackStrings.sendForDiagnosis)
    override val diagnosisInfoText get() = t("diagnosis_info_text", fallbackStrings.diagnosisInfoText)
    override val analyzing get() = t("analyzing", fallbackStrings.analyzing)
    override val analyzingPlantHealth get() = t("analyzing_plant_health", fallbackStrings.analyzingPlantHealth)

    // ============================================================================
    // DIAGNOSIS RESPONSE
    // ============================================================================
    override val healthStatus get() = t("health_status", fallbackStrings.healthStatus)
    override val detectedIssues get() = t("detected_issues", fallbackStrings.detectedIssues)
    override val severityLabel get() = t("severity_label", fallbackStrings.severityLabel)
    override val affectedLabel get() = t("affected_label", fallbackStrings.affectedLabel)
    override val growthStage get() = t("growth_stage", fallbackStrings.growthStage)
    override val listenToAdvice get() = t("listen_to_advice", fallbackStrings.listenToAdvice)

    // ============================================================================
    // DIAGNOSIS PENDING
    // ============================================================================
    override val analyzingYourCrop get() = t("analyzing_your_crop", fallbackStrings.analyzingYourCrop)
    override val estimatedTime get() = t("estimated_time", fallbackStrings.estimatedTime)
    override val notificationWhenReady get() = t("notification_when_ready", fallbackStrings.notificationWhenReady)
    override val whatsHappening get() = t("whats_happening", fallbackStrings.whatsHappening)
    override val diagnosisPendingExplanation get() = t("diagnosis_pending_explanation", fallbackStrings.diagnosisPendingExplanation)
    override val diagnosisPendingInfo get() = t("diagnosis_pending_info", fallbackStrings.diagnosisPendingInfo)
    override val jobIdLabel get() = t("job_id_label", fallbackStrings.jobIdLabel)
    override val plantImageBeingAnalyzed get() = t("plant_image_being_analyzed", fallbackStrings.plantImageBeingAnalyzed)

    // ============================================================================
    // TIMESTAMPS & RELATIVE TIME
    // ============================================================================
    override val justNow get() = t("just_now", fallbackStrings.justNow)
    override val minutesAgo get() = t("minutes_ago", fallbackStrings.minutesAgo)
    override val hoursAgo get() = t("hours_ago", fallbackStrings.hoursAgo)
    override val today get() = t("today", fallbackStrings.today)
    override val yesterday get() = t("yesterday", fallbackStrings.yesterday)
    override val daysAgo get() = t("days_ago", fallbackStrings.daysAgo)
    override val weeksAgo get() = t("weeks_ago", fallbackStrings.weeksAgo)
    override val monthsAgo get() = t("months_ago", fallbackStrings.monthsAgo)

    // ============================================================================
    // CONVERSATIONS LIST SCREEN
    // ============================================================================
    override val conversations get() = t("conversations", fallbackStrings.conversations)
    override val newConversation get() = t("new_conversation", fallbackStrings.newConversation)
    override val noConversationsYet get() = t("no_conversations_yet", fallbackStrings.noConversationsYet)
    override val noConversationsHint get() = t("no_conversations_hint", fallbackStrings.noConversationsHint)
    override val messageCountSuffix get() = t("message_count_suffix", fallbackStrings.messageCountSuffix)
    override val deleteConversation get() = t("delete_conversation", fallbackStrings.deleteConversation)
    override val deleteConversationTitle get() = t("delete_conversation_title", fallbackStrings.deleteConversationTitle)
    override val deleteConversationMessage get() = t("delete_conversation_message", fallbackStrings.deleteConversationMessage)
    override val deleteConversationConfirm get() = t("delete_conversation_confirm", fallbackStrings.deleteConversationConfirm)

    // ============================================================================
    // LOCATION
    // ============================================================================
    override val location get() = t("location", fallbackStrings.location)
    override val shareLocation get() = t("share_location", fallbackStrings.shareLocation)
    override val updateLocation get() = t("update_location", fallbackStrings.updateLocation)
    override val shareGpsLocation get() = t("share_gps_location", fallbackStrings.shareGpsLocation)
    override val openSettings get() = t("open_settings", fallbackStrings.openSettings)
    override val detectedLocationIp get() = t("detected_location_ip", fallbackStrings.detectedLocationIp)
    override val mySharedLocationGps get() = t("my_shared_location_gps", fallbackStrings.mySharedLocationGps)
    override val unableToDetectLocation get() = t("unable_to_detect_location", fallbackStrings.unableToDetectLocation)
    override val shareLocationDescription get() = t("share_location_description", fallbackStrings.shareLocationDescription)
    override val locationHelpText get() = t("location_help_text", fallbackStrings.locationHelpText)

    // ============================================================================
    // PERMISSIONS - VOICE
    // ============================================================================
    override val microphonePermission get() = t("microphone_permission", fallbackStrings.microphonePermission)
    override val voiceRecording get() = t("voice_recording", fallbackStrings.voiceRecording)
    override val microphonePermissionSettingsPrompt get() = t("microphone_permission_settings_prompt", fallbackStrings.microphonePermissionSettingsPrompt)
    override val microphonePermissionPrompt get() = t("microphone_permission_prompt", fallbackStrings.microphonePermissionPrompt)
    override val grantPermission get() = t("grant_permission", fallbackStrings.grantPermission)
    override val voiceMessageInfoText get() = t("voice_message_info_text", fallbackStrings.voiceMessageInfoText)

    // ============================================================================
    // PERMISSIONS - CAMERA & PHOTOS
    // ============================================================================
    override val cameraPhotoPermissions get() = t("camera_photo_permissions", fallbackStrings.cameraPhotoPermissions)
    override val cameraAccess get() = t("camera_access", fallbackStrings.cameraAccess)
    override val cameraPermissionSettingsPrompt get() = t("camera_permission_settings_prompt", fallbackStrings.cameraPermissionSettingsPrompt)
    override val cameraPermissionGranted get() = t("camera_permission_granted", fallbackStrings.cameraPermissionGranted)
    override val cameraPermissionPrompt get() = t("camera_permission_prompt", fallbackStrings.cameraPermissionPrompt)
    override val photoLibraryAccess get() = t("photo_library_access", fallbackStrings.photoLibraryAccess)
    override val photoLibraryPermissionSettingsPrompt get() = t("photo_library_permission_settings_prompt", fallbackStrings.photoLibraryPermissionSettingsPrompt)
    override val photoLibraryPermissionGranted get() = t("photo_library_permission_granted", fallbackStrings.photoLibraryPermissionGranted)
    override val photoLibraryPermissionPrompt get() = t("photo_library_permission_prompt", fallbackStrings.photoLibraryPermissionPrompt)
    override val imageUploadInfoText get() = t("image_upload_info_text", fallbackStrings.imageUploadInfoText)
    override val permissionGrantedText get() = t("permission_granted_text", fallbackStrings.permissionGrantedText)

    // ============================================================================
    // THEME
    // ============================================================================
    override val profile get() = t("profile", fallbackStrings.profile)
    override val settings get() = t("settings", fallbackStrings.settings)
    override val language get() = t("language", fallbackStrings.language)
    override val theme get() = t("theme", fallbackStrings.theme)
    override val light get() = t("light", fallbackStrings.light)
    override val dark get() = t("dark", fallbackStrings.dark)
    override val system get() = t("system", fallbackStrings.system)
    override val systemDefault get() = t("system_default", fallbackStrings.systemDefault)
    override val about get() = t("about", fallbackStrings.about)
    override val version get() = t("version", fallbackStrings.version)

    // ============================================================================
    // SHARE
    // ============================================================================
    override val shareResponse get() = t("share_response", fallbackStrings.shareResponse)
    override val shareAsText get() = t("share_as_text", fallbackStrings.shareAsText)
    override val shareAsImage get() = t("share_as_image", fallbackStrings.shareAsImage)
    override val shareViaMessaging get() = t("share_via_messaging", fallbackStrings.shareViaMessaging)
    override val saveOrShareScreenshot get() = t("save_or_share_screenshot", fallbackStrings.saveOrShareScreenshot)

    // ============================================================================
    // ERROR MESSAGES - GENERIC
    // ============================================================================
    override val errorGeneric get() = t("error_generic", fallbackStrings.errorGeneric)
    override val errorNetwork get() = t("error_network", fallbackStrings.errorNetwork)
    override val errorMicrophone get() = t("error_microphone", fallbackStrings.errorMicrophone)
    override val errorCamera get() = t("error_camera", fallbackStrings.errorCamera)
    override val errorPermission get() = t("error_permission", fallbackStrings.errorPermission)
    override val errorUnknown get() = t("error_unknown", fallbackStrings.errorUnknown)

    // ============================================================================
    // ERROR MESSAGES - SPECIFIC
    // ============================================================================
    override val errorFailedToSendMessage get() = t("error_failed_to_send_message", fallbackStrings.errorFailedToSendMessage)
    override val errorFailedToLoadConversations get() = t("error_failed_to_load_conversations", fallbackStrings.errorFailedToLoadConversations)
    override val errorFailedToCreateConversation get() = t("error_failed_to_create_conversation", fallbackStrings.errorFailedToCreateConversation)
    override val errorFailedToDeleteConversation get() = t("error_failed_to_delete_conversation", fallbackStrings.errorFailedToDeleteConversation)
    override val errorServerUpdating get() = t("error_server_updating", fallbackStrings.errorServerUpdating)
    override val errorConnectionFailed get() = t("error_connection_failed", fallbackStrings.errorConnectionFailed)
    override val errorFailedToArchiveConversation get() = t("error_failed_to_archive_conversation", fallbackStrings.errorFailedToArchiveConversation)
    override val errorFailedToSubmitDiagnosis get() = t("error_failed_to_submit_diagnosis", fallbackStrings.errorFailedToSubmitDiagnosis)
    override val errorImageTooLarge get() = t("error_image_too_large", fallbackStrings.errorImageTooLarge)
    override val errorFailedToProcessImage get() = t("error_failed_to_process_image", fallbackStrings.errorFailedToProcessImage)
    override val errorLoadingConversations get() = t("error_loading_conversations", fallbackStrings.errorLoadingConversations)

    // Diagnosis errors
    override val diagnosisCompleted get() = t("diagnosis_completed", fallbackStrings.diagnosisCompleted)
    override val errorDiagnosisFailed get() = t("error_diagnosis_failed", fallbackStrings.errorDiagnosisFailed)
    override val errorFailedToFetchDiagnosis get() = t("error_failed_to_fetch_diagnosis", fallbackStrings.errorFailedToFetchDiagnosis)
    override val errorFetchingDiagnosis get() = t("error_fetching_diagnosis", fallbackStrings.errorFetchingDiagnosis)
    override val diagnosisProcessingMessage get() = t("diagnosis_processing_message", fallbackStrings.diagnosisProcessingMessage)
    override val errorFailedToFetchStarterQuestions get() = t("error_failed_to_fetch_starter_questions", fallbackStrings.errorFailedToFetchStarterQuestions)

    // Voice recording errors
    override val errorRecordingTooShort get() = t("error_recording_too_short", fallbackStrings.errorRecordingTooShort)
    override val errorRecordingEmpty get() = t("error_recording_empty", fallbackStrings.errorRecordingEmpty)
    override val errorFailedToStartRecording get() = t("error_failed_to_start_recording", fallbackStrings.errorFailedToStartRecording)
    override val errorFailedToStopRecording get() = t("error_failed_to_stop_recording", fallbackStrings.errorFailedToStopRecording)
    override val errorTranscriptionFailed get() = t("error_transcription_failed", fallbackStrings.errorTranscriptionFailed)

    // Location errors
    override val errorFailedToInitializeLocation get() = t("error_failed_to_initialize_location", fallbackStrings.errorFailedToInitializeLocation)
    override val errorFailedToGetLocations get() = t("error_failed_to_get_locations", fallbackStrings.errorFailedToGetLocations)
    override val errorFailedToGetSavedLocations get() = t("error_failed_to_get_saved_locations", fallbackStrings.errorFailedToGetSavedLocations)
    override val errorFailedToSaveLocation get() = t("error_failed_to_save_location", fallbackStrings.errorFailedToSaveLocation)
    override val errorFailedToShareLocation get() = t("error_failed_to_share_location", fallbackStrings.errorFailedToShareLocation)
    override val errorFailedToSetPrimaryLocation get() = t("error_failed_to_set_primary_location", fallbackStrings.errorFailedToSetPrimaryLocation)
    override val errorFailedToDeleteLocation get() = t("error_failed_to_delete_location", fallbackStrings.errorFailedToDeleteLocation)

    // API/Network errors
    override val errorUploadTimeout get() = t("error_upload_timeout", fallbackStrings.errorUploadTimeout)
    override val errorNoInternetConnection get() = t("error_no_internet_connection", fallbackStrings.errorNoInternetConnection)
    override val errorCannotConnectToServer get() = t("error_cannot_connect_to_server", fallbackStrings.errorCannotConnectToServer)
    override val errorUploadFailed get() = t("error_upload_failed", fallbackStrings.errorUploadFailed)
    override val errorFailedToSaveVoiceMessage get() = t("error_failed_to_save_voice_message", fallbackStrings.errorFailedToSaveVoiceMessage)
    override val errorFailedToUpdateAudioUrl get() = t("error_failed_to_update_audio_url", fallbackStrings.errorFailedToUpdateAudioUrl)
    override val errorFailedToLoadHistory get() = t("error_failed_to_load_history", fallbackStrings.errorFailedToLoadHistory)
    override val errorFailedToLoadThreads get() = t("error_failed_to_load_threads", fallbackStrings.errorFailedToLoadThreads)
    override val errorFailedToCreateThread get() = t("error_failed_to_create_thread", fallbackStrings.errorFailedToCreateThread)
    override val errorFailedToGetActiveThread get() = t("error_failed_to_get_active_thread", fallbackStrings.errorFailedToGetActiveThread)
    override val errorFailedToLoadThreadMessages get() = t("error_failed_to_load_thread_messages", fallbackStrings.errorFailedToLoadThreadMessages)
    override val errorFailedToUpdateThread get() = t("error_failed_to_update_thread", fallbackStrings.errorFailedToUpdateThread)
    override val errorFailedToDeleteThread get() = t("error_failed_to_delete_thread", fallbackStrings.errorFailedToDeleteThread)
    override val errorFailedToRegisterFcmToken get() = t("error_failed_to_register_fcm_token", fallbackStrings.errorFailedToRegisterFcmToken)

    // Image validation errors
    override val errorImageTooLargeWithSize get() = t("error_image_too_large_with_size", fallbackStrings.errorImageTooLargeWithSize)
    override val errorImageFileEmpty get() = t("error_image_file_empty", fallbackStrings.errorImageFileEmpty)
    override val errorInvalidImageDimensions get() = t("error_invalid_image_dimensions", fallbackStrings.errorInvalidImageDimensions)
    override val errorImageDimensionsTooLarge get() = t("error_image_dimensions_too_large", fallbackStrings.errorImageDimensionsTooLarge)
    override val errorUnsupportedImageFormatMimeType get() = t("error_unsupported_image_format_mime_type", fallbackStrings.errorUnsupportedImageFormatMimeType)
    override val errorUnsupportedImageFormatExtension get() = t("error_unsupported_image_format_extension", fallbackStrings.errorUnsupportedImageFormatExtension)
    override val errorCannotDetermineImageFormat get() = t("error_cannot_determine_image_format", fallbackStrings.errorCannotDetermineImageFormat)

    // Platform-specific errors (Android)
    override val errorCannotAccessImageFile get() = t("error_cannot_access_image_file", fallbackStrings.errorCannotAccessImageFile)
    override val errorCannotReadImage get() = t("error_cannot_read_image", fallbackStrings.errorCannotReadImage)
    override val errorCouldNotOpenSettings get() = t("error_could_not_open_settings", fallbackStrings.errorCouldNotOpenSettings)
    override val errorLocationPermissionNotGranted get() = t("error_location_permission_not_granted", fallbackStrings.errorLocationPermissionNotGranted)
    override val errorCouldNotGetCurrentLocation get() = t("error_could_not_get_current_location", fallbackStrings.errorCouldNotGetCurrentLocation)

    // Toast messages (Android)
    override val toastSpeechAlreadyPlaying get() = t("toast_speech_already_playing", fallbackStrings.toastSpeechAlreadyPlaying)
    override val toastTtsError get() = t("toast_tts_error", fallbackStrings.toastTtsError)

    // Permission messages (Android)
    override val permissionMicrophoneDeniedSettings get() = t("permission_microphone_denied_settings", fallbackStrings.permissionMicrophoneDeniedSettings)
    override val permissionMicrophoneRationale get() = t("permission_microphone_rationale", fallbackStrings.permissionMicrophoneRationale)
    override val permissionCameraStorageDeniedSettings get() = t("permission_camera_storage_denied_settings", fallbackStrings.permissionCameraStorageDeniedSettings)
    override val permissionCameraDeniedSettings get() = t("permission_camera_denied_settings", fallbackStrings.permissionCameraDeniedSettings)
    override val permissionCameraRationale get() = t("permission_camera_rationale", fallbackStrings.permissionCameraRationale)
    override val permissionStorageDeniedSettings get() = t("permission_storage_denied_settings", fallbackStrings.permissionStorageDeniedSettings)
    override val permissionStorageRationale get() = t("permission_storage_rationale", fallbackStrings.permissionStorageRationale)
    override val permissionLocationDeniedSettings get() = t("permission_location_denied_settings", fallbackStrings.permissionLocationDeniedSettings)
    override val permissionLocationRationale get() = t("permission_location_rationale", fallbackStrings.permissionLocationRationale)

    // Notification messages (Android)
    override val notificationDiagnosisTitle get() = t("notification_diagnosis_title", fallbackStrings.notificationDiagnosisTitle)
    override val notificationDiagnosisBody get() = t("notification_diagnosis_body", fallbackStrings.notificationDiagnosisBody)
    override val notificationChannelName get() = t("notification_channel_name", fallbackStrings.notificationChannelName)
    override val notificationChannelDescription get() = t("notification_channel_description", fallbackStrings.notificationChannelDescription)

    // Share/UI strings
    override val shareImageTitle get() = t("share_image_title", fallbackStrings.shareImageTitle)

    // ============================================================================
    // VOICE MESSAGES
    // ============================================================================
    override val audioNotAvailable get() = t("audio_not_available", fallbackStrings.audioNotAvailable)
    override val playVoiceMessage get() = t("play_voice_message", fallbackStrings.playVoiceMessage)
    override val pauseVoiceMessage get() = t("pause_voice_message", fallbackStrings.pauseVoiceMessage)
    override val resumeVoiceMessage get() = t("resume_voice_message", fallbackStrings.resumeVoiceMessage)

    // ============================================================================
    // FULLSCREEN IMAGE
    // ============================================================================
    override val plantImage get() = t("plant_image", fallbackStrings.plantImage)
    override val plantImageFullscreen get() = t("plant_image_fullscreen", fallbackStrings.plantImageFullscreen)
    override val healthLabel get() = t("health_label", fallbackStrings.healthLabel)
    override val issuesDetected get() = t("issues_detected", fallbackStrings.issuesDetected)
    override val imageQualityLabel get() = t("image_quality_label", fallbackStrings.imageQualityLabel)
    override val noImage get() = t("no_image", fallbackStrings.noImage)
    override val cropLabel get() = t("crop_label", fallbackStrings.cropLabel)

    // ============================================================================
    // CONTENT DESCRIPTIONS (for accessibility)
    // ============================================================================
    override val cdSettings get() = t("cd_settings", fallbackStrings.cdSettings)
    override val cdScrollToBottom get() = t("cd_scroll_to_bottom", fallbackStrings.cdScrollToBottom)
    override val cdNewConversation get() = t("cd_new_conversation", fallbackStrings.cdNewConversation)
    override val cdDeleteConversation get() = t("cd_delete_conversation", fallbackStrings.cdDeleteConversation)
    override val cdVoiceInput get() = t("cd_voice_input", fallbackStrings.cdVoiceInput)
    override val cdAttachImage get() = t("cd_attach_image", fallbackStrings.cdAttachImage)
    override val cdCamera get() = t("cd_camera", fallbackStrings.cdCamera)
    override val cdGallery get() = t("cd_gallery", fallbackStrings.cdGallery)
    override val cdSend get() = t("cd_send", fallbackStrings.cdSend)
    override val cdBack get() = t("cd_back", fallbackStrings.cdBack)
    override val cdClose get() = t("cd_close", fallbackStrings.cdClose)
    override val cdCancel get() = t("cd_cancel", fallbackStrings.cdCancel)
    override val cdCopy get() = t("cd_copy", fallbackStrings.cdCopy)
    override val cdShare get() = t("cd_share", fallbackStrings.cdShare)
    override val cdPlay get() = t("cd_play", fallbackStrings.cdPlay)
    override val cdPause get() = t("cd_pause", fallbackStrings.cdPause)
    override val cdSelectedPlantImage get() = t("cd_selected_plant_image", fallbackStrings.cdSelectedPlantImage)
    override val cdPlantImageFullscreen get() = t("cd_plant_image_fullscreen", fallbackStrings.cdPlantImageFullscreen)
    override val cdCancelRecording get() = t("cd_cancel_recording", fallbackStrings.cdCancelRecording)
    override val cdSendRecording get() = t("cd_send_recording", fallbackStrings.cdSendRecording)
    override val cdCrop get() = t("cd_crop", fallbackStrings.cdCrop)

    // Formatting functions - delegate to fallback
    override fun formatImageTooLargeWithSize(currentSize: String, maxSize: String): String =
        fallbackStrings.formatImageTooLargeWithSize(currentSize, maxSize)

    override fun formatImageDimensionsTooLarge(width: Int, height: Int, maxDimension: Int): String =
        fallbackStrings.formatImageDimensionsTooLarge(width, height, maxDimension)

    override fun formatUnsupportedImageFormatMimeType(mimeType: String): String =
        fallbackStrings.formatUnsupportedImageFormatMimeType(mimeType)

    override fun formatUnsupportedImageFormatExtension(extension: String): String =
        fallbackStrings.formatUnsupportedImageFormatExtension(extension)
}

object LocalizationProvider {
    // Store API translations per language
    private val apiTranslations = mutableMapOf<Language, Map<String, String>>()

    /**
     * Set API translations for a language
     * Called when translations are fetched from the API
     */
    fun setApiTranslations(language: Language, translations: Map<String, String>) {
        apiTranslations[language] = translations
        println("📝 Set ${translations.size} API translations for ${language.displayName}")
    }

    /**
     * Clear API translations for a language
     */
    fun clearApiTranslations(language: Language) {
        apiTranslations.remove(language)
        println("🗑️ Cleared API translations for ${language.displayName}")
    }

    /**
     * Get strings for a language
     * Returns DynamicStrings that merges API translations with hardcoded fallbacks
     */
    fun getStrings(language: Language): Strings {
        val fallback = when (language) {
            Language.ENGLISH -> EnglishStrings
            Language.VIETNAMESE -> VietnameseStrings
        }

        val api = apiTranslations[language]
        return if (api != null && api.isNotEmpty()) {
            DynamicStrings(api, fallback)
        } else {
            fallback
        }
    }
}
