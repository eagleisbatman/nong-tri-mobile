package com.nongtri.app.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.nongtri.app.BuildConfig
import com.nongtri.app.data.preferences.UserPreferences
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

/**
 * Android implementation of AnalyticsService using Firebase Analytics + PostHog
 */
actual object AnalyticsService {
    private lateinit var analytics: FirebaseAnalytics
    private var isInitialized = false
    private var posthog: PostHogAndroid? = null

    actual fun initialize() {
        if (!isInitialized) {
            analytics = Firebase.analytics
            analytics.setAnalyticsCollectionEnabled(true)

            if (BuildConfig.POSTHOG_API_KEY.isNotBlank() && BuildConfig.POSTHOG_HOST.isNotBlank()) {
                val config = PostHogAndroidConfig(
                    apiKey = BuildConfig.POSTHOG_API_KEY,
                    host = BuildConfig.POSTHOG_HOST
                )
                posthog = PostHogAndroid.setup(Firebase.app.applicationContext, config)
                println("[AnalyticsService] ✅ PostHog initialized")
            } else {
                println("[AnalyticsService] ⚠️ PostHog not configured")
            }

            isInitialized = true
            println("[AnalyticsService] ✅ Initialized successfully")
        }
    }

    actual fun logEvent(eventName: String, params: Map<String, Any>) {
        if (!isInitialized) {
            println("[AnalyticsService] ⚠️ WARNING: Not initialized! Call initialize() first")
            initialize()
        }

        try {
            val bundle = Bundle()

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

            addGlobalParameters(bundle)
            analytics.logEvent(eventName, bundle)
            posthog?.capture(eventName, params.mapValues { it.value })

            Firebase.crashlytics.log("Event: $eventName")
            println("[AnalyticsService] 📊 Event logged: $eventName (${params.size} params)")

        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error logging event: ${e.message}")
            Firebase.crashlytics.recordException(e)
        }
    }

    private fun addGlobalParameters(bundle: Bundle) {
        try {
            val prefs = UserPreferences.getInstance()

            bundle.putString("app_version", BuildConfig.VERSION_NAME)
            posthog?.register(mapOf("app_version" to BuildConfig.VERSION_NAME))

            bundle.putString("user_language", prefs.language.value.code)
            posthog?.register(mapOf("user_language" to prefs.language.value.code))

            val locationRepo = com.nongtri.app.data.repository.LocationRepository.getInstance()
            val hasLocation = locationRepo.hasLocation()
            bundle.putBoolean("has_location", hasLocation)
            bundle.putString("location_type", locationRepo.getLocationType())
            posthog?.register(mapOf(
                "has_location" to hasLocation,
                "location_type" to locationRepo.getLocationType()
            ))

            val cachedLocation = locationRepo.getCachedLocation()
            if (cachedLocation != null) {
                val country = cachedLocation.geoLevel1 ?: cachedLocation.country ?: "Unknown"
                val region = cachedLocation.geoLevel2 ?: cachedLocation.region ?: "Unknown"
                val city = cachedLocation.geoLevel3 ?: cachedLocation.city ?: "Unknown"
                bundle.putString("location_country", country)
                bundle.putString("location_region", region)
                bundle.putString("location_city", city)
                posthog?.register(mapOf(
                    "location_country" to country,
                    "location_region" to region,
                    "location_city" to city
                ))
            }

            val timeZone = java.util.TimeZone.getDefault()
            bundle.putString("device_timezone_id", timeZone.id)
            bundle.putInt("timezone_offset_hours", timeZone.rawOffset / (1000 * 60 * 60))
            bundle.putString("platform", "android")
            posthog?.register(mapOf(
                "platform" to "android",
                "device_timezone_id" to timeZone.id,
                "timezone_offset_hours" to timeZone.rawOffset / (1000 * 60 * 60)
            ))

        } catch (e: Exception) {
            println("[AnalyticsService] ⚠️ Error adding global parameters: ${e.message}")
        }
    }

    actual fun setUserId(userId: String) {
        if (!isInitialized) initialize()

        try {
            analytics.setUserId(userId)
            Firebase.crashlytics.setUserId(userId)
            posthog?.identify(userId, null, mapOf("user_id" to userId))
            println("[AnalyticsService] 👤 User ID set: $userId")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error setting user ID: ${e.message}")
        }
    }

    actual fun setUserProperty(name: String, value: String) {
        if (!isInitialized) initialize()

        try {
            analytics.setUserProperty(name, value)
            posthog?.register(mapOf(name to value))
            println("[AnalyticsService] 🏷️ User property set: $name = $value")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error setting user property: ${e.message}")
        }
    }

    actual fun updateUserProperties() {
        if (!isInitialized) initialize()

        try {
            val prefs = UserPreferences.getInstance()
            setUserProperty("preferred_language", prefs.language.value.code)
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error updating user properties: ${e.message}")
        }
    }

    actual fun logScreenView(screenName: String, screenClass: String) {
        if (!isInitialized) initialize()

        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            posthog?.capture("screen_view", mapOf("screen_name" to screenName, "screen_class" to screenClass))
            println("[AnalyticsService] 📺 Screen view logged: $screenName ($screenClass)")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error logging screen view: ${e.message}")
        }
    }
}
