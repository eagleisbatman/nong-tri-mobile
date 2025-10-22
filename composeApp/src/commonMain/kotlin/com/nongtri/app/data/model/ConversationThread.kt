package com.nongtri.app.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a conversation thread (a logical grouping of messages)
 * Multiple conversations can exist for a single user
 */
@Serializable
data class ConversationThread(
    @SerialName("id")
    val id: Int,

    @SerialName("title")
    val title: String?,

    @SerialName("isActive")
    val isActive: Boolean = true,

    @SerialName("messageCount")
    val messageCount: Int = 0,

    @SerialName("createdAt")
    val createdAt: String,

    @SerialName("updatedAt")
    val updatedAt: String,

    @SerialName("lastMessageAt")
    val lastMessageAt: String? = null
) {
    /**
     * Get display title (use title or fallback to localized "New Conversation")
     */
    fun getDisplayTitle(strings: com.nongtri.app.l10n.Strings): String {
        return title?.takeIf { it.isNotBlank() } ?: strings.newConversation
    }

    /**
     * Get last activity time as Instant
     */
    fun getLastActivityTime(): Instant {
        return try {
            Instant.parse(lastMessageAt ?: updatedAt)
        } catch (e: Exception) {
            Instant.parse(updatedAt)
        }
    }
}

/**
 * API response for thread list
 */
@Serializable
data class ThreadsResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("threads")
    val threads: List<ConversationThread> = emptyList(),

    @SerialName("error")
    val error: String? = null
)

/**
 * API response for single thread
 */
@Serializable
data class ThreadResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("thread")
    val thread: ConversationThread? = null,

    @SerialName("error")
    val error: String? = null
)

/**
 * API response for thread messages
 */
@Serializable
data class ThreadMessagesResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("messages")
    val messages: List<com.nongtri.app.data.api.HistoryMessage> = emptyList(),

    @SerialName("error")
    val error: String? = null
)

/**
 * Request body for creating/updating thread
 */
@Serializable
data class ThreadUpdateRequest(
    @SerialName("title")
    val title: String? = null,

    @SerialName("isActive")
    val isActive: Boolean? = null
)
