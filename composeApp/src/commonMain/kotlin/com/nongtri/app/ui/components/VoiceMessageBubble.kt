package com.nongtri.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Voice message bubble with playback controls
 * Displays user voice recordings with waveform and play/pause button
 */
@Composable
fun VoiceMessageBubble(
    voiceAudioUrl: String?,
    transcription: String,
    isPlaying: Boolean = false,
    currentPosition: Float = 0f,  // 0.0 to 1.0
    duration: Int = 0,  // seconds
    strings: com.nongtri.app.l10n.Strings,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play/Pause button
                IconButton(
                    onClick = onPlayPause,
                    enabled = voiceAudioUrl != null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag(TestTags.VOICE_PLAY_BUTTON)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) strings.cdPause else strings.cdPlay,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Waveform or progress bar
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Simple waveform visualization (static bars)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 20 bars with varying heights for waveform effect
                        val barHeights = listOf(
                            0.4f, 0.6f, 0.8f, 0.5f, 0.7f, 0.9f, 0.6f, 0.4f, 0.7f, 0.8f,
                            0.5f, 0.6f, 0.9f, 0.7f, 0.5f, 0.8f, 0.6f, 0.4f, 0.7f, 0.5f
                        )

                        barHeights.forEachIndexed { index, height ->
                            val isPlayed = currentPosition * barHeights.size > index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(height)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        if (isPlayed) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                        }
                                    )
                            )
                        }
                    }

                    // Duration text
                    if (duration > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatDuration(duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Transcription text (collapsible)
            if (transcription.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                // ✅ Show loading state when transcription is placeholder
                if (transcription == "...") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = strings.transcribing,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Text(
                        text = transcription,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Error state if no audio URL
            if (voiceAudioUrl == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = strings.audioNotAvailable,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * Format duration in seconds to MM:SS format
 */
private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", mins, secs)
}
