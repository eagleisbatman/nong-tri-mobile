package com.nongtri.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.platform.AudioRecorder
import com.nongtri.app.platform.LocalAudioRecorder
import com.nongtri.app.platform.LocalShareManager
import com.nongtri.app.platform.LocalTextToSpeechManager
import com.nongtri.app.platform.LocalVoiceMessagePlayer
import com.nongtri.app.platform.ShareManager
import com.nongtri.app.platform.TextToSpeechManager
import com.nongtri.app.platform.VoiceMessagePlayer
import com.nongtri.app.ui.viewmodel.LocationViewModel
import com.nongtri.app.ui.viewmodel.VoicePermissionViewModel

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var voiceMessagePlayer: VoiceMessagePlayer

    // Location permission launcher
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        val granted = fineLocationGranted || coarseLocationGranted

        // Notify LocationViewModel about permission result
        LocationViewModel.permissionResultCallback?.invoke(granted)

        if (granted) {
            println("Location permission granted")
        } else {
            println("Location permission denied")
        }
    }

    // Audio recording permission launcher
    private val recordAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Notify VoicePermissionViewModel about permission result
        VoicePermissionViewModel.permissionResultCallback?.invoke(granted)

        if (granted) {
            println("RECORD_AUDIO permission granted")
        } else {
            println("RECORD_AUDIO permission denied")
        }
    }

    companion object {
        // Deprecated: Use VoicePermissionViewModel instead
        var audioPermissionResultCallback: ((Boolean) -> Unit)? = null
        var audioPermissionLauncher: (() -> Unit)? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize UserPreferences with device info provider
        UserPreferences.initialize(applicationContext)

        ttsManager = TextToSpeechManager(applicationContext)
        audioRecorder = AudioRecorder(applicationContext)
        voiceMessagePlayer = VoiceMessagePlayer(applicationContext)

        // Set up location permission launcher for LocationViewModel
        LocationViewModel.permissionLauncher = { permissions ->
            locationPermissionLauncher.launch(permissions)
        }

        // Set up voice permission launcher for VoicePermissionViewModel
        VoicePermissionViewModel.permissionLauncher = {
            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }

        // Deprecated: Keep for backward compatibility
        audioPermissionLauncher = {
            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }

        setContent {
            CompositionLocalProvider(
                LocalShareManager provides ShareManager(applicationContext),
                LocalTextToSpeechManager provides ttsManager,
                LocalAudioRecorder provides audioRecorder,
                LocalVoiceMessagePlayer provides voiceMessagePlayer
            ) {
                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        audioRecorder.shutdown()
        voiceMessagePlayer.shutdown()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
