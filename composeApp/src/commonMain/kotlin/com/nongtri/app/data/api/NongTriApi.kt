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
import com.nongtri.app.data.model.DiagnosisData

data class StreamMetadata(
    val responseType: String = "generic",
    val followUpQuestions: List<String> = emptyList(),
    val isGenericResponse: Boolean = false,
    val language: String = "en",
    val conversationId: Int? = null,  // Backend conversation ID for TTS audio URL caching
    val diagnosisData: DiagnosisData? = null  // Plant diagnosis data from AgriVision MCP
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

    /**
     * Send image diagnosis request with streaming response
     * Similar to sendMessageStream but includes base64 image data
     * Backend handles image upload to MinIO and calls AgriVision MCP for diagnosis
     *
     * @param userId Device ID
     * @param message User's question about the plant (e.g., "How is the health of my crop?")
     * @param imageData Base64 data URL (data:image/jpeg;base64,...)
     * @param onChunk Callback for each streaming content chunk
     * @param onMetadata Callback for metadata (diagnosis data, conversation ID, etc.)
     * @return Result with full response or error
     */
    suspend fun sendImageDiagnosisStream(
        userId: String,
        message: String,
        imageData: String,
        userName: String? = null,
        onChunk: (String) -> Unit,
        onMetadata: ((StreamMetadata) -> Unit)? = null
    ): Result<String> {
        return try {
            var fullResponse = ""

            // Get device info to send with request
            val deviceInfo = userPreferences.getDeviceInfo()

            println("[ImageDiagnosis] Starting image diagnosis upload...")
            println("[ImageDiagnosis] Image data size: ${imageData.length} chars")

            client.preparePost("$baseUrl/api/chat/stream") {
                contentType(ContentType.Application.Json)
                setBody(ImageDiagnosisRequest(
                    userId = userId,
                    message = message,
                    imageData = imageData,
                    userName = userName,
                    deviceInfo = deviceInfo
                ))
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
                                println("[ImageDiagnosis] SSE stream completed")
                                break
                            }

                            // Check for error
                            parsed["error"]?.let { errorMsg ->
                                val error = errorMsg.toString().trim('"')
                                println("[ImageDiagnosis] SSE error: $error")
                                throw Exception(error)
                            }

                            // Check for metadata chunk (includes diagnosisData)
                            if (parsed["__metadata"]?.toString() == "true") {
                                println("[ImageDiagnosis] Metadata chunk received")

                                val followUpQuestions = try {
                                    val questionsElement = parsed["followUpQuestions"]
                                    if (questionsElement != null) {
                                        val questionsArray = if (questionsElement is JsonArray) {
                                            questionsElement
                                        } else {
                                            Json.parseToJsonElement(questionsElement.toString()).jsonArray
                                        }

                                        questionsArray.map { element ->
                                            element.toString().trim('"')
                                        }.filter { it.isNotEmpty() && it != "null" }
                                    } else {
                                        emptyList()
                                    }
                                } catch (e: Exception) {
                                    println("[ImageDiagnosis] Error parsing follow-up questions: ${e.message}")
                                    emptyList()
                                }

                                // Parse diagnosisData if present
                                val diagnosisData = try {
                                    val diagnosisElement = parsed["diagnosisData"]
                                    if (diagnosisElement != null && diagnosisElement.toString() != "null") {
                                        val diagnosisJson = if (diagnosisElement is kotlinx.serialization.json.JsonObject) {
                                            diagnosisElement.toString()
                                        } else {
                                            diagnosisElement.toString().trim('"')
                                        }
                                        Json.decodeFromString<DiagnosisData>(diagnosisJson)
                                    } else {
                                        null
                                    }
                                } catch (e: Exception) {
                                    println("[ImageDiagnosis] Error parsing diagnosisData: ${e.message}")
                                    null
                                }

                                val metadata = StreamMetadata(
                                    responseType = parsed["responseType"]?.toString()?.trim('"') ?: "image_diagnosis",
                                    followUpQuestions = followUpQuestions,
                                    isGenericResponse = false,  // Diagnosis is never generic
                                    language = parsed["language"]?.toString()?.trim('"') ?: "en",
                                    conversationId = parsed["conversationId"]?.toString()?.toIntOrNull(),
                                    diagnosisData = diagnosisData
                                )

                                println("[ImageDiagnosis] Metadata received: conversationId=${metadata.conversationId}, questions=${metadata.followUpQuestions.size}")
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
                            println("[ImageDiagnosis] Failed to parse SSE data: $jsonData - ${e.message}")
                        }
                    }
                }
            }

            println("[ImageDiagnosis] ✓ Image diagnosis complete, response length: ${fullResponse.length}")
            Result.success(fullResponse)
        } catch (e: Exception) {
            println("[ImageDiagnosis] ✗ Error: ${e.message}")
            e.printStackTrace()

            // Provide farmer-friendly error messages based on error type
            val userMessage = when {
                e::class.simpleName?.contains("Timeout", ignoreCase = true) == true ||
                e.message?.contains("timeout", ignoreCase = true) == true ->
                    "Upload timed out. This may be due to slow internet. Please try a smaller image or wait and try again."
                e.message?.contains("host", ignoreCase = true) == true ||
                e.message?.contains("network", ignoreCase = true) == true ->
                    "No internet connection. Please check your network and try again."
                e.message?.contains("connection", ignoreCase = true) == true ->
                    "Cannot connect to server. Please check your internet connection."
                else ->
                    "Upload failed. Please try again."
            }
            Result.failure(Exception(userMessage))
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
            println("[NongTriApi] Starting transcription for file: ${audioFile.name}, size: ${audioFile.length()} bytes, language: $language")
            println("[NongTriApi] Transcription URL: $baseUrl/api/transcribe")

            val response: TranscriptionResponse = client.post("$baseUrl/api/transcribe") {
                setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                    io.ktor.client.request.forms.formData {
                        append("audio", audioFile.readBytes(), io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentType, "audio/m4a")
                            append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                        })
                        append("language", language)
                    }
                ))
            }.body()

            println("[NongTriApi] Transcription response: success=${response.success}, text=${response.text}, error=${response.error}")
            Result.success(response)
        } catch (e: Exception) {
            println("[NongTriApi] Transcription error: ${e.message}")
            println("[NongTriApi] Error type: ${e::class.simpleName}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Save voice message with transcription to backend
     * @param userId Device ID
     * @param audioFile Audio file to upload
     * @param transcription Transcribed text
     * @param language Language code (e.g., "en", "vi")
     * @return Result with VoiceMessageResponse containing conversation ID and audio URL
     */
    suspend fun saveVoiceMessage(
        userId: String,
        audioFile: java.io.File,
        transcription: String,
        language: String
    ): Result<VoiceMessageResponse> {
        return try {
            val response: VoiceMessageResponse = client.post("$baseUrl/api/conversation/voice-message") {
                setBody(io.ktor.client.request.forms.MultiPartFormDataContent(
                    io.ktor.client.request.forms.formData {
                        append("audio", audioFile.readBytes(), io.ktor.http.Headers.build {
                            append(HttpHeaders.ContentType, "audio/m4a")
                            append(HttpHeaders.ContentDisposition, "filename=\"${audioFile.name}\"")
                        })
                        append("userId", userId)
                        append("transcription", transcription)
                        append("language", language)
                    }
                ))
            }.body()

            if (response.success) {
                Result.success(response)
            } else {
                Result.failure(Exception(response.error ?: "Failed to save voice message"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Save voice message error: ${e.message}")
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
                setBody(UpdateAudioRequest(
                    conversationId = conversationId,
                    audioUrl = audioUrl,
                    ttsVoice = ttsVoice
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

    // ========================================================================
    // CONVERSATION THREADS API
    // ========================================================================

    /**
     * Get all conversation threads for user
     * @param userId Device ID
     * @param includeInactive Include archived threads
     * @return Result with list of threads
     */
    suspend fun getThreads(userId: String, includeInactive: Boolean = false): Result<List<com.nongtri.app.data.model.ConversationThread>> {
        return try {
            val response: com.nongtri.app.data.model.ThreadsResponse = client.get("$baseUrl/api/threads/$userId") {
                parameter("includeInactive", includeInactive)
            }.body()

            if (response.success) {
                Result.success(response.threads)
            } else {
                Result.failure(Exception(response.error ?: "Failed to load threads"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Get threads error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Create a new conversation thread
     * @param userId Device ID
     * @param title Optional thread title
     * @return Result with created thread
     */
    suspend fun createThread(userId: String, title: String? = null): Result<com.nongtri.app.data.model.ConversationThread> {
        return try {
            val response: com.nongtri.app.data.model.ThreadResponse = client.post("$baseUrl/api/threads/$userId") {
                contentType(ContentType.Application.Json)
                setBody(com.nongtri.app.data.model.ThreadUpdateRequest(title = title))
            }.body()

            if (response.success && response.thread != null) {
                Result.success(response.thread)
            } else {
                Result.failure(Exception(response.error ?: "Failed to create thread"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Create thread error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get or create active thread for user
     * @param userId Device ID
     * @return Result with active thread
     */
    suspend fun getActiveThread(userId: String): Result<com.nongtri.app.data.model.ConversationThread> {
        return try {
            val response: com.nongtri.app.data.model.ThreadResponse = client.get("$baseUrl/api/threads/$userId/active").body()

            if (response.success && response.thread != null) {
                Result.success(response.thread)
            } else {
                Result.failure(Exception(response.error ?: "Failed to get active thread"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Get active thread error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get messages for a specific thread
     * @param userId Device ID
     * @param threadId Thread ID
     * @param limit Number of messages to load
     * @return Result with list of messages
     */
    suspend fun getThreadMessages(userId: String, threadId: Int, limit: Int = 100): Result<List<HistoryMessage>> {
        return try {
            val response: com.nongtri.app.data.model.ThreadMessagesResponse =
                client.get("$baseUrl/api/threads/$userId/$threadId/messages") {
                    parameter("limit", limit)
                }.body()

            if (response.success) {
                Result.success(response.messages)
            } else {
                Result.failure(Exception(response.error ?: "Failed to load thread messages"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Get thread messages error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Update thread (title or archive status)
     * @param userId Device ID
     * @param threadId Thread ID
     * @param title New title (optional)
     * @param isActive Active status (optional)
     * @return Result with updated thread
     */
    suspend fun updateThread(
        userId: String,
        threadId: Int,
        title: String? = null,
        isActive: Boolean? = null
    ): Result<com.nongtri.app.data.model.ConversationThread> {
        return try {
            val response: com.nongtri.app.data.model.ThreadResponse = client.patch("$baseUrl/api/threads/$userId/$threadId") {
                contentType(ContentType.Application.Json)
                setBody(com.nongtri.app.data.model.ThreadUpdateRequest(title = title, isActive = isActive))
            }.body()

            if (response.success && response.thread != null) {
                Result.success(response.thread)
            } else {
                Result.failure(Exception(response.error ?: "Failed to update thread"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Update thread error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Delete a thread and all its messages
     * @param userId Device ID
     * @param threadId Thread ID
     * @return Result indicating success/failure
     */
    suspend fun deleteThread(userId: String, threadId: Int): Result<Unit> {
        return try {
            val response: com.nongtri.app.data.model.ThreadResponse = client.delete("$baseUrl/api/threads/$userId/$threadId").body()

            if (response.success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Failed to delete thread"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Delete thread error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Submit plant image for async diagnosis
     * Returns job ID for tracking
     */
    suspend fun submitDiagnosisJob(
        userId: String,
        imageUrl: String,
        question: String = "How is the health of my crop?"
    ): Result<DiagnosisJobResponse> {
        return try {
            val response: DiagnosisJobResponse = client.post("$baseUrl/api/diagnosis/submit") {
                contentType(ContentType.Application.Json)
                setBody(DiagnosisJobRequest(
                    userId = userId,
                    imageUrl = imageUrl,
                    question = question
                ))
            }.body()

            if (response.success) {
                println("[NongTriApi] Diagnosis job submitted: ${response.jobId}")
                Result.success(response)
            } else {
                Result.failure(Exception(response.error ?: "Failed to submit diagnosis"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Submit diagnosis error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Get diagnosis result by job ID
     */
    suspend fun getDiagnosisResult(jobId: String): Result<DiagnosisResultResponse> {
        return try {
            val response: DiagnosisResultResponse = client.get("$baseUrl/api/diagnosis/$jobId").body()

            if (response.success) {
                println("[NongTriApi] Diagnosis result fetched: status=${response.status}")
                Result.success(response)
            } else {
                Result.failure(Exception(response.error ?: "Failed to fetch diagnosis"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] Get diagnosis error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Register FCM device token for push notifications
     */
    suspend fun registerFCMToken(
        deviceId: String,
        fcmToken: String,
        platform: String,
        appVersion: String? = null,
        osVersion: String? = null
    ): Result<Unit> {
        return try {
            val response: FCMRegistrationResponse = client.post("$baseUrl/api/diagnosis/register-token") {
                contentType(ContentType.Application.Json)
                setBody(FCMRegistrationRequest(
                    userId = deviceId,
                    fcmToken = fcmToken,
                    platform = platform,
                    appVersion = appVersion,
                    osVersion = osVersion
                ))
            }.body()

            if (response.success) {
                println("[NongTriApi] FCM token registered successfully")
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.error ?: "Failed to register FCM token"))
            }
        } catch (e: Exception) {
            println("[NongTriApi] FCM token registration error: ${e.message}")
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
data class UpdateAudioRequest(
    val conversationId: Int,
    val audioUrl: String,
    val ttsVoice: String
)

@kotlinx.serialization.Serializable
data class FCMRegistrationRequest(
    val userId: String,
    val fcmToken: String,
    val platform: String,
    val appVersion: String? = null,
    val osVersion: String? = null
)

@kotlinx.serialization.Serializable
data class FCMRegistrationResponse(
    val success: Boolean,
    val message: String? = null,
    val error: String? = null
)

@kotlinx.serialization.Serializable
data class DiagnosisJobRequest(
    val userId: String,
    val imageUrl: String,
    val question: String
)

@kotlinx.serialization.Serializable
data class DiagnosisJobResponse(
    val success: Boolean,
    val jobId: String? = null,
    val message: String? = null,
    val estimatedTimeMinutes: Int? = null,
    val error: String? = null
)

@kotlinx.serialization.Serializable
data class DiagnosisResultResponse(
    val success: Boolean,
    val status: String? = null,  // "pending", "processing", "completed", "failed"
    val diagnosis: DiagnosisResult? = null,
    val message: String? = null,
    val error: String? = null,
    val createdAt: String? = null,
    val completedAt: String? = null
)

@kotlinx.serialization.Serializable
data class DiagnosisResult(
    val diagnosisData: DiagnosisData? = null,
    val aiResponse: String? = null,
    val responseLanguage: String? = null
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
    val diagnosisData: String? = null,        // Plant diagnosis JSON (JSONB from database)
    val language: String? = "en"              // Message language
)

@kotlinx.serialization.Serializable
data class VoiceMessageResponse(
    val success: Boolean,
    val conversation: VoiceMessageConversation? = null,
    val error: String? = null
)

@kotlinx.serialization.Serializable
data class VoiceMessageConversation(
    val id: Int,
    val voiceAudioUrl: String?,
    val transcription: String,
    val createdAt: String
)
