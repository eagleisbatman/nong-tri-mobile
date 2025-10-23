package com.nongtri.app.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.nongtri.app.BuildConfig
import com.nongtri.app.data.preferences.UserPreferences

/**
 * Android implementation of AnalyticsService using Firebase Analytics
 */
actual object AnalyticsService {
    private lateinit var analytics: FirebaseAnalytics
    private var isInitialized = false

    /**
     * Initialize Firebase Analytics
     * Call this once in MainActivity.onCreate()
     */
    actual fun initialize() {
        if (!isInitialized) {
            analytics = Firebase.analytics
            analytics.setAnalyticsCollectionEnabled(true)
            isInitialized = true
            println("[AnalyticsService] ✅ Initialized successfully")
        }
    }

    /**
     * Log an event with automatic global parameters
     *
     * @param eventName The event name (e.g., "chat_message_sent")
     * @param params Map of parameters
     */
    actual fun logEvent(eventName: String, params: Map<String, Any>) {
        if (!isInitialized) {
            println("[AnalyticsService] ⚠️ WARNING: Not initialized! Call initialize() first")
            initialize() // Auto-initialize if forgotten
        }

        try {
            val bundle = Bundle()

            // Add all provided parameters
            params.forEach { (key, value) ->
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Float -> bundle.putFloat(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                    else -> bundle.putString(key, value.toString())
                }
            }

            // Add global parameters automatically
            addGlobalParameters(bundle)

            // Log to Firebase Analytics
            analytics.logEvent(eventName, bundle)

            // Also log to Crashlytics for debugging (helps correlate crashes with events)
            Firebase.crashlytics.log("Event: $eventName")

            println("[AnalyticsService] 📊 Event logged: $eventName (${params.size} params)")

        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error logging event: ${e.message}")
            Firebase.crashlytics.recordException(e)
        }
    }

    /**
     * Add global parameters to every event
     */
    private fun addGlobalParameters(bundle: Bundle) {
        try {
            val prefs = UserPreferences.getInstance()

            // App version
            bundle.putString("app_version", BuildConfig.VERSION_NAME)

            // User language
            bundle.putString("user_language", prefs.language.value.code)

            // Location availability
            val hasLocation = false // TODO: Get from LocationManager when available
            bundle.putBoolean("has_location", hasLocation)

            // Platform
            bundle.putString("platform", "android")

        } catch (e: Exception) {
            println("[AnalyticsService] ⚠️ Error adding global parameters: ${e.message}")
        }
    }

    /**
     * Set user ID for analytics tracking
     *
     * @param userId The device_id (NOT PII)
     */
    actual fun setUserId(userId: String) {
        if (!isInitialized) initialize()

        try {
            analytics.setUserId(userId)
            Firebase.crashlytics.setUserId(userId)
            println("[AnalyticsService] 👤 User ID set: $userId")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error setting user ID: ${e.message}")
        }
    }

    /**
     * Set a user property
     *
     * @param name Property name (e.g., "preferred_language")
     * @param value Property value (e.g., "vi")
     */
    actual fun setUserProperty(name: String, value: String) {
        if (!isInitialized) initialize()

        try {
            analytics.setUserProperty(name, value)
            println("[AnalyticsService] 🏷️ User property set: $name = $value")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error setting user property: ${e.message}")
        }
    }

    /**
     * Update all user properties from UserPreferences
     * Call this after every session
     */
    actual fun updateUserProperties() {
        if (!isInitialized) initialize()

        try {
            val prefs = UserPreferences.getInstance()

            // Language & theme
            setUserProperty("preferred_language", prefs.language.value.code)

            // Lifecycle metrics
            setUserProperty("lifetime_sessions", prefs.sessionCount.value.toString())
            setUserProperty("lifetime_messages", prefs.messageCount.value.toString())
            setUserProperty("lifetime_voice_messages", prefs.voiceMessageCount.value.toString())
            setUserProperty("lifetime_image_messages", prefs.imageMessageCount.value.toString())

            // User segment (based on session count)
            val segment = when {
                prefs.sessionCount.value <= 3 -> "new"
                prefs.sessionCount.value <= 9 -> "casual"
                prefs.sessionCount.value <= 24 -> "regular"
                prefs.sessionCount.value <= 49 -> "engaged"
                else -> "power_user"
            }
            setUserProperty("user_segment", segment)

            // Feature adoption flags
            setUserProperty("has_used_voice", prefs.hasUsedVoice.value.toString())
            setUserProperty("has_used_image_diagnosis", prefs.hasUsedImageDiagnosis.value.toString())
            setUserProperty("has_shared_location", prefs.hasSharedGpsLocation.value.toString())

            // Days since install
            setUserProperty("days_since_install", prefs.daysSinceInstall().toString())

            println("[AnalyticsService] ✅ Updated all user properties")

        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error updating user properties: ${e.message}")
            Firebase.crashlytics.recordException(e)
        }
    }

    /**
     * Log a screen view event
     *
     * @param screenName The screen name (e.g., "ChatScreen")
     * @param screenClass The screen class (e.g., "com.nongtri.app.ui.screens.ChatScreen")
     */
    actual fun logScreenView(screenName: String, screenClass: String) {
        if (!isInitialized) initialize()

        val params = mapOf(
            FirebaseAnalytics.Param.SCREEN_NAME to screenName,
            FirebaseAnalytics.Param.SCREEN_CLASS to screenClass
        )

        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }
}
