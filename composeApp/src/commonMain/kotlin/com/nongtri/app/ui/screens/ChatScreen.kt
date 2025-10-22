package com.nongtri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider
import com.nongtri.app.ui.components.*
import com.nongtri.app.ui.viewmodel.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    onClearHistory: () -> Unit,
    onViewConversations: () -> Unit,
    onThemeModeChange: (com.nongtri.app.data.preferences.ThemeMode) -> Unit,
    currentThemeMode: com.nongtri.app.data.preferences.ThemeMode,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalizationProvider.getStrings(language)
    val isLightTheme = !isSystemInDarkTheme()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var showLocationBottomSheet by remember { mutableStateOf(false) }
    val locationViewModel = rememberLocationViewModel()
    val locationState by locationViewModel.locationState.collectAsState()

    // Image picker
    val imagePicker = com.nongtri.app.platform.rememberImagePicker()

    // Voice permission state
    var showVoicePermissionBottomSheet by remember { mutableStateOf(false) }
    val voicePermissionViewModel = rememberVoicePermissionViewModel()
    val voicePermissionState by voicePermissionViewModel.permissionState.collectAsState()

    // Voice recording state
    val audioRecorder = com.nongtri.app.platform.LocalAudioRecorder.current
    val voiceRecordingViewModel = remember { VoiceRecordingViewModel(audioRecorder) }
    val voiceRecordingState by voiceRecordingViewModel.state.collectAsState()
    val voiceAmplitude by voiceRecordingViewModel.amplitude.collectAsState()
    val isTranscribing by voiceRecordingViewModel.isTranscribing.collectAsState()
    val transcriptionText by voiceRecordingViewModel.transcriptionText.collectAsState()

    // Image permission state
    var showImagePermissionBottomSheet by remember { mutableStateOf(false) }
    val imagePermissionViewModel = rememberImagePermissionViewModel()
    val imagePermissionState by imagePermissionViewModel.permissionState.collectAsState()

    // Image selection state (critical state survives rotation to handle camera/gallery results)
    var showImageSourceSelector by remember { mutableStateOf(false) }
    var showImagePreviewDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var selectedImageUri by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }  // Display URI
    // Note: selectedImageBase64 cannot use rememberSaveable (too large for savedInstanceState)
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }  // Base64 data for upload
    var isImageProcessing by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }  // Prevent multiple simultaneous operations
    var showFullscreenImage by remember { mutableStateOf<String?>(null) }  // Image URL to show fullscreen

    // Snackbar for error messages (only for non-permission errors)
    val snackbarHostState = remember { SnackbarHostState() }
    var currentOptimisticMessageId by remember { mutableStateOf<String?>(null) }

    // Handle voice recording errors - remove optimistic message
    // Permission errors are handled via bottom sheet, not Snackbar
    LaunchedEffect(voiceRecordingState) {
        if (voiceRecordingState is VoiceRecordingState.Error) {
            val errorMessage = (voiceRecordingState as VoiceRecordingState.Error).message

            // Remove the optimistic message if it exists
            currentOptimisticMessageId?.let { messageId ->
                viewModel.removeVoiceMessage(messageId)
                currentOptimisticMessageId = null
            }

            // Only show Snackbar for NON-permission errors
            if (!errorMessage.contains("permission", ignoreCase = true)) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Auto-check image permission state while bottom sheet is visible
    LaunchedEffect(showImagePermissionBottomSheet) {
        if (showImagePermissionBottomSheet) {
            while (showImagePermissionBottomSheet) {
                imagePermissionViewModel.checkPermissionState()
                kotlinx.coroutines.delay(500)  // Check every 500ms
            }
        }
    }

    // Auto-dismiss image permission bottom sheet when permissions are granted
    LaunchedEffect(imagePermissionState.hasCameraPermission, imagePermissionState.hasStoragePermission) {
        if (imagePermissionState.hasCameraPermission && imagePermissionState.hasStoragePermission && showImagePermissionBottomSheet) {
            showImagePermissionBottomSheet = false
            // Automatically open image source selector
            showImageSourceSelector = true
        }
    }

    // Check if user has scrolled up
    val isScrolledToBottom by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem?.index == listState.layoutInfo.totalItemsCount - 1
        }
    }

    // Track the content of the last message for auto-scrolling during streaming
    val lastMessageContent = uiState.messages.lastOrNull()?.content ?: ""

    // Auto-scroll to bottom when new messages arrive, loading starts, or message content changes (streaming)
    LaunchedEffect(uiState.messages.size, uiState.isLoading, lastMessageContent) {
        if (uiState.messages.isNotEmpty() || uiState.isLoading) {
            coroutineScope.launch {
                listState.animateScrollToItem(
                    index = maxOf(0, listState.layoutInfo.totalItemsCount - 1)
                )
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.CHAT_SCREEN),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.chatTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                actions = {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag(TestTags.PROFILE_BUTTON)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Settings"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // 1. View Conversations (top priority)
                            DropdownMenuItem(
                                text = { Text("Conversations") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                },
                                onClick = {
                                    onViewConversations()
                                    showMenu = false
                                }
                            )

                            // 2. New Chat
                            DropdownMenuItem(
                                text = { Text("New Chat") },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                },
                                onClick = {
                                    viewModel.createNewThread()
                                    showMenu = false
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Share Location
                            DropdownMenuItem(
                                text = { Text("Share Location") },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                },
                                onClick = {
                                    showLocationBottomSheet = true
                                    showMenu = false
                                }
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Language selection
                            Text(
                                text = "Language",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            Language.entries.forEach { lang ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = lang.flag,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                                Text(lang.displayName)
                                            }
                                            if (lang == language) {
                                                Text(
                                                    text = "✓",
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onLanguageChange(lang)
                                        showMenu = false
                                    }
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 5. Theme (at bottom)
                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )

                            com.nongtri.app.data.preferences.ThemeMode.entries.forEach { mode ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                when (mode) {
                                                    com.nongtri.app.data.preferences.ThemeMode.LIGHT -> "Light"
                                                    com.nongtri.app.data.preferences.ThemeMode.DARK -> "Dark"
                                                    com.nongtri.app.data.preferences.ThemeMode.SYSTEM -> "System Default"
                                                }
                                            )
                                            if (mode == currentThemeMode) {
                                                Text(
                                                    text = "✓",
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onThemeModeChange(mode)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.testTag(TestTags.CHAT_APP_BAR)
            )
        },
        bottomBar = {
            // Track voice recording UI state locally
            var voiceRecordingUIState by remember { mutableStateOf<VoiceRecordingUIState>(VoiceRecordingUIState.Idle) }
            var recordedAudioFile by remember { mutableStateOf<java.io.File?>(null) }

            // Update voice recording UI state based on VoiceRecordingViewModel state
            LaunchedEffect(voiceRecordingState) {
                when (voiceRecordingState) {
                    is VoiceRecordingState.Recording -> {
                        voiceRecordingUIState = VoiceRecordingUIState.Recording(
                            durationMs = (voiceRecordingState as VoiceRecordingState.Recording).durationMs
                        )
                    }
                    is VoiceRecordingState.Cancelled -> {
                        // Only go back to Idle if user cancelled
                        voiceRecordingUIState = VoiceRecordingUIState.Idle
                    }
                    // DON'T reset to Idle on VoiceRecordingState.Idle - we might be in Preview!
                    // The UI state is managed independently by onStopRecording/onAccept/onReject
                    else -> {}
                }
            }

            // Populate input box when transcription completes successfully
            LaunchedEffect(isTranscribing, transcriptionText) {
                // When transcription completes and we have text, populate the input box
                val text = transcriptionText
                if (!isTranscribing && text != null && text.isNotEmpty() && recordedAudioFile != null) {
                    println("[ChatScreen] Transcription complete: $text")
                    viewModel.updateMessage(text)
                    // Clean up audio file
                    recordedAudioFile?.delete()
                    recordedAudioFile = null
                }
            }

            // Clean up audio file when transcription fails
            LaunchedEffect(voiceRecordingState) {
                if (voiceRecordingState is VoiceRecordingState.Error && recordedAudioFile != null) {
                    println("[ChatScreen] Transcription failed, cleaning up audio file")
                    recordedAudioFile?.delete()
                    recordedAudioFile = null
                }
            }

            Column {
                when (voiceRecordingUIState) {
                    is VoiceRecordingUIState.Idle -> {
                        // Normal input bar
                        WhatsAppStyleInputBar(
                            value = uiState.currentMessage,
                            onValueChange = viewModel::updateMessage,
                            onSend = {
                                viewModel.sendMessage(uiState.currentMessage)
                            },
                            onImageClick = {
                                // Prevent multiple simultaneous operations
                                if (isImageProcessing) {
                                    println("[ChatScreen] Image already being processed, ignoring click")
                                    return@WhatsAppStyleInputBar
                                }

                                // Check if both permissions are granted
                                if (imagePermissionState.hasCameraPermission && imagePermissionState.hasStoragePermission) {
                                    // Permissions granted, show image source selector
                                    showImageSourceSelector = true
                                } else {
                                    // Request permissions
                                    showImagePermissionBottomSheet = true
                                }
                            },
                            onVoiceClick = {
                                // Single tap - check permission and start recording
                                if (voicePermissionState.hasPermission) {
                                    voiceRecordingViewModel.startRecording()
                                } else {
                                    showVoicePermissionBottomSheet = true
                                }
                            },
                            onVoiceLongPress = {}, // Not used anymore
                            onVoiceRelease = {},   // Not used anymore
                            onVoiceCancel = {},    // Not used anymore
                            strings = strings,
                            isEnabled = !uiState.isLoading,
                            isTranscribing = isTranscribing  // Show transcribing feedback
                        )
                    }
                    is VoiceRecordingUIState.Recording -> {
                        // Recording UI - simplified flow
                        VoiceRecordingUI(
                            state = voiceRecordingUIState,
                            amplitude = voiceAmplitude,
                            onStopRecording = {
                                // Stop recording and start transcription in background
                                val audioFile = voiceRecordingViewModel.stopForPreview(
                                    userId = viewModel.getDeviceId(),
                                    language = if (language == Language.VIETNAMESE) "vi" else "en"
                                )

                                if (audioFile != null) {
                                    println("[ChatScreen] Recording stopped, transcription started in background")
                                    recordedAudioFile = audioFile
                                    // Return to Idle immediately - transcription will populate input box
                                    voiceRecordingUIState = VoiceRecordingUIState.Idle
                                } else {
                                    println("[ChatScreen] Recording failed or too short")
                                    voiceRecordingUIState = VoiceRecordingUIState.Idle
                                }
                            },
                            onCancel = {
                                // Cancel recording and clean up
                                println("[ChatScreen] Recording cancelled by user")
                                voiceRecordingViewModel.cancelRecording()
                                recordedAudioFile?.delete()
                                recordedAudioFile = null
                                voiceRecordingUIState = VoiceRecordingUIState.Idle
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Show scroll-to-bottom button when not at bottom
            if (!isScrolledToBottom && uiState.messages.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Scroll to bottom"
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTags.MESSAGE_LIST),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
            ) {
                // Welcome card (only show when no messages)
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    item {
                        WelcomeCard(
                            strings = strings,
                            language = language,
                            deviceId = viewModel.getDeviceId(),
                            locationName = locationState.currentLocation?.geoLevel3
                                ?: locationState.currentLocation?.city,
                            onStarterQuestionClick = { question ->
                                viewModel.sendMessage(question)
                            }
                        )
                    }
                }

                // Messages
                itemsIndexed(
                    items = uiState.messages,
                    key = { _, message -> message.id }
                ) { index, message ->
                    // Render specialized bubbles for image messages
                    when {
                        message.messageType == "image" && message.role == com.nongtri.app.data.model.MessageRole.USER -> {
                            // User image message
                            ImageMessageBubble(
                                message = message,
                                onImageClick = { imageUrl ->
                                    showFullscreenImage = imageUrl
                                }
                            )
                        }
                        message.diagnosisData != null && message.role == com.nongtri.app.data.model.MessageRole.ASSISTANT -> {
                            // AI diagnosis response
                            val ttsManager = com.nongtri.app.platform.LocalTextToSpeechManager.current

                            DiagnosisResponseBubble(
                                message = message,
                                onTtsClick = {
                                    // Play TTS for diagnosis advice
                                    coroutineScope.launch {
                                        ttsManager.speak(
                                            text = message.content,
                                            language = if (language == Language.VIETNAMESE) "vi" else "en"
                                        )
                                    }
                                }
                            )
                        }
                        else -> {
                            // Regular text/voice message
                            MessageBubble(
                                message = message,
                                index = index,
                                isLightTheme = isLightTheme,
                                language = language,
                                onFeedback = { conversationId, isPositive ->
                                    viewModel.submitFeedback(conversationId, isPositive)
                                },
                                onFollowUpClick = { question ->
                                    viewModel.sendMessage(question)
                                },
                                onAudioUrlCached = { messageId, audioUrl ->
                                    viewModel.updateMessageAudioUrl(messageId, audioUrl)
                                }
                            )
                        }
                    }
                }

                // Typing indicator
                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }

                // Bottom spacing to prevent overlap with input
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Error snackbar
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = viewModel::clearError) {
                            Text(strings.ok)
                        }
                    }
                ) {
                    Text(error)
                }
            }
        }
    }

    // Location Bottom Sheet
    if (showLocationBottomSheet) {
        val locationState by locationViewModel.locationState.collectAsState()

        // Check permission state when bottom sheet is shown
        // This handles the case when user returns from settings with permission granted
        LaunchedEffect(showLocationBottomSheet) {
            locationViewModel.checkPermissionState()
        }

        LocationBottomSheet(
            currentLocation = locationState.currentLocation,
            savedLocations = locationState.savedLocations,
            isLoading = locationState.isLoading,
            shouldShowSettings = locationState.shouldShowSettings,
            ipLocation = locationState.ipLocation,
            gpsLocation = locationState.gpsLocation,
            onShareLocation = {
                locationViewModel.requestLocationPermission()
            },
            onSetPrimary = { locationId ->
                locationViewModel.setPrimaryLocation(locationId)
            },
            onDeleteLocation = { locationId ->
                locationViewModel.deleteLocation(locationId)
            },
            onDismiss = {
                showLocationBottomSheet = false
            }
        )
    }

    // Voice Permission Bottom Sheet
    if (showVoicePermissionBottomSheet) {
        // Continuously check permission state while bottom sheet is visible
        // This handles the case when user goes to Settings and grants permission
        LaunchedEffect(showVoicePermissionBottomSheet) {
            while (true) {
                voicePermissionViewModel.checkPermissionState()
                kotlinx.coroutines.delay(500) // Check every 500ms
            }
        }

        // Auto-dismiss if permission was granted
        LaunchedEffect(voicePermissionState.hasPermission) {
            if (voicePermissionState.hasPermission) {
                showVoicePermissionBottomSheet = false
            }
        }

        VoicePermissionBottomSheet(
            shouldShowSettings = voicePermissionState.shouldShowSettings,
            onRequestPermission = {
                voicePermissionViewModel.requestPermission()
            },
            onDismiss = {
                showVoicePermissionBottomSheet = false
                // Check permission after dismissing in case user granted it
                voicePermissionViewModel.checkPermissionState()
            }
        )
    }

    // Image Permission Bottom Sheet
    if (showImagePermissionBottomSheet) {
        ImagePermissionBottomSheet(
            hasCameraPermission = imagePermissionState.hasCameraPermission,
            hasStoragePermission = imagePermissionState.hasStoragePermission,
            shouldShowSettings = imagePermissionState.shouldShowSettings,
            onRequestCameraPermission = {
                if (imagePermissionState.shouldShowSettings) {
                    imagePermissionViewModel.openSettings()
                } else {
                    imagePermissionViewModel.requestCameraPermission()
                }
            },
            onRequestStoragePermission = {
                if (imagePermissionState.shouldShowSettings) {
                    imagePermissionViewModel.openSettings()
                } else {
                    imagePermissionViewModel.requestStoragePermission()
                }
            },
            onDismiss = {
                showImagePermissionBottomSheet = false
                imagePermissionViewModel.checkPermissionState()
            }
        )
    }

    // Image Source Selector Bottom Sheet
    if (showImageSourceSelector) {
        ImageSourceBottomSheet(
            onCameraClick = {
                showImageSourceSelector = false
                isImageProcessing = true
                println("[ChatScreen] Launching camera...")

                imagePicker.launchCamera { result ->
                    if (result != null && result.base64Data != null) {
                        println("[ChatScreen] Camera image captured: ${result.width}x${result.height}, ${result.sizeBytes / 1024}KB")
                        selectedImageUri = result.uri
                        selectedImageBase64 = result.base64Data
                        showImagePreviewDialog = true
                    } else {
                        println("[ChatScreen] Camera capture cancelled or failed: base64Data=${result?.base64Data != null}")
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Failed to process image. Please try again.",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                    // Always reset processing flag
                    isImageProcessing = false
                }
            },
            onGalleryClick = {
                showImageSourceSelector = false
                isImageProcessing = true
                println("[ChatScreen] Launching gallery...")

                imagePicker.launchGallery { result ->
                    if (result != null && result.base64Data != null) {
                        println("[ChatScreen] Gallery image selected: ${result.width}x${result.height}, ${result.sizeBytes / 1024}KB")
                        selectedImageUri = result.uri
                        selectedImageBase64 = result.base64Data
                        showImagePreviewDialog = true
                    } else {
                        println("[ChatScreen] Gallery selection cancelled or failed: base64Data=${result?.base64Data != null}")
                        if (result != null && result.base64Data == null) {
                            // Image was selected but failed to process
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = "Failed to process image. Please try again.",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                    // Always reset processing flag
                    isImageProcessing = false
                }
            },
            onDismiss = {
                showImageSourceSelector = false
            }
        )
    }

    // Image Preview Dialog
    if (showImagePreviewDialog && selectedImageUri != null && selectedImageBase64 != null) {
        ImagePreviewDialog(
            imageUri = selectedImageUri!!,
            onDismiss = {
                showImagePreviewDialog = false
                selectedImageUri = null
                selectedImageBase64 = null
            },
            onConfirm = { question ->
                // Validate that we have the base64 data
                val base64Data = selectedImageBase64
                if (base64Data == null) {
                    println("[ChatScreen] Error: base64Data is null, cannot send image")
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Failed to process image. Please try again.",
                            duration = SnackbarDuration.Short
                        )
                    }
                    showImagePreviewDialog = false
                    selectedImageUri = null
                    selectedImageBase64 = null
                    return@ImagePreviewDialog
                }

                println("[ChatScreen] Sending image for diagnosis: $question")

                // Show optimistic user message with image immediately
                viewModel.showOptimisticImageMessage(
                    imageData = selectedImageUri!!,  // Use URI for local preview
                    question = question
                )

                // Send image to backend for diagnosis
                viewModel.sendImageDiagnosis(
                    imageData = base64Data,  // Use base64 for upload
                    question = question
                )

                // Close dialog and clear state
                showImagePreviewDialog = false
                selectedImageUri = null
                selectedImageBase64 = null
                isImageProcessing = false  // Reset after upload starts
            }
        )
    }

    // Fullscreen Image Viewer
    if (showFullscreenImage != null) {
        val diagnosisData = uiState.messages
            .firstOrNull { it.imageUrl == showFullscreenImage }
            ?.diagnosisData

        FullscreenImageDialog(
            imageUrl = showFullscreenImage!!,
            diagnosisData = diagnosisData,
            onDismiss = {
                showFullscreenImage = null
            }
        )
    }
}
