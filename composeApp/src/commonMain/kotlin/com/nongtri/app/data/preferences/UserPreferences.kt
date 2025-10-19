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

    fun setLanguage(language: Language)
    fun setThemeMode(mode: ThemeMode)
    fun completeOnboarding()

    // Device identification methods
    fun getDeviceId(): String
    fun getUuid(): String
    fun getDeviceInfo(): DeviceInfo

    companion object {
        fun getInstance(): UserPreferences
    }
}
