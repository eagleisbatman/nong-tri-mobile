package com.nongtri.app.analytics

import com.nongtri.app.data.preferences.UserPreferences

/**
 * Helper functions for logging Firebase Analytics events
 *
 * This file provides type-safe wrappers around AnalyticsService.logEvent()
 * for all core events tracked in the Nông Trí app.
 *
 * Usage:
 * ```kotlin
 * Events.logSessionStarted(entryPoint = "launcher")
 * Events.logChatMessageSent(messageLength = 150, hasLocation = true)
 * Events.logOnboardingLanguageSelected(language = "vi", timeToDecideMs = 5000)
 * ```
 */
object Events {

    // ============================================================================
    // SESSION EVENTS
    // ============================================================================

    /**
     * Log when a new session starts
     *
     * @param entryPoint How the app was opened ("launcher", "notification", "deeplink")
     */
    fun logSessionStarted(entryPoint: String = "launcher") {
        try {
            val prefs = UserPreferences.getInstance()

            AnalyticsService.logEvent("session_started", mapOf(
                "session_number" to prefs.sessionCount.value,
                "days_since_install" to prefs.daysSinceInstall(),
                "days_since_last_session" to prefs.daysSinceLastSession(),
                "previous_session_duration_ms" to prefs.getLastSessionDuration(),
                "is_returning_user" to (prefs.sessionCount.value > 1),
                "entry_point" to entryPoint
            ))

            println("[Events] 📊 session_started logged (session #${prefs.sessionCount.value})")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging session_started: ${e.message}")
        }
    }

    /**
     * Log when a session ends
     *
     * @param sessionDurationMs Duration of the session in milliseconds
     * @param messagesSent Number of messages sent this session
     * @param voiceMessagesSent Number of voice messages sent
     * @param imagesSent Number of images sent
     * @param ttsUsed Whether TTS was used
     * @param locationShared Whether GPS location was shared
     */
    fun logSessionEnded(
        sessionDurationMs: Long,
        messagesSent: Int = 0,
        voiceMessagesSent: Int = 0,
        imagesSent: Int = 0,
        ttsUsed: Boolean = false,
        locationShared: Boolean = false
    ) {
        try {
            AnalyticsService.logEvent("session_ended", mapOf(
                "session_duration_ms" to sessionDurationMs,
                "messages_sent" to messagesSent,
                "voice_messages_sent" to voiceMessagesSent,
                "images_sent" to imagesSent,
                "tts_used" to ttsUsed,
                "location_shared" to locationShared
            ))

            println("[Events] 📊 session_ended logged (duration: ${sessionDurationMs}ms, messages: $messagesSent)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging session_ended: ${e.message}")
        }
    }

    // ============================================================================
    // CHAT EVENTS
    // ============================================================================

    /**
     * Log when user sends a text message
     *
     * @param messageLength Length of the message in characters
     * @param hasLocation Whether location context is available
     * @param locationType Type of location ("ip", "gps", "none")
     * @param sessionMessageNumber Which message # in this session (1st, 2nd, 3rd)
     */
    fun logChatMessageSent(
        messageLength: Int,
        hasLocation: Boolean = false,
        locationType: String = "none",
        sessionMessageNumber: Int = 1
    ) {
        try {
            val prefs = UserPreferences.getInstance()

            AnalyticsService.logEvent("chat_message_sent", mapOf(
                "message_type" to "text",
                "session_message_number" to sessionMessageNumber,
                "lifetime_message_number" to prefs.messageCount.value,
                "message_length_chars" to messageLength,
                "has_location_context" to hasLocation,
                "location_type" to locationType,
                "language" to prefs.language.value.code
            ))

            println("[Events] 📊 chat_message_sent logged (length: $messageLength, lifetime: ${prefs.messageCount.value})")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_sent: ${e.message}")
        }
    }

    /**
     * Log when user sends a voice message
     *
     * @param durationMs Duration of the voice recording in milliseconds
     * @param hasLocation Whether location context is available
     * @param locationType Type of location ("ip", "gps", "none")
     * @param sessionMessageNumber Which message # in this session
     */
    fun logVoiceMessageSent(
        durationMs: Long,
        hasLocation: Boolean = false,
        locationType: String = "none",
        sessionMessageNumber: Int = 1
    ) {
        try {
            val prefs = UserPreferences.getInstance()

            AnalyticsService.logEvent("voice_message_sent", mapOf(
                "message_type" to "voice",
                "session_message_number" to sessionMessageNumber,
                "lifetime_message_number" to prefs.messageCount.value,
                "lifetime_voice_messages" to prefs.voiceMessageCount.value,
                "duration_ms" to durationMs,
                "has_location_context" to hasLocation,
                "location_type" to locationType,
                "language" to prefs.language.value.code
            ))

            println("[Events] 📊 voice_message_sent logged (duration: ${durationMs}ms)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_message_sent: ${e.message}")
        }
    }

    /**
     * Log when AI response is received
     *
     * @param responseTimeMs Backend latency in milliseconds
     * @param responseLength Length of response in characters
     * @param messageNumber Message number in the conversation
     */
    fun logChatMessageReceived(
        responseTimeMs: Long,
        responseLength: Int,
        messageNumber: Int
    ) {
        try {
            AnalyticsService.logEvent("chat_message_received", mapOf(
                "response_time_ms" to responseTimeMs,
                "response_length_chars" to responseLength,
                "message_number" to messageNumber
            ))

            println("[Events] 📊 chat_message_received logged (time: ${responseTimeMs}ms, length: $responseLength)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_received: ${e.message}")
        }
    }

    /**
     * Log when user uploads an image for diagnosis
     *
     * @param imageSource Source of the image ("camera" or "gallery")
     * @param hasLocation Whether location context is available
     * @param locationType Type of location ("ip", "gps", "none")
     */
    fun logImageDiagnosisRequested(
        imageSource: String,
        hasLocation: Boolean = false,
        locationType: String = "none"
    ) {
        try {
            val prefs = UserPreferences.getInstance()

            AnalyticsService.logEvent("diagnosis_image_uploaded", mapOf(
                "image_source" to imageSource,
                "lifetime_image_messages" to prefs.imageMessageCount.value,
                "has_location_context" to hasLocation,
                "location_type" to locationType,
                "language" to prefs.language.value.code
            ))

            println("[Events] 📊 diagnosis_image_uploaded logged (source: $imageSource)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_image_uploaded: ${e.message}")
        }
    }

    // ============================================================================
    // ONBOARDING EVENTS
    // ============================================================================

    /**
     * Log when language selection screen is viewed
     *
     * @param defaultSuggestion The language suggested based on device locale
     */
    fun logOnboardingLanguageScreenViewed(defaultSuggestion: String) {
        try {
            AnalyticsService.logEvent("onboarding_language_screen_viewed", mapOf(
                "default_suggestion" to defaultSuggestion
            ))

            println("[Events] 📊 onboarding_language_screen_viewed logged (suggestion: $defaultSuggestion)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging onboarding_language_screen_viewed: ${e.message}")
        }
    }

    /**
     * Log when user selects a language
     *
     * @param languageChosen The language chosen ("vi" or "en")
     * @param matchesDeviceLocale Whether the choice matches device locale
     * @param timeToDecideMs How long it took the user to decide
     */
    fun logOnboardingLanguageSelected(
        languageChosen: String,
        matchesDeviceLocale: Boolean,
        timeToDecideMs: Long
    ) {
        try {
            AnalyticsService.logEvent("onboarding_language_selected", mapOf(
                "language_chosen" to languageChosen,
                "matches_device_locale" to matchesDeviceLocale,
                "time_to_decide_ms" to timeToDecideMs
            ))

            println("[Events] 📊 onboarding_language_selected logged (language: $languageChosen, time: ${timeToDecideMs}ms)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging onboarding_language_selected: ${e.message}")
        }
    }

    /**
     * Log when user clicks continue after selecting language
     *
     * @param language The selected language
     */
    fun logOnboardingLanguageContinueClicked(language: String) {
        try {
            AnalyticsService.logEvent("onboarding_language_continue_clicked", mapOf(
                "language" to language
            ))

            println("[Events] 📊 onboarding_language_continue_clicked logged (language: $language)")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging onboarding_language_continue_clicked: ${e.message}")
        }
    }

    /**
     * Log when onboarding is completed
     */
    fun logOnboardingCompleted() {
        try {
            val prefs = UserPreferences.getInstance()

            AnalyticsService.logEvent("onboarding_completed", mapOf(
                "language" to prefs.language.value.code,
                "session_number" to prefs.sessionCount.value
            ))

            println("[Events] 📊 onboarding_completed logged")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging onboarding_completed: ${e.message}")
        }
    }

    // ============================================================================
    // FEATURE ADOPTION EVENTS
    // ============================================================================

    /**
     * Log when user uses TTS for the first time
     */
    fun logTtsFirstUse() {
        try {
            AnalyticsService.logEvent("tts_first_use", mapOf(
                "session_number" to UserPreferences.getInstance().sessionCount.value
            ))

            println("[Events] 📊 tts_first_use logged")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging tts_first_use: ${e.message}")
        }
    }

    /**
     * Log when user shares GPS location for the first time
     */
    fun logLocationFirstShare() {
        try {
            AnalyticsService.logEvent("location_first_share", mapOf(
                "session_number" to UserPreferences.getInstance().sessionCount.value
            ))

            println("[Events] 📊 location_first_share logged")

        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_first_share: ${e.message}")
        }
    }

    // ============================================================================
    // PERMISSION EVENTS
    // ============================================================================

    /**
     * Voice permission requested
     */
    fun logVoicePermissionRequested(trigger: String = "voice_button") {
        try {
            AnalyticsService.logEvent("voice_permission_requested", mapOf(
                "trigger" to trigger
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_permission_requested: ${e.message}")
        }
    }

    /**
     * Voice permission granted
     */
    fun logVoicePermissionGranted(timeToGrantMs: Long, firstGrant: Boolean) {
        try {
            AnalyticsService.logEvent("voice_permission_granted", mapOf(
                "time_to_grant_ms" to timeToGrantMs,
                "first_grant" to firstGrant
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_permission_granted: ${e.message}")
        }
    }

    /**
     * Voice permission denied
     */
    fun logVoicePermissionDenied(denialCount: Int, canRequestAgain: Boolean) {
        try {
            AnalyticsService.logEvent("voice_permission_denied", mapOf(
                "denial_count" to denialCount,
                "can_request_again" to canRequestAgain
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_permission_denied: ${e.message}")
        }
    }

    /**
     * Image permission requested
     */
    fun logImagePermissionRequested(permissionType: String) {
        try {
            AnalyticsService.logEvent("image_permission_requested", mapOf(
                "permission_type" to permissionType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_permission_requested: ${e.message}")
        }
    }

    /**
     * Image permission granted
     */
    fun logImagePermissionGranted(permissionType: String, timeToGrantMs: Long) {
        try {
            AnalyticsService.logEvent("image_permission_granted", mapOf(
                "permission_type" to permissionType,
                "time_to_grant_ms" to timeToGrantMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_permission_granted: ${e.message}")
        }
    }

    /**
     * Image permission denied
     */
    fun logImagePermissionDenied(permissionType: String, denialCount: Int, canRequestAgain: Boolean) {
        try {
            AnalyticsService.logEvent("image_permission_denied", mapOf(
                "permission_type" to permissionType,
                "denial_count" to denialCount,
                "can_request_again" to canRequestAgain
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_permission_denied: ${e.message}")
        }
    }

    /**
     * Location permission requested
     */
    fun logLocationPermissionRequested(trigger: String = "location_button") {
        try {
            AnalyticsService.logEvent("location_permission_requested", mapOf(
                "trigger" to trigger
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_permission_requested: ${e.message}")
        }
    }

    /**
     * Location permission granted
     */
    fun logLocationPermissionGranted(permissionType: String, timeToGrantMs: Long) {
        try {
            AnalyticsService.logEvent("location_permission_granted", mapOf(
                "permission_type" to permissionType,
                "time_to_grant_ms" to timeToGrantMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_permission_granted: ${e.message}")
        }
    }

    /**
     * Location permission denied
     */
    fun logLocationPermissionDenied(denialCount: Int, canRequestAgain: Boolean) {
        try {
            AnalyticsService.logEvent("location_permission_denied", mapOf(
                "denial_count" to denialCount,
                "can_request_again" to canRequestAgain
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_permission_denied: ${e.message}")
        }
    }

    // ============================================================================
    // ERROR TRACKING EVENTS
    // ============================================================================

    /**
     * Network request failed
     */
    fun logNetworkError(endpoint: String, errorType: String, errorMessage: String) {
        try {
            AnalyticsService.logEvent("network_request_failed", mapOf(
                "endpoint" to endpoint,
                "error_type" to errorType,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging network_request_failed: ${e.message}")
        }
    }

    /**
     * API error occurred
     */
    fun logApiError(endpoint: String, statusCode: Int, errorMessage: String) {
        try {
            AnalyticsService.logEvent("api_error_occurred", mapOf(
                "endpoint" to endpoint,
                "status_code" to statusCode,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging api_error_occurred: ${e.message}")
        }
    }

    /**
     * Voice recording error
     */
    fun logVoiceRecordingError(errorType: String, errorMessage: String) {
        try {
            AnalyticsService.logEvent("voice_recording_error", mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_recording_error: ${e.message}")
        }
    }

    /**
     * Image upload error
     */
    fun logImageUploadError(errorType: String, errorMessage: String, fileSize: Long = 0) {
        try {
            AnalyticsService.logEvent("image_upload_error", mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage,
                "file_size_kb" to (fileSize / 1024).toInt()
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_upload_error: ${e.message}")
        }
    }

    /**
     * TTS playback error
     */
    fun logTtsPlaybackError(errorType: String, errorMessage: String) {
        try {
            AnalyticsService.logEvent("tts_playback_error", mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging tts_playback_error: ${e.message}")
        }
    }

    // ============================================================================
    // TTS LIFECYCLE EVENTS
    // ============================================================================

    /**
     * TTS button clicked
     */
    fun logTtsButtonClicked(messageIndex: Int, messageLength: Int, language: String) {
        try {
            AnalyticsService.logEvent("tts_button_clicked", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "language" to language
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging tts_button_clicked: ${e.message}")
        }
    }

    /**
     * TTS playback started
     */
    fun logTtsPlaybackStarted(messageIndex: Int, audioDuration: Long, language: String) {
        try {
            AnalyticsService.logEvent("tts_playback_started", mapOf(
                "message_index" to messageIndex,
                "audio_duration_ms" to audioDuration,
                "language" to language
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging tts_playback_started: ${e.message}")
        }
    }

    /**
     * TTS playback completed
     */
    fun logTtsPlaybackCompleted(messageIndex: Int, playbackDuration: Long, listenedToEnd: Boolean) {
        try {
            AnalyticsService.logEvent("tts_playback_completed", mapOf(
                "message_index" to messageIndex,
                "playback_duration_ms" to playbackDuration,
                "listened_to_end" to listenedToEnd
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging tts_playback_completed: ${e.message}")
        }
    }

    /**
     * TTS playback paused
     */
    fun logTtsPlaybackPaused(messageIndex: Int, playbackPosition: Long, audioDuration: Long) {
        try {
            val completionPercent = if (audioDuration > 0) (playbackPosition.toFloat() / audioDuration * 100) else 0f
            AnalyticsService.logEvent("tts_playback_paused", mapOf(
                "message_index" to messageIndex,
                "playback_position_ms" to playbackPosition,
                "audio_duration_ms" to audioDuration,
                "completion_percent" to completionPercent
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging tts_playback_paused: ${e.message}")
        }
    }

    // ============================================================================
    // CHAT ACTIVATION EVENTS
    // ============================================================================

    /**
     * Chat screen first view (critical activation metric)
     */
    fun logChatScreenFirstView(
        hasWelcomeCard: Boolean,
        hasStarterQuestions: Boolean,
        starterQuestionsCount: Int,
        locationDisplayed: Boolean,
        locationType: String,
        timeSinceLanguageSelectionMs: Long
    ) {
        try {
            AnalyticsService.logEvent("chat_screen_first_view", mapOf(
                "has_welcome_card" to hasWelcomeCard,
                "has_starter_questions" to hasStarterQuestions,
                "starter_questions_count" to starterQuestionsCount,
                "location_displayed" to locationDisplayed,
                "location_type" to locationType,
                "time_since_language_selection_ms" to timeSinceLanguageSelectionMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_screen_first_view: ${e.message}")
        }
    }

    /**
     * First message sent (Time To First Message - TTFM)
     */
    fun logChatFirstMessageSent(
        messageType: String,
        timeSinceAppOpenMs: Long,
        timeSinceChatViewMs: Long,
        messageLength: Int,
        usedStarterQuestion: Boolean,
        hasLocationContext: Boolean
    ) {
        try {
            AnalyticsService.logEvent("chat_first_message_sent", mapOf(
                "message_type" to messageType,
                "time_since_app_open_ms" to timeSinceAppOpenMs,
                "time_since_chat_view_ms" to timeSinceChatViewMs,
                "message_length_chars" to messageLength,
                "used_starter_question" to usedStarterQuestion,
                "has_location_context" to hasLocationContext
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_first_message_sent: ${e.message}")
        }
    }

    /**
     * First response received (Time To First Value - TTFV)
     */
    fun logChatFirstResponseReceived(
        responseTimeMs: Long,
        timeSinceAppOpenMs: Long,
        responseLength: Int,
        hasFollowUpQuestions: Boolean,
        followUpCount: Int
    ) {
        try {
            AnalyticsService.logEvent("chat_first_response_received", mapOf(
                "response_time_ms" to responseTimeMs,
                "time_since_app_open_ms" to timeSinceAppOpenMs,
                "response_length_chars" to responseLength,
                "has_follow_up_questions" to hasFollowUpQuestions,
                "follow_up_count" to followUpCount
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_first_response_received: ${e.message}")
        }
    }

    // ============================================================================
    // IMAGE UPLOAD EVENTS
    // ============================================================================

    /**
     * Diagnosis upload started
     */
    fun logDiagnosisUploadStarted(fileSizeKb: Int, networkType: String) {
        try {
            AnalyticsService.logEvent("diagnosis_upload_started", mapOf(
                "file_size_kb" to fileSizeKb,
                "network_type" to networkType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_upload_started: ${e.message}")
        }
    }

    /**
     * Diagnosis upload completed
     */
    fun logDiagnosisUploadCompleted(fileSizeKb: Int, uploadTimeMs: Long, networkType: String) {
        try {
            AnalyticsService.logEvent("diagnosis_upload_completed", mapOf(
                "file_size_kb" to fileSizeKb,
                "upload_time_ms" to uploadTimeMs,
                "network_type" to networkType,
                "upload_speed_kbps" to if (uploadTimeMs > 0) (fileSizeKb * 1000 / uploadTimeMs).toInt() else 0
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_upload_completed: ${e.message}")
        }
    }

    /**
     * Diagnosis upload failed
     */
    fun logDiagnosisUploadFailed(fileSizeKb: Int, errorType: String, errorMessage: String) {
        try {
            AnalyticsService.logEvent("diagnosis_upload_failed", mapOf(
                "file_size_kb" to fileSizeKb,
                "error_type" to errorType,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_upload_failed: ${e.message}")
        }
    }

    /**
     * Diagnosis processing completed
     */
    fun logDiagnosisCompleted(processingTimeMs: Long, resultLength: Int) {
        try {
            AnalyticsService.logEvent("diagnosis_completed", mapOf(
                "processing_time_ms" to processingTimeMs,
                "result_length_chars" to resultLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_completed: ${e.message}")
        }
    }
}
