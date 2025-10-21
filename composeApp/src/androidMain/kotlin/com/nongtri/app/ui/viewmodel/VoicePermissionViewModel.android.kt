package com.nongtri.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual class VoicePermissionViewModel actual constructor() : ViewModel() {
    private val _permissionState = MutableStateFlow(VoicePermissionState())
    actual val permissionState: StateFlow<VoicePermissionState> = _permissionState.asStateFlow()

    private lateinit var context: Context

    companion object {
        var permissionLauncher: (() -> Unit)? = null
        var permissionResultCallback: ((Boolean) -> Unit)? = null
    }

    actual fun initialize(context: Any) {
        this.context = context as Context

        // Set up permission result callback
        permissionResultCallback = { granted -> onPermissionResult(granted) }

        // Check initial permission state
        checkInitialPermissionState()
    }

    /**
     * Check permission state when app starts to show correct state immediately
     */
    private fun checkInitialPermissionState() {
        if (hasRecordPermission()) {
            _permissionState.update { it.copy(hasPermission = true) }
            return
        }

        val activity = context as? ComponentActivity ?: return

        // Check SharedPreferences to see if we've ever requested permission before
        val prefs = context.getSharedPreferences("voice_prefs", Context.MODE_PRIVATE)
        val hasEverRequested = prefs.getBoolean("permission_requested", false)

        if (!hasEverRequested) {
            // First time user, show normal state
            return
        }

        // User has requested before, check if they can still see the permission dialog
        val shouldShowRationale = activity.shouldShowRequestPermissionRationale(
            Manifest.permission.RECORD_AUDIO
        )

        println("[VoicePermission] Initial check: hasPermission=false, shouldShowRationale=$shouldShowRationale, hasEverRequested=$hasEverRequested")

        // If shouldShowRationale=false AND hasEverRequested=true,
        // user has denied twice and exhausted the limit
        if (!shouldShowRationale) {
            println("[VoicePermission] User exhausted permission requests - showing Settings button")
            _permissionState.update {
                it.copy(
                    shouldShowSettings = true,
                    permissionRequested = true,
                    error = "Microphone permission denied. Please enable it in Settings to use voice messages."
                )
            }
        }
    }

    /**
     * Check permission state - call when returning from settings
     */
    actual fun checkPermissionState() {
        println("[VoicePermission] Checking permission state...")

        // If we were showing settings button but permission is now granted, reset state
        if (_permissionState.value.shouldShowSettings && hasRecordPermission()) {
            println("[VoicePermission] Permission granted in settings! Resetting state")
            _permissionState.update {
                it.copy(
                    hasPermission = true,
                    shouldShowSettings = false,
                    error = null
                )
            }
        }
    }

    actual fun requestPermission() {
        if (hasRecordPermission()) {
            // Permission already granted
            _permissionState.update { it.copy(hasPermission = true) }
            return
        }

        // Check if we should show settings button
        if (_permissionState.value.shouldShowSettings) {
            // User has exhausted permission requests, open settings
            println("[VoicePermission] Opening settings - user exhausted permission requests")
            openSettings()
        } else {
            // Save to SharedPreferences that we've requested permission
            val prefs = context.getSharedPreferences("voice_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("permission_requested", true).apply()

            _permissionState.update { it.copy(permissionRequested = true) }

            println("[VoicePermission] Requesting RECORD_AUDIO permission...")
            permissionLauncher?.invoke()
        }
    }

    actual fun onPermissionResult(granted: Boolean) {
        println("[VoicePermission] Permission result: granted=$granted")

        if (granted) {
            // Permission granted, reset state
            _permissionState.update {
                it.copy(
                    hasPermission = true,
                    shouldShowSettings = false,
                    permissionRequested = true,
                    error = null
                )
            }
        } else {
            // Permission denied - check if user has exhausted requests
            val shouldShowRationale = shouldShowRequestPermissionRationale()

            println("[VoicePermission] Permission denied: shouldShowRationale=$shouldShowRationale")

            // If shouldShowRationale=false, user has exhausted permission requests
            // Show "Open Settings" button
            if (!shouldShowRationale) {
                println("[VoicePermission] User exhausted permission requests - showing Settings button")
                _permissionState.update {
                    it.copy(
                        hasPermission = false,
                        shouldShowSettings = true,
                        permissionRequested = true,
                        error = "Microphone permission denied. Please enable it in Settings to use voice messages."
                    )
                }
            } else {
                // User can still request permission again
                _permissionState.update {
                    it.copy(
                        hasPermission = false,
                        permissionRequested = true,
                        error = "Microphone permission is needed to record voice messages"
                    )
                }
            }
        }
    }

    private fun shouldShowRequestPermissionRationale(): Boolean {
        val activity = context as? ComponentActivity ?: return false
        return activity.shouldShowRequestPermissionRationale(
            Manifest.permission.RECORD_AUDIO
        )
    }

    actual fun openSettings() {
        try {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.fromParts("package", context.packageName, null)
            )
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            _permissionState.update {
                it.copy(error = "Could not open settings: ${e.message}")
            }
        }
    }

    private fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Remember and initialize VoicePermissionViewModel
 */
@Composable
actual fun rememberVoicePermissionViewModel(): VoicePermissionViewModel {
    val context = LocalContext.current
    val viewModel = remember { VoicePermissionViewModel() }

    // Initialize on first composition
    remember {
        viewModel.initialize(context)
        viewModel
    }

    return viewModel
}
