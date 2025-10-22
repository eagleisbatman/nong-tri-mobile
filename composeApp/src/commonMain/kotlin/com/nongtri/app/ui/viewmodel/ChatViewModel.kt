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
    val error: String? = null,
    val currentThreadId: Int? = null,
    val currentThreadTitle: String? = null
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
        // Initialize location for first message
        // DO NOT load previous messages - start with blank screen
        // User can view history via Conversations menu
        initializeLocation()
    }

    /**
     * Load or create the active conversation thread
     * This ensures we always have a thread to work with
     * Falls back to old history API if threads not available yet
     */
    private fun loadActiveThread() {
        viewModelScope.launch {
            try {
                api.getActiveThread(userId).fold(
                    onSuccess = { thread ->
                        _uiState.update {
                            it.copy(
                                currentThreadId = thread.id,
                                currentThreadTitle = thread.title
                            )
                        }
                        println("✓ Loaded active thread: ${thread.id} - ${thread.title}")

                        // Load messages for this thread
                        loadThreadMessages(thread.id)
                    },
                    onFailure = { error ->
                        println("⚠ Failed to load active thread: ${error.message}")
                        // Fallback to old history API (for backward compatibility)
                        loadConversationHistoryFallback()
                    }
                )
            } catch (e: Exception) {
                println("⚠ Error loading active thread: ${e.message}")
                // Fallback to old history API
                loadConversationHistoryFallback()
            }
        }
    }

    /**
     * Fallback: Load conversation history using old API
     * Used when thread endpoints are not available yet
     */
    private fun loadConversationHistoryFallback() {
        viewModelScope.launch {
            try {
                api.getConversationHistory(userId, limit = 20).fold(
                    onSuccess = { history ->
                        if (history.isNotEmpty()) {
                            val messages = history.map { h ->
                                ChatMessage(
                                    id = h.id.toString(),
                                    role = if (h.role == "user") MessageRole.USER else MessageRole.ASSISTANT,
                                    content = h.content,
                                    timestamp = kotlinx.datetime.Instant.parse(h.timestamp),
                                    conversationId = h.id,
                                    audioUrl = h.audioUrl,
                                    audioVoice = h.ttsVoice,
                                    language = h.language ?: "en",
                                    messageType = h.messageType ?: "text",
                                    voiceAudioUrl = h.voiceAudioUrl,
                                    voiceTranscription = h.voiceTranscription,
                                    imageUrl = h.imageUrl,
                                    diagnosisData = h.diagnosisData?.let { json ->
                                        try {
                                            kotlinx.serialization.json.Json.decodeFromString(json)
                                        } catch (e: Exception) {
                                            println("[ChatViewModel] Failed to parse diagnosisData: ${e.message}")
                                            null
                                        }
                                    }
                                )
                            }
                            _uiState.update { it.copy(messages = messages) }
                            println("✓ Loaded ${messages.size} messages from history (fallback mode)")
                        }
                    },
                    onFailure = { error ->
                        println("⚠ Failed to load history: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                println("⚠ Error loading history: ${e.message}")
            }
        }
    }

    /**
     * Load messages for a specific thread
     * @param threadId Thread ID to load messages from
     */
    private fun loadThreadMessages(threadId: Int) {
        viewModelScope.launch {
            try {
                api.getThreadMessages(userId, threadId, limit = 100).fold(
                    onSuccess = { history ->
                        val messages = history.map { h ->
                            ChatMessage(
                                id = h.id.toString(),
                                role = if (h.role == "user") MessageRole.USER else MessageRole.ASSISTANT,
                                content = h.content,
                                timestamp = kotlinx.datetime.Instant.parse(h.timestamp),
                                conversationId = h.id,
                                audioUrl = h.audioUrl,
                                audioVoice = h.ttsVoice,
                                language = h.language ?: "en",
                                messageType = h.messageType ?: "text",
                                voiceAudioUrl = h.voiceAudioUrl,
                                voiceTranscription = h.voiceTranscription,
                                imageUrl = h.imageUrl,
                                diagnosisData = h.diagnosisData?.let { json ->
                                    try {
                                        kotlinx.serialization.json.Json.decodeFromString(json)
                                    } catch (e: Exception) {
                                        println("[ChatViewModel] Failed to parse diagnosisData: ${e.message}")
                                        null
                                    }
                                }
                            )
                        }
                        _uiState.update { it.copy(messages = messages) }
                        println("✓ Loaded ${messages.size} messages for thread $threadId")
                    },
                    onFailure = { error ->
                        println("⚠ Failed to load thread messages: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                println("⚠ Error loading thread messages: ${e.message}")
            }
        }
    }

    /**
     * Switch to a different conversation thread
     * @param threadId Thread ID to switch to
     */
    fun switchToThread(threadId: Int, threadTitle: String? = null) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    currentThreadId = threadId,
                    currentThreadTitle = threadTitle,
                    messages = emptyList() // Clear current messages
                )
            }
            loadThreadMessages(threadId)
        }
    }

    /**
     * Create a new conversation thread and switch to it
     * Falls back to clearing messages if threads not available
     * @param title Optional title for the new thread
     */
    fun createNewThread(title: String? = null) {
        viewModelScope.launch {
            try {
                api.createThread(userId, title).fold(
                    onSuccess = { thread ->
                        println("✓ Created new thread: ${thread.id}")
                        switchToThread(thread.id, thread.title)
                    },
                    onFailure = { error ->
                        println("⚠ Failed to create thread (fallback to clear): ${error.message}")
                        // Fallback: Just clear current messages
                        _uiState.update { it.copy(messages = emptyList()) }
                    }
                )
            } catch (e: Exception) {
                println("⚠ Error creating thread (fallback to clear): ${e.message}")
                // Fallback: Just clear current messages
                _uiState.update { it.copy(messages = emptyList()) }
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

    // ========================================================================
    // IMAGE DIAGNOSIS METHODS
    // ========================================================================

    /**
     * Show optimistic image message bubble immediately (with placeholder)
     * This provides instant feedback while image is uploading
     * @param imageData Base64 data URL or URI string for preview
     * @param question User's question about the plant
     * @return Message ID for updating later
     */
    fun showOptimisticImageMessage(imageData: String, question: String): String {
        val messageId = Uuid.random().toString()
        val optimisticMessage = ChatMessage(
            id = messageId,
            role = MessageRole.USER,
            content = question,
            timestamp = Clock.System.now(),
            messageType = "image",
            imageUrl = imageData,  // Temporarily use base64/URI for preview
            isLoading = true  // Show as loading/uploading
        )

        _uiState.update { state ->
            state.copy(
                messages = state.messages + optimisticMessage,
                error = null
            )
        }

        println("[ImageDiagnosis] Optimistic image message shown: $messageId")
        return messageId
    }

    /**
     * Update optimistic image message with server imageUrl
     * Called when upload completes
     */
    fun updateImageMessage(messageId: String, imageUrl: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(
                            imageUrl = imageUrl,
                            isLoading = false
                        )
                    } else {
                        msg
                    }
                }
            )
        }
        println("[ImageDiagnosis] Image message updated with server URL: $messageId")
    }

    /**
     * Remove failed image message
     * Called when upload or diagnosis fails
     */
    fun removeImageMessage(messageId: String) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.filter { it.id != messageId }
            )
        }
        println("[ImageDiagnosis] Failed image message removed: $messageId")
    }

    /**
     * Send image diagnosis request with streaming response
     * Image messages show plant image with diagnosis results
     * @param imageData Base64 data URL (data:image/jpeg;base64,...)
     * @param question User's question about the plant
     */
    fun sendImageDiagnosis(imageData: String, question: String) {
        if (imageData.isBlank() || question.isBlank()) {
            println("[ImageDiagnosis] ✗ Invalid input: imageData or question is blank")
            return
        }

        println("[ImageDiagnosis] Starting diagnosis upload...")
        println("[ImageDiagnosis] Question: $question")
        println("[ImageDiagnosis] Image data length: ${imageData.length}")

        // Note: Optimistic message should already be shown by caller
        // We just need to set loading state and trigger AI response

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

        // Send to API with streaming
        viewModelScope.launch {
            var firstChunkReceived = false

            api.sendImageDiagnosisStream(
                userId = userId,
                message = question,
                imageData = imageData,
                onChunk = { chunk ->
                    // On first chunk, clear loading state on user's image message
                    // (indicates backend received image and started processing)
                    if (!firstChunkReceived) {
                        firstChunkReceived = true
                        _uiState.update { state ->
                            state.copy(
                                messages = state.messages.map { msg ->
                                    // Find most recent user image message with isLoading=true
                                    if (msg.role == MessageRole.USER &&
                                        msg.messageType == "image" &&
                                        msg.isLoading) {
                                        msg.copy(isLoading = false)
                                    } else {
                                        msg
                                    }
                                }
                            )
                        }
                        println("[ImageDiagnosis] User image message loading cleared")
                    }

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
                    // Update the assistant message with metadata (diagnosis data, conversation ID, etc.)
                    println("[ImageDiagnosis] Metadata received: conversationId=${metadata.conversationId}, hasDiagnosisData=${metadata.diagnosisData != null}")
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        responseType = metadata.responseType,
                                        followUpQuestions = metadata.followUpQuestions,
                                        isGenericResponse = metadata.isGenericResponse,
                                        language = metadata.language,
                                        conversationId = metadata.conversationId,
                                        diagnosisData = metadata.diagnosisData  // ✅ Store diagnosis data
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
                    println("[ImageDiagnosis] ✓ Diagnosis complete, response length: ${fullResponse.length}")
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
                    println("[ImageDiagnosis] ✗ Error: ${error.message}")
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.filter { it.id != assistantMessageId },
                            isLoading = false,
                            error = error.message ?: "Failed to analyze image"
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
