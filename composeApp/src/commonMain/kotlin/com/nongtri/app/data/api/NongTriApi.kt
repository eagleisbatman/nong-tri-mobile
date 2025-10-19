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

class NongTriApi(
    private val baseUrl: String = BuildConfig.API_URL
) {
    private val userPreferences = UserPreferences.getInstance()

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
        onChunk: (String) -> Unit
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

    fun close() {
        client.close()
    }
}
