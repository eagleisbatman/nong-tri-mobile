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
    // VOICE INTERACTION EVENTS
    // ============================================================================

    /**
     * Voice button clicked (user initiates voice recording)
     * ROUND 7: Added missing event from strategy
     */
    fun logVoiceButtonClicked() {
        try {
            AnalyticsService.logEvent("voice_button_clicked", mapOf<String, Any>())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_button_clicked: ${e.message}")
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

    // ============================================================================
    // ROUND 4: CRITICAL MISSING EVENTS - IMAGE DIAGNOSIS (11 events)
    // ============================================================================

    /**
     * Image button clicked (funnel step 1 - standalone event)
     */
    fun logImageButtonClicked() {
        try {
            AnalyticsService.logEvent("image_button_clicked", mapOf(
                "session_message_number" to UserPreferences.getInstance().messageCount.value
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_button_clicked: ${e.message}")
        }
    }

    /**
     * Image source selected (camera vs gallery)
     */
    fun logImageSourceSelected(source: String) {
        try {
            AnalyticsService.logEvent("image_source_selected", mapOf(
                "source" to source
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_source_selected: ${e.message}")
        }
    }

    /**
     * Image captured from camera
     */
    fun logImageCaptured(fileSizeKb: Int, imageWidth: Int, imageHeight: Int) {
        try {
            AnalyticsService.logEvent("image_captured", mapOf(
                "file_size_kb" to fileSizeKb,
                "image_width" to imageWidth,
                "image_height" to imageHeight
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_captured: ${e.message}")
        }
    }

    /**
     * Image selected from gallery
     */
    fun logImageSelectedFromGallery(fileSizeKb: Int, imageWidth: Int, imageHeight: Int) {
        try {
            AnalyticsService.logEvent("image_selected_from_gallery", mapOf(
                "file_size_kb" to fileSizeKb,
                "image_width" to imageWidth,
                "image_height" to imageHeight
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_selected_from_gallery: ${e.message}")
        }
    }

    /**
     * Image validation failed
     */
    fun logImageValidationFailed(reason: String, fileSizeKb: Int) {
        try {
            AnalyticsService.logEvent("image_validation_failed", mapOf(
                "reason" to reason,
                "file_size_kb" to fileSizeKb
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_validation_failed: ${e.message}")
        }
    }

    /**
     * Diagnosis submission started (before upload)
     */
    fun logDiagnosisSubmissionStarted(fileSizeKb: Int, hasQuestion: Boolean, questionLength: Int) {
        try {
            AnalyticsService.logEvent("diagnosis_submission_started", mapOf(
                "file_size_kb" to fileSizeKb,
                "has_question" to hasQuestion,
                "question_length" to questionLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_submission_started: ${e.message}")
        }
    }

    /**
     * Diagnosis job created (backend job ID received)
     */
    fun logDiagnosisJobCreated(jobId: String) {
        try {
            AnalyticsService.logEvent("diagnosis_job_created", mapOf(
                "job_id" to jobId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_job_created: ${e.message}")
        }
    }

    /**
     * Diagnosis processing card displayed
     */
    fun logDiagnosisProcessingDisplayed(jobId: String) {
        try {
            AnalyticsService.logEvent("diagnosis_processing_displayed", mapOf(
                "job_id" to jobId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_processing_displayed: ${e.message}")
        }
    }

    /**
     * Diagnosis result viewed by user
     */
    fun logDiagnosisResultViewed(jobId: String, resultLength: Int) {
        try {
            AnalyticsService.logEvent("diagnosis_result_viewed", mapOf(
                "job_id" to jobId,
                "result_length_chars" to resultLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_result_viewed: ${e.message}")
        }
    }

    /**
     * Diagnosis failed
     */
    fun logDiagnosisFailed(jobId: String, errorType: String, errorMessage: String) {
        try {
            AnalyticsService.logEvent("diagnosis_failed", mapOf(
                "job_id" to jobId,
                "error_type" to errorType,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_failed: ${e.message}")
        }
    }

    /**
     * Diagnosis image fullscreen opened
     */
    fun logDiagnosisImageFullscreenOpened(jobId: String) {
        try {
            AnalyticsService.logEvent("diagnosis_image_fullscreen_opened", mapOf(
                "job_id" to jobId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_image_fullscreen_opened: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 4: CRITICAL MISSING EVENTS - LOCATION (6 events)
    // ============================================================================

    /**
     * Location initialization started (on app startup)
     */
    fun logLocationInitializationStarted() {
        try {
            AnalyticsService.logEvent("location_initialization_started", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_initialization_started: ${e.message}")
        }
    }

    /**
     * IP-based location detected
     */
    fun logLocationIpDetected(city: String, country: String) {
        try {
            AnalyticsService.logEvent("location_ip_detected", mapOf(
                "city" to city,
                "country" to country
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_ip_detected: ${e.message}")
        }
    }

    /**
     * GPS location obtained
     */
    fun logLocationGpsObtained(latitude: Double, longitude: Double, accuracy: Float) {
        try {
            AnalyticsService.logEvent("location_gps_obtained", mapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "accuracy_meters" to accuracy
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_gps_obtained: ${e.message}")
        }
    }

    /**
     * GPS location failed
     */
    fun logLocationGpsFailed(errorType: String, errorMessage: String) {
        try {
            AnalyticsService.logEvent("location_gps_failed", mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_gps_failed: ${e.message}")
        }
    }

    /**
     * Location shared to backend successfully
     */
    fun logLocationShared(locationType: String, city: String) {
        try {
            AnalyticsService.logEvent("location_shared", mapOf(
                "location_type" to locationType,
                "city" to city
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_shared: ${e.message}")
        }
    }

    /**
     * Location bottom sheet opened
     */
    fun logLocationBottomSheetOpened(trigger: String) {
        try {
            AnalyticsService.logEvent("location_bottom_sheet_opened", mapOf(
                "trigger" to trigger
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_bottom_sheet_opened: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 4: CRITICAL MISSING EVENTS - VOICE (4 events)
    // ============================================================================

    /**
     * Voice recording started (standalone event with full parameters)
     */
    fun logVoiceRecordingStarted() {
        try {
            AnalyticsService.logEvent("voice_recording_started", mapOf(
                "session_voice_messages" to UserPreferences.getInstance().voiceMessageCount.value
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_recording_started: ${e.message}")
        }
    }

    /**
     * Voice recording completed (standalone event with full parameters)
     */
    fun logVoiceRecordingCompleted(durationMs: Long, fileSizeKb: Int) {
        try {
            AnalyticsService.logEvent("voice_recording_completed", mapOf(
                "duration_ms" to durationMs,
                "file_size_kb" to fileSizeKb
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_recording_completed: ${e.message}")
        }
    }

    /**
     * Voice recording cancelled by user
     */
    fun logVoiceRecordingCancelled(durationMs: Long, reason: String) {
        try {
            AnalyticsService.logEvent("voice_recording_cancelled", mapOf(
                "duration_ms" to durationMs,
                "reason" to reason
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_recording_cancelled: ${e.message}")
        }
    }

    /**
     * Voice transcription completed successfully
     */
    fun logVoiceTranscriptionCompleted(durationMs: Long, transcriptionLength: Int, transcriptionTimeMs: Long) {
        try {
            AnalyticsService.logEvent("voice_transcription_completed", mapOf(
                "audio_duration_ms" to durationMs,
                "transcription_length_chars" to transcriptionLength,
                "transcription_time_ms" to transcriptionTimeMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_transcription_completed: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 4: CRITICAL MISSING EVENTS - CHAT ACTIONS (3 events)
    // ============================================================================

    /**
     * Chat message copied to clipboard
     */
    fun logChatMessageCopied(messageIndex: Int, messageLength: Int, messageType: String) {
        try {
            AnalyticsService.logEvent("chat_message_copied", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "message_type" to messageType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_copied: ${e.message}")
        }
    }

    /**
     * Chat message share dialog opened
     */
    fun logChatMessageShareOpened(messageIndex: Int, messageLength: Int, messageType: String) {
        try {
            AnalyticsService.logEvent("chat_message_share_opened", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "message_type" to messageType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_share_opened: ${e.message}")
        }
    }

    /**
     * Chat message shared via system share
     */
    fun logChatMessageShared(messageIndex: Int, messageLength: Int, messageType: String, shareTarget: String) {
        try {
            AnalyticsService.logEvent("chat_message_shared", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "message_type" to messageType,
                "share_target" to shareTarget
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_shared: ${e.message}")
        }
    }

    /**
     * User gave positive feedback (thumbs up) on AI response
     */
    fun logMessageFeedbackPositive(messageIndex: Int, messageLength: Int, conversationId: Int?) {
        try {
            AnalyticsService.logEvent("message_feedback_positive", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "conversation_id" to (conversationId ?: 0)
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging message_feedback_positive: ${e.message}")
        }
    }

    /**
     * User gave negative feedback (thumbs down) on AI response
     */
    fun logMessageFeedbackNegative(messageIndex: Int, messageLength: Int, conversationId: Int?) {
        try {
            AnalyticsService.logEvent("message_feedback_negative", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "conversation_id" to (conversationId ?: 0)
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging message_feedback_negative: ${e.message}")
        }
    }

    /**
     * User clicked copy button on AI response
     */
    fun logCopyButtonClicked(messageIndex: Int, messageLength: Int) {
        try {
            AnalyticsService.logEvent("copy_button_clicked", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging copy_button_clicked: ${e.message}")
        }
    }

    /**
     * User clicked share button on AI response
     */
    fun logShareButtonClicked(messageIndex: Int, messageLength: Int, shareType: String) {
        try {
            AnalyticsService.logEvent("share_button_clicked", mapOf(
                "message_index" to messageIndex,
                "message_length_chars" to messageLength,
                "share_type" to shareType // "text" or "image"
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging share_button_clicked: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: IMPORTANT EVENTS - IMAGE DIAGNOSIS (7 events)
    // ============================================================================

    /**
     * Image source selection bottom sheet opened
     */
    fun logImageSourceSheetOpened() {
        try {
            AnalyticsService.logEvent("image_source_sheet_opened", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_source_sheet_opened: ${e.message}")
        }
    }

    /**
     * Camera app opened for image capture
     */
    fun logImageCameraOpened() {
        try {
            AnalyticsService.logEvent("image_camera_opened", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_camera_opened: ${e.message}")
        }
    }

    /**
     * Gallery app opened for image selection
     */
    fun logImageGalleryOpened() {
        try {
            AnalyticsService.logEvent("image_gallery_opened", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_gallery_opened: ${e.message}")
        }
    }

    /**
     * Image preview dialog displayed
     */
    fun logImagePreviewDisplayed(fileSizeKb: Int, imageWidth: Int, imageHeight: Int) {
        try {
            AnalyticsService.logEvent("image_preview_displayed", mapOf(
                "file_size_kb" to fileSizeKb,
                "image_width" to imageWidth,
                "image_height" to imageHeight
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_preview_displayed: ${e.message}")
        }
    }

    /**
     * User edited diagnosis question in preview
     */
    fun logImageQuestionEdited(originalLength: Int, newLength: Int) {
        try {
            AnalyticsService.logEvent("image_question_edited", mapOf(
                "original_length" to originalLength,
                "new_length" to newLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_question_edited: ${e.message}")
        }
    }

    /**
     * User read diagnosis result (scroll tracking)
     */
    fun logDiagnosisResultRead(jobId: String, readTimeMs: Long, scrollPercent: Int) {
        try {
            AnalyticsService.logEvent("diagnosis_result_read", mapOf(
                "job_id" to jobId,
                "read_time_ms" to readTimeMs,
                "scroll_percent" to scrollPercent
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_result_read: ${e.message}")
        }
    }

    /**
     * User played TTS for diagnosis advice
     */
    fun logDiagnosisAdviceTtsPlayed(jobId: String, adviceLength: Int) {
        try {
            AnalyticsService.logEvent("diagnosis_advice_tts_played", mapOf(
                "job_id" to jobId,
                "advice_length_chars" to adviceLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_advice_tts_played: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: IMPORTANT EVENTS - VOICE (4 events)
    // ============================================================================

    /**
     * Voice transcription started
     */
    fun logVoiceTranscriptionStarted(fileSizeKb: Int, durationMs: Long) {
        try {
            AnalyticsService.logEvent("voice_transcription_started", mapOf(
                "file_size_kb" to fileSizeKb,
                "duration_ms" to durationMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_transcription_started: ${e.message}")
        }
    }

    /**
     * Voice transcription failed
     * ROUND 7: Added missing event from strategy
     */
    fun logVoiceTranscriptionFailed(errorType: String, errorMessage: String, fileSizeKb: Int = 0, durationMs: Long = 0) {
        try {
            AnalyticsService.logEvent("voice_transcription_failed", mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage,
                "file_size_kb" to fileSizeKb,
                "duration_ms" to durationMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_transcription_failed: ${e.message}")
        }
    }

    /**
     * Voice recording amplitude captured (waveform data point)
     */
    fun logVoiceRecordingAmplitudeCaptured(amplitude: Int, durationMs: Long) {
        try {
            AnalyticsService.logEvent("voice_recording_amplitude_captured", mapOf(
                "amplitude" to amplitude,
                "duration_ms" to durationMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_recording_amplitude_captured: ${e.message}")
        }
    }

    /**
     * User started playing back their voice message
     */
    fun logVoiceMessagePlaybackStarted(messageId: String, durationMs: Long) {
        try {
            AnalyticsService.logEvent("voice_message_playback_started", mapOf(
                "message_id" to messageId,
                "duration_ms" to durationMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_message_playback_started: ${e.message}")
        }
    }

    /**
     * User completed playing back their voice message
     */
    fun logVoiceMessagePlaybackCompleted(messageId: String, playbackDurationMs: Long, listenedToEnd: Boolean) {
        try {
            AnalyticsService.logEvent("voice_message_playback_completed", mapOf(
                "message_id" to messageId,
                "playback_duration_ms" to playbackDurationMs,
                "listened_to_end" to listenedToEnd
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging voice_message_playback_completed: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: IMPORTANT EVENTS - LOCATION (4 events)
    // ============================================================================

    /**
     * Location initialization completed successfully
     */
    fun logLocationInitializationCompleted(hasIpLocation: Boolean, initTimeMs: Long) {
        try {
            AnalyticsService.logEvent("location_initialization_completed", mapOf(
                "has_ip_location" to hasIpLocation,
                "init_time_ms" to initTimeMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_initialization_completed: ${e.message}")
        }
    }

    /**
     * User clicked share location button
     */
    fun logLocationShareButtonClicked() {
        try {
            AnalyticsService.logEvent("location_share_button_clicked", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_share_button_clicked: ${e.message}")
        }
    }

    /**
     * GPS location request initiated
     */
    fun logLocationGpsRequested() {
        try {
            AnalyticsService.logEvent("location_gps_requested", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_gps_requested: ${e.message}")
        }
    }

    /**
     * User opened settings from location permission denial
     */
    fun logLocationPermissionSettingsOpened() {
        try {
            AnalyticsService.logEvent("location_permission_settings_opened", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_permission_settings_opened: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: NICE TO HAVE EVENTS - IMAGE DIAGNOSIS (4 events)
    // ============================================================================

    /**
     * Image compression started
     */
    fun logImageCompressionStarted(originalSizeKb: Int, targetSizeKb: Int) {
        try {
            AnalyticsService.logEvent("image_compression_started", mapOf(
                "original_size_kb" to originalSizeKb,
                "target_size_kb" to targetSizeKb
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_compression_started: ${e.message}")
        }
    }

    /**
     * Image compression completed
     */
    fun logImageCompressionCompleted(originalSizeKb: Int, compressedSizeKb: Int, compressionTimeMs: Long) {
        try {
            AnalyticsService.logEvent("image_compression_completed", mapOf(
                "original_size_kb" to originalSizeKb,
                "compressed_size_kb" to compressedSizeKb,
                "compression_time_ms" to compressionTimeMs,
                "compression_ratio" to if (originalSizeKb > 0) (compressedSizeKb.toFloat() / originalSizeKb * 100).toInt() else 0
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging image_compression_completed: ${e.message}")
        }
    }

    /**
     * Diagnosis upload progress update
     */
    fun logDiagnosisUploadProgress(jobId: String, progressPercent: Int, uploadedKb: Int) {
        try {
            AnalyticsService.logEvent("diagnosis_upload_progress", mapOf(
                "job_id" to jobId,
                "progress_percent" to progressPercent,
                "uploaded_kb" to uploadedKb
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_upload_progress: ${e.message}")
        }
    }

    /**
     * Diagnosis pending card viewed by user
     */
    fun logDiagnosisPendingCardViewed(jobId: String) {
        try {
            AnalyticsService.logEvent("diagnosis_pending_card_viewed", mapOf(
                "job_id" to jobId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_pending_card_viewed: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: NICE TO HAVE EVENTS - CHAT (3 events)
    // ============================================================================

    /**
     * Chat message displayed on screen
     */
    fun logChatMessageDisplayed(messageIndex: Int, messageType: String, messageLength: Int) {
        try {
            AnalyticsService.logEvent("chat_message_displayed", mapOf(
                "message_index" to messageIndex,
                "message_type" to messageType,
                "message_length_chars" to messageLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_displayed: ${e.message}")
        }
    }

    /**
     * User read message (scroll into view + dwell time)
     */
    fun logChatMessageRead(messageIndex: Int, messageType: String, dwellTimeMs: Long) {
        try {
            AnalyticsService.logEvent("chat_message_read", mapOf(
                "message_index" to messageIndex,
                "message_type" to messageType,
                "dwell_time_ms" to dwellTimeMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_message_read: ${e.message}")
        }
    }

    /**
     * User read first response (engagement metric)
     */
    fun logChatFirstResponseRead(readTimeMs: Long, responseLength: Int) {
        try {
            AnalyticsService.logEvent("chat_first_response_read", mapOf(
                "read_time_ms" to readTimeMs,
                "response_length_chars" to responseLength
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging chat_first_response_read: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: FOLLOW-UP QUESTIONS (2 events)
    // ============================================================================

    /**
     * Follow-up questions displayed to user
     */
    fun logFollowUpQuestionsDisplayed(count: Int, messageIndex: Int) {
        try {
            AnalyticsService.logEvent("follow_up_questions_displayed", mapOf(
                "count" to count,
                "message_index" to messageIndex
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging follow_up_questions_displayed: ${e.message}")
        }
    }

    /**
     * User clicked a follow-up question
     */
    fun logFollowUpQuestionClicked(questionIndex: Int, questionText: String, messageIndex: Int) {
        try {
            AnalyticsService.logEvent("follow_up_question_clicked", mapOf(
                "question_index" to questionIndex,
                "question_text" to questionText,
                "message_index" to messageIndex
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging follow_up_question_clicked: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: FUTURE FEATURES - NOTIFICATIONS (2 events)
    // ============================================================================

    /**
     * Diagnosis notification sent (FUTURE FEATURE)
     */
    fun logDiagnosisNotificationSent(jobId: String, notificationType: String) {
        try {
            AnalyticsService.logEvent("diagnosis_notification_sent", mapOf(
                "job_id" to jobId,
                "notification_type" to notificationType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_notification_sent: ${e.message}")
        }
    }

    /**
     * User opened diagnosis from notification (FUTURE FEATURE)
     */
    fun logDiagnosisNotificationOpened(jobId: String, timeSinceSentMs: Long) {
        try {
            AnalyticsService.logEvent("diagnosis_notification_opened", mapOf(
                "job_id" to jobId,
                "time_since_sent_ms" to timeSinceSentMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging diagnosis_notification_opened: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: FUTURE FEATURES - CONVERSATIONS LIST (7 events)
    // ============================================================================

    /**
     * Conversations screen opened (FUTURE FEATURE)
     */
    fun logConversationsScreenOpened() {
        try {
            AnalyticsService.logEvent("conversations_screen_opened", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversations_screen_opened: ${e.message}")
        }
    }

    /**
     * Conversations list viewed (FUTURE FEATURE)
     */
    fun logConversationsListViewed(conversationCount: Int) {
        try {
            AnalyticsService.logEvent("conversations_list_viewed", mapOf(
                "conversation_count" to conversationCount
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversations_list_viewed: ${e.message}")
        }
    }

    /**
     * User clicked a conversation item (FUTURE FEATURE)
     */
    fun logConversationItemClicked(conversationId: String, position: Int) {
        try {
            AnalyticsService.logEvent("conversation_item_clicked", mapOf(
                "conversation_id" to conversationId,
                "position" to position
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversation_item_clicked: ${e.message}")
        }
    }

    /**
     * New conversation created (FUTURE FEATURE)
     */
    fun logConversationCreated(conversationId: String) {
        try {
            AnalyticsService.logEvent("conversation_created", mapOf(
                "conversation_id" to conversationId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversation_created: ${e.message}")
        }
    }

    /**
     * User clicked delete on conversation (FUTURE FEATURE)
     */
    fun logConversationDeleteClicked(conversationId: String) {
        try {
            AnalyticsService.logEvent("conversation_delete_clicked", mapOf(
                "conversation_id" to conversationId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversation_delete_clicked: ${e.message}")
        }
    }

    /**
     * User confirmed conversation deletion (FUTURE FEATURE)
     */
    fun logConversationDeleteConfirmed(conversationId: String, messageCount: Int) {
        try {
            AnalyticsService.logEvent("conversation_delete_confirmed", mapOf(
                "conversation_id" to conversationId,
                "message_count" to messageCount
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversation_delete_confirmed: ${e.message}")
        }
    }

    /**
     * User cancelled conversation deletion (FUTURE FEATURE)
     */
    fun logConversationDeleteCancelled(conversationId: String) {
        try {
            AnalyticsService.logEvent("conversation_delete_cancelled", mapOf(
                "conversation_id" to conversationId
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging conversation_delete_cancelled: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: FUTURE FEATURES - A/B TESTING (1 event)
    // ============================================================================

    /**
     * A/B test variant assigned (FUTURE FEATURE)
     */
    fun logAbTestVariantAssigned(testName: String, variantName: String) {
        try {
            AnalyticsService.logEvent("ab_test_variant_assigned", mapOf(
                "test_name" to testName,
                "variant_name" to variantName
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging ab_test_variant_assigned: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: ADVANCED ANALYTICS - APP LIFECYCLE (4 events)
    // ============================================================================

    /**
     * App first launch (handled by Firebase automatically, but can be overridden)
     */
    fun logAppFirstLaunch(installSource: String = "unknown") {
        try {
            AnalyticsService.logEvent("app_first_launch", mapOf(
                "install_source" to installSource
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging app_first_launch: ${e.message}")
        }
    }

    /**
     * App install completed (for tracking install attribution)
     */
    fun logAppInstallCompleted(installSource: String, referrer: String?) {
        try {
            AnalyticsService.logEvent("app_install_completed", mapOf(
                "install_source" to installSource,
                "referrer" to (referrer ?: "unknown")
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging app_install_completed: ${e.message}")
        }
    }

    /**
     * Campaign details received (marketing attribution)
     */
    fun logCampaignDetails(source: String, medium: String, campaign: String) {
        try {
            AnalyticsService.logEvent("campaign_details", mapOf(
                "source" to source,
                "medium" to medium,
                "campaign" to campaign
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging campaign_details: ${e.message}")
        }
    }

    /**
     * Splash screen viewed
     */
    fun logSplashScreenViewed(durationMs: Long) {
        try {
            AnalyticsService.logEvent("splash_screen_viewed", mapOf(
                "duration_ms" to durationMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging splash_screen_viewed: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: ADVANCED ANALYTICS - UI INTERACTIONS (12 events)
    // ============================================================================

    /**
     * Welcome card displayed
     */
    fun logWelcomeCardDisplayed() {
        try {
            AnalyticsService.logEvent("welcome_card_displayed", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging welcome_card_displayed: ${e.message}")
        }
    }

    /**
     * Welcome card read time
     */
    fun logWelcomeCardReadTime(readTimeMs: Long) {
        try {
            AnalyticsService.logEvent("welcome_card_read_time_ms", mapOf(
                "read_time_ms" to readTimeMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging welcome_card_read_time_ms: ${e.message}")
        }
    }

    /**
     * Starter questions displayed
     */
    fun logStarterQuestionsDisplayed(count: Int) {
        try {
            AnalyticsService.logEvent("starter_questions_displayed", mapOf(
                "count" to count
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging starter_questions_displayed: ${e.message}")
        }
    }

    /**
     * User clicked a starter question
     */
    fun logStarterQuestionClicked(questionIndex: Int, questionText: String) {
        try {
            AnalyticsService.logEvent("starter_question_clicked", mapOf(
                "question_index" to questionIndex,
                "question_text" to questionText
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging starter_question_clicked: ${e.message}")
        }
    }

    /**
     * Menu opened
     */
    fun logMenuOpened() {
        try {
            AnalyticsService.logEvent("menu_opened", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging menu_opened: ${e.message}")
        }
    }

    /**
     * Menu item clicked
     */
    fun logMenuItemClicked(itemName: String) {
        try {
            AnalyticsService.logEvent("menu_item_clicked", mapOf(
                "item_name" to itemName
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging menu_item_clicked: ${e.message}")
        }
    }

    /**
     * Language change clicked
     */
    fun logLanguageChangeClicked(currentLanguage: String) {
        try {
            AnalyticsService.logEvent("language_change_clicked", mapOf(
                "current_language" to currentLanguage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging language_change_clicked: ${e.message}")
        }
    }

    /**
     * Language changed
     */
    fun logLanguageChanged(fromLanguage: String, toLanguage: String) {
        try {
            AnalyticsService.logEvent("language_changed", mapOf(
                "from_language" to fromLanguage,
                "to_language" to toLanguage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging language_changed: ${e.message}")
        }
    }

    /**
     * Theme changed
     */
    fun logThemeChanged(fromTheme: String, toTheme: String) {
        try {
            AnalyticsService.logEvent("theme_changed", mapOf(
                "from_theme" to fromTheme,
                "to_theme" to toTheme
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging theme_changed: ${e.message}")
        }
    }

    /**
     * Location update clicked (user wants to change location)
     */
    fun logLocationUpdateClicked() {
        try {
            AnalyticsService.logEvent("location_update_clicked", emptyMap())
        } catch (e: Exception) {
            println("[Events] ❌ Error logging location_update_clicked: ${e.message}")
        }
    }

    /**
     * Screen viewed (generic screen tracking)
     */
    fun logScreenViewed(screenName: String) {
        try {
            AnalyticsService.logEvent("screen_viewed", mapOf(
                "screen_name" to screenName
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging screen_viewed: ${e.message}")
        }
    }

    /**
     * Screen time spent
     */
    fun logScreenTimeSpent(screenName: String, timeSpentMs: Long) {
        try {
            AnalyticsService.logEvent("screen_time_spent", mapOf(
                "screen_name" to screenName,
                "time_spent_ms" to timeSpentMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging screen_time_spent: ${e.message}")
        }
    }

    // ============================================================================
    // ROUND 5: ADVANCED ANALYTICS - ERROR & ENGAGEMENT (6 events)
    // ============================================================================

    /**
     * Generic app error occurred
     */
    fun logAppErrorOccurred(errorType: String, errorMessage: String, stackTrace: String?) {
        try {
            AnalyticsService.logEvent("app_error_occurred", mapOf(
                "error_type" to errorType,
                "error_message" to errorMessage,
                "stack_trace" to (stackTrace ?: "")
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging app_error_occurred: ${e.message}")
        }
    }

    /**
     * App crash detected (Crashlytics handles this, but can track custom info)
     */
    fun logAppCrashDetected(crashType: String, crashMessage: String) {
        try {
            AnalyticsService.logEvent("app_crash_detected", mapOf(
                "crash_type" to crashType,
                "crash_message" to crashMessage
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging app_crash_detected: ${e.message}")
        }
    }

    /**
     * Feature failed (generic feature failure)
     */
    fun logFeatureFailed(featureName: String, errorType: String) {
        try {
            AnalyticsService.logEvent("feature_failed", mapOf(
                "feature_name" to featureName,
                "error_type" to errorType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging feature_failed: ${e.message}")
        }
    }

    /**
     * User clicked retry after feature failure
     */
    fun logFeatureRetryClicked(featureName: String) {
        try {
            AnalyticsService.logEvent("feature_retry_clicked", mapOf(
                "feature_name" to featureName
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging feature_retry_clicked: ${e.message}")
        }
    }

    /**
     * Feature retry succeeded
     */
    fun logFeatureRetrySucceeded(featureName: String, retryAttempt: Int) {
        try {
            AnalyticsService.logEvent("feature_retry_succeeded", mapOf(
                "feature_name" to featureName,
                "retry_attempt" to retryAttempt
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging feature_retry_succeeded: ${e.message}")
        }
    }

    /**
     * Network reconnected after disconnection
     */
    fun logNetworkReconnected(disconnectionDurationMs: Long) {
        try {
            AnalyticsService.logEvent("network_reconnected", mapOf(
                "disconnection_duration_ms" to disconnectionDurationMs
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging network_reconnected: ${e.message}")
        }
    }

    /**
     * Permission friction point (user denied but feature requires it)
     */
    fun logPermissionFrictionPoint(permissionType: String, featureBlocked: String) {
        try {
            AnalyticsService.logEvent("permission_friction_point", mapOf(
                "permission_type" to permissionType,
                "feature_blocked" to featureBlocked
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_friction_point: ${e.message}")
        }
    }

    // ========== BATCH 2: Permission Detail Events ==========

    /**
     * Permission bottom sheet opened
     * BATCH 2: Track when permission bottom sheet is shown to user
     */
    fun logPermissionBottomSheetOpened(permissionType: String, trigger: String) {
        try {
            AnalyticsService.logEvent("permission_bottom_sheet_opened", mapOf(
                "permission_type" to permissionType, // "voice", "camera", "storage", "location"
                "trigger" to trigger // "first_request", "rationale", "settings_prompt"
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_bottom_sheet_opened: ${e.message}")
        }
    }

    /**
     * Permission allow button clicked
     * BATCH 2: Track when user clicks "Allow" button in permission bottom sheet
     */
    fun logPermissionAllowButtonClicked(permissionType: String) {
        try {
            AnalyticsService.logEvent("permission_allow_button_clicked", mapOf(
                "permission_type" to permissionType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_allow_button_clicked: ${e.message}")
        }
    }

    /**
     * Permission deny button clicked
     * BATCH 2: Track when user clicks "Deny" or dismisses permission bottom sheet
     */
    fun logPermissionDenyButtonClicked(permissionType: String) {
        try {
            AnalyticsService.logEvent("permission_deny_button_clicked", mapOf(
                "permission_type" to permissionType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_deny_button_clicked: ${e.message}")
        }
    }

    /**
     * Permission open settings clicked
     * BATCH 2: Track when user clicks "Open Settings" button after exhausting requests
     */
    fun logPermissionOpenSettingsClicked(permissionType: String) {
        try {
            AnalyticsService.logEvent("permission_open_settings_clicked", mapOf(
                "permission_type" to permissionType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_open_settings_clicked: ${e.message}")
        }
    }

    /**
     * Permission denial count milestone
     * BATCH 2: Track denial count milestones (not every denial, just key thresholds)
     */
    fun logPermissionDenialCountMilestone(permissionType: String, count: Int) {
        try {
            AnalyticsService.logEvent("permission_denial_count_milestone", mapOf(
                "permission_type" to permissionType,
                "denial_count" to count // Track at 2, 3, 5, 10 denials
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_denial_count_milestone: ${e.message}")
        }
    }

    /**
     * Permission granted from settings
     * BATCH 2: Track when user returns from settings with permission granted
     */
    fun logPermissionGrantedFromSettings(permissionType: String) {
        try {
            AnalyticsService.logEvent("permission_granted_from_settings", mapOf(
                "permission_type" to permissionType
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging permission_granted_from_settings: ${e.message}")
        }
    }

    /**
     * Milestone reached (user engagement milestone)
     */
    fun logMilestoneReached(milestoneName: String, milestoneValue: Int) {
        try {
            AnalyticsService.logEvent("milestone_reached", mapOf(
                "milestone_name" to milestoneName,
                "milestone_value" to milestoneValue
            ))
        } catch (e: Exception) {
            println("[Events] ❌ Error logging milestone_reached: ${e.message}")
        }
    }
}
