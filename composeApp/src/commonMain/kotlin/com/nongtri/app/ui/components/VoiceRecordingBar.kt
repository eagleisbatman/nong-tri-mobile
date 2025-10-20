package com.nongtri.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nongtri.app.ui.viewmodel.VoiceRecordingState
import kotlin.math.absoluteValue

/**
 * WhatsApp-style voice recording bar
 * Shows waveform animation and slide-to-cancel gesture
 */
@Composable
fun VoiceRecordingBar(
    recordingState: VoiceRecordingState,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableStateOf(0f) }
    val cancelThreshold = -150f // Slide left 150dp to cancel

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        tonalElevation = 3.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offsetX < cancelThreshold) {
                                onCancel()
                            }
                            offsetX = 0f
                        },
                        onDragCancel = {
                            offsetX = 0f
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            // Only allow left drag
                            if (dragAmount < 0 || offsetX < 0) {
                                offsetX = (offsetX + dragAmount).coerceIn(cancelThreshold * 2, 0f)
                            }
                        }
                    )
                }
        ) {
            when (recordingState) {
                is VoiceRecordingState.Recording -> {
                    RecordingContent(
                        durationMs = recordingState.durationMs,
                        waveformAmplitudes = recordingState.waveformAmplitudes,
                        offsetX = offsetX
                    )
                }
                is VoiceRecordingState.Transcribing -> {
                    TranscribingContent()
                }
                is VoiceRecordingState.Error -> {
                    ErrorContent(message = recordingState.message)
                }
                else -> {
                    // Should not happen, but show placeholder
                    Text(
                        text = "Recording...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Cancel indicator (shows when dragging left)
            if (offsetX < 0) {
                val alpha = (offsetX.absoluteValue / cancelThreshold.absoluteValue).coerceIn(0f, 1f)
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .alpha(alpha),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordingContent(
    durationMs: Long,
    waveformAmplitudes: List<Float>,
    offsetX: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = offsetX.dp / 3), // Dampen the visual offset
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pulsing red dot
        PulsingRecordingIndicator()

        // Recording duration
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / 1000) / 60
        Text(
            text = String.format("%d:%02d", minutes, seconds),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )

        // Waveform animation
        Waveform(
            amplitudes = waveformAmplitudes,
            modifier = Modifier.weight(1f)
        )

        // Swipe left hint
        if (offsetX == 0f) {
            Text(
                text = "< Slide to cancel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun PulsingRecordingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.error.copy(alpha = alpha))
    )
}

@Composable
private fun Waveform(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(32.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.takeLast(20).forEach { amplitude ->
            val height = (amplitude * 32).coerceIn(4f, 32f)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onErrorContainer)
            )
        }
    }
}

@Composable
private fun TranscribingContent() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Text(
            text = "Transcribing...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
    }
}
