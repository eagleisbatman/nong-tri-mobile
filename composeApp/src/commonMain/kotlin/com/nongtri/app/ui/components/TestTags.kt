package com.nongtri.app.ui.components

/**
 * Centralized test tags for UI automation testing
 *
 * Naming Convention:
 * - Screens: {SCREEN_NAME}_SCREEN
 * - Buttons: {ACTION}BUTTON
 * - Menu items: MENU_{ITEM_NAME}
 * - Dialogs: {PURPOSE}_DIALOG
 * - Bottom Sheets: {PURPOSE}_BOTTOM_SHEET
 * - Dynamic elements: Use factory functions with parameters
 */
object TestTags {
    // ============================================================================
    // SCREENS
    // ============================================================================
    const val LANGUAGE_SELECTION_SCREEN = "language_selection_screen"
    const val CHAT_SCREEN = "chat_screen"
    const val CONVERSATIONS_SCREEN = "conversations_screen"
    const val PROFILE_SCREEN = "profile_screen"

    // ============================================================================
    // LANGUAGE SELECTION SCREEN
    // ============================================================================
    const val LANGUAGE_HEADER = "language_header"
    const val LANGUAGE_TITLE = "language_title"
    const val CONTINUE_BUTTON = "continue_button"
    fun languageCard(code: String) = "language_card_$code"
    fun selectedIcon(code: String) = "selected_icon_$code"

    // ============================================================================
    // CHAT SCREEN - TOP BAR
    // ============================================================================
    const val CHAT_APP_BAR = "chat_app_bar"
    const val CHAT_TITLE = "chat_title"
    const val PROFILE_BUTTON = "profile_button"
    const val MENU_BUTTON = "menu_button"

    // ============================================================================
    // CHAT SCREEN - MENU ITEMS
    // ============================================================================
    const val MENU_CONVERSATIONS = "menu_conversations"
    const val MENU_NEW_CHAT = "menu_new_chat"
    const val MENU_SHARE_LOCATION = "menu_share_location"
    fun menuLanguage(code: String) = "menu_language_$code"
    fun menuTheme(mode: String) = "menu_theme_$mode"

    // ============================================================================
    // CHAT SCREEN - MESSAGE LIST
    // ============================================================================
    const val MESSAGE_LIST = "message_list"
    const val WELCOME_CARD = "welcome_card"
    const val LOADING_INDICATOR = "loading_indicator"
    const val SCROLL_TO_BOTTOM_FAB = "scroll_to_bottom_fab"

    // ============================================================================
    // MESSAGE BUBBLES
    // ============================================================================
    fun messageBubble(index: Int) = "message_bubble_$index"
    fun messageText(index: Int) = "message_text_$index"
    fun messageTimestamp(index: Int) = "message_timestamp_$index"
    fun messageTypeIndicator(index: Int) = "message_type_indicator_$index"
    fun followUpQuestion(index: Int) = "follow_up_question_$index"
    fun starterQuestion(index: Int) = "starter_question_$index"

    // ============================================================================
    // MESSAGE ACTIONS
    // ============================================================================
    const val COPY_BUTTON = "copy_button"
    const val SHARE_BUTTON = "share_button"
    const val TTS_BUTTON = "tts_button"
    const val THUMBS_UP_BUTTON = "thumbs_up_button"
    const val THUMBS_DOWN_BUTTON = "thumbs_down_button"

    // ============================================================================
    // INPUT AREA
    // ============================================================================
    const val INPUT_AREA = "input_area"
    const val TEXT_FIELD = "text_field"
    const val SEND_BUTTON = "send_button"
    const val VOICE_BUTTON = "voice_button"
    const val IMAGE_BUTTON = "image_button"
    const val ATTACH_MENU = "attach_menu"

    // ============================================================================
    // VOICE RECORDING
    // ============================================================================
    const val VOICE_RECORDING_OVERLAY = "voice_recording_overlay"
    const val VOICE_RECORDING_BAR = "voice_recording_bar"
    const val VOICE_ANIMATION = "voice_animation"
    const val VOICE_CANCEL_BUTTON = "voice_cancel_button"
    const val VOICE_SEND_BUTTON = "voice_send_button"
    const val VOICE_PLAY_BUTTON = "voice_play_button"
    const val CANCEL_RECORDING_BUTTON = "cancel_recording_button"
    const val SEND_RECORDING_BUTTON = "send_recording_button"

    // ============================================================================
    // IMAGE & DIAGNOSIS
    // ============================================================================
    const val IMAGE_CARD = "image_card"
    const val IMAGE_PREVIEW = "image_preview"
    const val IMAGE_REMOVE_BUTTON = "image_remove_button"
    const val DIAGNOSIS_TTS_BUTTON = "diagnosis_tts_button"
    const val TAKE_PHOTO_BUTTON = "take_photo_button"
    const val CHOOSE_GALLERY_BUTTON = "choose_gallery_button"

    // ============================================================================
    // CONVERSATIONS LIST SCREEN
    // ============================================================================
    const val CONVERSATIONS_TOP_BAR = "conversations_top_bar"
    const val BACK_BUTTON = "back_button"
    const val NEW_CONVERSATION_FAB = "new_conversation_fab"
    const val CONVERSATION_LIST = "conversation_list"
    const val RETRY_BUTTON = "retry_button"
    fun conversationItem(threadId: Int) = "conversation_item_$threadId"
    fun deleteButton(threadId: Int) = "delete_button_$threadId"

    // ============================================================================
    // DIALOGS
    // ============================================================================
    const val DELETE_DIALOG = "delete_dialog"
    const val DELETE_CONFIRM_BUTTON = "delete_confirm_button"
    const val DELETE_CANCEL_BUTTON = "delete_cancel_button"
    const val IMAGE_PREVIEW_DIALOG = "image_preview_dialog"
    const val FULLSCREEN_IMAGE_DIALOG = "fullscreen_image_dialog"
    const val QUESTION_INPUT = "question_input"
    const val CANCEL_BUTTON = "cancel_button"
    const val SEND_DIAGNOSIS_BUTTON = "send_diagnosis_button"

    // ============================================================================
    // BOTTOM SHEETS
    // ============================================================================
    const val LOCATION_BOTTOM_SHEET = "location_bottom_sheet"
    const val VOICE_PERMISSION_SHEET = "voice_permission_sheet"
    const val IMAGE_PERMISSION_SHEET = "image_permission_sheet"
    const val IMAGE_SOURCE_SHEET = "image_source_sheet"
    const val SHARE_BOTTOM_SHEET = "share_bottom_sheet"
    const val CLOSE_BUTTON = "close_button"
    const val UPDATE_LOCATION_BUTTON = "update_location_button"
    const val SHARE_LOCATION_BUTTON = "share_location_button"
    const val GRANT_PERMISSION_BUTTON = "grant_permission_button"
    const val GRANT_CAMERA_BUTTON = "grant_camera_button"
    const val CAMERA_OPTION = "camera_option"
    const val GALLERY_OPTION = "gallery_option"
    const val SHARE_AS_TEXT = "share_as_text"
    const val SHARE_AS_IMAGE = "share_as_image"

    // ============================================================================
    // COMMON BUTTONS
    // ============================================================================
    const val OK_BUTTON = "ok_button"
    const val YES_BUTTON = "yes_button"
    const val NO_BUTTON = "no_button"

    // ============================================================================
    // LANGUAGE SETTING
    // ============================================================================
    const val LANGUAGE_SETTING = "language_setting"

    // ============================================================================
    // THEME SETTING
    // ============================================================================
    const val THEME_SETTING = "theme_setting"

    // ============================================================================
    // ABOUT SECTION
    // ============================================================================
    const val ABOUT_SECTION = "about_section"
}
