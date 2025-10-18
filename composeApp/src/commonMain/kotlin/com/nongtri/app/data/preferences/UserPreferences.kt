package com.nongtri.app.data.preferences

import com.nongtri.app.l10n.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}

class UserPreferences private constructor() {
    private val _language = MutableStateFlow(Language.ENGLISH)
    val language: StateFlow<Language> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    fun setLanguage(language: Language) {
        println("UserPreferences: Setting language to $language")
        _language.value = language
        // TODO: Persist to local storage
    }

    fun setThemeMode(mode: ThemeMode) {
        println("UserPreferences: Setting theme mode to $mode")
        _themeMode.value = mode
        // TODO: Persist to local storage
    }

    fun completeOnboarding() {
        println("UserPreferences: Completing onboarding")
        _hasCompletedOnboarding.value = true
        // TODO: Persist to local storage
    }

    companion object {
        private val instance = UserPreferences()

        fun getInstance(): UserPreferences = instance
    }
}
