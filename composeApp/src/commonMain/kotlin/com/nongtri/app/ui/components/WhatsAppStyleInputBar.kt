package com.nongtri.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Strings

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
                // Long press to start, release to send, slide away to cancel
                var isRecording by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    // Simple tap - do nothing, just vibrate or show hint
                                    onVoiceClick()
                                },
                                onLongPress = {
                                    // Start recording on long press
                                    isRecording = true
                                    onVoiceLongPress()
                                },
                                onPress = {
                                    // Wait for release
                                    val release = tryAwaitRelease()

                                    // Only process release if we were actually recording
                                    if (isRecording) {
                                        if (release) {
                                            // Released inside button - send voice message
                                            onVoiceRelease()
                                        } else {
                                            // Dragged outside button - cancel recording
                                            onVoiceCancel()
                                        }
                                        isRecording = false
                                    }
                                }
                            )
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
        }
    }
}
