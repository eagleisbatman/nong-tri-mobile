package com.nongtri.app.platform

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Handles runtime permissions for audio recording
 * Required for Android 6.0+ (API 23+)
 */
class PermissionHandler(private val activity: ComponentActivity) {
    private val _recordAudioPermissionGranted = MutableStateFlow(false)
    val recordAudioPermissionGranted: StateFlow<Boolean> = _recordAudioPermissionGranted.asStateFlow()

    private var onPermissionResult: ((Boolean) -> Unit)? = null

    private val requestPermissionLauncher = activity.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        _recordAudioPermissionGranted.value = isGranted
        onPermissionResult?.invoke(isGranted)
        onPermissionResult = null
    }

    init {
        // Check initial permission state
        checkRecordAudioPermission()
    }

    /**
     * Check if RECORD_AUDIO permission is granted
     */
    fun checkRecordAudioPermission(): Boolean {
        val granted = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        _recordAudioPermissionGranted.value = granted
        return granted
    }

    /**
     * Request RECORD_AUDIO permission
     * @param onResult Callback with permission result (true = granted, false = denied)
     */
    fun requestRecordAudioPermission(onResult: (Boolean) -> Unit) {
        // Check if already granted
        if (checkRecordAudioPermission()) {
            onResult(true)
            return
        }

        // Request permission
        onPermissionResult = onResult
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Check if we should show permission rationale
     * (user previously denied, but didn't select "Don't ask again")
     */
    fun shouldShowRecordAudioRationale(): Boolean {
        return activity.shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)
    }
}
