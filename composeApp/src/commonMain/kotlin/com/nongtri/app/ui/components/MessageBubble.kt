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
    onAudioUrlCached: (String, String) -> Unit = { _, _ -> },  // (messageId, audioUrl)
    modifier: Modifier = Modifier
) {
    val strings = com.nongtri.app.l10n.LocalizationProvider.getStrings(language)
    val isUser = message.role == MessageRole.USER

    // Voice message player for user voice recordings
    val voicePlayer = com.nongtri.app.platform.LocalVoiceMessagePlayer.current
    val voicePlayerCurrentUrl by voicePlayer.currentUrl.collectAsState()
    val voicePlayerIsPlaying by voicePlayer.isPlaying.collectAsState()
    val voicePlayerDuration by voicePlayer.duration.collectAsState()
    val voicePlayerPosition by voicePlayer.position.collectAsState()

    // Only show as playing if THIS message's URL is the one currently playing
    val isThisMessagePlaying = voicePlayerCurrentUrl == message.voiceAudioUrl && voicePlayerIsPlaying

    // TTS manager for stopping TTS when voice message plays
    val ttsManager = com.nongtri.app.platform.LocalTextToSpeechManager.current

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
        enter = fadeIn(animationSpec = tween(100)),  // Removed slideIn - causes flickering during streaming
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
                        text = if (isUser) strings.userLabel else strings.aiLabel,
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
                        // Check if this is a voice message
                        if (message.messageType == "voice") {
                            // Only show position/duration for THIS message when it's playing
                            val positionPercent = if (isThisMessagePlaying && voicePlayerDuration > 0) {
                                voicePlayerPosition.toFloat() / voicePlayerDuration
                            } else 0f

                            val displayDuration = if (isThisMessagePlaying) {
                                voicePlayerDuration / 1000  // Convert ms to seconds
                            } else 0

                            VoiceMessageBubble(
                                voiceAudioUrl = message.voiceAudioUrl,
                                transcription = message.voiceTranscription ?: message.content,
                                isPlaying = isThisMessagePlaying,
                                currentPosition = positionPercent,
                                duration = displayDuration,
                                strings = strings,
                                onPlayPause = {
                                    message.voiceAudioUrl?.let { url ->
                                        if (isThisMessagePlaying) {
                                            voicePlayer.pause()
                                        } else {
                                            // Stop TTS before playing voice message
                                            ttsManager.stop()
                                            voicePlayer.play(url)
                                        }
                                    }
                                },
                                modifier = Modifier.testTag(TestTags.messageText(index))
                            )
                        } else {
                            // Regular text message
                            Text(
                                text = message.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.testTag(TestTags.messageText(index))
                            )
                        }
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
                                strings = strings,
                                isGenericResponse = message.isGenericResponse,
                                cachedAudioUrl = message.audioUrl,
                                onCopy = { },
                                onShare = { },
                                onListen = { },
                                onFeedback = { isPositive ->
                                    onFeedback(message.conversationId, isPositive)
                                },
                                onAudioUrlCached = { audioUrl ->
                                    onAudioUrlCached(message.id, audioUrl)
                                }
                            )
                        }

                        // Follow-up question chips (only for agricultural responses with questions)
                        if (!message.isLoading && !message.isGenericResponse && message.followUpQuestions.isNotEmpty()) {
                            // ROUND 9: Track follow-up questions displayed
                            LaunchedEffect(message.id, message.followUpQuestions.size) {
                                com.nongtri.app.analytics.Events.logFollowUpQuestionsDisplayed(
                                    count = message.followUpQuestions.size,
                                    messageIndex = 0 // Message index not available in this context
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp)
                            ) {
                                // Section header
                                Text(
                                    text = strings.followUpSectionHeader,
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
                                    message.followUpQuestions.forEachIndexed { questionIndex, question ->
                                        SuggestionChip(
                                            onClick = {
                                                // ROUND 9: Track follow-up question clicked
                                                com.nongtri.app.analytics.Events.logFollowUpQuestionClicked(
                                                    questionIndex = questionIndex,
                                                    questionText = question,
                                                    messageIndex = 0 // Message index not available in this context
                                                )
                                                onFollowUpClick(question)
                                            },
                                            label = {
                                                Text(
                                                    text = question,
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                                                )
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag(TestTags.followUpQuestion(questionIndex))
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
