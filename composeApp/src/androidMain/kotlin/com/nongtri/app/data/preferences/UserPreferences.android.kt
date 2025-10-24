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

    private val _hapticsEnabled = MutableStateFlow(true)  // Default: enabled
    actual val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    // Analytics tracking properties
    private val _sessionCount = MutableStateFlow(0)
    actual val sessionCount: StateFlow<Int> = _sessionCount.asStateFlow()

    private val _messageCount = MutableStateFlow(0)
    actual val messageCount: StateFlow<Int> = _messageCount.asStateFlow()

    private val _voiceMessageCount = MutableStateFlow(0)
    actual val voiceMessageCount: StateFlow<Int> = _voiceMessageCount.asStateFlow()

    private val _imageMessageCount = MutableStateFlow(0)
    actual val imageMessageCount: StateFlow<Int> = _imageMessageCount.asStateFlow()

    private val _hasUsedVoice = MutableStateFlow(false)
    actual val hasUsedVoice: StateFlow<Boolean> = _hasUsedVoice.asStateFlow()

    private val _hasUsedImageDiagnosis = MutableStateFlow(false)
    actual val hasUsedImageDiagnosis: StateFlow<Boolean> = _hasUsedImageDiagnosis.asStateFlow()

    private val _hasSharedGpsLocation = MutableStateFlow(false)
    actual val hasSharedGpsLocation: StateFlow<Boolean> = _hasSharedGpsLocation.asStateFlow()

    private val _hasUsedTts = MutableStateFlow(false)
    actual val hasUsedTts: StateFlow<Boolean> = _hasUsedTts.asStateFlow()

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

        // Load haptics preference
        _hapticsEnabled.value = prefs.getBoolean("haptics_enabled", true)  // Default: enabled
        println("UserPreferences: Loaded haptics setting: ${_hapticsEnabled.value}")

        // Load analytics tracking properties
        _sessionCount.value = prefs.getInt("session_count", 0)
        _messageCount.value = prefs.getInt("message_count", 0)
        _voiceMessageCount.value = prefs.getInt("voice_message_count", 0)
        _imageMessageCount.value = prefs.getInt("image_message_count", 0)
        _hasUsedVoice.value = prefs.getBoolean("has_used_voice", false)
        _hasUsedImageDiagnosis.value = prefs.getBoolean("has_used_image_diagnosis", false)
        _hasSharedGpsLocation.value = prefs.getBoolean("has_shared_gps_location", false)
        _hasUsedTts.value = prefs.getBoolean("has_used_tts", false)

        // Set install date if first launch
        if (!prefs.contains("install_date")) {
            val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                .format(java.util.Date())
            prefs.edit().putString("install_date", currentDate).apply()
            println("UserPreferences: Set install date: $currentDate")
        }

        println("UserPreferences: Loaded analytics - sessions: ${_sessionCount.value}, messages: ${_messageCount.value}")
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

    actual fun setHapticsEnabled(enabled: Boolean) {
        println("UserPreferences: Setting haptics to $enabled")

        // Step 1: Update in-memory state
        _hapticsEnabled.value = enabled

        // Step 2: Save to local SharedPreferences immediately
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        println("UserPreferences: Saved haptics setting to local storage")

        // Note: No backend sync needed - haptics is a client-side preference
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

                val requestBody = PreferencesSyncRequest(
                    language = _language.value.code,
                    themeMode = when(_themeMode.value) {
                        ThemeMode.LIGHT -> "light"
                        ThemeMode.DARK -> "dark"
                        ThemeMode.SYSTEM -> "system"
                    },
                    onboardingCompleted = _hasCompletedOnboarding.value
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

    @kotlinx.serialization.Serializable
    private data class PreferencesSyncRequest(
        val language: String,
        val themeMode: String,
        val onboardingCompleted: Boolean
    )

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

    // Pending diagnosis job ID (for notification tap handling)
    actual fun setPendingDiagnosisJobId(jobId: String?) {
        prefs.edit().apply {
            if (jobId != null) {
                putString("pending_diagnosis_job_id", jobId)
                println("UserPreferences: Saved pending diagnosis job ID: $jobId")
            } else {
                remove("pending_diagnosis_job_id")
                println("UserPreferences: Cleared pending diagnosis job ID")
            }
            apply()
        }
    }

    actual fun getPendingDiagnosisJobId(): String? {
        val jobId = prefs.getString("pending_diagnosis_job_id", null)
        println("UserPreferences: Retrieved pending diagnosis job ID: $jobId")
        return jobId
    }

    // Analytics tracking methods
    actual fun incrementSessionCount() {
        _sessionCount.value += 1
        prefs.edit().putInt("session_count", _sessionCount.value).apply()
        println("UserPreferences: Incremented session count to ${_sessionCount.value}")
    }

    actual fun incrementMessageCount() {
        _messageCount.value += 1
        prefs.edit().putInt("message_count", _messageCount.value).apply()
        println("UserPreferences: Incremented message count to ${_messageCount.value}")
    }

    actual fun incrementVoiceMessageCount() {
        _voiceMessageCount.value += 1
        prefs.edit().putInt("voice_message_count", _voiceMessageCount.value).apply()
        println("UserPreferences: Incremented voice message count to ${_voiceMessageCount.value}")
    }

    actual fun incrementImageMessageCount() {
        _imageMessageCount.value += 1
        prefs.edit().putInt("image_message_count", _imageMessageCount.value).apply()
        println("UserPreferences: Incremented image message count to ${_imageMessageCount.value}")
    }

    actual fun setHasUsedVoice(used: Boolean) {
        _hasUsedVoice.value = used
        prefs.edit().putBoolean("has_used_voice", used).apply()
        println("UserPreferences: Set has used voice to $used")
    }

    actual fun setHasUsedImageDiagnosis(used: Boolean) {
        _hasUsedImageDiagnosis.value = used
        prefs.edit().putBoolean("has_used_image_diagnosis", used).apply()
        println("UserPreferences: Set has used image diagnosis to $used")
    }

    actual fun setHasSharedGpsLocation(shared: Boolean) {
        _hasSharedGpsLocation.value = shared
        prefs.edit().putBoolean("has_shared_gps_location", shared).apply()
        println("UserPreferences: Set has shared GPS location to $shared")
    }

    actual fun setHasUsedTts(used: Boolean) {
        _hasUsedTts.value = used
        prefs.edit().putBoolean("has_used_tts", used).apply()
        println("UserPreferences: Set has used TTS to $used")
    }

    actual fun getInstallDate(): String {
        return prefs.getString("install_date", "") ?: ""
    }

    actual fun getLastSessionDate(): String {
        return prefs.getString("last_session_date", "") ?: ""
    }

    actual fun setLastSessionDate(date: String) {
        prefs.edit().putString("last_session_date", date).apply()
    }

    actual fun getLastSessionDuration(): Long {
        return prefs.getLong("last_session_duration", 0L)
    }

    actual fun setLastSessionDuration(durationMs: Long) {
        prefs.edit().putLong("last_session_duration", durationMs).apply()
    }

    actual fun daysSinceInstall(): Int {
        val installDate = getInstallDate()
        if (installDate.isEmpty()) return 0

        try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val install = format.parse(installDate)
            val now = java.util.Date()
            val diffMs = now.time - (install?.time ?: 0)
            return (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            println("UserPreferences: Error calculating days since install: ${e.message}")
            return 0
        }
    }

    actual fun daysSinceLastSession(): Int {
        val lastSessionDate = getLastSessionDate()
        if (lastSessionDate.isEmpty()) return 0

        try {
            val format = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val lastSession = format.parse(lastSessionDate)
            val now = java.util.Date()
            val diffMs = now.time - (lastSession?.time ?: 0)
            return (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            println("UserPreferences: Error calculating days since last session: ${e.message}")
            return 0
        }
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
