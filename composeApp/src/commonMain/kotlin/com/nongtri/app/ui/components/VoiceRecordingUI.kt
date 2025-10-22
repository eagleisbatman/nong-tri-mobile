package com.nongtri.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Strings
import kotlinx.coroutines.delay

/**
 * Voice recording UI states
 * Simplified: Only Idle and Recording - no Preview UI
 * Transcription populates input box directly
 */
sealed class VoiceRecordingUIState {
    object Idle : VoiceRecordingUIState()
    data class Recording(val durationMs: Long = 0) : VoiceRecordingUIState()
}

/**
 * Beautiful Material Design 3 voice recording UI
 * Shown as a bottom sheet modal with smooth waveform visualization
 *
 * Flow:
 * 1. Tap mic → Bottom sheet slides up, recording starts
 * 2. Show centered waveform, timer, cancel (X) and send (↑) buttons
 * 3. Tap send → Transcribe in background, populate input box
 * 4. Tap cancel → Discard recording
 */
@Composable
fun VoiceRecordingUI(
    state: VoiceRecordingUIState,
    onStopRecording: () -> Unit,
    onCancel: () -> Unit = {},
    amplitude: Int = 0,
    strings: Strings,
    modifier: Modifier = Modifier
) {
    when (state) {
        is VoiceRecordingUIState.Recording -> {
            // Beautiful bottom sheet with gradient background
            Surface(
                modifier = modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                // Gradient background using theme colors
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Timer at top
                        Text(
                            text = formatDuration(state.durationMs),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Centered waveform visualization
                        RealTimeWaveform(
                            amplitude = amplitude,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        )

                        // Action buttons row: Cancel (X) and Send (↑)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cancel button
                            FilledTonalIconButton(
                                onClick = onCancel,
                                modifier = Modifier.size(64.dp).testTag(TestTags.VOICE_CANCEL_BUTTON),
                                colors = IconButtonDefaults.filledTonalIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = strings.cdCancelRecording,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            // Send button (stop and transcribe)
                            FilledIconButton(
                                onClick = onStopRecording,
                                modifier = Modifier.size(64.dp).testTag(TestTags.VOICE_SEND_BUTTON),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = strings.cdSendRecording,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        else -> {
            // Idle state - should not show anything
        }
    }
}

/**
 * Beautiful real-time waveform visualization - Material Design 3
 * Properly centered with smooth, rounded bars
 * Amplitude ranges from 0-32767 (MediaRecorder.getMaxAmplitude())
 *
 * Visual scaling: Uses square root for more dramatic height differences
 * - Silent (0-100): Very short bars (2-8dp)
 * - Quiet (100-1000): Short bars (8-20dp)
 * - Medium (1000-10000): Medium bars (20-45dp)
 * - Loud (10000+): Tall bars (45-64dp)
 */
@Composable
private fun RealTimeWaveform(
    amplitude: Int,
    modifier: Modifier = Modifier
) {
    // Keep rolling window of amplitude values (60 bars for smooth visualization)
    var amplitudes by remember { mutableStateOf(List(60) { 0 }) }

    LaunchedEffect(amplitude) {
        // Update the amplitude list - drop first, add new at end
        amplitudes = amplitudes.drop(1) + amplitude
    }

    // Properly centered container
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            amplitudes.forEachIndexed { index, amp ->
                // Use square root scaling for more dramatic height differences
                // This makes quiet sounds much shorter and loud sounds much taller
                val normalizedHeight = if (amp > 0) {
                    val sqrtAmp = kotlin.math.sqrt(amp.toFloat()) / kotlin.math.sqrt(32768f)
                    val maxHeight = 64f
                    val minHeight = 2f
                    (sqrtAmp * (maxHeight - minHeight) + minHeight).coerceIn(minHeight, maxHeight)
                } else {
                    2f // Very short bars when silent
                }

                // Smooth animation between height changes
                val animatedHeight by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = normalizedHeight,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessHigh
                    ),
                    label = "waveform_height_$index"
                )

                // Beautiful rounded bars - solid color, height shows amplitude
                Surface(
                    modifier = Modifier
                        .width(3.dp)
                        .height(animatedHeight.dp),
                    shape = RoundedCornerShape(1.5.dp),
                    color = MaterialTheme.colorScheme.primary  // Solid color, no alpha
                ) {}
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
