package com.nongtri.app.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.nongtri.app.AppConfig
import com.nongtri.app.data.preferences.UserPreferences
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import com.posthog.PostHog

/**
 * Android implementation of AnalyticsService using Firebase Analytics + PostHog
 */
actual object AnalyticsService {
    private lateinit var analytics: FirebaseAnalytics
    private var isInitialized = false
    private var posthogInitialized = false
    private var applicationContext: android.content.Context? = null

    actual fun initialize(context: Any?) {
        if (!isInitialized) {
            analytics = Firebase.analytics
            analytics.setAnalyticsCollectionEnabled(true)

            // Get application context from parameter
            applicationContext = if (context is android.content.Context) {
                context.applicationContext
            } else {
                null
            }
            
            // Initialize PostHog if configured
            val posthogApiKey = com.nongtri.app.BuildConfig.POSTHOG_API_KEY
            val posthogHost = com.nongtri.app.BuildConfig.POSTHOG_HOST
            if (posthogApiKey.isNotBlank() && posthogHost.isNotBlank() && applicationContext != null) {
                try {
                    val config = PostHogAndroidConfig(
                        apiKey = posthogApiKey,
                        host = posthogHost
                    )
                    // PostHog Android SDK 3.0: Initialize PostHog
                    PostHogAndroid.setup(
                        applicationContext!! as android.app.Application, 
                        config
                    )
                    posthogInitialized = true
                    println("[AnalyticsService] ✅ PostHog initialized with API key: ${posthogApiKey.take(10)}...")
                } catch (e: Exception) {
                    println("[AnalyticsService] ⚠️ PostHog initialization failed: ${e.message}")
                    e.printStackTrace()
                }
            } else {
                if (posthogApiKey.isBlank()) {
                    println("[AnalyticsService] ⚠️ PostHog not configured: API key is empty")
                } else if (posthogHost.isBlank()) {
                    println("[AnalyticsService] ⚠️ PostHog not configured: Host is empty")
                } else {
                    println("[AnalyticsService] ⚠️ PostHog not configured: Application context unavailable")
                }
            }

            isInitialized = true
            println("[AnalyticsService] ✅ Initialized successfully")
        }
    }

    actual fun logEvent(eventName: String, params: Map<String, Any>) {
        if (!isInitialized) {
            println("[AnalyticsService] ⚠️ WARNING: Not initialized! Call initialize() first")
            initialize(null)
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
            
            // PostHog: Capture event using static API
            if (posthogInitialized) {
                try {
                    PostHog.capture(eventName, properties = params)
                } catch (e: Exception) {
                    println("[AnalyticsService] ⚠️ PostHog capture failed: ${e.message}")
                }
            }

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

            bundle.putString("app_version", AppConfig.VERSION_NAME)
            if (posthogInitialized) {
                try {
                    PostHog.register("app_version", AppConfig.VERSION_NAME)
                } catch (e: Exception) {
                    // Ignore PostHog errors
                }
            }

            bundle.putString("user_language", prefs.language.value.code)
            if (posthogInitialized) {
                try {
                    PostHog.register("user_language", prefs.language.value.code)
                } catch (e: Exception) {
                    // Ignore PostHog errors
                }
            }

            val locationRepo = com.nongtri.app.data.repository.LocationRepository.getInstance()
            val hasLocation = locationRepo.hasLocation()
            bundle.putBoolean("has_location", hasLocation)
            bundle.putString("location_type", locationRepo.getLocationType())
            if (posthogInitialized) {
                try {
                    PostHog.register("has_location", hasLocation)
                    PostHog.register("location_type", locationRepo.getLocationType())
                } catch (e: Exception) {
                    // Ignore PostHog errors
                }
            }

            val cachedLocation = locationRepo.getCachedLocation()
            if (cachedLocation != null) {
                val country = cachedLocation.geoLevel1 ?: cachedLocation.country ?: "Unknown"
                val region = cachedLocation.geoLevel2 ?: cachedLocation.region ?: "Unknown"
                val city = cachedLocation.geoLevel3 ?: cachedLocation.city ?: "Unknown"
                bundle.putString("location_country", country)
                bundle.putString("location_region", region)
                bundle.putString("location_city", city)
                if (posthogInitialized) {
                    try {
                        PostHog.register("location_country", country)
                        PostHog.register("location_region", region)
                        PostHog.register("location_city", city)
                    } catch (e: Exception) {
                        // Ignore PostHog errors
                    }
                }
            }

            val timeZone = java.util.TimeZone.getDefault()
            bundle.putString("device_timezone_id", timeZone.id)
            bundle.putInt("timezone_offset_hours", timeZone.rawOffset / (1000 * 60 * 60))
            bundle.putString("platform", "android")
            if (posthogInitialized) {
                try {
                    PostHog.register("platform", "android")
                    PostHog.register("device_timezone_id", timeZone.id)
                    PostHog.register("timezone_offset_hours", timeZone.rawOffset / (1000 * 60 * 60))
                } catch (e: Exception) {
                    // Ignore PostHog errors
                }
            }

        } catch (e: Exception) {
            println("[AnalyticsService] ⚠️ Error adding global parameters: ${e.message}")
        }
    }

    actual fun setUserId(userId: String) {
        if (!isInitialized) initialize(null)

        try {
            analytics.setUserId(userId)
            Firebase.crashlytics.setUserId(userId)
            if (posthogInitialized) {
                try {
                    PostHog.identify(userId, userProperties = mapOf("user_id" to userId))
                } catch (e: Exception) {
                    println("[AnalyticsService] ⚠️ PostHog identify failed: ${e.message}")
                }
            }
            println("[AnalyticsService] 👤 User ID set: $userId")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error setting user ID: ${e.message}")
        }
    }

    actual fun setUserProperty(name: String, value: String) {
        if (!isInitialized) initialize(null)

        try {
            analytics.setUserProperty(name, value)
            if (posthogInitialized) {
                try {
                    PostHog.register(name, value)
                } catch (e: Exception) {
                    println("[AnalyticsService] ⚠️ PostHog register failed: ${e.message}")
                }
            }
            println("[AnalyticsService] 🏷️ User property set: $name = $value")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error setting user property: ${e.message}")
        }
    }

    actual fun updateUserProperties() {
        if (!isInitialized) initialize(null)

        try {
            val prefs = UserPreferences.getInstance()
            setUserProperty("preferred_language", prefs.language.value.code)
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error updating user properties: ${e.message}")
        }
    }

    actual fun logScreenView(screenName: String, screenClass: String) {
        if (!isInitialized) initialize(null)

        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
            }
            analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            if (posthogInitialized) {
                try {
                    PostHog.screen(screenName, properties = mapOf("screen_class" to screenClass))
                } catch (e: Exception) {
                    println("[AnalyticsService] ⚠️ PostHog screen failed: ${e.message}")
                }
            }
            println("[AnalyticsService] 📺 Screen view logged: $screenName ($screenClass)")
        } catch (e: Exception) {
            println("[AnalyticsService] ❌ Error logging screen view: ${e.message}")
        }
    }
}
