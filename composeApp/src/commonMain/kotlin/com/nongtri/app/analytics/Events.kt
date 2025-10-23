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
}
