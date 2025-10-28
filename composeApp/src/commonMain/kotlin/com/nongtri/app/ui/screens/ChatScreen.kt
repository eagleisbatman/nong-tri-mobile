package com.nongtri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.unit.IntOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val streamingContent by viewModel.streamingContent.collectAsState()
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
    val hapticFeedback = com.nongtri.app.platform.LocalHapticFeedback.current
    val voiceRecordingViewModel = remember { VoiceRecordingViewModel(audioRecorder, hapticFeedback) }
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
    var permissionSheetOpenedFromImageSelector by remember { mutableStateOf(false) }
    var showImagePreviewDialog by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    var selectedImageUri by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf<String?>(null) }  // Display URI
    // Note: selectedImageBase64 cannot use rememberSaveable (too large for savedInstanceState)
    var selectedImageBase64 by remember { mutableStateOf<String?>(null) }  // Base64 data for upload
    var selectedImageSource by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("unknown") }  // "camera" or "gallery" for analytics
    var isImageProcessing by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }  // Prevent multiple simultaneous operations
    var showFullscreenImage by remember { mutableStateOf<String?>(null) }  // Image URL to show fullscreen

    // Snackbar for error messages (only for non-permission errors)
    val snackbarHostState = remember { SnackbarHostState() }
    var currentOptimisticMessageId by remember { mutableStateOf<String?>(null) }

    // ROUND 10: Track screen display time
    val screenDisplayTime = remember { System.currentTimeMillis() }

    // Track first view for analytics
    LaunchedEffect(Unit) {
        // ROUND 10: Track generic screen view
        com.nongtri.app.analytics.Events.logScreenViewed("chat")

        val hasWelcomeCard = uiState.messages.isEmpty()
        val locationDisplayed = locationState.currentLocation != null || locationState.ipLocation != null
        val locationType = when {
            locationState.gpsLocation != null -> "gps"
            locationState.ipLocation != null -> "ip"
            else -> "none"
        }

        com.nongtri.app.analytics.Events.logChatScreenFirstView(
            hasWelcomeCard = hasWelcomeCard,
            hasStarterQuestions = false, // Starter questions not implemented yet
            starterQuestionsCount = 0,
            locationDisplayed = locationDisplayed,
            locationType = locationType,
            timeSinceLanguageSelectionMs = 0L // TODO: Track from language selection screen
        )
    }

    // ROUND 10: Track screen time spent
    DisposableEffect(Unit) {
        onDispose {
            val timeSpentMs = System.currentTimeMillis() - screenDisplayTime
            com.nongtri.app.analytics.Events.logScreenTimeSpent("chat", timeSpentMs)
        }
    }

    // CRITICAL: Clear conversation when GPS location is shared
    // This ensures clean context with updated location information
    // Don't create new thread yet - wait until user sends first message
    LaunchedEffect(locationState.gpsLocation?.id) {
        // Only trigger if GPS location exists and has changed (id changed)
        locationState.gpsLocation?.let {
            println("[ChatScreen] GPS location changed (id=${it.id}), clearing conversation")
            viewModel.clearHistory()
        }
    }

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

    // Auto-scroll to bottom ONLY when new messages arrive
    // DO NOT track content or scroll during streaming - causes jumping!
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                val lastIndex = listState.layoutInfo.totalItemsCount - 1
                if (lastIndex >= 0) {
                    listState.scrollToItem(lastIndex)  // Instant scroll to bottom
                }
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
                            onClick = {
                                // ROUND 6: Track menu opened
                                com.nongtri.app.analytics.Events.logMenuOpened()
                                showMenu = true
                            },
                            modifier = Modifier.testTag(TestTags.PROFILE_BUTTON)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = strings.cdSettings
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            // 1. View Conversations (top priority)
                            DropdownMenuItem(
                                text = { Text(strings.menuConversations) },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                },
                                onClick = {
                                    // ROUND 6: Track menu item clicked
                                    com.nongtri.app.analytics.Events.logMenuItemClicked("conversations")
                                    onViewConversations()
                                    showMenu = false
                                },
                                modifier = Modifier.testTag(TestTags.MENU_CONVERSATIONS)
                            )

                            // 2. New Chat
                            DropdownMenuItem(
                                text = { Text(strings.menuNewChat) },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                },
                                onClick = {
                                    // ROUND 6: Track menu item clicked
                                    com.nongtri.app.analytics.Events.logMenuItemClicked("new_chat")
                                    viewModel.createNewThread()
                                    showMenu = false
                                },
                                modifier = Modifier.testTag(TestTags.MENU_NEW_CHAT)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Share Location
                            DropdownMenuItem(
                                text = { Text(strings.menuShareLocation) },
                                leadingIcon = {
                                    Icon(Icons.Default.LocationOn, contentDescription = null)
                                },
                                onClick = {
                                    // ROUND 6: Track menu item clicked
                                    com.nongtri.app.analytics.Events.logMenuItemClicked("share_location")

                                    // ROUND 4: Track location bottom sheet opened
                                    com.nongtri.app.analytics.Events.logLocationBottomSheetOpened(trigger = "menu_item")

                                    showLocationBottomSheet = true
                                    showMenu = false
                                },
                                modifier = Modifier.testTag(TestTags.MENU_SHARE_LOCATION)
                            )

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 3. Language selection
                            Text(
                                text = strings.menuLanguageSection,
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
                                        // ROUND 6: Track language changed
                                        com.nongtri.app.analytics.Events.logLanguageChanged(
                                            fromLanguage = language.code,
                                            toLanguage = lang.code
                                        )

                                        // ROUND 6: Track menu item clicked
                                        com.nongtri.app.analytics.Events.logMenuItemClicked("language_${lang.code}")

                                        onLanguageChange(lang)
                                        showMenu = false
                                    },
                                    modifier = Modifier.testTag(TestTags.menuLanguage(lang.code))
                                )
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                            // 5. Theme (at bottom)
                            Text(
                                text = strings.menuThemeSection,
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
                                                    com.nongtri.app.data.preferences.ThemeMode.LIGHT -> strings.light
                                                    com.nongtri.app.data.preferences.ThemeMode.DARK -> strings.dark
                                                    com.nongtri.app.data.preferences.ThemeMode.SYSTEM -> strings.systemDefault
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
                                    },
                                    modifier = Modifier.testTag(TestTags.menuTheme(mode.name.lowercase()))
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .testTag(TestTags.CHAT_APP_BAR)
            )
        },
        bottomBar = {
            // Track voice recording UI state locally
            var voiceRecordingUIState by remember { mutableStateOf<VoiceRecordingUIState>(VoiceRecordingUIState.Idle) }
            var recordedAudioFile by remember { mutableStateOf<java.io.File?>(null) }

            // Track pending voice metadata for when user sends
            var pendingVoiceUrl by remember { mutableStateOf<String?>(null) }
            var pendingVoiceDuration by remember { mutableStateOf<Long?>(null) }

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

            // Populate input field when transcription completes successfully
            LaunchedEffect(isTranscribing, transcriptionText) {
                // When transcription completes and we have text, populate input field
                val text = transcriptionText
                if (!isTranscribing && text != null && text.isNotEmpty() && recordedAudioFile != null) {
                    println("[ChatScreen] Transcription complete: $text")
                    println("[ChatScreen] Populating input field with transcription")

                    // Populate input field with transcribed text
                    viewModel.updateMessage(text)

                    // Store voice metadata for when user sends
                    val result = voiceRecordingViewModel.getTranscriptionResult()
                    val voiceAudioUrl = result?.second
                    val durationMs = voiceRecordingViewModel.getLastRecordingDurationMs()

                    // Store these for when user clicks send
                    pendingVoiceUrl = voiceAudioUrl
                    pendingVoiceDuration = durationMs

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

            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                    // Show image thumbnail above input bar when image is attached
                    if (uiState.attachedImageUri != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Image thumbnail
                            coil3.compose.AsyncImage(
                                model = uiState.attachedImageUri!!,
                                contentDescription = "Attached image",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Remove button
                            IconButton(onClick = { viewModel.removeAttachedImage() }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove image")
                            }
                        }

                        HorizontalDivider()
                    }

                    when (voiceRecordingUIState) {
                    is VoiceRecordingUIState.Idle -> {
                        // Normal input bar
                        WhatsAppStyleInputBar(
                            value = uiState.currentMessage,
                            onValueChange = viewModel::updateMessage,
                            onSend = {
                                // Light haptic feedback - message sent
                                hapticFeedback.tick()

                                // Check if this is a voice message with pending metadata
                                if (pendingVoiceUrl != null) {
                                    println("[ChatScreen] Sending voice message with transcription: ${uiState.currentMessage}")

                                    // Add user's voice message to UI first (optimistic update)
                                    viewModel.addUserVoiceMessage(
                                        transcription = uiState.currentMessage,
                                        voiceAudioUrl = pendingVoiceUrl,
                                        durationMs = pendingVoiceDuration ?: 0L
                                    )

                                    // Then trigger AI response
                                    viewModel.sendVoiceMessage(
                                        transcription = uiState.currentMessage,
                                        voiceAudioUrl = pendingVoiceUrl,
                                        durationMs = pendingVoiceDuration ?: 0L
                                    )

                                    // Clear pending voice metadata and input field
                                    pendingVoiceUrl = null
                                    pendingVoiceDuration = null
                                } else {
                                    // Regular text message
                                    viewModel.sendMessage(uiState.currentMessage)
                                }
                            },
                            hasAttachedImage = uiState.attachedImageUri != null,
                            onImageClick = {
                                // Track image funnel step 1: Image button clicked
                                com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step1_ImageButtonClicked()

                                // ROUND 4: Track standalone image button clicked event
                                com.nongtri.app.analytics.Events.logImageButtonClicked()

                                // Prevent multiple simultaneous operations
                                if (isImageProcessing) {
                                    println("[ChatScreen] Image already being processed, ignoring click")
                                    return@WhatsAppStyleInputBar
                                }

                                // Prevent attaching multiple images - only one allowed
                                if (uiState.attachedImageUri != null) {
                                    println("[ChatScreen] Image already attached, ignoring click")
                                    return@WhatsAppStyleInputBar
                                }

                                // Check permissions - allow gallery access if storage permission is granted
                                if (imagePermissionState.hasStoragePermission) {
                                    // At least storage permission granted, show image source selector
                                    // (Camera option will be disabled if camera permission not granted)
                                    showImageSourceSelector = true
                                } else {
                                    // No storage permission, request permissions
                                    showImagePermissionBottomSheet = true
                                }
                            },
                            onVoiceClick = {
                                // ROUND 7: Track voice button clicked (individual event)
                                com.nongtri.app.analytics.Events.logVoiceButtonClicked()
                                // Track voice funnel step 1: Voice button clicked
                                com.nongtri.app.analytics.Funnels.voiceAdoptionFunnel.step1_VoiceButtonClicked()

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
                            isEnabled = !uiState.isLoading && !uiState.isDiagnosisInProgress,  // Disable during loading or diagnosis
                            isTranscribing = isTranscribing  // Show transcribing feedback
                        )
                    }
                    is VoiceRecordingUIState.Recording -> {
                        // Recording UI - simplified flow
                        VoiceRecordingUI(
                            state = voiceRecordingUIState,
                            amplitude = voiceAmplitude,
                            strings = strings,
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
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(TestTags.SCROLL_TO_BOTTOM_FAB)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = strings.cdScrollToBottom
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
                reverseLayout = false,  // Keep normal order - oldest at top, newest at bottom
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTags.MESSAGE_LIST),
                contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)  // Better performance with spacedBy
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
                                // Light haptic feedback - starter question clicked
                                hapticFeedback.tick()
                                viewModel.sendMessage(question)
                            }
                        )
                    }
                }

                // Messages - show ALL messages including streaming ones
                itemsIndexed(
                    items = uiState.messages,
                    key = { _, message -> message.id },  // Stable unique key
                    contentType = { _, message -> message.role }  // Helps Compose optimize item reuse
                ) { index, message ->
                    // Render specialized bubbles for image messages
                    when {
                        message.messageType == "image" && message.role == com.nongtri.app.data.model.MessageRole.USER -> {
                            // User image message
                            ImageMessageBubble(
                                message = message,
                                strings = strings,
                                onImageClick = { imageUrl ->
                                    showFullscreenImage = imageUrl
                                }
                            )
                        }
                        message.messageType == "diagnosis_pending" && message.role == com.nongtri.app.data.model.MessageRole.ASSISTANT -> {
                            // Diagnosis pending - show informative card
                            com.nongtri.app.ui.components.DiagnosisPendingCard(
                                imageUrl = message.diagnosisPendingImageUrl ?: "",
                                jobId = message.diagnosisPendingJobId ?: "",
                                strings = strings,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        message.diagnosisData != null && message.role == com.nongtri.app.data.model.MessageRole.ASSISTANT -> {
                            // AI diagnosis response
                            val ttsManager = com.nongtri.app.platform.LocalTextToSpeechManager.current

                            DiagnosisResponseBubble(
                                message = message,
                                strings = strings,
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
                                    // Light haptic feedback - follow-up question clicked
                                    hapticFeedback.tick()
                                    viewModel.sendMessage(question)
                                },
                                onAudioUrlCached = { messageId, audioUrl ->
                                    viewModel.updateMessageAudioUrl(messageId, audioUrl)
                                }
                            )
                        }
                    }
                }

                // Typing indicator is now handled inside MessageBubble when message.isLoading && content.isEmpty()

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
            language = language,
            currentLocation = locationState.currentLocation,
            savedLocations = locationState.savedLocations,
            isLoading = locationState.isLoading,
            shouldShowSettings = locationState.shouldShowSettings,
            ipLocation = locationState.ipLocation,
            gpsLocation = locationState.gpsLocation,
            onShareLocation = {
                // ROUND 10: Track location update button clicked
                com.nongtri.app.analytics.Events.logLocationUpdateClicked()
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
            },
            language = language
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
            },
            onBack = if (permissionSheetOpenedFromImageSelector) {
                {
                    // Reopen image source selector
                    permissionSheetOpenedFromImageSelector = false
                    showImageSourceSelector = true
                }
            } else null,
            language = language
        )
    }

    // Image Source Selector Bottom Sheet
    if (showImageSourceSelector) {
        // ROUND 5: Track image source sheet opened
        LaunchedEffect(Unit) {
            com.nongtri.app.analytics.Events.logImageSourceSheetOpened()
        }

        ImageSourceBottomSheet(
            language = language,
            hasCameraPermission = imagePermissionState.hasCameraPermission,
            onRequestCameraPermission = {
                // Show permission sheet to request camera permission
                permissionSheetOpenedFromImageSelector = true
                showImagePermissionBottomSheet = true
            },
            onCameraClick = {
                // Track image funnel step 3: Source selected
                com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step3_SourceSelected("camera")

                // ROUND 4: Track standalone image source selected event
                com.nongtri.app.analytics.Events.logImageSourceSelected("camera")

                // ROUND 5: Track camera opened
                com.nongtri.app.analytics.Events.logImageCameraOpened()

                showImageSourceSelector = false
                isImageProcessing = true
                println("[ChatScreen] Launching camera...")

                imagePicker.launchCamera { result ->
                    if (result != null && result.base64Data != null) {
                        println("[ChatScreen] Camera image captured: ${result.width}x${result.height}, ${result.sizeBytes / 1024}KB, source: ${result.source}")

                        // Track image funnel step 4: Image captured
                        com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step4_ImageCaptured()

                        // ROUND 4: Track standalone image captured event with full details
                        com.nongtri.app.analytics.Events.logImageCaptured(
                            fileSizeKb = (result.sizeBytes / 1024).toInt(),
                            imageWidth = result.width,
                            imageHeight = result.height
                        )

                        // Show preview dialog with async diagnosis flow
                        selectedImageUri = result.uri
                        selectedImageBase64 = result.base64Data
                        selectedImageSource = "camera"
                        showImagePreviewDialog = true
                    } else {
                        println("[ChatScreen] Camera capture cancelled or failed: base64Data=${result?.base64Data != null}")
                        // Show specific error message if available (farmer-friendly)
                        val errorMessage = result?.error ?: "Failed to process image. Please try again."
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = errorMessage,
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                    // Always reset processing flag
                    isImageProcessing = false
                }
            },
            onGalleryClick = {
                // Track image funnel step 3: Source selected
                com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step3_SourceSelected("gallery")

                // ROUND 4: Track standalone image source selected event
                com.nongtri.app.analytics.Events.logImageSourceSelected("gallery")

                // ROUND 5: Track gallery opened
                com.nongtri.app.analytics.Events.logImageGalleryOpened()

                showImageSourceSelector = false
                isImageProcessing = true
                println("[ChatScreen] Launching gallery...")

                imagePicker.launchGallery { result ->
                    if (result != null && result.base64Data != null) {
                        println("[ChatScreen] Gallery image selected: ${result.width}x${result.height}, ${result.sizeBytes / 1024}KB, source: ${result.source}")

                        // Track image funnel step 4: Image captured
                        com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step4_ImageCaptured()

                        // ROUND 4: Track standalone image selected from gallery event with full details
                        com.nongtri.app.analytics.Events.logImageSelectedFromGallery(
                            fileSizeKb = (result.sizeBytes / 1024).toInt(),
                            imageWidth = result.width,
                            imageHeight = result.height
                        )

                        // Show preview dialog with async diagnosis flow
                        selectedImageUri = result.uri
                        selectedImageBase64 = result.base64Data
                        selectedImageSource = "gallery"
                        showImagePreviewDialog = true
                    } else {
                        println("[ChatScreen] Gallery selection cancelled or failed: base64Data=${result?.base64Data != null}")
                        if (result != null && result.base64Data == null) {
                            // Image was selected but failed to process - show specific error
                            val errorMessage = result.error ?: "Failed to process image. Please try again."
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = errorMessage,
                                    duration = SnackbarDuration.Long
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
            language = language,
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
                    question = question,
                    imageSource = selectedImageSource
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
        val message = uiState.messages
            .firstOrNull { it.imageUrl == showFullscreenImage }
        val diagnosisData = message?.diagnosisData
        val jobId = message?.diagnosisPendingJobId

        FullscreenImageDialog(
            language = language,
            imageUrl = showFullscreenImage!!,
            diagnosisData = diagnosisData,
            jobId = jobId,
            onDismiss = {
                showFullscreenImage = null
            }
        )
    }
}

