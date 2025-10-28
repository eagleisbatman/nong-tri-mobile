package com.nongtri.app.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.flow.StateFlow
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.MessageRole
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.Serializable
// Remove androidx.webkit imports - not available in all configurations

@Serializable
data class WebMessage(
    val id: String,
    val role: String,
    val content: String,
    val isLoading: Boolean,
    val followUpQuestions: List<String>
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun WebViewChat(
    messages: List<ChatMessage>,
    streamingContent: StateFlow<String>,
    onSendMessage: (String) -> Unit,
    onFollowUpClick: (String) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    val webView = remember { WebView(context) }
    val streamingText by streamingContent.collectAsState()

    // JavaScript interface for communication
    val jsInterface = remember {
        object {
            @JavascriptInterface
            fun sendMessage(message: String) {
                onSendMessage(message)
            }

            @JavascriptInterface
            fun onFollowUpClick(question: String) {
                onFollowUpClick(question)
            }
        }
    }

    DisposableEffect(webView) {
        webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true

            // Enable hardware acceleration for smooth scrolling
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            addJavascriptInterface(jsInterface, "AndroidBridge")

            webViewClient = WebViewClient()

            // Load the HTML content
            loadDataWithBaseURL(
                null,
                generateHtmlContent(),
                "text/html",
                "UTF-8",
                null
            )
        }

        onDispose {
            webView.destroy()
        }
    }

    // Update messages when they change (excluding streaming messages)
    LaunchedEffect(messages) {
        val nonStreamingMessages = messages.filter { !it.isLoading }
        val webMessages = nonStreamingMessages.map { msg ->
            WebMessage(
                id = msg.id,
                role = if (msg.role == MessageRole.USER) "user" else "assistant",
                content = msg.content,
                isLoading = msg.isLoading,
                followUpQuestions = msg.followUpQuestions
            )
        }
        val messagesJson = Json.encodeToString(webMessages)
        webView.evaluateJavascript("updateMessages($messagesJson)", null)
    }

    // Handle streaming content separately
    LaunchedEffect(streamingText) {
        if (streamingText.isNotEmpty()) {
            // Escape the content for JavaScript
            val escapedContent = streamingText
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")

            webView.evaluateJavascript(
                "updateStreamingContent(\"$escapedContent\")",
                null
            )
        } else {
            webView.evaluateJavascript("clearStreamingContent()", null)
        }
    }

    AndroidView(
        factory = { webView },
        modifier = modifier
    )
}

private fun generateHtmlContent(): String = """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: #1a1a1a;
            color: #ffffff;
            padding-bottom: 80px;
        }

        #messages {
            padding: 16px;
            min-height: 100vh;
        }

        .message {
            margin-bottom: 16px;
            opacity: 0;
            animation: fadeIn 0.3s ease-out forwards;
        }

        @keyframes fadeIn {
            to {
                opacity: 1;
            }
        }

        .message-header {
            font-size: 12px;
            color: #888;
            margin-bottom: 4px;
            font-weight: 600;
        }

        .message-content {
            padding: 12px 16px;
            border-radius: 12px;
            line-height: 1.5;
            white-space: pre-wrap;
            word-wrap: break-word;
        }

        .user .message-content {
            background: #2563eb;
            margin-left: 20%;
        }

        .assistant .message-content {
            background: #374151;
            margin-right: 20%;
        }

        #streaming-container {
            margin-bottom: 16px;
            min-height: 60px;
            display: none;
        }

        #streaming-container.active {
            display: block;
        }

        #streaming-content {
            padding: 12px 16px;
            background: #374151;
            border-radius: 12px;
            margin-right: 20%;
            line-height: 1.5;
            white-space: pre-wrap;
            word-wrap: break-word;
        }

        .follow-up-questions {
            margin-top: 12px;
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }

        .follow-up-chip {
            padding: 8px 16px;
            background: #4b5563;
            border-radius: 20px;
            cursor: pointer;
            transition: background 0.2s;
            border: 1px solid #6b7280;
        }

        .follow-up-chip:active {
            background: #6b7280;
        }

        #input-container {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            background: #1a1a1a;
            border-top: 1px solid #374151;
            padding: 12px;
            display: flex;
            gap: 8px;
        }

        #input-field {
            flex: 1;
            padding: 12px;
            background: #374151;
            border: none;
            border-radius: 24px;
            color: white;
            font-size: 16px;
            outline: none;
        }

        #send-button {
            padding: 12px 20px;
            background: #2563eb;
            color: white;
            border: none;
            border-radius: 24px;
            cursor: pointer;
            font-weight: 600;
        }

        #send-button:active {
            background: #1d4ed8;
        }

        /* Smooth scrolling */
        html {
            scroll-behavior: smooth;
        }

        /* Prevent layout shifts during streaming */
        .streaming-message {
            min-height: 40px;
            transition: none !important;
        }
    </style>
</head>
<body>
    <div id="messages"></div>
    <div id="streaming-container">
        <div class="message-header">Nông Trí AI</div>
        <div id="streaming-content"></div>
    </div>

    <div id="input-container">
        <input type="text" id="input-field" placeholder="Type a message..." />
        <button id="send-button">Send</button>
    </div>

    <script>
        let messages = [];
        let isStreaming = false;
        let streamingContent = '';

        function renderMessages() {
            const container = document.getElementById('messages');
            container.innerHTML = messages.map(msg => {
                const role = msg.role;
                const header = role === 'user' ? 'You' : 'Nông Trí AI';
                const content = escapeHtml(msg.content);
                const followUps = msg.followUpQuestions && msg.followUpQuestions.length > 0
                    ? renderFollowUps(msg.followUpQuestions)
                    : '';

                return '<div class="message ' + role + '">' +
                    '<div class="message-header">' + header + '</div>' +
                    '<div class="message-content">' + content + '</div>' +
                    followUps +
                    '</div>';
            }).join('');

            // Scroll to bottom smoothly
            window.scrollTo({
                top: document.body.scrollHeight,
                behavior: 'smooth'
            });
        }

        function renderFollowUps(questions) {
            return '<div class="follow-up-questions">' +
                questions.map(q => {
                    const escaped = escapeJs(q);
                    const html = escapeHtml(q);
                    return '<div class="follow-up-chip" onclick="handleFollowUp(\'' + escaped + '\')">' + html + '</div>';
                }).join('') +
                '</div>';
        }

        function updateMessages(newMessages) {
            messages = newMessages;
            renderMessages();
        }

        function updateStreamingContent(content) {
            isStreaming = true;
            streamingContent = content;

            const container = document.getElementById('streaming-container');
            const contentEl = document.getElementById('streaming-content');

            container.classList.add('active');
            contentEl.textContent = content;

            // Smooth scroll to show streaming content
            requestAnimationFrame(() => {
                window.scrollTo({
                    top: document.body.scrollHeight,
                    behavior: 'smooth'
                });
            });
        }

        function clearStreamingContent() {
            isStreaming = false;
            streamingContent = '';
            document.getElementById('streaming-container').classList.remove('active');
        }

        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        function escapeJs(text) {
            return text.replace(/'/g, "\\'").replace(/"/g, '\\"');
        }

        function handleFollowUp(question) {
            AndroidBridge.onFollowUpClick(question);
        }

        // Handle input
        document.getElementById('send-button').addEventListener('click', () => {
            const input = document.getElementById('input-field');
            const message = input.value.trim();
            if (message) {
                AndroidBridge.sendMessage(message);
                input.value = '';
            }
        });

        document.getElementById('input-field').addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                document.getElementById('send-button').click();
            }
        });
    </script>
</body>
</html>
""".trimIndent()