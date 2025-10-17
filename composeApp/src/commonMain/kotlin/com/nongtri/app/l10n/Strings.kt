package com.nongtri.app.l10n

enum class Language(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    VIETNAMESE("vi", "Tiếng Việt")
}

interface Strings {
    // App
    val appName: String
    val appTagline: String

    // Language Selection
    val selectLanguage: String
    val chooseYourLanguage: String
    val continue_: String

    // Chat Screen
    val chatTitle: String
    val welcomeMessage: String
    val welcomeDescription: String
    val typeMessage: String
    val send: String
    val listening: String
    val processing: String
    val tapToSpeak: String
    val releaseToSend: String
    val attachImage: String
    val takePhoto: String
    val chooseFromGallery: String
    val cancel: String

    // Message Types
    val textMessage: String
    val voiceMessage: String
    val imageMessage: String

    // Profile
    val profile: String
    val settings: String
    val language: String
    val theme: String
    val light: String
    val dark: String
    val system: String
    val about: String
    val version: String

    // Errors
    val errorGeneric: String
    val errorNetwork: String
    val errorMicrophone: String
    val errorCamera: String
    val errorPermission: String

    // Common
    val ok: String
    val yes: String
    val no: String
    val retry: String
}

object EnglishStrings : Strings {
    override val appName = "Nông Trí"
    override val appTagline = "Your AI Farming Assistant"

    override val selectLanguage = "Select Language"
    override val chooseYourLanguage = "Choose your preferred language"
    override val continue_ = "Continue"

    override val chatTitle = "Nông Trí"
    override val welcomeMessage = "Hello! I'm your AI farming assistant"
    override val welcomeDescription = "Ask me anything about crops, livestock, pest management, or farming techniques. I'm here to help!"
    override val typeMessage = "Type your message..."
    override val send = "Send"
    override val listening = "Listening..."
    override val processing = "Processing..."
    override val tapToSpeak = "Tap to speak"
    override val releaseToSend = "Release to send"
    override val attachImage = "Attach image"
    override val takePhoto = "Take photo"
    override val chooseFromGallery = "Choose from gallery"
    override val cancel = "Cancel"

    override val textMessage = "Text message"
    override val voiceMessage = "Voice message"
    override val imageMessage = "Image message"

    override val profile = "Profile"
    override val settings = "Settings"
    override val language = "Language"
    override val theme = "Theme"
    override val light = "Light"
    override val dark = "Dark"
    override val system = "System default"
    override val about = "About"
    override val version = "Version"

    override val errorGeneric = "Something went wrong. Please try again."
    override val errorNetwork = "Network error. Please check your connection."
    override val errorMicrophone = "Microphone access denied"
    override val errorCamera = "Camera access denied"
    override val errorPermission = "Permission required"

    override val ok = "OK"
    override val yes = "Yes"
    override val no = "No"
    override val retry = "Retry"
}

object VietnameseStrings : Strings {
    override val appName = "Nông Trí"
    override val appTagline = "Trợ lý AI nông nghiệp của bạn"

    override val selectLanguage = "Chọn ngôn ngữ"
    override val chooseYourLanguage = "Chọn ngôn ngữ ưa thích của bạn"
    override val continue_ = "Tiếp tục"

    override val chatTitle = "Nông Trí"
    override val welcomeMessage = "Xin chào! Tôi là trợ lý AI nông nghiệp của bạn"
    override val welcomeDescription = "Hỏi tôi bất cứ điều gì về cây trồng, vật nuôi, quản lý sâu bệnh hoặc kỹ thuật canh tác. Tôi ở đây để giúp bạn!"
    override val typeMessage = "Nhập tin nhắn của bạn..."
    override val send = "Gửi"
    override val listening = "Đang nghe..."
    override val processing = "Đang xử lý..."
    override val tapToSpeak = "Chạm để nói"
    override val releaseToSend = "Thả ra để gửi"
    override val attachImage = "Đính kèm hình ảnh"
    override val takePhoto = "Chụp ảnh"
    override val chooseFromGallery = "Chọn từ thư viện"
    override val cancel = "Hủy"

    override val textMessage = "Tin nhắn văn bản"
    override val voiceMessage = "Tin nhắn thoại"
    override val imageMessage = "Tin nhắn hình ảnh"

    override val profile = "Hồ sơ"
    override val settings = "Cài đặt"
    override val language = "Ngôn ngữ"
    override val theme = "Giao diện"
    override val light = "Sáng"
    override val dark = "Tối"
    override val system = "Mặc định hệ thống"
    override val about = "Giới thiệu"
    override val version = "Phiên bản"

    override val errorGeneric = "Đã xảy ra lỗi. Vui lòng thử lại."
    override val errorNetwork = "Lỗi mạng. Vui lòng kiểm tra kết nối của bạn."
    override val errorMicrophone = "Quyền truy cập microphone bị từ chối"
    override val errorCamera = "Quyền truy cập camera bị từ chối"
    override val errorPermission = "Yêu cầu quyền truy cập"

    override val ok = "OK"
    override val yes = "Có"
    override val no = "Không"
    override val retry = "Thử lại"
}

object LocalizationProvider {
    fun getStrings(language: Language): Strings {
        return when (language) {
            Language.ENGLISH -> EnglishStrings
            Language.VIETNAMESE -> VietnameseStrings
        }
    }
}
