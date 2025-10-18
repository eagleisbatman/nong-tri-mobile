package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Strings

@Composable
fun WhatsAppStyleInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onImageClick: () -> Unit,
    onVoiceClick: () -> Unit,
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
                Text(
                    text = "📷",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isEnabled) MaterialTheme.colorScheme.primary
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
                        imageVector = Icons.Default.Send,
                        contentDescription = strings.send,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Voice button
                IconButton(
                    onClick = onVoiceClick,
                    enabled = isEnabled
                ) {
                    Text(
                        text = "🎤",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary
                              else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
        }
    }
}
