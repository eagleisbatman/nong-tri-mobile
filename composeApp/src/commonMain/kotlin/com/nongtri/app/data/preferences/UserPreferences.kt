package com.nongtri.app.data.preferences

import com.nongtri.app.l10n.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class UserPreferences {
    private val _language = MutableStateFlow(Language.ENGLISH)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    fun setLanguage(language: Language) {
        _language.value = language
        // TODO: Persist to local storage
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        // TODO: Persist to local storage
    }

    fun completeOnboarding() {
        _hasCompletedOnboarding.value = true
        // TODO: Persist to local storage
    }

    companion object {
        private var instance: UserPreferences? = null

        fun getInstance(): UserPreferences {
            return instance ?: synchronized(this) {
                instance ?: UserPreferences().also { instance = it }
            }
        }
    }
}
