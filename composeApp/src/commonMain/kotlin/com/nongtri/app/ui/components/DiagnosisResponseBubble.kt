package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.HealthStatusColor
import com.nongtri.app.data.model.getHealthStatusColor
import com.nongtri.app.data.model.getSeverityIcon
import com.nongtri.app.ui.components.TestTags

/**
 * Message bubble for AI diagnosis responses
 * Shows diagnosis summary card with crop info, health status, and issues
 * Displays full Vietnamese treatment advice below
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
                jobId = message.diagnosisPendingJobId ?: message.id,  // Use pending job ID or message ID
                readTimeMs = readTimeMs,
                scrollPercent = 100  // Assume full view (no scroll tracking yet)
            )
        }
    }

    Column(
        modifier = modifier
            .widthIn(max = 320.dp)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Diagnosis summary card (if diagnosis data available)
        if (message.diagnosisData != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Crop identification
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Eco,
                            contentDescription = strings.cropLabel,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${message.diagnosisData.crop.nameVi} (${message.diagnosisData.crop.nameEn})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            message.diagnosisData.crop.scientificName?.let { scientific ->
                                Text(
                                    text = scientific,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Health status with color coding
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val healthColor = when (message.diagnosisData.getHealthStatusColor()) {
                            HealthStatusColor.GREEN -> Color(0xFF4CAF50)
                            HealthStatusColor.YELLOW -> Color(0xFFFFC107)
                            HealthStatusColor.ORANGE -> Color(0xFFFF9800)
                            HealthStatusColor.RED -> Color(0xFFF44336)
                            HealthStatusColor.GRAY -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Icon(
                            Icons.Default.HealthAndSafety,
                            contentDescription = strings.healthStatus,
                            tint = healthColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = message.diagnosisData.healthStatus,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = healthColor
                        )
                    }

                    // Issues list (if any)
                    if (message.diagnosisData.issues.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = strings.detectedIssues,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        message.diagnosisData.issues.forEach { issue ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = issue.getSeverityIcon(),
                                    modifier = Modifier.width(24.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = issue.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "${issue.category}${strings.severityLabel}${issue.severity}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    if (issue.affectedParts.isNotEmpty()) {
                                        Text(
                                            text = "${strings.affectedLabel}${issue.affectedParts.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Growth stage (if available)
                    message.diagnosisData.growthStage?.let { stage ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${strings.growthStage}$stage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Full advice text (Vietnamese treatment recommendations)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Advice content
                if (message.content.isNotBlank()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else if (message.isLoading) {
                    // Show loading indicator during streaming
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = strings.analyzingPlantHealth,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // TTS button (if content available and TTS handler provided)
                if (message.content.isNotBlank() && onTtsClick != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = {
                            // ROUND 6: Track diagnosis advice TTS played
                            com.nongtri.app.analytics.Events.logDiagnosisAdviceTtsPlayed(
                                jobId = message.diagnosisPendingJobId ?: message.id,  // Use pending job ID or message ID
                                adviceLength = message.content.length
                            )
                            onTtsClick()
                        },
                        modifier = Modifier.fillMaxWidth().testTag(TestTags.DIAGNOSIS_TTS_BUTTON)
                    ) {
                        Icon(
                            Icons.Default.VolumeUp,
                            contentDescription = strings.listenToAdvice,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.listenToAdvice)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Timestamp
        Text(
            text = formatTimestamp(message.timestamp, strings),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Format timestamp for message display with localized strings
 */
private fun formatTimestamp(timestamp: kotlinx.datetime.Instant, strings: com.nongtri.app.l10n.Strings): String {
    val now = kotlinx.datetime.Clock.System.now()
    val duration = now - timestamp

    return when {
        duration.inWholeMinutes < 1 -> strings.justNow
        duration.inWholeMinutes < 60 -> "${duration.inWholeMinutes}${strings.minutesAgo}"
        duration.inWholeHours < 24 -> "${duration.inWholeHours}${strings.hoursAgo}"
        else -> {
            val local = timestamp.toString()
            local.substring(0, 16).replace("T", " ")
        }
    }
}
