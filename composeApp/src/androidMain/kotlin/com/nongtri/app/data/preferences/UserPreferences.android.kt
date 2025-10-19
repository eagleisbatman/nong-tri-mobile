package com.nongtri.app.data.preferences

import android.content.Context
import com.nongtri.app.l10n.Language
import com.nongtri.app.data.model.DeviceInfo
import com.nongtri.app.data.provider.DeviceInfoProvider
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

actual class UserPreferences private constructor(context: Context) {
    private val deviceInfoProvider = DeviceInfoProvider(context)
    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _language = MutableStateFlow(Language.ENGLISH)
    actual val language: StateFlow<Language> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    actual val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    actual val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    init {
        // Load preferences from local storage on initialization
        loadPreferencesFromLocal()

        // If local storage is empty, fetch from backend
        if (_language.value == Language.ENGLISH &&
            _themeMode.value == ThemeMode.SYSTEM &&
            !_hasCompletedOnboarding.value) {
            // Defaults detected - might be first launch or cleared data
            // Try fetching from backend asynchronously
            fetchPreferencesFromBackend()
        }
    }

    private fun loadPreferencesFromLocal() {
        // Load language
        val savedLanguage = prefs.getString("language", null)
        if (savedLanguage != null) {
            try {
                _language.value = when(savedLanguage) {
                    "en" -> Language.ENGLISH
                    "vi" -> Language.VIETNAMESE
                    else -> Language.ENGLISH
                }
                println("UserPreferences: Loaded language from local: ${_language.value}")
            } catch (e: Exception) {
                println("UserPreferences: Error loading language: ${e.message}")
            }
        }

        // Load theme mode
        val savedThemeMode = prefs.getString("theme_mode", "system")
        _themeMode.value = when(savedThemeMode) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
        println("UserPreferences: Loaded theme from local: ${_themeMode.value}")

        // Load onboarding status
        _hasCompletedOnboarding.value = prefs.getBoolean("onboarding_completed", false)
        println("UserPreferences: Loaded onboarding status: ${_hasCompletedOnboarding.value}")
    }

    actual fun setLanguage(language: Language) {
        println("UserPreferences: Setting language to $language")

        // Step 1: Update in-memory state
        _language.value = language

        // Step 2: Save to local SharedPreferences immediately
        prefs.edit().putString("language", language.code).apply()
        println("UserPreferences: Saved language to local storage")

        // Step 3: Sync to backend asynchronously
        syncPreferencesToBackend()
    }

    actual fun setThemeMode(mode: ThemeMode) {
        println("UserPreferences: Setting theme mode to $mode")

        // Step 1: Update in-memory state
        _themeMode.value = mode

        // Step 2: Save to local SharedPreferences immediately
        val modeString = when(mode) {
            ThemeMode.LIGHT -> "light"
            ThemeMode.DARK -> "dark"
            ThemeMode.SYSTEM -> "system"
        }
        prefs.edit().putString("theme_mode", modeString).apply()
        println("UserPreferences: Saved theme to local storage")

        // Step 3: Sync to backend asynchronously
        syncPreferencesToBackend()
    }

    actual fun completeOnboarding() {
        println("UserPreferences: Completing onboarding")

        // Step 1: Update in-memory state
        _hasCompletedOnboarding.value = true

        // Step 2: Save to local SharedPreferences immediately
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        println("UserPreferences: Saved onboarding status to local storage")

        // Step 3: Sync to backend asynchronously
        syncPreferencesToBackend()
    }

    private fun syncPreferencesToBackend() {
        coroutineScope.launch {
            try {
                val deviceId = deviceInfoProvider.getDeviceId()
                val apiClient = com.nongtri.app.data.api.ApiClient.getInstance()

                val requestBody = mapOf(
                    "language" to _language.value.code,
                    "themeMode" to when(_themeMode.value) {
                        ThemeMode.LIGHT -> "light"
                        ThemeMode.DARK -> "dark"
                        ThemeMode.SYSTEM -> "system"
                    },
                    "onboardingCompleted" to _hasCompletedOnboarding.value
                )

                val response = apiClient.client.put("${apiClient.baseUrl}/api/preferences/$deviceId") {
                    contentType(io.ktor.http.ContentType.Application.Json)
                    setBody(requestBody)
                }

                if (response.status.isSuccess()) {
                    println("UserPreferences: Successfully synced preferences to backend")
                } else {
                    println("UserPreferences: Failed to sync preferences to backend: ${response.status}")
                }
            } catch (e: Exception) {
                println("UserPreferences: Error syncing preferences to backend: ${e.message}")
                // Don't throw - local storage already updated, backend sync is best-effort
            }
        }
    }

    private fun fetchPreferencesFromBackend() {
        coroutineScope.launch {
            try {
                val deviceId = deviceInfoProvider.getDeviceId()
                val apiClient = com.nongtri.app.data.api.ApiClient.getInstance()

                println("UserPreferences: Fetching preferences from backend for deviceId: $deviceId")

                val response: io.ktor.client.statement.HttpResponse = apiClient.client.get("/api/preferences/$deviceId")

                if (response.status.isSuccess()) {
                    val responseBody = response.bodyAsText()
                    val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                    val prefsResponse = json.decodeFromString<PreferencesResponse>(responseBody)

                    if (prefsResponse.success && prefsResponse.preferences != null) {
                        println("UserPreferences: Found preferences on backend, loading...")

                        // Update in-memory state
                        prefsResponse.preferences.language?.let { lang ->
                            _language.value = when(lang) {
                                "en" -> Language.ENGLISH
                                "vi" -> Language.VIETNAMESE
                                else -> Language.ENGLISH
                            }
                            // Save to local for next time
                            prefs.edit().putString("language", lang).apply()
                            println("UserPreferences: Loaded language from backend: ${_language.value}")
                        }

                        prefsResponse.preferences.themeMode?.let { theme ->
                            _themeMode.value = when(theme) {
                                "light" -> ThemeMode.LIGHT
                                "dark" -> ThemeMode.DARK
                                else -> ThemeMode.SYSTEM
                            }
                            prefs.edit().putString("theme_mode", theme).apply()
                            println("UserPreferences: Loaded theme from backend: ${_themeMode.value}")
                        }

                        prefsResponse.preferences.onboardingCompleted?.let { completed ->
                            _hasCompletedOnboarding.value = completed
                            prefs.edit().putBoolean("onboarding_completed", completed).apply()
                            println("UserPreferences: Loaded onboarding status from backend: $completed")
                        }
                    } else {
                        println("UserPreferences: No preferences found on backend (new user)")
                    }
                } else {
                    println("UserPreferences: Failed to fetch preferences from backend: ${response.status}")
                }
            } catch (e: Exception) {
                println("UserPreferences: Error fetching preferences from backend: ${e.message}")
                // Don't throw - app can continue with defaults
            }
        }
    }

    @kotlinx.serialization.Serializable
    private data class PreferencesResponse(
        val success: Boolean,
        val preferences: PreferencesData? = null
    )

    @kotlinx.serialization.Serializable
    private data class PreferencesData(
        val language: String? = null,
        val themeMode: String? = null,
        val onboardingCompleted: Boolean? = null
    )

    // Device identification methods
    actual fun getDeviceId(): String {
        return deviceInfoProvider.getDeviceId()
    }

    actual fun getUuid(): String {
        return deviceInfoProvider.getUuid()
    }

    actual fun getDeviceInfo(): DeviceInfo {
        return deviceInfoProvider.getDeviceInfo()
    }

    actual companion object {
        private var instance: UserPreferences? = null

        fun initialize(context: Context) {
            if (instance == null) {
                instance = UserPreferences(context.applicationContext)
            }
        }

        actual fun getInstance(): UserPreferences {
            return instance ?: throw IllegalStateException(
                "UserPreferences not initialized. Call initialize(context) first."
            )
        }
    }
}
