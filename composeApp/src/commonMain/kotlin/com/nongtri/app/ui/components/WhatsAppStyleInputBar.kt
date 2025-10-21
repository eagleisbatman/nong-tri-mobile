package com.nongtri.app.ui.components

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Strings
import kotlinx.coroutines.withTimeout

@Composable
fun WhatsAppStyleInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onImageClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onVoiceLongPress: () -> Unit = {},      // Start recording
    onVoiceRelease: () -> Unit = {},        // Stop recording and send
    onVoiceCancel: () -> Unit = {},         // Cancel recording (dragged off button)
    strings: Strings,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image button
            IconButton(
                onClick = onImageClick,
                enabled = isEnabled
            ) {
                Icon(
                    imageVector = Icons.Outlined.CameraAlt,
                    contentDescription = "Attach image",
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary
                          else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }

            // Text input field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = strings.typeMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                enabled = isEnabled,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 5
            )

            // Send or Voice button
            if (value.isNotBlank()) {
                // Send button
                FilledIconButton(
                    onClick = onSend,
                    enabled = isEnabled,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = strings.send,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Voice button with hold-to-record (WhatsApp style)
                // Proper gesture handling: tap for hint, long-press to record, drag to cancel
                VoiceRecordButton(
                    onTap = onVoiceClick,
                    onLongPressStart = onVoiceLongPress,
                    onLongPressEnd = onVoiceRelease,
                    onCancel = onVoiceCancel,
                    isEnabled = isEnabled
                )
            }
        }
    }
}

/**
 * Voice recording button with WhatsApp-style gesture handling
 * - Quick tap: Show hint
 * - Long press: Start recording
 * - Release while inside: Send recording
 * - Drag outside (>100dp): Cancel recording
 */
@Composable
private fun VoiceRecordButton(
    onTap: () -> Unit,
    onLongPressStart: () -> Unit,
    onLongPressEnd: () -> Unit,
    onCancel: () -> Unit,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    var isRecording by remember { mutableStateOf(false) }
    var initialPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(48.dp)
            .pointerInput(Unit) {
                val longPressTimeout = 500L // WhatsApp uses ~500ms
                val cancelThresholdPx = 100.dp.toPx() // Drag >100dp to cancel

                awaitEachGesture {
                    // Wait for initial press
                    val down = awaitFirstDown(requireUnconditional = false)
                    initialPosition = down.position

                    // Start long press timeout
                    var longPressTriggered = false

                    try {
                        // Wait for long press timeout OR pointer up/cancel
                        withTimeout(longPressTimeout) {
                            waitForUpOrCancellation()
                        }

                        // If we get here, user released BEFORE long press timeout
                        // This is a quick tap - show hint
                        if (!longPressTriggered && !isRecording) {
                            onTap()
                        }

                    } catch (e: PointerEventTimeoutCancellationException) {
                        // Long press timeout reached - start recording
                        longPressTriggered = true
                        isRecording = true
                        onLongPressStart()

                        // Now track pointer movement for slide-to-cancel
                        val upOrCancel = waitForUpOrCancellation()

                        if (upOrCancel != null) {
                            // Pointer was released - check if it's inside or outside cancel threshold
                            val currentPosition = upOrCancel.position
                            val dragDistance = (initialPosition - currentPosition).getDistance()

                            if (dragDistance > cancelThresholdPx) {
                                // Dragged too far - cancel recording
                                println("[VoiceButton] Cancelled - dragged ${dragDistance}px")
                                onCancel()
                            } else {
                                // Released inside threshold - send recording
                                println("[VoiceButton] Released - sending recording")
                                onLongPressEnd()
                            }
                        } else {
                            // Gesture was cancelled (e.g., another pointer down)
                            println("[VoiceButton] Gesture cancelled")
                            onCancel()
                        }

                        isRecording = false
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Mic,
            contentDescription = "Voice input (hold to record)",
            tint = if (isEnabled) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
    }
}
