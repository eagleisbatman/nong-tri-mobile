package com.nongtri.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nongtri.app.analytics.Events
import com.nongtri.app.data.api.NongTriApi
import com.nongtri.app.data.model.ChatMessage
import com.nongtri.app.data.model.MessageRole
import com.nongtri.app.data.preferences.UserPreferences
import com.nongtri.app.data.repository.LocationRepository
import com.nongtri.app.l10n.LocalizationProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
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
    val currentThreadTitle: String? = null,
    val attachedImageUri: String? = null,  // Display URI for preview
    val attachedImageBase64: String? = null,  // Base64 data for upload
    val isDiagnosisInProgress: Boolean = false  // True when diagnosis is being processed
)

@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
    private val api: NongTriApi = NongTriApi(),
    private val hapticFeedback: com.nongtri.app.platform.HapticFeedback? = null
) : ViewModel() {

    private val userPreferences by lazy { UserPreferences.getInstance() }

    // Use device ID from UserPreferences (backed by Android ID)
    private val userId: String by lazy { userPreferences.getDeviceId() }

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val locationRepository by lazy { LocationRepository.getInstance() }

    // Session tracking for analytics
    private var sessionMessageCount = 0
    private var sessionVoiceMessageCount = 0
    private var sessionImageCount = 0

    // Chunk throttling for smoother streaming (reduces layout reflows)
    private val chunkBuffer = StringBuilder()
    private var lastChunkFlushTime = 0L
    private var currentStreamingMessageId: String? = null
    private var isFirstChunk = true  // Track first chunk for haptic feedback

    // APPROACH 1: Channel for streaming updates - no list recomposition
    data class StreamingChunk(val messageId: String, val chunk: String)
    private val _streamingChannel = Channel<StreamingChunk>(Channel.UNLIMITED)
    val streamingUpdates = _streamingChannel.receiveAsFlow()

    // Keep for backward compatibility
    private val _streamingContent = MutableStateFlow("")
    val streamingContent: StateFlow<String> = _streamingContent.asStateFlow()

    // Getters for MainActivity to access session stats
    fun getSessionMessageCount() = sessionMessageCount
    fun getSessionVoiceMessageCount() = sessionVoiceMessageCount
    fun getSessionImageCount() = sessionImageCount

    /**
     * Flush buffered chunks to UI
     * Reduces layout reflows by batching small chunks
     */
    private fun flushChunkBuffer() {
        if (chunkBuffer.isEmpty() || currentStreamingMessageId == null) return

        val content = chunkBuffer.toString()
        val messageId = currentStreamingMessageId ?: return

        println("[ChatViewModel] Flushing chunk buffer: ${content.length} chars")

        // Update both the streaming content AND the message
        // Streaming content for immediate display
        _streamingContent.value += content

        // Update the message content directly in the list
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        // Simply append content - no special handling needed
                        msg.copy(content = msg.content + content)
                    } else {
                        msg
                    }
                }
            )
        }

        chunkBuffer.clear()
        lastChunkFlushTime = System.currentTimeMillis()
    }

    init {
        // Initialize location for first message
        // DO NOT load previous messages - start with blank screen
        // User can view history via Conversations menu
        initializeLocation()

        // Check if there's a pending diagnosis to fetch (from notification tap)
        checkPendingDiagnosis()
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
                                    // For voice messages, content should be empty (transcription shown in bubble)
                                    content = if (h.messageType == "voice") "" else h.content,
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
                                // For voice messages, content should be empty (transcription shown in bubble)
                                content = if (h.messageType == "voice") "" else h.content,
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
                                },
                                followUpQuestions = h.followUpQuestions
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

    /**
     * Check if there's a pending diagnosis to fetch (from notification tap)
     * Called on ViewModel initialization
     * Reads from UserPreferences (persists across process death)
     */
    private fun checkPendingDiagnosis() {
        val jobId = userPreferences.getPendingDiagnosisJobId() ?: return

        println("[ChatViewModel] Fetching pending diagnosis: $jobId")

        viewModelScope.launch {
            try {
                api.getDiagnosisResult(jobId).fold(
                    onSuccess = { response ->
                        when (response.status) {
                            "completed" -> {
                                response.diagnosis?.let { diagnosis ->
                                    println("[ChatViewModel] Diagnosis completed, adding to messages")

                                    // Track image funnel step 6: Diagnosis completed
                                    // Note: processing time not tracked from backend, using 0
                                    com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step6_DiagnosisCompleted(
                                        processingTimeMs = 0
                                    )

                                    // Track diagnosis completed event
                                    Events.logDiagnosisCompleted(
                                        processingTimeMs = 0L, // TODO: Backend should return processing time
                                        resultLength = diagnosis.aiResponse?.length ?: 0
                                    )

                                    // Remove "diagnosis_pending" message with this jobId if it exists
                                    _uiState.update { state ->
                                        state.copy(
                                            messages = state.messages.filter { msg ->
                                                !(msg.messageType == "diagnosis_pending" &&
                                                  msg.diagnosisPendingJobId == jobId)
                                            }
                                        )
                                    }

                                    // Add completed diagnosis message
                                    val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                                    val diagnosisMessage = ChatMessage(
                                        id = Uuid.random().toString(),
                                        role = MessageRole.ASSISTANT,
                                        content = diagnosis.aiResponse ?: strings.diagnosisCompleted,
                                        timestamp = Clock.System.now(),
                                        diagnosisData = diagnosis.diagnosisData,
                                        language = diagnosis.responseLanguage ?: "vi"
                                    )

                                    _uiState.update { state ->
                                        state.copy(
                                            messages = state.messages + diagnosisMessage,
                                            isDiagnosisInProgress = false  // Re-enable input
                                        )
                                    }

                                    // ROUND 4: Track diagnosis result viewed event
                                    Events.logDiagnosisResultViewed(
                                        jobId = jobId,
                                        resultLength = diagnosis.aiResponse?.length ?: 0
                                    )
                                }
                            }
                            "pending", "processing" -> {
                                println("[ChatViewModel] Diagnosis still processing: ${response.status}")
                                // Keep the pending card visible and disable input
                                _uiState.update { state ->
                                    state.copy(isDiagnosisInProgress = true)
                                }
                            }
                            "failed" -> {
                                println("[ChatViewModel] Diagnosis failed: ${response.error}")
                                // Show error message
                                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                                _uiState.update { state ->
                                    state.copy(
                                        error = "${strings.errorDiagnosisFailed}: ${response.error}",
                                        isDiagnosisInProgress = false  // Re-enable input
                                    )
                                }
                            }
                            else -> {
                                println("[ChatViewModel] Unknown diagnosis status: ${response.status}")
                            }
                        }
                    },
                    onFailure = { error ->
                        println("[ChatViewModel] Failed to fetch diagnosis: ${error.message}")
                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                        _uiState.update { state ->
                            state.copy(error = "${strings.errorFailedToFetchDiagnosis}: ${error.message}")
                        }
                    }
                )
            } catch (e: Exception) {
                println("[ChatViewModel] Error fetching diagnosis: ${e.message}")
                val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                _uiState.update { state ->
                    state.copy(error = "${strings.errorFetchingDiagnosis}: ${e.message}")
                }
            } finally {
                // Clear the pending job ID after handling
                userPreferences.setPendingDiagnosisJobId(null)
            }
        }
    }

    /**
     * Start polling for diagnosis job completion
     * Polls every 10 seconds until diagnosis is completed or max retries reached
     * Updates "Processing..." card to completed diagnosis when ready
     */
    private fun startPollingForDiagnosis(jobId: String) {
        println("[ChatViewModel] Starting diagnosis polling for jobId: $jobId")

        viewModelScope.launch {
            var pollCount = 0
            val maxPolls = 30  // 30 polls × 10 seconds = 5 minutes max

            while (pollCount < maxPolls) {
                // Wait 10 seconds before each poll
                kotlinx.coroutines.delay(10000)
                pollCount++

                println("[ChatViewModel] Polling diagnosis status (attempt $pollCount/$maxPolls)")

                try {
                    api.getDiagnosisResult(jobId).fold(
                        onSuccess = { response ->
                            when (response.status) {
                                "completed" -> {
                                    println("[ChatViewModel] ✓ Diagnosis completed!")

                                    response.diagnosis?.let { diagnosis ->
                                        // Track completion
                                        com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step6_DiagnosisCompleted(
                                            processingTimeMs = pollCount * 10000L
                                        )
                                        Events.logDiagnosisCompleted(
                                            processingTimeMs = pollCount * 10000L,
                                            resultLength = diagnosis.aiResponse?.length ?: 0
                                        )

                                        // Remove "diagnosis_pending" card
                                        _uiState.update { state ->
                                            state.copy(
                                                messages = state.messages.filter { msg ->
                                                    !(msg.messageType == "diagnosis_pending" &&
                                                      msg.diagnosisPendingJobId == jobId)
                                                }
                                            )
                                        }

                                        // Add completed diagnosis message
                                        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                                        val diagnosisMessage = ChatMessage(
                                            id = Uuid.random().toString(),
                                            role = MessageRole.ASSISTANT,
                                            content = diagnosis.aiResponse ?: strings.diagnosisCompleted,
                                            timestamp = Clock.System.now(),
                                            diagnosisData = diagnosis.diagnosisData,
                                            language = diagnosis.responseLanguage ?: "vi"
                                        )

                                        _uiState.update { state ->
                                            state.copy(
                                                messages = state.messages + diagnosisMessage,
                                                isDiagnosisInProgress = false  // Re-enable input
                                            )
                                        }

                                        // Haptic feedback - diagnosis ready
                                        hapticFeedback?.success()

                                        // Clear pending job ID
                                        userPreferences.setPendingDiagnosisJobId(null)

                                        // Track result viewed
                                        Events.logDiagnosisResultViewed(
                                            jobId = jobId,
                                            resultLength = diagnosis.aiResponse?.length ?: 0
                                        )
                                    }

                                    // Stop polling - diagnosis complete
                                    return@launch
                                }
                                "failed" -> {
                                    println("[ChatViewModel] ✗ Diagnosis failed: ${response.error}")

                                    // Remove pending card and show error
                                    _uiState.update { state ->
                                        state.copy(
                                            messages = state.messages.filter { msg ->
                                                !(msg.messageType == "diagnosis_pending" &&
                                                  msg.diagnosisPendingJobId == jobId)
                                            },
                                            error = response.error ?: "Diagnosis failed",
                                            isDiagnosisInProgress = false  // Re-enable input even on failure
                                        )
                                    }

                                    // Haptic feedback - error
                                    hapticFeedback?.error()

                                    // Clear pending job ID
                                    userPreferences.setPendingDiagnosisJobId(null)

                                    // Stop polling - job failed
                                    return@launch
                                }
                                "pending", "processing" -> {
                                    println("[ChatViewModel] Diagnosis still processing: ${response.status}")
                                    // Continue polling
                                }
                            }
                        },
                        onFailure = { error ->
                            println("[ChatViewModel] ✗ Polling error: ${error.message}")
                            // Continue polling despite error (might be network hiccup)
                        }
                    )
                } catch (e: Exception) {
                    println("[ChatViewModel] ✗ Exception during polling: ${e.message}")
                    // Continue polling despite exception
                }
            }

            // Max retries reached
            println("[ChatViewModel] ⚠ Max polling retries reached for jobId: $jobId")
            val strings = LocalizationProvider.getStrings(userPreferences.language.value)
            _uiState.update { state ->
                state.copy(error = "Diagnosis is taking longer than expected. You'll receive a notification when ready.")
            }
        }
    }

    fun updateMessage(message: String) {
        _uiState.update { it.copy(currentMessage = message) }
    }

    fun attachImage(uri: String, base64Data: String) {
        _uiState.update { it.copy(attachedImageUri = uri, attachedImageBase64 = base64Data) }
    }

    fun removeAttachedImage() {
        _uiState.update { it.copy(attachedImageUri = null, attachedImageBase64 = null) }
    }

    fun sendMessage(message: String) {
        if (message.isBlank() && _uiState.value.attachedImageBase64 == null) return

        // Get attached image before clearing
        val imageData = _uiState.value.attachedImageBase64
        val hasImage = imageData != null

        // Use default question if image attached but no text
        val actualMessage = if (hasImage && message.isBlank()) {
            "What's wrong with my plant?"
        } else {
            message
        }

        // Clear input field and attached image
        _uiState.update { it.copy(currentMessage = "", attachedImageUri = null, attachedImageBase64 = null) }

        // Track analytics: message sent
        sessionMessageCount++
        userPreferences.incrementMessageCount()

        // Track image analytics if present
        if (hasImage) {
            sessionImageCount++
            userPreferences.incrementImageMessageCount()
            userPreferences.setHasUsedImageDiagnosis(true)
            Events.logImageDiagnosisRequested(
                imageSource = "gallery",
                hasLocation = locationRepository.hasLocation(),
                locationType = locationRepository.getLocationType()
            )
        }

        // Log message sent event
        Events.logChatMessageSent(
            messageLength = actualMessage.trim().length,
            hasLocation = locationRepository.hasLocation(),
            locationType = locationRepository.getLocationType(),
            sessionMessageNumber = sessionMessageCount
        )

        // Track onboarding funnel step 3: First message sent (only for first message ever)
        if (userPreferences.messageCount.value == 1) {
            com.nongtri.app.analytics.Funnels.onboardingFunnel.step3_FirstMessageSent()

            // Track first message sent event with detailed metrics
            Events.logChatFirstMessageSent(
                messageType = if (hasImage) "image" else "text",
                timeSinceAppOpenMs = 0L, // TODO: Need MainActivity app start time tracking
                timeSinceChatViewMs = 0L, // TODO: Need ChatScreen first view time tracking
                messageLength = actualMessage.trim().length,
                usedStarterQuestion = false, // Starter questions not implemented
                hasLocationContext = locationRepository.hasLocation()
            )
        }

        // Add user message with image if present
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = actualMessage.trim(),
            timestamp = Clock.System.now(),
            messageType = if (hasImage) "image" else "text",
            imageUrl = if (hasImage) imageData else null  // Show preview in chat
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
            content = "",  // Start empty - typing indicator will show
            timestamp = Clock.System.now(),
            isLoading = true  // Mark as loading during streaming
        )

        _uiState.update { state ->
            state.copy(messages = state.messages + initialAssistantMessage)
        }

        // Track response time
        val messageStartTime = System.currentTimeMillis()

        // Initialize streaming state
        currentStreamingMessageId = assistantMessageId
        _streamingContent.value = ""  // Reset streaming content
        isFirstChunk = true  // Reset for new response

        // Send to API with streaming
        viewModelScope.launch {
            api.sendMessageStream(
                userId = userId,
                message = actualMessage,
                language = userPreferences.language.value.code,  // Pass current language to backend
                imageData = imageData,  // Pass image if attached
                onChunk = { chunk ->
                    println("[ChatViewModel] onChunk received: '${chunk.take(50)}' (${chunk.length} chars)")

                    // Haptic feedback - AI response started (first chunk only)
                    if (isFirstChunk) {
                        hapticFeedback?.gentleTick()
                        isFirstChunk = false
                        println("[ChatViewModel] First chunk received - haptic feedback triggered")
                    }

                    // Update ONLY streaming content during streaming
                    // DO NOT update the message list to avoid recomposition
                    currentStreamingMessageId?.let { messageId ->
                        // Update streaming content immediately for display
                        _streamingContent.value += chunk

                        // DON'T update message list until complete to avoid flickering
                    }
                },
                onMetadata = { metadata ->
                    println("[ChatViewModel] onMetadata received: ${metadata.followUpQuestions.size} follow-up questions, conversationId=${metadata.conversationId}")

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
                    // Now update the message with the complete content
                    val completeContent = _streamingContent.value

                    // Update message with complete content and mark as not loading
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        content = completeContent,
                                        isLoading = false
                                    )
                                } else {
                                    msg
                                }
                            }
                        )
                    }

                    currentStreamingMessageId = null
                    _streamingContent.value = ""  // Clear streaming content

                    // Haptic feedback - AI response completed
                    hapticFeedback?.tick()

                    // Track analytics: response received
                    val responseTime = System.currentTimeMillis() - messageStartTime
                    Events.logChatMessageReceived(
                        responseTimeMs = responseTime,
                        responseLength = fullResponse.length,
                        messageNumber = sessionMessageCount
                    )

                    // Track onboarding funnel step 4: First response received (only for first message ever)
                    if (userPreferences.messageCount.value == 1) {
                        com.nongtri.app.analytics.Funnels.onboardingFunnel.step4_FirstResponseReceived(responseTime)

                        // Get follow-up questions from the assistant message
                        val assistantMsg = _uiState.value.messages.find { it.id == assistantMessageId }
                        val followUpQuestions = assistantMsg?.followUpQuestions ?: emptyList()

                        // Track first response received event with detailed metrics
                        Events.logChatFirstResponseReceived(
                            responseTimeMs = responseTime,
                            timeSinceAppOpenMs = 0L, // TODO: Need MainActivity app start time tracking
                            responseLength = fullResponse.length,
                            hasFollowUpQuestions = followUpQuestions.isNotEmpty(),
                            followUpCount = followUpQuestions.size
                        )
                    }

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
                    // Haptic feedback - network error
                    hapticFeedback?.error()

                    // Clear streaming state on error
                    currentStreamingMessageId = null
                    _streamingContent.value = ""

                    // Track error for analytics
                    val errorType = when {
                        error.message?.contains("timeout", ignoreCase = true) == true -> "timeout"
                        error.message?.contains("network", ignoreCase = true) == true -> "network"
                        error.message?.contains("connect", ignoreCase = true) == true -> "connection"
                        error.message?.contains("503", ignoreCase = true) == true -> "service_unavailable"
                        error.message?.contains("502", ignoreCase = true) == true -> "bad_gateway"
                        else -> "unknown"
                    }

                    // Use network error event for network-level failures (no HTTP status code)
                    Events.logNetworkError(
                        endpoint = "/api/chat",
                        errorType = errorType,
                        errorMessage = error.message ?: "Unknown error"
                    )

                    // Determine user-friendly error message using localized strings
                    val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                    val errorMessage = when (errorType) {
                        "service_unavailable", "bad_gateway", "timeout" ->
                            "⚠️ ${strings.errorServerUpdating}"
                        "connection", "network" ->
                            "🌐 ${strings.errorConnectionFailed}"
                        else -> error.message ?: strings.errorFailedToSendMessage
                    }

                    // KEEP the assistant message but show error content instead of removing it
                    // This prevents the UI from "going blank" during Railway deployments
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        content = errorMessage,
                                        isLoading = false,
                                        responseType = "error"
                                    )
                                } else {
                                    msg
                                }
                            },
                            isLoading = false,
                            error = null  // Don't show banner error, error is already in message bubble
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
     * Add user's voice message to UI (optimistic update)
     * Should be called BEFORE sendVoiceMessage() to show the user's message immediately
     */
    fun addUserVoiceMessage(transcription: String, voiceAudioUrl: String?, durationMs: Long = 0) {
        if (transcription.isBlank()) return

        // Clear input field
        _uiState.update { it.copy(currentMessage = "") }

        // Add user's voice message to chat
        // Note: content is empty because transcription is shown inside VoiceMessageBubble
        val userMessage = ChatMessage(
            role = MessageRole.USER,
            content = "",  // Empty - transcription shown in voice bubble
            timestamp = Clock.System.now(),
            messageType = "voice",
            voiceAudioUrl = voiceAudioUrl,
            voiceTranscription = transcription.trim()  // Transcription for voice bubble display
        )

        _uiState.update { state ->
            state.copy(messages = state.messages + userMessage)
        }
    }

    /**
     * Send voice message with transcription and audio URL
     * Voice messages are displayed with audio playback controls
     * @param transcription Transcribed text from Whisper
     * @param voiceAudioUrl MinIO URL for voice recording
     * @param durationMs Recording duration in milliseconds (for analytics)
     */
    fun sendVoiceMessage(transcription: String, voiceAudioUrl: String?, durationMs: Long = 0) {
        if (transcription.isBlank()) return

        // Track analytics: voice message sent
        sessionMessageCount++
        sessionVoiceMessageCount++
        userPreferences.incrementMessageCount()
        userPreferences.incrementVoiceMessageCount()
        userPreferences.setHasUsedVoice(true)

        // Log voice message event
        Events.logVoiceMessageSent(
            durationMs = durationMs,
            hasLocation = locationRepository.hasLocation(),
            locationType = locationRepository.getLocationType(),
            sessionMessageNumber = sessionMessageCount
        )

        // Track voice funnel step 5: Message sent (funnel completed)
        com.nongtri.app.analytics.Funnels.voiceAdoptionFunnel.step5_MessageSent()

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
            content = "",  // Start empty - typing indicator will show
            timestamp = Clock.System.now(),
            isLoading = true  // Mark as loading during streaming
        )

        _uiState.update { state ->
            state.copy(messages = state.messages + initialAssistantMessage)
        }

        // Initialize streaming state
        currentStreamingMessageId = assistantMessageId
        _streamingContent.value = ""  // Reset streaming content
        isFirstChunk = true  // Reset for new response

        // Send transcription to API with streaming (same as sendMessage)
        viewModelScope.launch {
            api.sendMessageStream(
                userId = userId,
                message = transcription,
                language = userPreferences.language.value.code,  // Pass current language to backend
                messageType = "voice",  // CRITICAL: Mark as voice to prevent duplicate message creation
                onChunk = { chunk ->
                    // Update ONLY streaming content during streaming
                    currentStreamingMessageId?.let { messageId ->
                        // Update streaming content immediately for display
                        _streamingContent.value += chunk

                        // DON'T update message list until complete to avoid flickering
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
                    // Now update the message with the complete content
                    val completeContent = _streamingContent.value

                    // Update message with complete content and mark as not loading
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        content = completeContent,
                                        isLoading = false
                                    )
                                } else {
                                    msg
                                }
                            }
                        )
                    }

                    currentStreamingMessageId = null
                    _streamingContent.value = ""  // Clear streaming content

                    // Haptic feedback - AI response completed
                    hapticFeedback?.tick()

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
                    // Haptic feedback - network error
                    hapticFeedback?.error()

                    // Clear streaming state on error
                    currentStreamingMessageId = null
                    _streamingContent.value = ""

                    // Track error for analytics
                    val errorType = when {
                        error.message?.contains("timeout", ignoreCase = true) == true -> "timeout"
                        error.message?.contains("network", ignoreCase = true) == true -> "network"
                        error.message?.contains("connect", ignoreCase = true) == true -> "connection"
                        error.message?.contains("503", ignoreCase = true) == true -> "service_unavailable"
                        error.message?.contains("502", ignoreCase = true) == true -> "bad_gateway"
                        else -> "unknown"
                    }

                    // Determine user-friendly error message using localized strings
                    val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                    val errorMessage = when (errorType) {
                        "service_unavailable", "bad_gateway", "timeout" ->
                            "⚠️ ${strings.errorServerUpdating}"
                        "connection", "network" ->
                            "🌐 ${strings.errorConnectionFailed}"
                        else -> error.message ?: strings.errorFailedToSendMessage
                    }

                    // KEEP the assistant message but show error content instead of removing it
                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages.map { msg ->
                                if (msg.id == assistantMessageId) {
                                    msg.copy(
                                        content = errorMessage,
                                        isLoading = false,
                                        responseType = "error"
                                    )
                                } else {
                                    msg
                                }
                            },
                            isLoading = false,
                            error = null  // Don't show banner error, error is already in message bubble
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
     * @param imageSource "camera" or "gallery" for analytics tracking
     */
    fun sendImageDiagnosis(imageData: String, question: String, imageSource: String = "unknown") {
        println("[ImageDiagnosis] ======== FUNCTION CALLED ========")
        println("[ImageDiagnosis] imageData.length: ${imageData.length}")
        println("[ImageDiagnosis] question.length: ${question.length}")
        println("[ImageDiagnosis] imageSource: $imageSource")

        if (imageData.isBlank() || question.isBlank()) {
            println("[ImageDiagnosis] ✗ Invalid input: imageData or question is blank")
            return
        }

        // Validate image size using localized ImageValidator
        val estimatedSizeBytes = (imageData.length * 3L / 4)  // base64 to bytes
        println("[ImageDiagnosis] Estimated size: $estimatedSizeBytes bytes (${estimatedSizeBytes / 1024}KB, ${estimatedSizeBytes / (1024 * 1024)}MB)")

        val strings = LocalizationProvider.getStrings(userPreferences.language.value)
        val validationResult = com.nongtri.app.util.ImageValidator.validateFileSize(
            estimatedSizeBytes,
            strings
        )

        println("[ImageDiagnosis] Validation result: $validationResult")

        if (validationResult is com.nongtri.app.util.ImageValidationResult.Invalid) {
            println("[ImageDiagnosis] ✗ Image validation failed: ${validationResult.reason}")

            // ROUND 4: Track image validation failed event
            Events.logImageValidationFailed(
                reason = validationResult.reason,
                fileSizeKb = (estimatedSizeBytes / 1024).toInt()
            )

            _uiState.update { state ->
                state.copy(
                    error = validationResult.reason,
                    isLoading = false
                )
            }
            return
        }

        // Track analytics: image diagnosis requested
        sessionMessageCount++
        sessionImageCount++
        userPreferences.incrementMessageCount()
        userPreferences.incrementImageMessageCount()
        userPreferences.setHasUsedImageDiagnosis(true)

        // Log image diagnosis event
        Events.logImageDiagnosisRequested(
            imageSource = imageSource,
            hasLocation = locationRepository.hasLocation(),
            locationType = locationRepository.getLocationType()
        )

        // Track image funnel step 5: Diagnosis submitted
        com.nongtri.app.analytics.Funnels.imageDiagnosisFunnel.step5_DiagnosisSubmitted()

        // ROUND 4: Track diagnosis submission started event with full context
        Events.logDiagnosisSubmissionStarted(
            fileSizeKb = (estimatedSizeBytes / 1024).toInt(),
            hasQuestion = question.trim().isNotEmpty(),
            questionLength = question.trim().length
        )

        println("[ImageDiagnosis] Starting async diagnosis upload...")
        println("[ImageDiagnosis] Question: $question")
        println("[ImageDiagnosis] Image size: ${estimatedSizeBytes / (1024 * 1024)}MB")

        // Note: Optimistic message should already be shown by caller
        // We just need to set loading state and submit diagnosis job

        _uiState.update { state ->
            state.copy(
                isLoading = true,
                error = null
            )
        }

        // Track upload started for analytics
        val uploadStartTime = System.currentTimeMillis()
        val fileSizeKb = (estimatedSizeBytes / 1024).toInt()
        Events.logDiagnosisUploadStarted(
            fileSizeKb = fileSizeKb,
            networkType = "unknown" // TODO: Get actual network type
        )

        // Submit diagnosis job (async)
        viewModelScope.launch {
            api.submitDiagnosisJob(
                userId = userId,
                imageData = imageData,
                question = question,
                language = userPreferences.language.value.code  // Pass current language to backend
            ).fold(
                onSuccess = { response ->
                    println("[ImageDiagnosis] ✓ Job submitted: jobId=${response.jobId}")

                    // ROUND 4: Track diagnosis job created event
                    Events.logDiagnosisJobCreated(jobId = response.jobId ?: "unknown")

                    // Track upload completed for analytics
                    val uploadTime = System.currentTimeMillis() - uploadStartTime
                    Events.logDiagnosisUploadCompleted(
                        fileSizeKb = fileSizeKb,
                        uploadTimeMs = uploadTime,
                        networkType = "unknown" // TODO: Get actual network type
                    )

                    // Haptic feedback - upload success
                    hapticFeedback?.success()

                    // CRITICAL: Save job ID to UserPreferences for crash recovery
                    // If app crashes after upload, we can still fetch diagnosis on restart
                    response.jobId?.let { jobId ->
                        userPreferences.setPendingDiagnosisJobId(jobId)
                        println("[ImageDiagnosis] Job ID saved to preferences: $jobId")
                    }

                    // Clear loading state on user's image message
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
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

                    // Add informative "diagnosis pending" message
                    val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                    val pendingMessageId = Uuid.random().toString()
                    val pendingMessage = ChatMessage(
                        id = pendingMessageId,
                        role = MessageRole.ASSISTANT,
                        content = response.message ?: strings.diagnosisProcessingMessage,
                        timestamp = Clock.System.now(),
                        messageType = "diagnosis_pending",
                        diagnosisPendingJobId = response.jobId,
                        diagnosisPendingImageUrl = response.imageUrl  // Use MinIO URL from backend response
                    )

                    _uiState.update { state ->
                        state.copy(
                            messages = state.messages + pendingMessage,
                            isDiagnosisInProgress = true  // Disable input while diagnosis is processing
                        )
                    }

                    // ROUND 4: Track diagnosis processing card displayed event
                    Events.logDiagnosisProcessingDisplayed(jobId = response.jobId ?: "unknown")

                    // Start polling for diagnosis completion
                    response.jobId?.let { jobId ->
                        startPollingForDiagnosis(jobId)
                    }
                },
                onFailure = { error ->
                    println("[ImageDiagnosis] ✗ Error submitting job: ${error.message}")

                    // Haptic feedback - upload/network error
                    hapticFeedback?.error()

                    // ROUND 4: Track diagnosis failed event
                    val errorType = when {
                        error.message?.contains("timeout", ignoreCase = true) == true -> "timeout"
                        error.message?.contains("network", ignoreCase = true) == true -> "network"
                        error.message?.contains("size", ignoreCase = true) == true -> "file_too_large"
                        else -> "upload_error"
                    }
                    Events.logDiagnosisFailed(
                        jobId = "unknown", // Job wasn't created yet
                        errorType = errorType,
                        errorMessage = error.message ?: "Unknown error"
                    )

                    // Track upload failed for analytics
                    val uploadErrorType = when {
                        error.message?.contains("timeout", ignoreCase = true) == true -> "timeout"
                        error.message?.contains("network", ignoreCase = true) == true -> "network"
                        error.message?.contains("size", ignoreCase = true) == true -> "file_too_large"
                        else -> "upload_error"
                    }
                    Events.logDiagnosisUploadFailed(
                        fileSizeKb = fileSizeKb,
                        errorType = errorType,
                        errorMessage = error.message ?: "Unknown error"
                    )

                    val strings = LocalizationProvider.getStrings(userPreferences.language.value)
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages.map { msg ->
                                if (msg.role == MessageRole.USER &&
                                    msg.messageType == "image" &&
                                    msg.isLoading) {
                                    msg.copy(isLoading = false)
                                } else {
                                    msg
                                }
                            },
                            error = error.message ?: strings.errorFailedToSubmitDiagnosis
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
