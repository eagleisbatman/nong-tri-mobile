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
import kotlinx.serialization.json.jsonObject

data class StreamMetadata(
    val responseType: String = "generic",
    val followUpQuestions: List<String> = emptyList(),
    val isGenericResponse: Boolean = false,
    val language: String = "en"
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
                                val followUpQuestionsRaw = parsed["followUpQuestions"]?.toString() ?: "[]"
                                val followUpQuestions = try {
                                    Json.parseToJsonElement(followUpQuestionsRaw).toString()
                                        .removeSurrounding("[", "]")
                                        .split(",")
                                        .map { it.trim().removeSurrounding("\"") }
                                        .filter { it.isNotEmpty() && it != "null" }
                                } catch (e: Exception) {
                                    emptyList()
                                }

                                val metadata = StreamMetadata(
                                    responseType = parsed["responseType"]?.toString()?.trim('"') ?: "generic",
                                    followUpQuestions = followUpQuestions,
                                    isGenericResponse = parsed["isGenericResponse"]?.toString() == "true",
                                    language = parsed["language"]?.toString()?.trim('"') ?: "en"
                                )
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
    val season: String,
    val isCoastal: Boolean,
    val locationName: String?,
    val language: String
)
