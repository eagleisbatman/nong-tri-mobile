package com.nongtri.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import com.nongtri.app.data.model.ChatMessage

@Composable
expect fun WebViewChat(
    messages: List<ChatMessage>,
    streamingContent: StateFlow<String>,
    onSendMessage: (String) -> Unit,
    onFollowUpClick: (String) -> Unit,
    modifier: Modifier = Modifier
)