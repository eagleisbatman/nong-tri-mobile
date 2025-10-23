package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nongtri.app.l10n.Language
import com.nongtri.app.l10n.LocalizationProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoicePermissionBottomSheet(
    shouldShowSettings: Boolean = false,  // Show "Open Settings" instead of "Grant Permission"
    onRequestPermission: () -> Unit,
    onDismiss: () -> Unit,
    language: Language,
    modifier: Modifier = Modifier
) {
    val strings = LocalizationProvider.getStrings(language)

    // BATCH 2: Track bottom sheet opened
    LaunchedEffect(Unit) {
        val trigger = if (shouldShowSettings) "settings_prompt" else "rationale"
        com.nongtri.app.analytics.Events.logPermissionBottomSheetOpened(
            permissionType = "voice",
            trigger = trigger
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            // BATCH 2: Track dismiss (user swipes down or taps outside)
            com.nongtri.app.analytics.Events.logPermissionDenyButtonClicked("voice")
            onDismiss()
        },
        modifier = modifier.testTag(TestTags.VOICE_PERMISSION_SHEET),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.microphonePermission,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        // BATCH 2: Track close button (explicit deny)
                        com.nongtri.app.analytics.Events.logPermissionDenyButtonClicked("voice")
                        onDismiss()
                    },
                    modifier = Modifier.testTag(TestTags.CLOSE_BUTTON)
                ) {
                    Icon(Icons.Default.Close, contentDescription = strings.cdClose)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = strings.voiceRecording,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (shouldShowSettings) {
                            strings.microphonePermissionSettingsPrompt
                        } else {
                            strings.microphonePermissionPrompt
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            // BATCH 2: Track button click
                            if (shouldShowSettings) {
                                com.nongtri.app.analytics.Events.logPermissionOpenSettingsClicked("voice")
                            } else {
                                com.nongtri.app.analytics.Events.logPermissionAllowButtonClicked("voice")
                            }
                            onRequestPermission()
                        },
                        modifier = Modifier.fillMaxWidth().testTag(TestTags.GRANT_PERMISSION_BUTTON),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (shouldShowSettings) Icons.Default.Settings else Icons.Default.Mic,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (shouldShowSettings) strings.openSettings else strings.grantPermission)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Info text
            Text(
                text = strings.voiceMessageInfoText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
