package com.nongtri.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

actual class ImagePermissionViewModel actual constructor() : ViewModel() {
    private val _permissionState = MutableStateFlow(ImagePermissionState())
    actual val permissionState: StateFlow<ImagePermissionState> = _permissionState.asStateFlow()

    private lateinit var context: Context
    private val userPreferences = UserPreferences.getInstance()
    private val strings get() = LocalizationProvider.getStrings(userPreferences.language.value)

    companion object {
        var cameraPermissionLauncher: (() -> Unit)? = null
        var storagePermissionLauncher: (() -> Unit)? = null
        var cameraPermissionResultCallback: ((Boolean) -> Unit)? = null
        var storagePermissionResultCallback: ((Boolean) -> Unit)? = null
    }

    actual fun initialize(context: Any) {
        this.context = context as Context

        // Set up permission result callbacks
        cameraPermissionResultCallback = { granted -> onCameraPermissionResult(granted) }
        storagePermissionResultCallback = { granted -> onStoragePermissionResult(granted) }

        // Check initial permission state
        checkInitialPermissionState()
    }

    /**
     * Check permission state when app starts to show correct state immediately
     */
    private fun checkInitialPermissionState() {
        val hasCameraPermission = hasCameraPermission()
        val hasStoragePermission = hasStoragePermission()

        if (hasCameraPermission && hasStoragePermission) {
            _permissionState.update {
                it.copy(
                    hasCameraPermission = true,
                    hasStoragePermission = true
                )
            }
            return
        }

        val activity = context as? ComponentActivity ?: return

        // Check SharedPreferences to see if we've ever requested permissions before
        val prefs = context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE)
        val hasEverRequestedCamera = prefs.getBoolean("camera_permission_requested", false)
        val hasEverRequestedStorage = prefs.getBoolean("storage_permission_requested", false)

        var shouldShowSettings = false

        // Check camera permission rationale
        if (!hasCameraPermission && hasEverRequestedCamera) {
            val shouldShowRationale = activity.shouldShowRequestPermissionRationale(
                Manifest.permission.CAMERA
            )
            if (!shouldShowRationale) {
                shouldShowSettings = true
            }
        }

        // Check storage permission rationale
        if (!hasStoragePermission && hasEverRequestedStorage) {
            val storagePermission = getStoragePermission()
            val shouldShowRationale = activity.shouldShowRequestPermissionRationale(storagePermission)
            if (!shouldShowRationale) {
                shouldShowSettings = true
            }
        }

        if (shouldShowSettings) {
            println("[ImagePermission] User exhausted permission requests - showing Settings button")
            _permissionState.update {
                it.copy(
                    hasCameraPermission = hasCameraPermission,
                    hasStoragePermission = hasStoragePermission,
                    shouldShowSettings = true,
                    permissionRequested = true,
                    error = strings.permissionCameraStorageDeniedSettings
                )
            }
        } else {
            _permissionState.update {
                it.copy(
                    hasCameraPermission = hasCameraPermission,
                    hasStoragePermission = hasStoragePermission
                )
            }
        }
    }

    /**
     * Check permission state - call when returning from settings
     */
    actual fun checkPermissionState() {
        println("[ImagePermission] Checking permission state...")

        val hasCameraPermission = hasCameraPermission()
        val hasStoragePermission = hasStoragePermission()

        // If we were showing settings button but permissions are now granted, reset state
        if (_permissionState.value.shouldShowSettings && hasCameraPermission && hasStoragePermission) {
            println("[ImagePermission] Permissions granted in settings! Resetting state")
            _permissionState.update {
                it.copy(
                    hasCameraPermission = true,
                    hasStoragePermission = true,
                    shouldShowSettings = false,
                    error = null
                )
            }
        } else {
            _permissionState.update {
                it.copy(
                    hasCameraPermission = hasCameraPermission,
                    hasStoragePermission = hasStoragePermission
                )
            }
        }
    }

    actual fun requestCameraPermission() {
        if (hasCameraPermission()) {
            _permissionState.update { it.copy(hasCameraPermission = true) }
            return
        }

        if (_permissionState.value.shouldShowSettings) {
            println("[ImagePermission] Opening settings - user exhausted permission requests")
            openSettings()
        } else {
            val prefs = context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("camera_permission_requested", true).apply()

            _permissionState.update { it.copy(permissionRequested = true) }

            println("[ImagePermission] Requesting CAMERA permission...")
            cameraPermissionLauncher?.invoke()
        }
    }

    actual fun requestStoragePermission() {
        if (hasStoragePermission()) {
            _permissionState.update { it.copy(hasStoragePermission = true) }
            return
        }

        if (_permissionState.value.shouldShowSettings) {
            println("[ImagePermission] Opening settings - user exhausted permission requests")
            openSettings()
        } else {
            val prefs = context.getSharedPreferences("image_prefs", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("storage_permission_requested", true).apply()

            _permissionState.update { it.copy(permissionRequested = true) }

            println("[ImagePermission] Requesting ${getStoragePermission()} permission...")
            storagePermissionLauncher?.invoke()
        }
    }

    actual fun onCameraPermissionResult(granted: Boolean) {
        println("[ImagePermission] Camera permission result: granted=$granted")

        if (granted) {
            _permissionState.update {
                it.copy(
                    hasCameraPermission = true,
                    permissionRequested = true,
                    error = null
                )
            }

            // Track image funnel step 2 if both permissions are now granted
            if (hasStoragePermission()) {
                com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step2_PermissionGranted()
            }
        } else {
            val shouldShowRationale = shouldShowCameraRationale()
            println("[ImagePermission] Camera denied: shouldShowRationale=$shouldShowRationale")

            if (!shouldShowRationale) {
                println("[ImagePermission] User exhausted camera permission requests")
                _permissionState.update {
                    it.copy(
                        hasCameraPermission = false,
                        shouldShowSettings = true,
                        permissionRequested = true,
                        error = strings.permissionCameraDeniedSettings
                    )
                }
            } else {
                _permissionState.update {
                    it.copy(
                        hasCameraPermission = false,
                        permissionRequested = true,
                        error = strings.permissionCameraRationale
                    )
                }
            }
        }
    }

    actual fun onStoragePermissionResult(granted: Boolean) {
        println("[ImagePermission] Storage permission result: granted=$granted")

        if (granted) {
            _permissionState.update {
                it.copy(
                    hasStoragePermission = true,
                    permissionRequested = true,
                    error = null
                )
            }

            // Track image funnel step 2 if both permissions are now granted
            if (hasCameraPermission()) {
                com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step2_PermissionGranted()
            }
        } else {
            val shouldShowRationale = shouldShowStorageRationale()
            println("[ImagePermission] Storage denied: shouldShowRationale=$shouldShowRationale")

            if (!shouldShowRationale) {
                println("[ImagePermission] User exhausted storage permission requests")
                _permissionState.update {
                    it.copy(
                        hasStoragePermission = false,
                        shouldShowSettings = true,
                        permissionRequested = true,
                        error = strings.permissionStorageDeniedSettings
                    )
                }
            } else {
                _permissionState.update {
                    it.copy(
                        hasStoragePermission = false,
                        permissionRequested = true,
                        error = strings.permissionStorageRationale
                    )
                }
            }
        }
    }

    private fun shouldShowCameraRationale(): Boolean {
        val activity = context as? ComponentActivity ?: return false
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
    }

    private fun shouldShowStorageRationale(): Boolean {
        val activity = context as? ComponentActivity ?: return false
        return activity.shouldShowRequestPermissionRationale(getStoragePermission())
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

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun hasStoragePermission(): Boolean {
        val permission = getStoragePermission()
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the appropriate storage permission based on Android version
     * Android 13+ (API 33+): READ_MEDIA_IMAGES
     * Android 12 and below: READ_EXTERNAL_STORAGE
     */
    private fun getStoragePermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }
}

/**
 * Remember and initialize ImagePermissionViewModel
 */
@Composable
actual fun rememberImagePermissionViewModel(): ImagePermissionViewModel {
    val context = LocalContext.current
    val viewModel = remember { ImagePermissionViewModel() }

    // Initialize on first composition
    remember {
        viewModel.initialize(context)
        viewModel
    }

    return viewModel
}
