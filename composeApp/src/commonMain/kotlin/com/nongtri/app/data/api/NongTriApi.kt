package com.nongtri.app.data.api

import com.nongtri.app.BuildConfig
import com.nongtri.app.data.preferences.UserPreferences
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

data class StreamMetadata(
    val responseType: String = "generic",
    val followUpQuestions: List<String> = emptyList(),
    val isGenericResponse: Boolean = false,
    val language: String = "en",
    val conversationId: Int? = null  // Backend conversation ID for TTS audio URL caching
)

class NongTriApi(
    private val baseUrl: String = BuildConfig.API_URL
) {
    private val userPreferences by lazy { UserPreferences.getInstance() }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 60000  // 60 seconds for AI responses
            connectTimeoutMillis = 15000  // 15 seconds to establish connection
            socketTimeoutMillis = 60000   // 60 seconds for socket read/write
        }
    }

    suspend fun sendMessageStream(
        userId: String,
        message: String,
        userName: String? = null,
        onChunk: (String) -> Unit,
        onMetadata: ((StreamMetadata) -> Unit)? = null
    ): Result<String> {
        return try {
            var fullResponse = ""

            // Get device info to send with request
            val deviceInfo = userPreferences.getDeviceInfo()

            client.preparePost("$baseUrl/api/chat/stream") {
                contentType(ContentType.Application.Json)
                setBody(ChatRequest(userId, message, userName, deviceInfo))
            }.execute { response ->
                val channel: ByteReadChannel = response.body()

                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line() ?: break

                    if (line.startsWith("data: ")) {
                        val jsonData = line.substring(6)
                        try {
                            val parsed = Json.parseToJsonElement(jsonData).jsonObject

                            // Check if done
                            if (parsed["done"]?.toString() == "true") {
                                break
                            }

                            // Check for error
                            parsed["error"]?.let { errorMsg ->
                                throw Exception(errorMsg.toString())
                            }

                            // Check for metadata chunk (special chunk sent at end)
                            if (parsed["__metadata"]?.toString() == "true") {
                                println("[SSE] Metadata chunk received: $parsed")

                                val followUpQuestions = try {
                                    val questionsElement = parsed["followUpQuestions"]
                                    if (questionsElement != null) {
                                        // Parse as JSON array
                                        val questionsArray = if (questionsElement is JsonArray) {
                                            questionsElement
                                        } else {
                                            // If it's a string, parse it
                                            Json.parseToJsonElement(questionsElement.toString()).jsonArray
                                        }

                                        questionsArray.map { element ->
                                            element.toString().trim('"')
                                        }.filter { it.isNotEmpty() && it != "null" }
                                    } else {
                                        emptyList()
                                    }
                                } catch (e: Exception) {
                                    println("[SSE] Error parsing follow-up questions: ${e.message}")
                                    e.printStackTrace()
                                    emptyList()
                                }

                                println("[SSE] Parsed ${followUpQuestions.size} follow-up questions: $followUpQuestions")

                                val metadata = StreamMetadata(
                                    responseType = parsed["responseType"]?.toString()?.trim('"') ?: "generic",
                                    followUpQuestions = followUpQuestions,
                                    isGenericResponse = parsed["isGenericResponse"]?.toString() == "true",
                                    language = parsed["language"]?.toString()?.trim('"') ?: "en",
                                    conversationId = parsed["conversationId"]?.toString()?.toIntOrNull()
                                )

                                println("[SSE] Metadata created: isGenericResponse=${metadata.isGenericResponse}, questions=${metadata.followUpQuestions.size}")
                                onMetadata?.invoke(metadata)
                                continue
                            }

                            // Extract content chunk
                            parsed["content"]?.let { content ->
                                val chunk = content.toString().trim('"')
                                fullResponse += chunk
                                onChunk(chunk)
                            }
                        } catch (e: Exception) {
                            println("Failed to parse SSE data: $jsonData - ${e.message}")
                        }
                    }
                }
            }

            Result.success(fullResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getHistory(userId: String, limit: Int = 20): Result<HistoryResponse> {
        return try {
            val response: HistoryResponse = client.get("$baseUrl/api/chat/history/$userId") {
                parameter("limit", limit)
            }.body()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearHistory(userId: String): Result<Boolean> {
        return try {
            client.delete("$baseUrl/api/chat/history/$userId")
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitFeedback(
        userId: String,
        conversationId: Int,
        isPositive: Boolean,
        feedbackText: String? = null
    ): Result<Boolean> {
        return try {
            client.post("$baseUrl/api/feedback") {
                contentType(ContentType.Application.Json)
                setBody(FeedbackRequest(userId, conversationId, isPositive, feedbackText))
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStarterQuestions(
        language: String,
        deviceId: String
    ): Result<List<String>> {
        return try {
            println("[NongTriApi] Requesting starter questions: deviceId=$deviceId, language=$language")
            println("[NongTriApi] URL: $baseUrl/api/starter-questions")

            val response: StarterQuestionsResponse = client.get("$baseUrl/api/starter-questions") {
                parameter("language", language)
                parameter("device_id", deviceId)
            }.body()

            println("[NongTriApi] Response received: success=${response.success}, questions=${response.questions.size}")

            if (response.success) {
                Result.success(response.questions)
            } else {
                println("[NongTriApi] API returned success=false")
                Result.failure(Exception("Failed to fetch starter questions"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Exception occurred: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Transcribe audio file to text using Whisper API
     * @param audioFile Audio file to transcribe
     * @param language Language code (e.g., "en", "vi")
     * @return Result with TranscriptionResponse
     */
    suspend fun transcribeAudio(audioFile: java.io.File, language: String): Result<TranscriptionResponse> {
        return try {
            val response: TranscriptionResponse = client.submitFormWithBinaryData(
                url = "$baseUrl/api/transcribe",
                formData = io.ktor.client.request.forms.formData {
                    append("audio", audioFile.readBytes(), io.ktor.http.Headers.build {
                        append(HttpHeaders.ContentType, "audio/m4a")
                        append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                    })
                    append("language", language)
                }
            ).body()

            Result.success(response)
        } catch (e: Exception) {
            println("[NongTriApi] Transcription error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Update conversation with TTS audio URL for persistence
     * @param conversationId Backend conversation ID
     * @param audioUrl MinIO audio URL
     * @param ttsVoice Voice used for TTS (alloy, echo, etc.)
     * @return Result indicating success/failure
     */
    suspend fun updateConversationAudioUrl(
        conversationId: Int,
        audioUrl: String,
        ttsVoice: String
    ): Result<Unit> {
        return try {
            val response: UpdateAudioResponse = client.post("$baseUrl/api/conversation/update-audio") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "conversationId" to conversationId,
                    "audioUrl" to audioUrl,
                    "ttsVoice" to ttsVoice
                ))
            }.body()

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Failed to update audio URL"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Update audio URL error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get conversation history for a user
     * Includes all audio URLs for TTS and voice messages
     * @param userId Device ID
     * @param limit Number of messages to load (default 20)
     * @return Result with list of history messages
     */
    suspend fun getConversationHistory(userId: String, limit: Int = 20): Result<List<HistoryMessage>> {
        return try {
            val response: ConversationHistoryResponse = client.get("$baseUrl/api/chat/history/$userId") {
                parameter("limit", limit)
            }.body()

            if (response.success) {
                Result.success(response.history)
            } else {
                Result.failure(Exception(response.message ?: "Failed to load history"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] History loading error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun close() {
        client.close()
    }
}

@kotlinx.serialization.Serializable
data class StarterQuestionsResponse(
    val success: Boolean,
    val questions: List<String>,
    val context: StarterQuestionsContext? = null
)

@kotlinx.serialization.Serializable
data class StarterQuestionsContext(
    val locationName: String?,
    val language: String,
    val personalized: Boolean
)

@kotlinx.serialization.Serializable
data class TranscriptionResponse(
    val success: Boolean,
    val text: String = "",
    val language: String = "",
    val error: String? = null
)

@kotlinx.serialization.Serializable
data class UpdateAudioResponse(
    val success: Boolean,
    val message: String? = null
)

@kotlinx.serialization.Serializable
data class ConversationHistoryResponse(
    val success: Boolean,
    val history: List<HistoryMessage> = emptyList(),
    val message: String? = null
)

@kotlinx.serialization.Serializable
data class HistoryMessage(
    val id: Int,                              // Conversation ID
    val role: String,                         // "user" or "assistant"
    val content: String,                      // Message text
    val timestamp: String,                    // ISO timestamp
    val messageType: String? = "text",        // text, voice, image
    val audioUrl: String? = null,             // TTS audio URL
    val ttsVoice: String? = null,             // Voice used
    val voiceAudioUrl: String? = null,        // User voice recording
    val voiceTranscription: String? = null,   // Transcribed text
    val imageUrl: String? = null,             // Image URL
    val language: String? = "en"              // Message language
)
