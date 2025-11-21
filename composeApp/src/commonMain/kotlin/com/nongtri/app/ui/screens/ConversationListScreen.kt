package com.nongtri.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nongtri.app.data.model.ConversationThread
import com.nongtri.app.ui.components.TestTags
import com.nongtri.app.ui.viewmodel.ConversationListViewModel
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel,
    onThreadSelected: (Int, String?) -> Unit,
    onNavigateBack: () -> Unit,
    onNewConversation: (Int) -> Unit,
    language: com.nongtri.app.l10n.Language,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = com.nongtri.app.l10n.LocalizationProvider.getStrings(language)

    // ROUND 10: Track screen display time
    val screenDisplayTime = remember { System.currentTimeMillis() }

    // ROUND 8: Track conversations screen opened
    LaunchedEffect(Unit) {
        // ROUND 10: Track generic screen view
        com.nongtri.app.analytics.Events.logScreenViewed("conversation_list")
        com.nongtri.app.analytics.Events.logConversationsScreenOpened()
    }

    // ROUND 10: Track screen time spent
    DisposableEffect(Unit) {
        onDispose {
            val timeSpentMs = System.currentTimeMillis() - screenDisplayTime
            com.nongtri.app.analytics.Events.logScreenTimeSpent("conversation_list", timeSpentMs)
        }
    }

    // ROUND 8: Track conversations list viewed when threads are loaded
    LaunchedEffect(uiState.threads) {
        if (uiState.threads.isNotEmpty()) {
            com.nongtri.app.analytics.Events.logConversationsListViewed(
                conversationCount = uiState.threads.size
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag(TestTags.CONVERSATIONS_SCREEN),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.conversations,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag(TestTags.BACK_BUTTON)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.cdBack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag(TestTags.CONVERSATIONS_TOP_BAR)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.createNewThread { thread ->
                        // ROUND 8: Track conversation created (in callback when we have the ID)
                        com.nongtri.app.analytics.Events.logConversationCreated(
                            conversationId = thread.id.toString()
                        )
                        onNewConversation(thread.id)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag(TestTags.NEW_CONVERSATION_FAB)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.cdNewConversation
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.errorLoadingConversations,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // ROUND 11: Track retry button clicked
                                com.nongtri.app.analytics.Events.logFeatureRetryClicked(
                                    featureName = "conversation_list_load"
                                )
                                viewModel.loadThreads()
                            },
                            modifier = Modifier.testTag(TestTags.RETRY_BUTTON)
                        ) {
                            Text(strings.retry)
                        }
                    }
                }

                uiState.threads.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = strings.noConversationsYet,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = strings.noConversationsHint,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().testTag(TestTags.CONVERSATION_LIST),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        itemsIndexed(
                            items = uiState.threads,
                            key = { _, thread -> thread.id }
                        ) { index, thread ->
                            ConversationThreadItem(
                                thread = thread,
                                onClick = {
                                    // ROUND 8: Track conversation item clicked with actual position
                                    com.nongtri.app.analytics.Events.logConversationItemClicked(
                                        conversationId = thread.id.toString(),
                                        position = index + 1 // 1-based for analytics
                                    )
                                    onThreadSelected(thread.id, thread.title)
                                },
                                onDelete = {
                                    // ROUND 8: Track conversation delete clicked
                                    com.nongtri.app.analytics.Events.logConversationDeleteClicked(
                                        conversationId = thread.id.toString()
                                    )
                                    viewModel.deleteThread(thread.id)
                                },
                                strings = strings
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationThreadItem(
    thread: ConversationThread,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    strings: com.nongtri.app.l10n.Strings,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteConversationTitle) },
            text = { Text(strings.deleteConversationMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        // ROUND 8: Track conversation delete confirmed
                        com.nongtri.app.analytics.Events.logConversationDeleteConfirmed(
                            conversationId = thread.id.toString(),
                            messageCount = thread.messageCount
                        )
                        showDeleteDialog = false
                        onDelete()
                    },
                    modifier = Modifier.testTag(TestTags.DELETE_CONFIRM_BUTTON)
                ) {
                    Text(strings.deleteConversationConfirm, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        // ROUND 8: Track conversation delete cancelled
                        com.nongtri.app.analytics.Events.logConversationDeleteCancelled(
                            conversationId = thread.id.toString()
                        )
                        showDeleteDialog = false
                    },
                    modifier = Modifier.testTag(TestTags.DELETE_CANCEL_BUTTON)
                ) {
                    Text(strings.cancel)
                }
            },
            modifier = Modifier.testTag(TestTags.DELETE_DIALOG)
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
            .testTag(TestTags.conversationItem(thread.id)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = thread.getDisplayTitle(strings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${thread.messageCount}${strings.messageCountSuffix}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = formatRelativeTime(thread.getLastActivityTime(), strings),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.testTag(TestTags.deleteButton(thread.id))
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = strings.cdDeleteConversation,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatRelativeTime(instant: Instant, strings: com.nongtri.app.l10n.Strings): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = now - instant

    return when {
        duration < 1.days -> strings.today
        duration < 2.days -> strings.yesterday
        duration < 7.days -> "${duration.inWholeDays}${strings.daysAgo}"
        duration < 30.days -> "${duration.inWholeDays / 7}${strings.weeksAgo}"
        else -> "${duration.inWholeDays / 30}${strings.monthsAgo}"
    }
}
