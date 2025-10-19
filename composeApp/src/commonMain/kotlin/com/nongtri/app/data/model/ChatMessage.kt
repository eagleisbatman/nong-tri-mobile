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
    val conversationId: Int? = null  // Backend database ID for feedback linking
)

@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
