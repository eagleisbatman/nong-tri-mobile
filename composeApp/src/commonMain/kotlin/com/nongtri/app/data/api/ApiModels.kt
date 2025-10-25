package com.nongtri.app.data.api

import com.nongtri.app.data.model.DeviceInfo
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val userId: String,
    val message: String,
    val userName: String? = null,
    val deviceInfo: DeviceInfo? = null,
    val language: String? = null,  // "en" or "vi" - language preference for AI response
    val imageData: String? = null,  // Base64 image data for image messages
    val messageType: String? = null  // "text" or "image"
)

@Serializable
data class ChatResponse(
    val success: Boolean,
    val response: String? = null,
    val timestamp: String? = null,
    val error: String? = null
)

@Serializable
data class HistoryResponse(
    val success: Boolean,
    val history: List<HistoryItem> = emptyList(),
    val error: String? = null
)

@Serializable
data class HistoryItem(
    val role: String,
    val content: String,
    val timestamp: String
)

@Serializable
data class ApiError(
    val success: Boolean = false,
    val error: String,
    val details: String? = null
)

@Serializable
data class FeedbackRequest(
    val userId: String,
    val conversationId: Int,
    val isPositive: Boolean,
    val feedbackText: String? = null
)

@Serializable
data class ImageDiagnosisRequest(
    val userId: String,
    val message: String,  // User's question about the plant
    val imageData: String,  // Base64 data URL: data:image/jpeg;base64,...
    val messageType: String = "image",
    val userName: String? = null,
    val deviceInfo: DeviceInfo? = null,
    val language: String? = null  // "en" or "vi" - language preference for AI response
)
