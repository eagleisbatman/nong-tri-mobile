package com.nongtri.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Voice recording UI states
 */
sealed class VoiceRecordingUIState {
    object Idle : VoiceRecordingUIState()
    data class Recording(val durationMs: Long = 0) : VoiceRecordingUIState()
    data class Preview(val durationMs: Long, val audioFilePath: String) : VoiceRecordingUIState()
}

/**
 * Complete voice recording UI that replaces the input bar
 *
 * Flow:
 * 1. Tap mic → Start recording immediately
 * 2. Show timer + stop button while recording
 * 3. Stop → Show preview with play, accept (✓), reject (✗)
 * 4. Accept → Transcribe and send
 * 5. Reject → Discard and restore input
 */
@Composable
fun VoiceRecordingUI(
    state: VoiceRecordingUIState,
    onStopRecording: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onPlayPause: () -> Unit,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        when (state) {
            is VoiceRecordingUIState.Recording -> {
                RecordingActiveUI(
                    durationMs = state.durationMs,
                    onStop = onStopRecording,
                    modifier = Modifier.padding(16.dp)
                )
            }
            is VoiceRecordingUIState.Preview -> {
                RecordingPreviewUI(
                    durationMs = state.durationMs,
                    isPlaying = isPlaying,
                    onPlayPause = onPlayPause,
                    onAccept = onAccept,
                    onReject = onReject,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                // Should never reach here - parent handles Idle state
            }
        }
    }
}

/**
 * UI shown while actively recording
 * Shows: Animated mic icon, waveform, timer, stop button
 */
@Composable
private fun RecordingActiveUI(
    durationMs: Long,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Animated recording indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pulsing red dot
                var isPulsing by remember { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    while (true) {
                        isPulsing = !isPulsing
                        delay(500)
                    }
                }

                Box(
                    modifier = Modifier.size(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(if (isPulsing) 12.dp else 10.dp)
                    ) {}
                }

                // Mic icon
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Recording",
                    tint = MaterialTheme.colorScheme.error
                )

                // Timer
                Text(
                    text = formatDuration(durationMs),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Stop button
            FilledTonalButton(
                onClick = onStop,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop recording"
                )
                Spacer(Modifier.width(8.dp))
                Text("Stop")
            }
        }

        // Waveform visualization
        AnimatedWaveform(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        )
    }
}

/**
 * Animated waveform visualization
 */
@Composable
private fun AnimatedWaveform(
    modifier: Modifier = Modifier
) {
    // Generate random waveform amplitudes
    var amplitudes by remember { mutableStateOf(List(40) { kotlin.random.Random.nextFloat() * 0.5f + 0.3f }) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100) // Update every 100ms
            amplitudes = amplitudes.drop(1) + kotlin.random.Random.nextFloat() * 0.5f + 0.3f
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        amplitudes.forEach { amplitude ->
            Surface(
                modifier = Modifier
                    .width(4.dp)
                    .height((amplitude * 48).dp),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.primary
            ) {}
        }
    }
}

/**
 * UI shown after recording stops
 * Shows: Play button, duration, accept (✓), reject (✗)
 */
@Composable
private fun RecordingPreviewUI(
    durationMs: Long,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Play/Pause button + duration
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onPlayPause) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column {
                Text(
                    text = "Voice message",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Accept / Reject buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reject (✗)
            FilledTonalIconButton(
                onClick = onReject,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Reject recording"
                )
            }

            // Accept (✓)
            FilledIconButton(
                onClick = onAccept,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Accept recording",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Format milliseconds to MM:SS
 */
private fun formatDuration(ms: Long): String {
    val seconds = (ms / 1000).toInt()
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return "%d:%02d".format(minutes, remainingSeconds)
}
