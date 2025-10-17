package com.nongtri.app.ui.components

/**
 * Centralized test tags for UI testing
 * Following religious practice of test tag naming
 */
object TestTags {
    // Language Selection Screen
    const val LANGUAGE_SELECTION_SCREEN = "language_selection_screen"
    const val LANGUAGE_HEADER = "language_header"
    const val LANGUAGE_TITLE = "language_title"
    fun languageCard(code: String) = "language_card_$code"
    fun selectedIcon(code: String) = "selected_icon_$code"
    const val CONTINUE_BUTTON = "continue_button"

    // Chat Screen
    const val CHAT_SCREEN = "chat_screen"
    const val CHAT_APP_BAR = "chat_app_bar"
    const val CHAT_TITLE = "chat_title"
    const val PROFILE_BUTTON = "profile_button"
    const val MESSAGE_LIST = "message_list"
    const val WELCOME_CARD = "welcome_card"
    const val LOADING_INDICATOR = "loading_indicator"

    // Message Bubbles
    fun messageBubble(index: Int) = "message_bubble_$index"
    fun messageText(index: Int) = "message_text_$index"
    fun messageTimestamp(index: Int) = "message_timestamp_$index"
    fun messageTypeIndicator(index: Int) = "message_type_indicator_$index"

    // Input Area
    const val INPUT_AREA = "input_area"
    const val TEXT_FIELD = "text_field"
    const val SEND_BUTTON = "send_button"
    const val VOICE_BUTTON = "voice_button"
    const val IMAGE_BUTTON = "image_button"
    const val ATTACH_MENU = "attach_menu"
    const val TAKE_PHOTO_BUTTON = "take_photo_button"
    const val CHOOSE_GALLERY_BUTTON = "choose_gallery_button"

    // Voice Input
    const val VOICE_RECORDING_OVERLAY = "voice_recording_overlay"
    const val VOICE_ANIMATION = "voice_animation"
    const val VOICE_CANCEL_BUTTON = "voice_cancel_button"
    const val VOICE_SEND_BUTTON = "voice_send_button"

    // Image Preview
    const val IMAGE_PREVIEW = "image_preview"
    const val IMAGE_REMOVE_BUTTON = "image_remove_button"

    // Profile Screen
    const val PROFILE_SCREEN = "profile_screen"
    const val LANGUAGE_SETTING = "language_setting"
    const val THEME_SETTING = "theme_setting"
    const val ABOUT_SECTION = "about_section"
}
