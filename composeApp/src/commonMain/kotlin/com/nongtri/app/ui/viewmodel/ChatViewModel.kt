package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.MessageRole
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.data.repository.LocationRepository
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
    private val api: NongTriApi = NongTriApi()
) : ViewModel() {

    private val userPreferences by lazy { UserPreferences.getInstance() }

    // Use device ID from UserPreferences (backed by Android ID)
    private val userId: String by lazy { userPreferences.getDeviceId() }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val locationRepository by lazy { LocationRepository.getInstance() }

    init {
        // Don't load history on init - start fresh chat each time
        // History will be loaded only when user opens conversation history screen
        initializeLocation()
    }

    /**
     * Initialize user's IP-based location on app startup
     * This ensures location is available for the first conversation query
     */
    private fun initializeLocation() {
        viewModelScope.launch {
            try {
                locationRepository.initializeLocation()
                println("✓ Location initialized successfully")
            } catch (e: Exception) {
                println("⚠ Failed to initialize location: ${e.message}")
                // Don't block app startup if location fails
            }
        }
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
                },
                onMetadata = { metadata ->
                    // Update the assistant message with metadata (response type, follow-up questions, etc.)
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        responseType = metadata.responseType,
                                        followUpQuestions = metadata.followUpQuestions,
                                        isGenericResponse = metadata.isGenericResponse,
                                        language = metadata.language
                                    )
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

    fun getDeviceId(): String = userId

    override fun onCleared() {
        super.onCleared()
        api.close()
    }
}
