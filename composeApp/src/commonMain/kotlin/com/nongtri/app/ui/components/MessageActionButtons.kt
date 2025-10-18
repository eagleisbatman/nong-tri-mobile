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
import com.nongtri.app.l10n.Language
import com.nongtri.app.platform.LocalShareManager
import com.nongtri.app.platform.LocalTextToSpeechManager
import kotlinx.coroutines.launch

@Composable
fun MessageActionButtons(
    messageContent: String,
    language: Language,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onListen: () -> Unit,
    onFeedback: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val shareManager = LocalShareManager.current
    val ttsManager = LocalTextToSpeechManager.current
    val coroutineScope = rememberCoroutineScope()
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    var feedbackGiven by remember { mutableStateOf<Boolean?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp, start = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Copy button
        IconButton(
            onClick = {
                clipboardManager.setText(AnnotatedString(messageContent))
                showCopiedSnackbar = true
                onCopy()
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.ContentCopy,
                contentDescription = "Copy",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // Share button
        IconButton(
            onClick = { showShareSheet = true },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // Listen button (TTS with OpenAI)
        IconButton(
            onClick = {
                coroutineScope.launch {
                    if (ttsManager.isSpeaking()) {
                        ttsManager.stop()
                    } else {
                        // Use OpenAI TTS with tone control
                        ttsManager.speak(
                            text = messageContent,
                            language = language.code,
                            voice = "alloy", // Options: alloy, echo, fable, onyx, nova, shimmer
                            tone = "friendly" // friendly, professional, empathetic, excited, calm, neutral
                        )
                    }
                    onListen()
                }
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.VolumeUp,
                contentDescription = "Listen",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Thumbs up
        IconButton(
            onClick = {
                feedbackGiven = true
                onFeedback(true)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (feedbackGiven == true) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp,
                contentDescription = "Good response",
                tint = if (feedbackGiven == true) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }

        // Thumbs down
        IconButton(
            onClick = {
                feedbackGiven = false
                onFeedback(false)
            },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (feedbackGiven == false) Icons.Filled.ThumbDown else Icons.Outlined.ThumbDown,
                contentDescription = "Bad response",
                tint = if (feedbackGiven == false) MaterialTheme.colorScheme.error
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
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
                shareManager.shareText(
                    text = messageContent,
                    title = "Share AI Response"
                )
                onShare()
            },
            onShareAsImage = {
                // TODO: Implement screenshot capture + share
                // This will require composable screenshot functionality
                showShareSheet = false
            }
        )
    }
}
