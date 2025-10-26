package com.nongtri.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.HealthStatusColor
import com.nongtri.app.data.model.getHealthStatusColor
import com.nongtri.app.data.model.getSeverityIcon
import com.nongtri.app.ui.components.TestTags
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Message bubble for AI diagnosis responses
 * Matches the clean MessageBubble style - no purple cards, just plain text
 * Shows diagnosis data inline within the message content
 */
@Composable
fun DiagnosisResponseBubble(
    message: ChatMessage,
    strings: com.nongtri.app.l10n.Strings,
    onTtsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // ROUND 6: Track diagnosis result read when displayed
    val displayStartTime = remember { System.currentTimeMillis() }
    LaunchedEffect(message.diagnosisData) {
        if (message.diagnosisData != null) {
            val readTimeMs = System.currentTimeMillis() - displayStartTime
            com.nongtri.app.analytics.Events.logDiagnosisResultRead(
                jobId = message.diagnosisPendingJobId ?: message.id,
                readTimeMs = readTimeMs,
                scrollPercent = 100
            )
        }
    }

    // Entrance animation (same as MessageBubble)
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
                .scale(scale),
            horizontalArrangement = Arrangement.Start
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(min = 100.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Sender label and timestamp at top (same as MessageBubble)
                Row(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.aiLabel,
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

                // Message content (no bubble, just text - same as MessageBubble)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Show diagnosis summary inline (if available)
                    message.diagnosisData?.let { diagnosis ->
                        // Crop info
                        diagnosis.crop?.let { crop ->
                            Text(
                                text = "🌱 ${crop.nameVi} (${crop.nameEn})",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            crop.scientificName?.let { scientific ->
                                Text(
                                    text = scientific,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        // Health status
                        val healthColor = when (diagnosis.getHealthStatusColor()) {
                            HealthStatusColor.GREEN -> Color(0xFF4CAF50)
                            HealthStatusColor.YELLOW -> Color(0xFFFFC107)
                            HealthStatusColor.ORANGE -> Color(0xFFFF9800)
                            HealthStatusColor.RED -> Color(0xFFF44336)
                            HealthStatusColor.GRAY -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Text(
                            text = "💚 Tình trạng: ${diagnosis.healthStatus}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = healthColor,
                            fontWeight = FontWeight.Medium
                        )

                        // Issues (if any)
                        if (diagnosis.issues.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Vấn đề phát hiện:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            diagnosis.issues.forEach { issue ->
                                Text(
                                    text = "• ${issue.name} (${issue.category}, mức độ: ${issue.severity})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Vietnamese advice (markdown format, same as regular AI messages)
                    if (message.content.isNotBlank()) {
                        MarkdownText(
                            text = message.content,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    } else if (message.isLoading) {
                        // Loading indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = strings.analyzingPlantHealth,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // Action buttons (same as MessageBubble for AI messages)
                    if (!message.isLoading && message.content.isNotEmpty()) {
                        MessageActionButtons(
                            messageContent = message.content,
                            language = com.nongtri.app.l10n.Language.VIETNAMESE,  // Diagnosis always in Vietnamese
                            strings = strings,
                            isGenericResponse = false,
                            cachedAudioUrl = message.audioUrl,
                            onCopy = { },
                            onShare = { },
                            onListen = { },
                            onFeedback = { },
                            onAudioUrlCached = { audioUrl ->
                                // ROUND 6: Track diagnosis advice TTS played
                                com.nongtri.app.analytics.Events.logDiagnosisAdviceTtsPlayed(
                                    jobId = message.diagnosisPendingJobId ?: message.id,
                                    adviceLength = message.content.length
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Format timestamp to HH:mm format (same as MessageBubble)
 */
private fun formatTimestamp(timestamp: Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = localDateTime.hour
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}
