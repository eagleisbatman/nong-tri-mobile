package com.nongtri.app

import android.content.Intent
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
import com.nongtri.app.platform.FCMService
import com.nongtri.app.platform.HapticFeedback
import com.nongtri.app.platform.LocalAudioRecorder
import com.nongtri.app.platform.LocalHapticFeedback
import com.nongtri.app.platform.LocalShareManager
import com.nongtri.app.platform.LocalTextToSpeechManager
import com.nongtri.app.platform.LocalVoiceMessagePlayer
import com.nongtri.app.platform.ShareManager
import com.nongtri.app.platform.TextToSpeechManager
import com.nongtri.app.platform.VoiceMessagePlayer
import com.nongtri.app.ui.viewmodel.LocationViewModel
import com.nongtri.app.ui.viewmodel.VoicePermissionViewModel
import com.nongtri.app.ui.viewmodel.ImagePermissionViewModel
import com.nongtri.app.platform.ImagePicker

class MainActivity : ComponentActivity() {
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var voiceMessagePlayer: VoiceMessagePlayer
    private lateinit var imagePicker: ImagePicker
    private lateinit var networkMonitor: com.nongtri.app.platform.NetworkMonitor // BATCH 3
    private lateinit var hapticFeedback: HapticFeedback

    // Session tracking
    private var sessionStartTime = 0L
    private var sessionMessagesCount = 0
    private var sessionVoiceMessagesCount = 0
    private var sessionImagesCount = 0

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

    // Camera permission launcher
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Notify ImagePermissionViewModel about permission result
        ImagePermissionViewModel.cameraPermissionResultCallback?.invoke(granted)

        if (granted) {
            println("CAMERA permission granted")
        } else {
            println("CAMERA permission denied")
        }
    }

    // Storage permission launcher (READ_MEDIA_IMAGES or READ_EXTERNAL_STORAGE)
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        // Notify ImagePermissionViewModel about permission result
        ImagePermissionViewModel.storagePermissionResultCallback?.invoke(granted)

        if (granted) {
            println("Storage permission granted")
        } else {
            println("Storage permission denied")
        }
    }

    // Camera capture launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        println("[MainActivity] Camera capture result: $success")
        imagePicker.onCameraResult(success)
    }

    // Gallery image picker launcher
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        println("[MainActivity] Gallery selection result: $uri")
        imagePicker.onGalleryResult(uri)
    }

    // BATCH 3: Push notification permission launcher (Android 13+)
    private var pushPermissionRequestTime = 0L
    private var pushDenialCount = 0
    private val pushNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val timeToGrantMs = if (pushPermissionRequestTime > 0) {
            System.currentTimeMillis() - pushPermissionRequestTime
        } else 0L

        if (granted) {
            com.nongtri.app.analytics.Events.logPushNotificationPermissionGranted(timeToGrantMs)
            println("[MainActivity] POST_NOTIFICATIONS permission granted")
        } else {
            pushDenialCount++
            val canRequestAgain = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)
            } else false

            com.nongtri.app.analytics.Events.logPushNotificationPermissionDenied(
                denialCount = pushDenialCount,
                canRequestAgain = canRequestAgain
            )
            println("[MainActivity] POST_NOTIFICATIONS permission denied (count: $pushDenialCount)")
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

        // Initialize Firebase Analytics
        com.nongtri.app.analytics.AnalyticsService.initialize()

        // Set user ID for analytics
        val deviceId = UserPreferences.getInstance().getDeviceId()
        com.nongtri.app.analytics.AnalyticsService.setUserId(deviceId)

        // Update user properties
        com.nongtri.app.analytics.AnalyticsService.updateUserProperties()

        // Track session start
        sessionStartTime = System.currentTimeMillis()
        UserPreferences.getInstance().incrementSessionCount()
        com.nongtri.app.analytics.Events.logSessionStarted(
            entryPoint = if (intent?.action == android.content.Intent.ACTION_VIEW) "deeplink"
            else if (intent?.hasExtra("jobId") == true) "notification"
            else "launcher"
        )

        // Track onboarding funnel step 1 (for first-time users only)
        if (UserPreferences.getInstance().sessionCount.value == 1) {
            // ROUND 10: Track app first launch
            com.nongtri.app.analytics.Events.logAppFirstLaunch(
                installSource = packageManager.getInstallerPackageName(packageName) ?: "unknown"
            )
            com.nongtri.app.analytics.Funnels.onboardingFunnel.step1_AppLaunched()
        }

        ttsManager = TextToSpeechManager(applicationContext)
        audioRecorder = AudioRecorder(applicationContext)
        voiceMessagePlayer = VoiceMessagePlayer(applicationContext)
        imagePicker = ImagePicker(applicationContext)
        imagePicker.initialize(this)
        hapticFeedback = HapticFeedback(applicationContext)

        // BATCH 3: Initialize NetworkMonitor for reconnection tracking
        networkMonitor = com.nongtri.app.platform.NetworkMonitor.getInstance(applicationContext)

        // Initialize FCM for push notifications
        val fcmService = FCMService(applicationContext)
        fcmService.initialize()

        // BATCH 3: Request push notification permission (Android 13+)
        requestPushNotificationPermission()

        // Handle notification tap if app was opened from notification
        handleNotificationIntent(intent)

        // Set up location permission launcher for LocationViewModel
        LocationViewModel.permissionLauncher = { permissions ->
            locationPermissionLauncher.launch(permissions)
        }

        // Set up voice permission launcher for VoicePermissionViewModel
        VoicePermissionViewModel.permissionLauncher = {
            recordAudioPermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }

        // Set up image permission launchers for ImagePermissionViewModel
        ImagePermissionViewModel.cameraPermissionLauncher = {
            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }

        ImagePermissionViewModel.storagePermissionLauncher = {
            val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                android.Manifest.permission.READ_MEDIA_IMAGES
            } else {
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            }
            storagePermissionLauncher.launch(permission)
        }

        // Set up image picker launchers
        ImagePicker.cameraLauncher = { uri ->
            takePictureLauncher.launch(uri)
        }

        ImagePicker.galleryLauncher = {
            pickImageLauncher.launch(
                androidx.activity.result.PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
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
                LocalVoiceMessagePlayer provides voiceMessagePlayer,
                LocalHapticFeedback provides hapticFeedback
            ) {
                App()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Handle notification tap when app is already running
        handleNotificationIntent(intent)
    }

    /**
     * Handle notification intent data (jobId for diagnosis completion)
     */
    private fun handleNotificationIntent(intent: Intent?) {
        intent?.extras?.let { extras ->
            val jobId = extras.getString("jobId")
            val openDiagnosis = extras.getBoolean("openDiagnosis", false)

            if (jobId != null && openDiagnosis) {
                println("[MainActivity] Notification tap detected - jobId: $jobId")
                // Save to UserPreferences (persists across process death)
                UserPreferences.getInstance().setPendingDiagnosisJobId(jobId)
            }
        }
    }

    /**
     * BATCH 3: Request push notification permission for Android 13+
     */
    private fun requestPushNotificationPermission() {
        // Only request on Android 13+ (API 33+) where POST_NOTIFICATIONS permission is required
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Check if permission is already granted
            val hasPermission = checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                // Track permission request
                pushPermissionRequestTime = System.currentTimeMillis()
                com.nongtri.app.analytics.Events.logPushNotificationPermissionRequested("app_launch")

                // Request permission
                pushNotificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onStop() {
        super.onStop()

        // Track session end
        val sessionDuration = System.currentTimeMillis() - sessionStartTime

        // Save session data for next session
        val prefs = UserPreferences.getInstance()

        // Force Vietnam timezone for consistent session dates
        val vietnamTimeZone = java.util.TimeZone.getTimeZone("Asia/Ho_Chi_Minh")
        val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        dateFormat.timeZone = vietnamTimeZone

        prefs.setLastSessionDate(dateFormat.format(java.util.Date()))
        prefs.setLastSessionDuration(sessionDuration)

        // Log session ended event
        com.nongtri.app.analytics.Events.logSessionEnded(
            sessionDurationMs = sessionDuration,
            messagesSent = sessionMessagesCount,
            voiceMessagesSent = sessionVoiceMessagesCount,
            imagesSent = sessionImagesCount
        )

        println("[MainActivity] Session ended - duration: ${sessionDuration}ms, messages: $sessionMessagesCount")
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        audioRecorder.shutdown()
        voiceMessagePlayer.shutdown()

        // BATCH 3: Stop NetworkMonitor
        networkMonitor.stopMonitoring()

        // Clean up ImagePicker callbacks to prevent memory leaks
        ImagePicker.cameraResultCallback = null
        ImagePicker.galleryResultCallback = null
        ImagePicker.pendingCameraUri = null
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
