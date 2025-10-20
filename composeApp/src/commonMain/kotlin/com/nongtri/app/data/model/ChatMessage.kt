package com.nongtri.app.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class ChatMessage(
    val id: String = Uuid.random().toString(),
    val role: MessageRole,
    val content: String,
    val timestamp: Instant = Clock.System.now(),
    val isLoading: Boolean = false,
    val hasError: Boolean = false,
    val conversationId: Int? = null,  // Backend database ID for feedback linking
    // Response metadata (from AI agent)
    val responseType: String? = null,  // "generic" | "agricultural_weather" | "agricultural_crops" | etc.
    val followUpQuestions: List<String> = emptyList(),  // Tappable follow-up questions
    val isGenericResponse: Boolean = false,  // true = greeting/casual, false = agricultural advice
    val language: String = "en",  // "vi" | "en"
    // TTS audio caching (for assistant messages)
    val audioUrl: String? = null,  // Cached TTS audio URL to prevent regeneration
    val audioVoice: String? = null,  // Voice used for TTS (alloy, echo, etc.)
    // Voice message fields (for user messages)
    val messageType: String = "text",  // "text" | "voice" | "image"
    val voiceAudioUrl: String? = null,  // User voice recording URL (from MinIO)
    val voiceTranscription: String? = null  // Transcribed text from voice message
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
