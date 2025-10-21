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
        // Load conversation history to restore TTS audio URLs and voice messages
        loadConversationHistory()
        initializeLocation()
    }

    /**
     * Load conversation history from backend
     * Restores messages with TTS audio URLs for offline playback
     */
    private fun loadConversationHistory() {
        viewModelScope.launch {
            try {
                api.getConversationHistory(userId, limit = 20).fold(
                    onSuccess = { history ->
                        if (history.isNotEmpty()) {
                            val messages = history.map { h ->
                                ChatMessage(
                                    id = h.id.toString(), // Use conversation ID as message ID
                                    role = if (h.role == "user") MessageRole.USER else MessageRole.ASSISTANT,
                                    content = h.content,
                                    timestamp = kotlinx.datetime.Instant.parse(h.timestamp),
                                    conversationId = h.id,
                                    audioUrl = h.audioUrl,
                                    audioVoice = h.ttsVoice,
                                    language = h.language ?: "en",
                                    // Voice message fields
                                    messageType = h.messageType ?: "text",
                                    voiceAudioUrl = h.voiceAudioUrl,
                                    voiceTranscription = h.voiceTranscription
                                )
                            }
                            _uiState.update { it.copy(messages = messages) }
                            println("✓ Loaded ${messages.size} messages from history with audio URLs")
                        }
                    },
                    onFailure = { error ->
                        println("⚠ Failed to load history: ${error.message}")
                        // Don't block app if history fails
                    }
                )
            } catch (e: Exception) {
                println("⚠ Error loading history: ${e.message}")
            }
        }
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
            timestamp = Clock.System.now(),
            isLoading = true  // Mark as loading during streaming
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
                    // Update the assistant message with metadata (response type, follow-up questions, conversation ID, etc.)
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        responseType = metadata.responseType,
                                        followUpQuestions = metadata.followUpQuestions,
                                        isGenericResponse = metadata.isGenericResponse,
                                        language = metadata.language,
                                        conversationId = metadata.conversationId  // Store for TTS audio URL persistence
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
                    // Mark loading as complete and mark message as not loading
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(isLoading = false)
                                } else {
                                    msg
                                }
                            }
                        )
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

    /**
     * Show optimistic voice message bubble immediately (with placeholder)
     * This provides instant feedback while transcription is in progress
     * @return Message ID for updating later
     */
    fun showOptimisticVoiceMessage(): String {
        val messageId = Uuid.random().toString()
        val optimisticMessage = ChatMessage(
            id = messageId,
            role = MessageRole.USER,
            content = "...",  // Placeholder text
            timestamp = Clock.System.now(),
            messageType = "voice",
            voiceAudioUrl = null,  // Will be filled when transcription completes
            voiceTranscription = null,
            isLoading = true  // Show as loading
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + optimisticMessage,
                error = null
            )
        }

        return messageId
    }

    /**
     * Update optimistic voice message with actual transcription
     * Called when transcription completes
     */
    fun updateVoiceMessage(messageId: String, transcription: String, voiceAudioUrl: String?) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(
                            content = transcription.trim(),
                            voiceAudioUrl = voiceAudioUrl,
                            voiceTranscription = transcription.trim(),
                            isLoading = false
                        )
                    } else {
                        msg
                    }
                }
            )
        }
    }

    /**
     * Remove failed voice message
     * Called when recording or transcription fails
     */
    fun removeVoiceMessage(messageId: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.filter { it.id != messageId }
            )
        }
    }

    /**
     * Send voice message with transcription and audio URL
     * Voice messages are displayed with audio playback controls
     * @param transcription Transcribed text from Whisper
     * @param voiceAudioUrl MinIO URL for voice recording
     */
    fun sendVoiceMessage(transcription: String, voiceAudioUrl: String?) {
        if (transcription.isBlank()) return

        // Note: This method is now called AFTER optimistic message is shown
        // The optimistic message is already in the UI, we just need to trigger AI response

        _uiState.update { state ->
            state.copy(
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
            timestamp = Clock.System.now(),
            isLoading = true  // Mark as loading during streaming
        )

        _uiState.update { state ->
            state.copy(messages = state.messages + initialAssistantMessage)
        }

        // Send transcription to API with streaming (same as sendMessage)
        viewModelScope.launch {
            api.sendMessageStream(
                userId = userId,
                message = transcription,
                onChunk = { chunk ->
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
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        responseType = metadata.responseType,
                                        followUpQuestions = metadata.followUpQuestions,
                                        isGenericResponse = metadata.isGenericResponse,
                                        language = metadata.language,
                                        conversationId = metadata.conversationId
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
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(isLoading = false)
                                } else {
                                    msg
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
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

    /**
     * Update message with cached TTS audio URL to prevent regeneration
     * Also persists to backend database for survival across app restarts
     * @param messageId The message ID to update
     * @param audioUrl The cached audio URL from TTS generation
     */
    fun updateMessageAudioUrl(messageId: String, audioUrl: String) {
        // Update in-memory state
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(audioUrl = audioUrl)
                    } else {
                        msg
                    }
                }
            )
        }

        // Persist to backend database (if conversation ID exists)
        viewModelScope.launch {
            val message = _uiState.value.messages.find { it.id == messageId }
            if (message?.conversationId != null) {
                api.updateConversationAudioUrl(
                    conversationId = message.conversationId,
                    audioUrl = audioUrl,
                    ttsVoice = "alloy"  // TODO: Track voice used for TTS
                ).fold(
                    onSuccess = {
                        println("[ChatViewModel] Audio URL persisted to database for conversation ${message.conversationId}")
                    },
                    onFailure = { error ->
                        println("[ChatViewModel] Failed to persist audio URL: ${error.message}")
                    }
                )
            } else {
                println("[ChatViewModel] No conversation ID for message $messageId, skipping database persistence")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        api.close()
    }
}
