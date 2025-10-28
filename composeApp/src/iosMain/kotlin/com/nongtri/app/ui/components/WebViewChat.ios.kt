package com.nongtri.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.StateFlow
import com.nongtri.app.data.model.ChatMessage

@Composable
actual fun WebViewChat(
    messages: List<ChatMessage>,
    streamingContent: StateFlow<String>,
    onSendMessage: (String) -> Unit,
    onFollowUpClick: (String) -> Unit,
    modifier: Modifier
) {
    // iOS implementation would use WKWebView
    // For now, show a placeholder
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("WebView not implemented for iOS")
    }
}