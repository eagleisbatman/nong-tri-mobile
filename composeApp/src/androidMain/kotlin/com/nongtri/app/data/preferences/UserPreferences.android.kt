package com.nongtri.app.data.preferences

import android.content.Context
import com.nongtri.app.l10n.Language
import com.nongtri.app.data.model.DeviceInfo
import com.nongtri.app.data.provider.DeviceInfoProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class UserPreferences private constructor(context: Context) {
    private val deviceInfoProvider = DeviceInfoProvider(context)

    private val _language = MutableStateFlow(Language.ENGLISH)
    actual val language: StateFlow<Language> = _language.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    actual val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(false)
    actual val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    actual fun setLanguage(language: Language) {
        println("UserPreferences: Setting language to $language")
        _language.value = language
        // TODO: Persist to local storage
    }

    actual fun setThemeMode(mode: ThemeMode) {
        println("UserPreferences: Setting theme mode to $mode")
        _themeMode.value = mode
        // TODO: Persist to local storage
    }

    actual fun completeOnboarding() {
        println("UserPreferences: Completing onboarding")
        _hasCompletedOnboarding.value = true
        // TODO: Persist to local storage
    }

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
