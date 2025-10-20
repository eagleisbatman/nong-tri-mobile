package com.nongtri.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.MessageRole
import com.nongtri.app.ui.theme.DarkColors
import com.nongtri.app.ui.theme.LightColors
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    index: Int,
    isLightTheme: Boolean,
    language: com.nongtri.app.l10n.Language,
    onFeedback: (Int?, Boolean) -> Unit = { _, _ -> },
    onFollowUpClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isUser = message.role == MessageRole.USER

    // Entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(message.id) {
        visible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)) +
                slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(300)
                ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .scale(scale)
                .testTag(TestTags.messageBubble(index)),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 100.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Sender label and timestamp at top
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUser) "You" else "Nông Trí AI",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimestamp(message.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Message content (no bubble, just text)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Use markdown rendering for AI messages, plain text for user messages
                    if (isUser) {
                        Text(
                            text = message.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag(TestTags.messageText(index))
                        )
                    } else {
                        // Render markdown for AI responses
                        MarkdownText(
                            text = message.content,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag(TestTags.messageText(index))
                        )

                        // Action buttons for AI messages (only show when message is complete)
                        if (!message.isLoading && message.content.isNotEmpty()) {
                            MessageActionButtons(
                                messageContent = message.content,
                                language = language,
                                isGenericResponse = message.isGenericResponse,
                                onCopy = { },
                                onShare = { },
                                onListen = { },
                                onFeedback = { isPositive ->
                                    onFeedback(message.conversationId, isPositive)
                                }
                            )
                        }

                        // Follow-up question chips (only for agricultural responses with questions)
                        if (!message.isLoading && !message.isGenericResponse && message.followUpQuestions.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                // Section header
                                Text(
                                    text = "💡 What's next?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    message.followUpQuestions.forEach { question ->
                                        SuggestionChip(
                                            onClick = { onFollowUpClick(question) },
                                            label = {
                                                Text(
                                                    text = question,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

@Composable
fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(TestTags.LOADING_INDICATOR),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    TypingDot(
                        delay = index * 200
                    )
                }
            }
        }
    }
}

@Composable
private fun TypingDot(
    delay: Int,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delay),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .scale(scale)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
    )
}
