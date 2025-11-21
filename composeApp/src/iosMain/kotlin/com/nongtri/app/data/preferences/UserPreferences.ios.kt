package com.nongtri.app.data.preferences

import com.nongtri.app.l10n.Language
import com.nongtri.app.data.model.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

actual class UserPreferences private constructor() {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val prefsKeyPrefix = "nongtri_user_prefs_"

    private val _language = MutableStateFlow(Language.ENGLISH)
    actual val language: StateFlow<Language> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    actual val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    actual val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(true)
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
        loadPreferencesFromLocal()
    }

    private fun loadPreferencesFromLocal() {
        // Load language
        val savedLanguage = userDefaults.stringForKey("${prefsKeyPrefix}language")
        if (savedLanguage != null) {
            _language.value = when (savedLanguage) {
                "en" -> Language.ENGLISH
                "vi" -> Language.VIETNAMESE
                else -> Language.ENGLISH
            }
        }

        // Load theme mode
        val savedThemeMode = userDefaults.stringForKey("${prefsKeyPrefix}theme_mode") ?: "system"
        _themeMode.value = when (savedThemeMode) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }

        // Load onboarding status
        _hasCompletedOnboarding.value = userDefaults.boolForKey("${prefsKeyPrefix}onboarding_completed")

        // Load haptics preference
        _hapticsEnabled.value = userDefaults.boolForKey("${prefsKeyPrefix}haptics_enabled")

        // Load analytics tracking properties
        _sessionCount.value = userDefaults.integerForKey("${prefsKeyPrefix}session_count").toInt()
        _messageCount.value = userDefaults.integerForKey("${prefsKeyPrefix}message_count").toInt()
        _voiceMessageCount.value = userDefaults.integerForKey("${prefsKeyPrefix}voice_message_count").toInt()
        _imageMessageCount.value = userDefaults.integerForKey("${prefsKeyPrefix}image_message_count").toInt()
        _hasUsedVoice.value = userDefaults.boolForKey("${prefsKeyPrefix}has_used_voice")
        _hasUsedImageDiagnosis.value = userDefaults.boolForKey("${prefsKeyPrefix}has_used_image_diagnosis")
        _hasSharedGpsLocation.value = userDefaults.boolForKey("${prefsKeyPrefix}has_shared_gps_location")
        _hasUsedTts.value = userDefaults.boolForKey("${prefsKeyPrefix}has_used_tts")
    }

    actual fun setLanguage(language: Language) {
        _language.value = language
        userDefaults.setObject(language.code, forKey = "${prefsKeyPrefix}language")
    }

    actual fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        val modeString = when (mode) {
            ThemeMode.LIGHT -> "light"
            ThemeMode.DARK -> "dark"
            ThemeMode.SYSTEM -> "system"
        }
        userDefaults.setObject(modeString, forKey = "${prefsKeyPrefix}theme_mode")
    }

    actual fun setHapticsEnabled(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        userDefaults.setBool(enabled, forKey = "${prefsKeyPrefix}haptics_enabled")
    }

    actual fun completeOnboarding() {
        _hasCompletedOnboarding.value = true
        userDefaults.setBool(true, forKey = "${prefsKeyPrefix}onboarding_completed")
    }

    actual fun getDeviceId(): String {
        // iOS: Use identifierForVendor (IDFV) as device ID
        val identifier = platform.UIKit.UIDevice.currentDevice.identifierForVendor
        return identifier?.UUIDString ?: "ios-unknown"
    }

    actual fun getUuid(): String {
        return getDeviceId()
    }

    actual fun getDeviceInfo(): DeviceInfo {
        // iOS implementation - return basic device info
        return DeviceInfo(
            device_type = "ios",
            device_os = "iOS",
            device_os_version = platform.UIKit.UIDevice.currentDevice.systemVersion,
            device_manufacturer = "Apple",
            device_model = platform.UIKit.UIDevice.currentDevice.model,
            device_brand = "Apple",
            screen_width = 0, // Would need UIKit screen size
            screen_height = 0,
            screen_density = 0.0,
            client_source = "ios",
            client_version = "1.0.0",
            client_build_number = "1",
            timezone_offset = 0,
            device_language = "en"
        )
    }

    // Pending diagnosis job ID (for notification tap handling - single ID for backward compatibility)
    actual fun setPendingDiagnosisJobId(jobId: String?) {
        if (jobId != null) {
            userDefaults.setObject(jobId, forKey = "${prefsKeyPrefix}pending_diagnosis_job_id")
        } else {
            userDefaults.removeObjectForKey("${prefsKeyPrefix}pending_diagnosis_job_id")
        }
    }

    actual fun getPendingDiagnosisJobId(): String? {
        return userDefaults.stringForKey("${prefsKeyPrefix}pending_diagnosis_job_id")
    }

    // Multiple pending diagnosis jobs (for concurrent diagnoses)
    actual fun addPendingDiagnosisJobId(jobId: String) {
        val currentIds = getPendingDiagnosisJobIds().toMutableSet()
        currentIds.add(jobId)
        savePendingDiagnosisJobIds(currentIds)
    }

    actual fun removePendingDiagnosisJobId(jobId: String) {
        val currentIds = getPendingDiagnosisJobIds().toMutableSet()
        currentIds.remove(jobId)
        savePendingDiagnosisJobIds(currentIds)
    }

    actual fun getPendingDiagnosisJobIds(): Set<String> {
        val idsString = userDefaults.stringForKey("${prefsKeyPrefix}pending_diagnosis_job_ids")
        return if (idsString == null || idsString.isBlank()) {
            emptySet()
        } else {
            try {
                // Try parsing as JSON array first (new format)
                if (idsString.startsWith("[") && idsString.endsWith("]")) {
                    // Parse JSON array: ["jobId1","jobId2","jobId3"]
                    val trimmed = idsString.removePrefix("[").removeSuffix("]")
                    val ids = mutableSetOf<String>()
                    trimmed.split(",").forEach { item ->
                        val cleaned = item.trim().removeSurrounding("\"")
                        if (cleaned.isNotBlank()) {
                            ids.add(cleaned)
                        }
                    }
                    return ids
                } else {
                    // Fallback to comma-separated format (legacy)
                    idsString.split(",").filter { it.isNotBlank() }.toSet()
                }
            } catch (e: Exception) {
                // If parsing fails, try comma-separated format
                idsString.split(",").filter { it.isNotBlank() }.toSet()
            }
        }
    }

    private fun savePendingDiagnosisJobIds(ids: Set<String>) {
        if (ids.isEmpty()) {
            userDefaults.removeObjectForKey("${prefsKeyPrefix}pending_diagnosis_job_ids")
        } else {
            // Store as JSON array for better structure and future extensibility
            // Format: ["jobId1","jobId2","jobId3"] - allows for metadata/ordering later
            val jsonArray = ids.joinToString(",", prefix = "[", postfix = "]") { "\"$it\"" }
            userDefaults.setObject(jsonArray, forKey = "${prefsKeyPrefix}pending_diagnosis_job_ids")
        }
        // Also update single ID for backward compatibility (most recent)
        if (ids.isNotEmpty()) {
            setPendingDiagnosisJobId(ids.maxOrNull())
        } else {
            setPendingDiagnosisJobId(null)
        }
    }

    // Analytics tracking methods
    actual fun incrementSessionCount() {
        _sessionCount.value += 1
        userDefaults.setInteger(_sessionCount.value.toLong(), forKey = "${prefsKeyPrefix}session_count")
    }

    actual fun incrementMessageCount() {
        _messageCount.value += 1
        userDefaults.setInteger(_messageCount.value.toLong(), forKey = "${prefsKeyPrefix}message_count")
    }

    actual fun incrementVoiceMessageCount() {
        _voiceMessageCount.value += 1
        userDefaults.setInteger(_voiceMessageCount.value.toLong(), forKey = "${prefsKeyPrefix}voice_message_count")
    }

    actual fun incrementImageMessageCount() {
        _imageMessageCount.value += 1
        userDefaults.setInteger(_imageMessageCount.value.toLong(), forKey = "${prefsKeyPrefix}image_message_count")
    }

    actual fun setHasUsedVoice(used: Boolean) {
        _hasUsedVoice.value = used
        userDefaults.setBool(used, forKey = "${prefsKeyPrefix}has_used_voice")
    }

    actual fun setHasUsedImageDiagnosis(used: Boolean) {
        _hasUsedImageDiagnosis.value = used
        userDefaults.setBool(used, forKey = "${prefsKeyPrefix}has_used_image_diagnosis")
    }

    actual fun setHasSharedGpsLocation(shared: Boolean) {
        _hasSharedGpsLocation.value = shared
        userDefaults.setBool(shared, forKey = "${prefsKeyPrefix}has_shared_gps_location")
    }

    actual fun setHasUsedTts(used: Boolean) {
        _hasUsedTts.value = used
        userDefaults.setBool(used, forKey = "${prefsKeyPrefix}has_used_tts")
    }

    actual fun getInstallDate(): String {
        val installDate = userDefaults.stringForKey("${prefsKeyPrefix}install_date")
        if (installDate == null) {
            val formatter = platform.Foundation.NSDateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            val currentDate = formatter.stringFromDate(platform.Foundation.NSDate())
            userDefaults.setObject(currentDate, forKey = "${prefsKeyPrefix}install_date")
            return currentDate
        }
        return installDate
    }

    actual fun getLastSessionDate(): String {
        return userDefaults.stringForKey("${prefsKeyPrefix}last_session_date") ?: ""
    }

    actual fun setLastSessionDate(date: String) {
        userDefaults.setObject(date, forKey = "${prefsKeyPrefix}last_session_date")
    }

    actual fun getLastSessionDuration(): Long {
        return userDefaults.doubleForKey("${prefsKeyPrefix}last_session_duration").toLong()
    }

    actual fun setLastSessionDuration(durationMs: Long) {
        userDefaults.setDouble(durationMs.toDouble(), forKey = "${prefsKeyPrefix}last_session_duration")
    }

    actual fun daysSinceInstall(): Int {
        // Simplified implementation for iOS
        return 0
    }

    actual fun daysSinceLastSession(): Int {
        // Simplified implementation for iOS
        return 0
    }

    actual companion object {
        private var instance: UserPreferences? = null

        actual fun getInstance(): UserPreferences {
            if (instance == null) {
                instance = UserPreferences()
            }
            return instance!!
        }
    }
}

