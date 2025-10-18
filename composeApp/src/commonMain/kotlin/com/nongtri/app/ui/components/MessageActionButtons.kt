package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

@Composable
fun MessageActionButtons(
    messageContent: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onListen: () -> Unit,
    onFeedback: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copy button
        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(messageContent))
                showCopiedSnackbar = true
                onCopy()
            }
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Share button
        IconButton(
            onClick = { showShareSheet = true }
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Listen button (TTS)
        IconButton(
            onClick = onListen
        ) {
            Icon(
                imageVector = Icons.Outlined.VolumeUp,
                contentDescription = "Listen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Thumbs up
        IconButton(
            onClick = {
                feedbackGiven = true
                onFeedback(true)
            }
        ) {
            Icon(
                imageVector = if (feedbackGiven == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = "Good response",
                tint = if (feedbackGiven == true) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Thumbs down
        IconButton(
            onClick = {
                feedbackGiven = false
                onFeedback(false)
            }
        ) {
            Icon(
                imageVector = if (feedbackGiven == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = "Bad response",
                tint = if (feedbackGiven == false) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    // Show copied confirmation
    if (showCopiedSnackbar) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopiedSnackbar = false
        }
    }

    // Share bottom sheet
    if (showShareSheet) {
        ShareBottomSheet(
            messageContent = messageContent,
            onDismiss = { showShareSheet = false },
            onShareAsText = {
                onShare()
                // TODO: Implement Android share intent for text
            },
            onShareAsImage = {
                // TODO: Implement screenshot + share
            }
        )
    }
}
