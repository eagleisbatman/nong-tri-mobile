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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.debounce
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
    val strings = LocalizationProvider.getStrings(language)
    val isLightTheme = !isSystemInDarkTheme()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }
    var showLocationBottomSheet by remember { mutableStateOf(false) }
    var locationSheetStart by remember { mutableStateOf<Long?>(null) }
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
    var selectedImageSource by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("unknown") }  // "camera" or "gallery" for analytics
    var isImageProcessing by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }  // Prevent multiple simultaneous operations
    var showFullscreenImage by remember { mutableStateOf<String?>(null) }  // Image URL to show fullscreen

    // Snackbar for error messages (only for non-permission errors)
    val snackbarHostState = remember { SnackbarHostState() }
    var currentOptimisticMessageId by remember { mutableStateOf<String?>(null) }
    var starterQuestionsCount by remember { mutableStateOf(0) }
    var firstViewLogged by remember { mutableStateOf(false) }

    // ROUND 10: Track screen display time
    val screenDisplayTime = remember { System.currentTimeMillis() }

    // Track first view for analytics (wait for starter questions to load)
    LaunchedEffect(uiState.messages.size, locationState.currentLocation, locationState.ipLocation, starterQuestionsCount, firstViewLogged) {
        if (!firstViewLogged) {
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
                hasStarterQuestions = starterQuestionsCount > 0,
                starterQuestionsCount = starterQuestionsCount,
                locationDisplayed = locationDisplayed,
                locationType = locationType,
                timeSinceLanguageSelectionMs = viewModel.timeSinceLanguageSelectionMs()
            )

            firstViewLogged = true
        }
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

    // Auto-scroll behavior
    // 1. Scroll to bottom when new messages arrive (user sends or AI responds)
    // 2. Only auto-scroll if user is already near bottom (not reading old messages)
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            // Calculate if we should auto-scroll
            val shouldAutoScroll = isScrolledToBottom ||
                // Also auto-scroll if this is a new user message (just sent)
                (uiState.messages.size > 0 &&
                 uiState.messages.lastOrNull()?.role == com.nongtri.app.data.model.MessageRole.USER)

            if (shouldAutoScroll) {
                // Wait for the list to update with new items
                kotlinx.coroutines.delay(100)

                // Use the actual item count from LazyListState
                val totalItems = listState.layoutInfo.totalItemsCount
                val targetIndex = totalItems - 1

                if (targetIndex >= 0) {
                    // Smooth animated scroll to the last item
                    try {
                        listState.animateScrollToItem(
                            index = targetIndex,
                            scrollOffset = 0
                        )
                    } catch (e: Exception) {
                        // Fallback to instant scroll if animation fails
                        listState.scrollToItem(targetIndex)
                    }
                }
            }
        }
    }

    // Auto-scroll during streaming - monitor last assistant message content length
    // Use LaunchedEffect with content length as key to trigger on every content update
    val lastMessage = uiState.messages.lastOrNull()
    val lastMessageContentLength = lastMessage?.content?.length ?: 0
    
    LaunchedEffect(lastMessage?.id, lastMessageContentLength) {
        // Re-read the message to get fresh state
        val currentLastMessage = uiState.messages.lastOrNull()
        val isStreaming = currentLastMessage?.role == com.nongtri.app.data.model.MessageRole.ASSISTANT && 
                          currentLastMessage.isLoading == true
        
        // Only auto-scroll if streaming is active
        if (isStreaming && lastMessageContentLength > 0) {
            // Small delay to let layout update after content change
            kotlinx.coroutines.delay(50)
            
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastIndex = totalItems - 1
            
            if (lastIndex >= 0) {
                try {
                    // Use instant scroll for streaming (smoother during rapid updates)
                    listState.scrollToItem(lastIndex)
                } catch (e: Exception) {
                    // Ignore if fails (might be during layout)
                }
            }
        }
    }

    // Additional auto-scroll trigger based on actual list changes
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.totalItemsCount }
            .collect { itemCount ->
                // Auto-scroll when a new user message is added
                if (itemCount > 0) {
                    // Use fresh state from viewModel
                    val messages = viewModel.uiState.value.messages
                    val lastMessage = messages.lastOrNull()

                    if (lastMessage?.role == com.nongtri.app.data.model.MessageRole.USER) {
                        // User just sent a message - scroll to it
                        kotlinx.coroutines.delay(50)
                        val targetIndex = messages.lastIndex
                        if (targetIndex >= 0) {
                            listState.animateScrollToItem(targetIndex)
                        }
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
                    com.nongtri.app.analytics.Events.logVoiceTranscriptionApplied(
                        durationMs = voiceRecordingViewModel.getLastRecordingDurationMs(),
                        transcriptionLength = text.length
                    )

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
                                contentDescription = strings.cdSelectedPlantImage,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Remove button
                            IconButton(onClick = {
                                com.nongtri.app.analytics.Events.logImageAttachmentRemoved(selectedImageSource)
                                viewModel.removeAttachedImage()
                                selectedImageSource = "unknown"
                                if (viewModel.uiState.value.currentMessage == strings.defaultPlantQuestion) {
                                    viewModel.updateMessage("")
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = strings.close)
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

                                val hasAttachedImage = uiState.attachedImageBase64 != null

                                when {
                                    pendingVoiceUrl != null -> {
                                        println("[ChatScreen] Sending voice message with transcription: ${uiState.currentMessage}")

                                        viewModel.addUserVoiceMessage(
                                            transcription = uiState.currentMessage,
                                            voiceAudioUrl = pendingVoiceUrl,
                                            durationMs = pendingVoiceDuration ?: 0L
                                        )

                                        viewModel.sendVoiceMessage(
                                            transcription = uiState.currentMessage,
                                            voiceAudioUrl = pendingVoiceUrl,
                                            durationMs = pendingVoiceDuration ?: 0L
                                        )

                                        pendingVoiceUrl = null
                                        pendingVoiceDuration = null

                                        coroutineScope.launch {
                                            snapshotFlow { viewModel.uiState.value.messages.size }
                                                .drop(1)
                                                .first()

                                            val lastIndex = viewModel.uiState.value.messages.lastIndex
                                            if (lastIndex >= 0) {
                                                listState.animateScrollToItem(lastIndex)
                                            }
                                        }
                                    }
                                    hasAttachedImage -> {
                                        val preview = uiState.attachedImageUri ?: uiState.attachedImageBase64!!
                                        val question = uiState.currentMessage.ifBlank { strings.defaultPlantQuestion }

                                        println("[ChatScreen] Sending attached image for diagnosis")

                                        currentOptimisticMessageId = viewModel.showOptimisticImageMessage(
                                            imageData = preview,
                                            question = question
                                        )

                                        viewModel.sendImageDiagnosis(
                                            imageData = uiState.attachedImageBase64!!,
                                            question = question,
                                            imageSource = selectedImageSource
                                        )

                                        viewModel.removeAttachedImage()
                                        viewModel.updateMessage("")
                                        selectedImageSource = "unknown"

                                        coroutineScope.launch {
                                            snapshotFlow { viewModel.uiState.value.messages.size }
                                                .drop(1)
                                                .first()

                                            val lastIndex = viewModel.uiState.value.messages.lastIndex
                                            if (lastIndex >= 0) {
                                                listState.animateScrollToItem(lastIndex)
                                            }
                                        }
                                    }
                                    else -> {
                                        viewModel.sendMessage(uiState.currentMessage)

                                        coroutineScope.launch {
                                            snapshotFlow { viewModel.uiState.value.messages.size }
                                                .drop(1)
                                                .first()

                                            val lastIndex = viewModel.uiState.value.messages.lastIndex
                                            if (lastIndex >= 0) {
                                                listState.animateScrollToItem(lastIndex)
                                            }
                                        }
                                    }
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
                            isEnabled = !uiState.isLoading,  // Allow chat while diagnosis runs in background
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
            // Add fade animation for smooth appearance/disappearance
            androidx.compose.animation.AnimatedVisibility(
                visible = !isScrolledToBottom && uiState.messages.isNotEmpty(),
                enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            // Use the actual LazyColumn item count for accurate scrolling
                            val totalItems = listState.layoutInfo.totalItemsCount
                            val lastIndex = totalItems - 1

                            if (lastIndex >= 0) {
                                // Smooth animated scroll to the very last item
                                try {
                                    listState.animateScrollToItem(
                                        index = lastIndex,
                                        scrollOffset = 0
                                    )
                                } catch (e: Exception) {
                                    // Fallback to instant scroll if animation fails
                                    listState.scrollToItem(lastIndex)
                                }
                            }
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

                                // Auto-scroll to show the new message using snapshotFlow
                                coroutineScope.launch {
                                    // Wait for the message list to actually update
                                    val newSize = snapshotFlow { viewModel.uiState.value.messages.size }
                                        .drop(1)  // Skip the current value, wait for the change
                                        .first()

                                    // Now scroll to the last item with the fresh state
                                    val lastIndex = viewModel.uiState.value.messages.lastIndex
                                    if (lastIndex >= 0) {
                                        listState.animateScrollToItem(
                                            index = lastIndex,
                                            scrollOffset = 0
                                        )
                                    }
                                }
                            }
                            ,
                            onStarterQuestionsLoaded = { count ->
                                starterQuestionsCount = count
                                viewModel.updateStarterQuestionsCount(count)
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
                            DiagnosisResponseBubble(
                                message = message,
                                strings = strings,
                                onAudioUrlCached = { messageId, audioUrl ->
                                    viewModel.updateMessageAudioUrl(messageId, audioUrl)
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

                                    // Auto-scroll to show the new message using snapshotFlow
                                    coroutineScope.launch {
                                        // Wait for the message list to actually update
                                        val newSize = snapshotFlow { viewModel.uiState.value.messages.size }
                                            .drop(1)  // Skip the current value, wait for the change
                                            .first()

                                        // Now scroll to the last item with the fresh state
                                        val lastIndex = viewModel.uiState.value.messages.lastIndex
                                        if (lastIndex >= 0) {
                                            listState.animateScrollToItem(
                                                index = lastIndex,
                                                scrollOffset = 0
                                            )
                                        }
                                    }
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
            locationSheetStart = System.currentTimeMillis()
            com.nongtri.app.analytics.Events.logScreenViewed("location_sheet")
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
                locationSheetStart?.let { start ->
                    com.nongtri.app.analytics.Events.logScreenTimeSpent("location_sheet", System.currentTimeMillis() - start)
                }
                locationSheetStart = null
            }
        )
    } else {
        locationSheetStart?.let { start ->
            com.nongtri.app.analytics.Events.logScreenTimeSpent("location_sheet", System.currentTimeMillis() - start)
        }
        locationSheetStart = null
    }

    // Voice Permission Bottom Sheet
    var voicePermissionSheetStart by remember { mutableStateOf<Long?>(null) }
    if (showVoicePermissionBottomSheet) {
        LaunchedEffect(Unit) {
            voicePermissionSheetStart = System.currentTimeMillis()
            com.nongtri.app.analytics.Events.logScreenViewed("voice_permission_sheet")
        }
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
                com.nongtri.app.analytics.Events.logVoicePermissionDismissed()
                voicePermissionSheetStart?.let { start ->
                    com.nongtri.app.analytics.Events.logScreenTimeSpent("voice_permission_sheet", System.currentTimeMillis() - start)
                }
                voicePermissionSheetStart = null
            },
            language = language
        )
    } else {
        voicePermissionSheetStart?.let { start ->
            com.nongtri.app.analytics.Events.logScreenTimeSpent("voice_permission_sheet", System.currentTimeMillis() - start)
        }
        voicePermissionSheetStart = null
    }

    // Image Permission Bottom Sheet
    var imagePermissionSheetStart by remember { mutableStateOf<Long?>(null) }
    if (showImagePermissionBottomSheet) {
        LaunchedEffect(Unit) {
            imagePermissionSheetStart = System.currentTimeMillis()
            com.nongtri.app.analytics.Events.logScreenViewed("image_permission_sheet")
        }
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
                com.nongtri.app.analytics.Events.logImagePermissionDismissed("camera_storage")
                imagePermissionSheetStart?.let { start ->
                    com.nongtri.app.analytics.Events.logScreenTimeSpent("image_permission_sheet", System.currentTimeMillis() - start)
                }
                imagePermissionSheetStart = null
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
    } else {
        imagePermissionSheetStart?.let { start ->
            com.nongtri.app.analytics.Events.logScreenTimeSpent("image_permission_sheet", System.currentTimeMillis() - start)
        }
        imagePermissionSheetStart = null
    }

    // Image Source Selector Bottom Sheet
    var imageSourceSheetStart by remember { mutableStateOf<Long?>(null) }
    if (showImageSourceSelector) {
        // ROUND 5: Track image source sheet opened
        LaunchedEffect(Unit) {
            com.nongtri.app.analytics.Events.logImageSourceSheetOpened()
            imageSourceSheetStart = System.currentTimeMillis()
            com.nongtri.app.analytics.Events.logScreenViewed("image_source_sheet")
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

                        // Attach image inline with chat composer
                        selectedImageSource = "camera"
                        viewModel.attachImage(
                            uri = result.uri,
                            base64Data = result.base64Data!!
                        )

                        // Auto-populate default question if input is empty
                        if (viewModel.uiState.value.currentMessage.isBlank()) {
                            viewModel.updateMessage(strings.defaultPlantQuestion)
                        }

                        // Track preview displayed analytics (mirrors old dialog event)
                        com.nongtri.app.analytics.Events.logImagePreviewDisplayed(
                            fileSizeKb = (result.sizeBytes / 1024).toInt(),
                            imageWidth = result.width,
                            imageHeight = result.height
                        )
                    } else {
                        println("[ChatScreen] Camera capture cancelled or failed: base64Data=${result?.base64Data != null}")
                        // Show specific error message if available (farmer-friendly)
                        val errorMessage = result?.error ?: strings.errorFailedToProcessImage
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

                        // Attach image inline with chat composer
                        selectedImageSource = "gallery"
                        viewModel.attachImage(
                            uri = result.uri,
                            base64Data = result.base64Data!!
                        )

                        // Auto-populate default question if input is empty
                        if (viewModel.uiState.value.currentMessage.isBlank()) {
                            viewModel.updateMessage(strings.defaultPlantQuestion)
                        }

                        // Mirror legacy analytics event for preview display
                        com.nongtri.app.analytics.Events.logImagePreviewDisplayed(
                            fileSizeKb = (result.sizeBytes / 1024).toInt(),
                            imageWidth = result.width,
                            imageHeight = result.height
                        )
                    } else {
                        println("[ChatScreen] Gallery selection cancelled or failed: base64Data=${result?.base64Data != null}")
                        if (result != null && result.base64Data == null) {
                            // Image was selected but failed to process - show specific error
                            val errorMessage = result.error ?: strings.errorFailedToProcessImage
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
                imageSourceSheetStart?.let { start ->
                    com.nongtri.app.analytics.Events.logScreenTimeSpent("image_source_sheet", System.currentTimeMillis() - start)
                }
                imageSourceSheetStart = null
            }
        )
    } else {
        imageSourceSheetStart?.let { start ->
            com.nongtri.app.analytics.Events.logScreenTimeSpent("image_source_sheet", System.currentTimeMillis() - start)
        }
        imageSourceSheetStart = null
    }

    // Fullscreen Image Viewer
    var fullscreenStart by remember { mutableStateOf<Long?>(null) }
    if (showFullscreenImage != null) {
        val message = uiState.messages
            .firstOrNull { it.imageUrl == showFullscreenImage }
        val diagnosisData = message?.diagnosisData
        val jobId = message?.diagnosisPendingJobId

        LaunchedEffect(showFullscreenImage) {
            fullscreenStart = System.currentTimeMillis()
            com.nongtri.app.analytics.Events.logScreenViewed("image_fullscreen_dialog")
        }

        FullscreenImageDialog(
            language = language,
            imageUrl = showFullscreenImage!!,
            diagnosisData = diagnosisData,
            jobId = jobId,
            onDismiss = {
                showFullscreenImage = null
                fullscreenStart?.let { start ->
                    com.nongtri.app.analytics.Events.logScreenTimeSpent("image_fullscreen_dialog", System.currentTimeMillis() - start)
                }
                fullscreenStart = null
            }
        )
    } else {
        fullscreenStart?.let { start ->
            com.nongtri.app.analytics.Events.logScreenTimeSpent("image_fullscreen_dialog", System.currentTimeMillis() - start)
        }
        fullscreenStart = null
    }
}
