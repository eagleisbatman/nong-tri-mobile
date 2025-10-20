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
import com.nongtri.app.platform.LocalShareManager
import com.nongtri.app.platform.LocalTextToSpeechManager
import com.nongtri.app.platform.ShareManager
import com.nongtri.app.platform.TextToSpeechManager
import com.nongtri.app.ui.viewmodel.LocationViewModel

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TextToSpeechManager

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize UserPreferences with device info provider
        UserPreferences.initialize(applicationContext)

        ttsManager = TextToSpeechManager(applicationContext)

        // Set up location permission launcher for LocationViewModel
        LocationViewModel.permissionLauncher = { permissions ->
            locationPermissionLauncher.launch(permissions)
        }

        setContent {
            CompositionLocalProvider(
                LocalShareManager provides ShareManager(applicationContext),
                LocalTextToSpeechManager provides ttsManager
            ) {
                App()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
