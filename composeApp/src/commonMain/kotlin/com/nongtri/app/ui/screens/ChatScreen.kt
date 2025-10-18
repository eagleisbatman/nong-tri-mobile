package com.nongtri.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
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
import com.nongtri.app.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalizationProvider.getStrings(language)
    val isLightTheme = !isSystemInDarkTheme()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showMenu by remember { mutableStateOf(false) }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size)
            }
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag(TestTags.CHAT_SCREEN),
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
                            // Language selection
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

                            // Clear history
                            DropdownMenuItem(
                                text = { Text("Clear history") },
                                onClick = {
                                    onClearHistory()
                                    showMenu = false
                                }
                            )
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
            MessageInputBar(
                value = uiState.currentMessage,
                onValueChange = viewModel::updateMessage,
                onSend = {
                    viewModel.sendMessage(uiState.currentMessage)
                },
                strings = strings,
                isEnabled = !uiState.isLoading
            )
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
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Welcome card (only show when no messages)
                if (uiState.messages.isEmpty() && !uiState.isLoading) {
                    item {
                        WelcomeCard(strings = strings)
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
                        isLightTheme = isLightTheme
                    )
                }

                // Typing indicator
                if (uiState.isLoading) {
                    item {
                        TypingIndicator()
                    }
                }

                // Add spacing at bottom for better UX
                item {
                    Spacer(modifier = Modifier.height(8.dp))
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
}
