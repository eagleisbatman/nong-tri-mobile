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
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.l10n.LocalizationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

actual class VoicePermissionViewModel actual constructor() : ViewModel() {
    private val _permissionState = MutableStateFlow(VoicePermissionState())
    actual val permissionState: StateFlow<VoicePermissionState> = _permissionState.asStateFlow()

    private lateinit var context: Context
    private val userPreferences = UserPreferences.getInstance()
    private val strings get() = LocalizationProvider.getStrings(userPreferences.language.value)

    // Permission tracking for analytics
    private var permissionRequestTime = 0L
    private var denialCount = 0

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
                    error = strings.permissionMicrophoneDeniedSettings
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

            // Track permission request for analytics
            permissionRequestTime = System.currentTimeMillis()
            com.nongtri.app.analytics.Events.logVoicePermissionRequested("voice_button")

            println("[VoicePermission] Requesting RECORD_AUDIO permission...")
            permissionLauncher?.invoke()
        }
    }

    actual fun onPermissionResult(granted: Boolean) {
        println("[VoicePermission] Permission result: granted=$granted")

        val timeToGrantMs = if (permissionRequestTime > 0) System.currentTimeMillis() - permissionRequestTime else 0L

        if (granted) {
            // Track voice funnel step 2: Permission granted
            com.nongtri.app.analytics.Funnels.voiceAdoptionFunnel.step2_PermissionGranted()

            // Track detailed permission granted event
            val isFirstGrant = !userPreferences.hasUsedVoice.value
            com.nongtri.app.analytics.Events.logVoicePermissionGranted(timeToGrantMs, isFirstGrant)

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
            // Track denial
            denialCount++

            // Permission denied - check if user has exhausted requests
            val shouldShowRationale = shouldShowRequestPermissionRationale()

            // Track permission denial event
            com.nongtri.app.analytics.Events.logVoicePermissionDenied(
                denialCount = denialCount,
                canRequestAgain = shouldShowRationale
            )

            // BATCH 1: Track permission friction point (2+ denials)
            if (denialCount >= 2) {
                com.nongtri.app.analytics.Events.logPermissionFrictionPoint(
                    permissionType = "voice",
                    featureBlocked = "voice_recording"
                )
            }

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
                        error = strings.permissionMicrophoneDeniedSettings
                    )
                }
            } else {
                // User can still request permission again
                _permissionState.update {
                    it.copy(
                        hasPermission = false,
                        permissionRequested = true,
                        error = strings.permissionMicrophoneRationale
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
                it.copy(error = "${strings.errorCouldNotOpenSettings}: ${e.message}")
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
