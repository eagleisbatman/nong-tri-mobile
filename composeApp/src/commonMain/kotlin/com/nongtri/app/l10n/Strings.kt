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
    val errorLoadingConversations: String

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
    override val errorImageTooLarge = "Image is too large. Please try a smaller image."
    override val errorFailedToProcessImage = "Failed to process image. Please try again."
    override val errorLoadingConversations = "Error loading conversations"

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
    override val errorImageTooLarge = "Hình ảnh quá lớn. Vui lòng thử hình ảnh nhỏ hơn."
    override val errorFailedToProcessImage = "Không xử lý được hình ảnh. Vui lòng thử lại."
    override val errorLoadingConversations = "Lỗi tải cuộc trò chuyện"

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
}

object LocalizationProvider {
    fun getStrings(language: Language): Strings {
        return when (language) {
            Language.ENGLISH -> EnglishStrings
            Language.VIETNAMESE -> VietnameseStrings
        }
    }
}
