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

    // Voice recording state
    val audioRecorder = com.nongtri.app.platform.LocalAudioRecorder.current
    val voiceRecordingViewModel = remember { VoiceRecordingViewModel(audioRecorder) }
    val voiceRecordingState by voiceRecordingViewModel.state.collectAsState()

    // Snackbar for error messages
    val snackbarHostState = remember { SnackbarHostState() }
    var currentOptimisticMessageId by remember { mutableStateOf<String?>(null) }

    // Handle voice recording errors - remove optimistic message and show error
    LaunchedEffect(voiceRecordingState) {
        if (voiceRecordingState is VoiceRecordingState.Error) {
            val errorMessage = (voiceRecordingState as VoiceRecordingState.Error).message

            // Remove the optimistic message if it exists
            currentOptimisticMessageId?.let { messageId ->
                viewModel.removeVoiceMessage(messageId)
                currentOptimisticMessageId = null
            }

            // Show error message to user
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short
            )
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

                            // 4. Conversation History
                            DropdownMenuItem(
                                text = { Text("Conversation History") },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                                },
                                onClick = {
                                    // TODO: Navigate to history screen
                                    showMenu = false
                                }
                            )

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
            Column {
                // Voice recording bar (shown when recording)
                if (voiceRecordingState !is VoiceRecordingState.Idle && voiceRecordingState !is VoiceRecordingState.Cancelled) {
                    VoiceRecordingBar(
                        recordingState = voiceRecordingState,
                        onCancel = {
                            voiceRecordingViewModel.cancelRecording()
                        }
                    )
                }

                // Input bar
                WhatsAppStyleInputBar(
                    value = uiState.currentMessage,
                    onValueChange = viewModel::updateMessage,
                    onSend = {
                        viewModel.sendMessage(uiState.currentMessage)
                    },
                    onImageClick = {
                        // TODO: Handle image selection
                    },
                    onVoiceClick = {
                        // Short click - do nothing for now
                    },
                    onVoiceLongPress = {
                        // Start recording
                        voiceRecordingViewModel.startRecording()
                    },
                    onVoiceRelease = {
                        // ✅ OPTIMISTIC UI: Show voice bubble INSTANTLY with "..." placeholder
                        val optimisticMessageId = viewModel.showOptimisticVoiceMessage()
                        currentOptimisticMessageId = optimisticMessageId

                        // Stop recording and transcribe in background
                        voiceRecordingViewModel.stopRecording(
                            userId = viewModel.getDeviceId(),
                            language = if (language == Language.VIETNAMESE) "vi" else "en"
                        ) { transcription, voiceAudioUrl ->
                            // ✅ Update optimistic message with actual transcription
                            viewModel.updateVoiceMessage(optimisticMessageId, transcription, voiceAudioUrl)
                            currentOptimisticMessageId = null

                            // ✅ Then send to AI for response
                            viewModel.sendVoiceMessage(transcription, voiceAudioUrl)
                        }
                    },
                    onVoiceCancel = {
                        // User dragged finger off button - cancel recording
                        voiceRecordingViewModel.cancelRecording()
                    },
                    strings = strings,
                    isEnabled = !uiState.isLoading
                )
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
}
