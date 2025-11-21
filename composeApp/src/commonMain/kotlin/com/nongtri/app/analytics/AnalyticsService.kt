package com.nongtri.app.analytics

/**
 * Centralized analytics service for Firebase Analytics
 *
 * This is the common (expect) declaration. Platform-specific implementations
 * are in androidMain/kotlin/com/nongtri/app/analytics/AnalyticsService.android.kt
 *
 * Usage:
 * ```kotlin
 * AnalyticsService.initialize()
 * AnalyticsService.logEvent("chat_message_sent", mapOf(
 *     "message_type" to "text",
 *     "message_length" to 150
 * ))
 * ```
 */
expect object AnalyticsService {
    /**
     * Initialize analytics providers (Firebase, PostHog, etc.)
     * Call this in MainActivity.onCreate()
     */
    fun initialize()

    /**
     * Log an event with parameters
     * Automatically adds global parameters (app_version, user_language, has_location)
     *
     * @param eventName The name of the event (e.g., "chat_message_sent")
     * @param params Map of event parameters
     */
    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap())

    /**
     * Set the user ID for analytics tracking
     *
     * @param userId The device_id (NOT PII - use device ID, not email/phone)
     */
    fun setUserId(userId: String)

    /**
     * Set a user property
     *
     * @param name Property name (e.g., "preferred_language")
     * @param value Property value (e.g., "vi")
     */
    fun setUserProperty(name: String, value: String)

    /**
     * Update all user properties from UserPreferences
     * Call this after every session to keep Firebase in sync
     */
    fun updateUserProperties()

    /**
     * Log a screen view event
     *
     * @param screenName The name of the screen (e.g., "ChatScreen")
     * @param screenClass The class name (e.g., "com.nongtri.app.ui.screens.ChatScreen")
     */
    fun logScreenView(screenName: String, screenClass: String)
}
