package com.nongtri.app.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Strings
import com.nongtri.app.ui.components.TestTags

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
    isTranscribing: Boolean = false,        // Show transcribing feedback
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().testTag(TestTags.INPUT_AREA),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Image button - hide when text is present to save space
            if (value.isBlank()) {
                IconButton(
                    onClick = onImageClick,
                    enabled = isEnabled,
                    modifier = Modifier.size(40.dp).testTag(TestTags.IMAGE_BUTTON)  // Smaller icon
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CameraAlt,
                        contentDescription = strings.attachImage,
                        modifier = Modifier.size(24.dp),
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }

            // Text input field - expandable based on content
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).testTag(TestTags.TEXT_FIELD),
                placeholder = {
                    Text(
                        text = if (isTranscribing) strings.transcribing else strings.typeMessage,
                        style = MaterialTheme.typography.bodyMedium  // Smaller font
                    )
                },
                trailingIcon = {
                    if (isTranscribing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                enabled = isEnabled && !isTranscribing,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp)  // Better line spacing
                ),
                minLines = 1,  // Start with 1 line
                maxLines = 4   // Reduced from 6 to 4 to prevent too-tall box
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
                    modifier = Modifier.size(44.dp).testTag(TestTags.SEND_BUTTON)  // Slightly smaller
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = strings.send,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Voice button - simple tap to start recording
                IconButton(
                    onClick = onVoiceClick,
                    enabled = isEnabled,
                    modifier = Modifier.size(44.dp).testTag(TestTags.VOICE_BUTTON)  // Slightly smaller
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = strings.cdVoiceInput,
                        modifier = Modifier.size(24.dp),
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}
