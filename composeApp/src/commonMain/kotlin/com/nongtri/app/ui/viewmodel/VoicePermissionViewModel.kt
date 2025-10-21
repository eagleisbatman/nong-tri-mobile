package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Permission state for audio recording
 */
data class VoicePermissionState(
    val hasPermission: Boolean = false,
    val shouldShowSettings: Boolean = false,
    val permissionRequested: Boolean = false,
    val error: String? = null
)

/**
 * Manages voice recording permission (expect/actual pattern)
 * Similar to LocationViewModel permission flow
 */
expect class VoicePermissionViewModel() : ViewModel {
    val permissionState: StateFlow<VoicePermissionState>

    /**
     * Initialize the ViewModel with platform context
     */
    fun initialize(context: Any)

    /**
     * Request RECORD_AUDIO permission
     * - First 2 attempts: Shows system permission dialog
     * - After 2 denials: Shows "Open Settings" button
     */
    fun requestPermission()

    /**
     * Called when permission result is received from system
     */
    fun onPermissionResult(granted: Boolean)

    /**
     * Open app settings to manually enable permission
     */
    fun openSettings()

    /**
     * Check current permission state (e.g., when returning from settings)
     */
    fun checkPermissionState()
}
