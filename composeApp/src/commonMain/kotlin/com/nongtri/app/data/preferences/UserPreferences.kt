package com.nongtri.app.data.preferences

import com.nongtri.app.l10n.Language
import com.nongtri.app.data.model.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

expect class UserPreferences {
    val language: StateFlow<Language>
    val themeMode: StateFlow<ThemeMode>
    val hasCompletedOnboarding: StateFlow<Boolean>
    val hapticsEnabled: StateFlow<Boolean>  // Haptic feedback preference

    // Analytics tracking properties
    val sessionCount: StateFlow<Int>
    val messageCount: StateFlow<Int>
    val voiceMessageCount: StateFlow<Int>
    val imageMessageCount: StateFlow<Int>
    val hasUsedVoice: StateFlow<Boolean>
    val hasUsedImageDiagnosis: StateFlow<Boolean>
    val hasSharedGpsLocation: StateFlow<Boolean>
    val hasUsedTts: StateFlow<Boolean>

    fun setLanguage(language: Language)
    fun setThemeMode(mode: ThemeMode)
    fun setHapticsEnabled(enabled: Boolean)
    fun completeOnboarding()

    // Device identification methods
    fun getDeviceId(): String
    fun getUuid(): String
    fun getDeviceInfo(): DeviceInfo

    // Pending diagnosis job (for notification tap handling)
    fun setPendingDiagnosisJobId(jobId: String?)
    fun getPendingDiagnosisJobId(): String?
    
    // Multiple pending diagnosis jobs (for concurrent diagnoses)
    fun addPendingDiagnosisJobId(jobId: String)
    fun removePendingDiagnosisJobId(jobId: String)
    fun getPendingDiagnosisJobIds(): Set<String>

    // Language selection timestamp (for time-to-chat metrics)
    fun setLanguageSelectionTimestamp(timestampMs: Long)
    fun getLanguageSelectionTimestamp(): Long

    // Analytics tracking methods
    fun incrementSessionCount()
    fun incrementMessageCount()
    fun incrementVoiceMessageCount()
    fun incrementImageMessageCount()
    fun setHasUsedVoice(used: Boolean)
    fun setHasUsedImageDiagnosis(used: Boolean)
    fun setHasSharedGpsLocation(shared: Boolean)
    fun setHasUsedTts(used: Boolean)
    fun getInstallDate(): String
    fun getLastSessionDate(): String
    fun setLastSessionDate(date: String)
    fun getLastSessionDuration(): Long
    fun setLastSessionDuration(durationMs: Long)
    fun daysSinceInstall(): Int
    fun daysSinceLastSession(): Int

    companion object {
        fun getInstance(): UserPreferences
    }
}
