package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
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
import com.nongtri.app.platform.TtsState
import kotlinx.coroutines.launch

@Composable
fun MessageActionButtons(
    messageContent: String,
    language: Language,
    isGenericResponse: Boolean = false,  // true = greeting/casual, false = agricultural advice
    cachedAudioUrl: String? = null,  // Cached TTS audio URL
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onListen: () -> Unit,
    onFeedback: (Boolean) -> Unit,
    onAudioUrlCached: (String) -> Unit = {},  // Callback when audio URL is generated
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val shareManager = LocalShareManager.current
    val ttsManager = LocalTextToSpeechManager.current
    val ttsState by ttsManager.state.collectAsState()
    val voicePlayer = com.nongtri.app.platform.LocalVoiceMessagePlayer.current
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
        // For generic responses (greetings, casual): ONLY show Listen button
        // For agricultural responses: Show ALL buttons (Copy, Share, Listen, Feedback)

        if (!isGenericResponse) {
            // Copy button (only for agricultural responses)
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

            // Share button (only for agricultural responses)
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
        }

        // Listen button (TTS) - ALWAYS shown for all responses
        IconButton(
            onClick = {
                coroutineScope.launch {
                    when (ttsState) {
                        TtsState.PLAYING -> {
                            // Pause playback
                            ttsManager.pause()
                        }
                        TtsState.PAUSED -> {
                            // Resume playback from paused position
                            ttsManager.resume()
                        }
                        TtsState.IDLE, TtsState.ERROR -> {
                            // Stop voice message player before starting TTS
                            voicePlayer.stop()

                            // Start new playback with caching
                            val audioUrl = ttsManager.speak(
                                text = messageContent,
                                language = language.code,
                                voice = "alloy", // Options: alloy, echo, fable, onyx, nova, shimmer
                                tone = "friendly", // friendly, professional, empathetic, excited, calm, neutral
                                cachedAudioUrl = cachedAudioUrl  // Use cached if available
                            )
                            // Cache the audio URL for future use
                            audioUrl?.let { onAudioUrlCached(it) }
                        }
                        TtsState.LOADING -> {
                            // Do nothing while loading
                        }
                    }
                    onListen()
                }
            },
            enabled = ttsState != TtsState.LOADING,
            modifier = Modifier.size(32.dp)
        ) {
            when (ttsState) {
                TtsState.LOADING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TtsState.PLAYING -> {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = "Pause",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                TtsState.PAUSED -> {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Resume",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                TtsState.ERROR -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = "Listen (Error - tap to retry)",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                TtsState.IDLE -> {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.VolumeUp,
                        contentDescription = "Listen",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (!isGenericResponse) {
            Spacer(modifier = Modifier.weight(1f))

            // Thumbs up (only for agricultural responses)
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

            // Thumbs down (only for agricultural responses)
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
