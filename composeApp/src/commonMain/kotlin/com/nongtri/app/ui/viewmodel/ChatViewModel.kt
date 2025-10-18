package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.MessageRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentMessage: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
    private val api: NongTriApi = NongTriApi(),
    private val userId: String = Uuid.random().toString() // Will be replaced with device ID
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        // Clear input field
        _uiState.update { it.copy(currentMessage = "") }

        // Add user message
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = message.trim(),
            timestamp = Clock.System.now()
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                isLoading = true,
                error = null
            )
        }

        // Create placeholder for streaming assistant message
        val assistantMessageId = Uuid.random().toString()
        val initialAssistantMessage = ChatMessage(
            id = assistantMessageId,
            role = MessageRole.ASSISTANT,
            content = "",
            timestamp = Clock.System.now()
        )

        _uiState.update { state ->
            state.copy(messages = state.messages + initialAssistantMessage)
        }

        // Send to API with streaming
        viewModelScope.launch {
            api.sendMessageStream(
                userId = userId,
                message = message,
                onChunk = { chunk ->
                    // Update the assistant message with each chunk
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(content = msg.content + chunk)
                                } else {
                                    msg
                                }
                            }
                        )
                    }
                }
            ).fold(
                onSuccess = { fullResponse ->
                    // Mark loading as complete
                    _uiState.update { state ->
                        state.copy(isLoading = false)
                    }
                },
                onFailure = { error ->
                    // Remove the placeholder message and show error
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.filter { it.id != assistantMessageId },
                            isLoading = false,
                            error = error.message ?: "Failed to send message"
                        )
                    }
                }
            )
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            api.getHistory(userId).fold(
                onSuccess = { response ->
                    if (response.success) {
                        val messages = response.history.map { item ->
                            ChatMessage(
                                role = when (item.role) {
                                    "user" -> MessageRole.USER
                                    "assistant" -> MessageRole.ASSISTANT
                                    else -> MessageRole.SYSTEM
                                },
                                content = item.content,
                                timestamp = Clock.System.now() // TODO: Parse actual timestamp
                            )
                        }
                        _uiState.update { it.copy(messages = messages) }
                    }
                },
                onFailure = { /* Silently fail for history */ }
            )
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            api.clearHistory(userId).fold(
                onSuccess = {
                    _uiState.update { it.copy(messages = emptyList()) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun submitFeedback(conversationId: Int?, isPositive: Boolean) {
        if (conversationId == null) {
            // Can't submit feedback without conversation ID
            return
        }

        viewModelScope.launch {
            api.submitFeedback(
                userId = userId,
                conversationId = conversationId,
                isPositive = isPositive
            ).fold(
                onSuccess = {
                    // Feedback submitted successfully (silent success)
                },
                onFailure = { error ->
                    // Optionally log or handle error
                    println("Failed to submit feedback: ${error.message}")
                }
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        api.close()
    }
}
