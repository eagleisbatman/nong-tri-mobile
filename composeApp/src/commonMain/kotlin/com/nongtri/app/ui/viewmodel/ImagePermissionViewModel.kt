package com.nongtri.app.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * Permission state for image capture and gallery access
 */
data class ImagePermissionState(
    val hasCameraPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val shouldShowSettings: Boolean = false,
    val permissionRequested: Boolean = false,
    val error: String? = null
)

/**
 * Manages camera and storage permissions for plant diagnosis (expect/actual pattern)
 * Similar to VoicePermissionViewModel flow
 */
expect class ImagePermissionViewModel() : ViewModel {
    val permissionState: StateFlow<ImagePermissionState>

    /**
     * Initialize the ViewModel with platform context
     */
    fun initialize(context: Any)

    /**
     * Request CAMERA permission
     * - First 2 attempts: Shows system permission dialog
     * - After 2 denials: Shows "Open Settings" button
     */
    fun requestCameraPermission()

    /**
     * Request READ_MEDIA_IMAGES (Android 13+) or READ_EXTERNAL_STORAGE (Android 12-) permission
     * - First 2 attempts: Shows system permission dialog
     * - After 2 denials: Shows "Open Settings" button
     */
    fun requestStoragePermission()

    /**
     * Called when camera permission result is received from system
     */
    fun onCameraPermissionResult(granted: Boolean)

    /**
     * Called when storage permission result is received from system
     */
    fun onStoragePermissionResult(granted: Boolean)

    /**
     * Open app settings to manually enable permissions
     */
    fun openSettings()

    /**
     * Check current permission state (e.g., when returning from settings)
     */
    fun checkPermissionState()
}

/**
 * Remember and initialize ImagePermissionViewModel
 */
@Composable
expect fun rememberImagePermissionViewModel(): ImagePermissionViewModel
